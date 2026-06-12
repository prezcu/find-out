package dev.andrei.app_backend.service;

import dev.andrei.app_backend.dto.AttributeDto;
import dev.andrei.app_backend.dto.LocationDto;
import dev.andrei.app_backend.model.Location;

import java.util.List;

/** Maps a Location (with its attributes already fetched) to the API LocationDto. */
public final class LocationMapper {

    private LocationMapper() {}

    public static LocationDto toDto(Location location) {
        return toDto(location, null);
    }

    public static LocationDto toDto(Location location, Double matchScore) {
        List<AttributeDto> attributes = location.getLocationAttributes().stream()
                .map(la -> {
                    var concept = la.getAttribute().getConcept();
                    return new AttributeDto(
                            la.getAttribute().getName(),
                            concept != null ? concept.getSlug() : null,
                            la.getAverage_score());
                })
                .toList();

        return new LocationDto(
                location.getId(),
                location.getName(),
                location.getPrimary_category(),
                location.getPrimaryCategoryDisplayName(),
                location.getCoordinate_point().getX(),
                location.getCoordinate_point().getY(),
                location.has_toilets(),
                location.has_accessibility_features(),
                location.getAverage_score(),
                attributes,
                matchScore
        );
    }
}
