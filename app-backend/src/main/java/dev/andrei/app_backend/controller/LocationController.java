package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.dto.JustCoordinatesDto;
import dev.andrei.app_backend.dto.LocationDto;
import dev.andrei.app_backend.dto.LocationPhotosDto;
import dev.andrei.app_backend.dto.review.ReviewDto;
import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.service.GooglePlacesPhotoService;
import dev.andrei.app_backend.service.LocationService;
import dev.andrei.app_backend.service.ReviewService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private static final int MAX_QUERY_LENGTH = 100;

    private final LocationService locationService;
    private final ReviewService reviewService;
    private final GooglePlacesPhotoService photoService;

    public LocationController(LocationService locationService,
                             ReviewService reviewService,
                             GooglePlacesPhotoService photoService) {
        this.locationService = locationService;
        this.reviewService = reviewService;
        this.photoService = photoService;
    }

    // Public: a location's reviews are viewable without signing in (like the other location reads).
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewDto>> getReviews(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.getLocationReviews(id));
    }

    // Public: how many Google photos this location has. Resolves + persists them on first hit, so the
    // list endpoints stay free of Google calls. Count is 0 when no key is set or no photos were found.
    @GetMapping("/{id}/photos")
    public ResponseEntity<LocationPhotosDto> getPhotoCount(@PathVariable UUID id) {
        int count = photoService.ensureResolvedPhotoNames(id).size();
        return ResponseEntity.ok(new LocationPhotosDto(count));
    }

    // Public: redirects to a fresh, key-less Google image URL for the photo at the given index.
    // The API key never leaves the server. 404 when the index is out of range or resolution fails.
    @GetMapping("/{id}/photo")
    public ResponseEntity<Void> getPhoto(@PathVariable UUID id,
                                         @RequestParam(name = "index", defaultValue = "0") int index) {
        List<String> names = photoService.ensureResolvedPhotoNames(id);
        if (index < 0 || index >= names.size()) {
            return ResponseEntity.notFound().build();
        }
        String uri = photoService.resolvePhotoUri(names.get(index), photoService.getPhotoMaxWidthPx());
        if (uri == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(uri))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .build();
    }

    @PostMapping("/nearby")
    public ResponseEntity<List<LocationDto>> fetchTop10CloseLocations(
            @RequestBody JustCoordinatesDto request,
            @RequestParam(name = "limit", defaultValue = "30") int limit
            ) {

        List<LocationDto> results = locationService.getTop10CloseLocations(request, limit);

        return ResponseEntity.ok(results);
    }

    // Authenticated: locations near the user ranked by their personal match score.
    @PostMapping("/recommended")
    public ResponseEntity<List<LocationDto>> recommended(
            @AuthenticationPrincipal User user,
            @RequestBody JustCoordinatesDto request,
            @RequestParam(name = "limit", defaultValue = "30") int limit) {
        return ResponseEntity.ok(locationService.getRecommendedLocations(user.getId(), request, limit));
    }

    // Authenticated: nearby high-quality places that lean on concepts the user usually doesn't
    // prioritize -- "try something new", the inverse of /recommended. Returns a batch the client
    // rerolls through. Empty when the user has no effective preferences.
    @PostMapping("/discover")
    public ResponseEntity<List<LocationDto>> discover(
            @AuthenticationPrincipal User user,
            @RequestBody JustCoordinatesDto request,
            @RequestParam(name = "limit", defaultValue = "12") int limit) {
        return ResponseEntity.ok(locationService.getDiscoveryLocations(user.getId(), request, limit));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocationDto>> search(@RequestParam("q") String query) {
        if (query.length() > MAX_QUERY_LENGTH) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(locationService.searchByName(query));
    }
}
