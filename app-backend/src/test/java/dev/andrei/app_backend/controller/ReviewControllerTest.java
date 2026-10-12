package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.dto.review.AttributeScoreDto;
import dev.andrei.app_backend.dto.review.MyReviewDto;
import dev.andrei.app_backend.dto.review.SubmitReviewRequest;
import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.repository.UserRepository;
import dev.andrei.app_backend.service.JwtService;
import dev.andrei.app_backend.service.ReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    // This @WebMvcTest slice doesn't register Spring Security's argument resolver, so
    // @AuthenticationPrincipal would otherwise be (mis)handled as a model attribute (a fresh
    // User with null fields). Register it explicitly; it reads the principal from the
    // SecurityContext set in @BeforeEach.
    @TestConfiguration
    static class SecurityResolverConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    // JwtAuthenticationFilter is a @Component, so @WebMvcTest still wires it up.
    // Mock its constructor deps so the context can start; addFilters=false stops the
    // filter chain from running on each request.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    // AppBackendApplication declares a CommandLineRunner that needs JdbcTemplate.
    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    private final UUID userId = UUID.randomUUID();
    private final UUID locationId = UUID.randomUUID();

    @BeforeEach
    void authenticate() {
        // The controller reads @AuthenticationPrincipal User. With the filter chain off, we put
        // the principal straight into the context — the argument resolver reads it from here.
        User principal = new User(userId, "user@test.com", "hash", Instant.now());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void submitReview_whenValidAndAuthenticated_returns201AndPassesUserId() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated());

        // The userId must come from the token, not the body.
        verify(reviewService).submitReview(eq(userId), any(SubmitReviewRequest.class));
    }

    @Test
    void submitReview_whenLocationMissing_returns404() throws Exception {
        doThrow(new ReviewService.LocationNotFoundException())
                .when(reviewService).submitReview(any(), any());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("LOCATION_NOT_FOUND"));
    }

    @Test
    void submitReview_whenAlreadyReviewed_returns409() throws Exception {
        doThrow(new ReviewService.AlreadyReviewedException())
                .when(reviewService).submitReview(any(), any());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ALREADY_REVIEWED"));
    }

    @Test
    void submitReview_whenServiceRejectsInput_returns400() throws Exception {
        doThrow(new ReviewService.InvalidReviewException("bad attribute"))
                .when(reviewService).submitReview(any(), any());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    void submitReview_whenBodyFailsBeanValidation_returns400AndSkipsService() throws Exception {
        // Empty attributeRatings violates @NotEmpty -> MethodArgumentNotValidException.
        String invalidBody = """
                {"locationId": "%s", "content": "x", "attributeRatings": []}
                """.formatted(locationId);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        Mockito.verifyNoInteractions(reviewService);
    }

    @Test
    void getMyReviews_returns200WithReviews() throws Exception {
        Mockito.when(reviewService.getMyReviews(userId)).thenReturn(List.of(
                new MyReviewDto(UUID.randomUUID(), locationId, "Park", "Nice", Instant.now(), 4.0,
                        List.of(new AttributeScoreDto("Service", 4.0)))));

        mockMvc.perform(get("/api/reviews/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].locationName").value("Park"))
                .andExpect(jsonPath("$[0].overallScore").value(4.0));
    }

    private String validBody() {
        return """
                {
                  "locationId": "%s",
                  "content": "Great place",
                  "attributeRatings": [
                    {"attribute": "Cleanliness", "rating": 4.5},
                    {"attribute": "Service", "rating": 5.0},
                    {"attribute": "Atmosphere", "rating": 4.0}
                  ]
                }
                """.formatted(locationId);
    }
}