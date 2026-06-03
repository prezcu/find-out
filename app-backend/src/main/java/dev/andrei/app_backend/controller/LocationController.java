package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.dto.JustCoordinatesDto;
import dev.andrei.app_backend.dto.LocationDto;
import dev.andrei.app_backend.dto.review.ReviewDto;
import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.service.LocationService;
import dev.andrei.app_backend.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private static final int MAX_QUERY_LENGTH = 100;

    private final LocationService locationService;
    private final ReviewService reviewService;

    public LocationController(LocationService locationService, ReviewService reviewService) {
        this.locationService = locationService;
        this.reviewService = reviewService;
    }

    // Public: a location's reviews are viewable without signing in (like the other location reads).
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewDto>> getReviews(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.getLocationReviews(id));
    }

    @PostMapping("/nearby")
    public ResponseEntity<List<LocationDto>> fetchTop10CloseLocations(
            @RequestBody JustCoordinatesDto request
            ) {

        List<LocationDto> results = locationService.getTop10CloseLocations(request);

        return ResponseEntity.ok(results);
    }

    // Authenticated: locations near the user ranked by their personal match score.
    @PostMapping("/recommended")
    public ResponseEntity<List<LocationDto>> recommended(
            @AuthenticationPrincipal User user,
            @RequestBody JustCoordinatesDto request) {
        return ResponseEntity.ok(locationService.getRecommendedLocations(user.getId(), request));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocationDto>> search(@RequestParam("q") String query) {
        if (query.length() > MAX_QUERY_LENGTH) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(locationService.searchByName(query));
    }
}
