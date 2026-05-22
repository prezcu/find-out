package dev.andrei.app_backend.service;

import dev.andrei.app_backend.dto.JustCoordinatesDto;
import dev.andrei.app_backend.dto.LocationDto;
import dev.andrei.app_backend.model.Location;
import dev.andrei.app_backend.repository.LocationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private static final int SEARCH_LIMIT = 50;

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional(readOnly = true)
    public List<LocationDto> getTop10CloseLocations(JustCoordinatesDto request) {

        double latitude = request.latitude();
        double longitude = request.longitude();

        return locationRepository.findTop10CloseLocations(latitude, longitude)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationDto> searchByName(String rawQuery) {
        String normalized = TextNormalizer.normalize(rawQuery);
        if (normalized.isBlank()) {
            return List.of();
        }

        PageRequest page = PageRequest.of(0, SEARCH_LIMIT, Sort.by("name").ascending());
        return locationRepository.findByNormalizedNameContaining(normalized, page)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private LocationDto toDto(Location location) {
        return new LocationDto(
                location.getId(),
                location.getName(),
                location.getPrimary_category(),
                location.getCoordinate_point().getX(),
                location.getCoordinate_point().getY(),
                location.has_toilets(),
                location.has_accessibility_features(),
                location.getAverage_score() != null ? location.getAverage_score() : 0.0
        );
    }
}
