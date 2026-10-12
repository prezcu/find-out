package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.dto.AttributeDto;
import dev.andrei.app_backend.dto.LocationDto;
import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.repository.UserRepository;
import dev.andrei.app_backend.service.JwtService;
import dev.andrei.app_backend.service.WishlistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
@AutoConfigureMockMvc(addFilters = false)
class WishlistControllerTest {

    // Same reason as ReviewControllerTest: register the @AuthenticationPrincipal resolver.
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
    private WishlistService wishlistService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    private final UUID userId = UUID.randomUUID();
    private final UUID locationId = UUID.randomUUID();

    @BeforeEach
    void authenticate() {
        User principal = new User(userId, "user@test.com", "hash", Instant.now());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getWishlist_returns200WithLocations() throws Exception {
        Mockito.when(wishlistService.getWishlist(userId)).thenReturn(List.of(
                new LocationDto(locationId, "Park", "park", "Park", 1.0, 2.0, true, false, 4.5,
                        List.of(new AttributeDto("Cleanliness", "cleanliness", 4.5)), null)));

        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(locationId.toString()))
                .andExpect(jsonPath("$[0].name").value("Park"));
    }

    @Test
    void status_returns200WithFlag() throws Exception {
        Mockito.when(wishlistService.isWishlisted(userId, locationId)).thenReturn(true);

        mockMvc.perform(get("/api/wishlist/{locationId}", locationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishlisted").value(true));
    }

    @Test
    void add_whenValid_returns201AndPassesUserAndLocation() throws Exception {
        mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":\"" + locationId + "\"}"))
                .andExpect(status().isCreated());

        verify(wishlistService).addToWishlist(eq(userId), eq(locationId));
    }

    @Test
    void add_whenLocationMissing_returns404() throws Exception {
        doThrow(new WishlistService.LocationNotFoundException())
                .when(wishlistService).addToWishlist(any(), any());

        mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":\"" + locationId + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("LOCATION_NOT_FOUND"));
    }

    @Test
    void add_whenDuplicate_returns409() throws Exception {
        doThrow(new WishlistService.AlreadyWishlistedException())
                .when(wishlistService).addToWishlist(any(), any());

        mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":\"" + locationId + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ALREADY_WISHLISTED"));
    }

    @Test
    void add_whenLocationIdMissing_returns400AndSkipsService() throws Exception {
        mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        Mockito.verifyNoInteractions(wishlistService);
    }

    @Test
    void remove_returns204() throws Exception {
        mockMvc.perform(delete("/api/wishlist/{locationId}", locationId))
                .andExpect(status().isNoContent());

        verify(wishlistService).removeFromWishlist(userId, locationId);
    }
}
