package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.dto.review.AttributeScoreDto;
import dev.andrei.app_backend.dto.review.ReviewDto;
import dev.andrei.app_backend.repository.UserRepository;
import dev.andrei.app_backend.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationService locationService;
    @MockitoBean
    private ReviewService reviewService;
    @MockitoBean
    private GooglePlacesPhotoService photoService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private JdbcTemplate jdbcTemplate;
    @MockitoBean
    private GooglePlacesDetailsService detailsService;

    @Test
    void getLocationReviews_returns200WithReviews() throws Exception {
        UUID locationId = UUID.randomUUID();
        Mockito.when(reviewService.getLocationReviews(locationId)).thenReturn(List.of(
                new ReviewDto(UUID.randomUUID(), "Alice", "Great place", Instant.now(), 4.5,
                        List.of(new AttributeScoreDto("Cleanliness", 4.5)))));

        mockMvc.perform(get("/api/locations/{id}/reviews", locationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewerDisplayName").value("Alice"))
                .andExpect(jsonPath("$[0].overallScore").value(4.5))
                .andExpect(jsonPath("$[0].attributeScores[0].attribute").value("Cleanliness"));
    }

    @Test
    void getPhotoCount_returnsResolvedCount() throws Exception {
        UUID locationId = UUID.randomUUID();
        Mockito.when(photoService.ensureResolvedPhotoNames(locationId))
                .thenReturn(List.of("places/a/photos/1", "places/a/photos/2"));

        mockMvc.perform(get("/api/locations/{id}/photos", locationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    void getPhoto_redirectsToResolvedImageUrl() throws Exception {
        UUID locationId = UUID.randomUUID();
        Mockito.when(photoService.ensureResolvedPhotoNames(locationId))
                .thenReturn(List.of("places/a/photos/1"));
        Mockito.when(photoService.getPhotoMaxWidthPx()).thenReturn(800);
        Mockito.when(photoService.resolvePhotoUri("places/a/photos/1", 800))
                .thenReturn("https://lh3.googleusercontent.com/abc");

        mockMvc.perform(get("/api/locations/{id}/photo", locationId))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://lh3.googleusercontent.com/abc"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("max-age")));
    }

    @Test
    void getPhoto_returns404WhenNoPhotoAtIndex() throws Exception {
        UUID locationId = UUID.randomUUID();
        Mockito.when(photoService.ensureResolvedPhotoNames(locationId)).thenReturn(List.of());

        mockMvc.perform(get("/api/locations/{id}/photo", locationId))
                .andExpect(status().isNotFound());
    }
}