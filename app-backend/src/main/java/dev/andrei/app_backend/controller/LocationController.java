package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.dto.JustCoordinatesDto;
import dev.andrei.app_backend.dto.LocationDto;
import dev.andrei.app_backend.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private static final int MAX_QUERY_LENGTH = 100;

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/nearby")
    public ResponseEntity<List<LocationDto>> fetchTop10CloseLocations(
            @RequestBody JustCoordinatesDto request
            ) {

        List<LocationDto> results = locationService.getTop10CloseLocations(request);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocationDto>> search(@RequestParam("q") String query) {
        if (query.length() > MAX_QUERY_LENGTH) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(locationService.searchByName(query));
    }
}
