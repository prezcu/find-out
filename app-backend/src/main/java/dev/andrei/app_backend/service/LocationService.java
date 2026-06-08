package dev.andrei.app_backend.service;

import dev.andrei.app_backend.dto.JustCoordinatesDto;
import dev.andrei.app_backend.dto.LocationDto;
import dev.andrei.app_backend.model.AttributeConcept;
import dev.andrei.app_backend.model.Location;
import dev.andrei.app_backend.model.LocationAttribute;
import dev.andrei.app_backend.model.UserAttributePreference;
import dev.andrei.app_backend.repository.LocationRepository;
import dev.andrei.app_backend.repository.UserAttributePreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private static final int SEARCH_LIMIT = 50;
    private static final int RESULT_LIMIT = 10;
    private static final double MATCH_RADIUS_METERS = 2000.0;

    private final LocationRepository locationRepository;
    private final UserAttributePreferenceRepository preferenceRepository;

    public LocationService(LocationRepository locationRepository,
                           UserAttributePreferenceRepository preferenceRepository) {
        this.locationRepository = locationRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @Transactional(readOnly = true)
    public List<LocationDto> getTop10CloseLocations(JustCoordinatesDto request) {
        List<UUID> orderedIds = locationRepository.findTop10CloseLocationIds(
                request.latitude(), request.longitude());
        if (orderedIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Location> byId = locationRepository
                .findAllWithAttributesByIdIn(orderedIds)
                .stream()
                .collect(Collectors.toMap(Location::getId, l -> l));

        return orderedIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(LocationMapper::toDto)
                .toList();
    }

    /**
     * Locations within {@value #MATCH_RADIUS_METERS} m of the device, ranked by the user's
     * personal match score. When the user has no effective (importance &gt; 0) preferences, falls
     * back to a rating-sorted list (matchScore null).
     */
    @Transactional(readOnly = true)
    public List<LocationDto> getRecommendedLocations(UUID userId, JustCoordinatesDto request) {
        // Only concepts the user actually cares about contribute to the score.
        Map<UUID, Integer> importanceByConcept = preferenceRepository.findByUser_Id(userId).stream()
                .filter(p -> p.getImportance() > 0)
                .collect(Collectors.toMap(p -> p.getConcept().getId(),
                        UserAttributePreference::getImportance, (a, b) -> a));

        List<UUID> candidateIds = locationRepository.findLocationIdsWithinRadius(
                request.latitude(), request.longitude(), MATCH_RADIUS_METERS);
        if (candidateIds.isEmpty()) {
            return List.of();
        }

        List<Location> locations = locationRepository.findAllWithAttributesAndConceptByIdIn(candidateIds);

        // Fallback: no preferences -> behave like the rating-sorted nearby list.
        if (importanceByConcept.isEmpty()) {
            return locations.stream()
                    .filter(l -> l.getAverage_score() != null)
                    .sorted(Comparator.comparingDouble(Location::getAverage_score).reversed())
                    .limit(RESULT_LIMIT)
                    .map(LocationMapper::toDto)
                    .toList();
        }

        return locations.stream()
                .map(l -> Map.entry(l, computeMatchScore(l, importanceByConcept)))
                .sorted(Map.Entry.<Location, Double>comparingByValue().reversed())
                .limit(RESULT_LIMIT)
                .map(e -> LocationMapper.toDto(e.getKey(), e.getValue()))
                .toList();
    }

    // MatchScore = sum(S_la * P_concept * W_a) / sum(P_concept * W_a) over the location's
    // rated attributes whose concept the user cares about. Undefined (no overlap) -> 0.0.
    private double computeMatchScore(Location location, Map<UUID, Integer> importanceByConcept) {
        if (location.getLocationAttributes() == null) {
            return 0.0;
        }
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        for (LocationAttribute la : location.getLocationAttributes()) {
            Integer count = la.getScore_count();
            Double avg = la.getAverage_score();
            if (count == null || count == 0 || avg == null) {
                continue; // unrated attribute
            }
            AttributeConcept concept = la.getAttribute().getConcept();
            if (concept == null) {
                continue; // attribute not mapped to a concept
            }
            int p = importanceByConcept.getOrDefault(concept.getId(), 0);
            if (p == 0) {
                continue; // user doesn't care about this concept
            }
            double w = la.getAttribute().getGlobal_weight();
            weightedSum += avg * p * w;
            weightTotal += p * w;
        }
        return weightTotal > 0 ? weightedSum / weightTotal : 0.0;
    }

    @Transactional(readOnly = true)
    public List<LocationDto> searchByName(String rawQuery) {
        String normalized = TextNormalizer.normalize(rawQuery);
        if (normalized.isBlank()) {
            return List.of();
        }

        // Native ranking query returns ids in display order; hydrate the attribute graph in a
        // second SELECT (native queries can't hydrate associations), then restore that order.
        List<UUID> orderedIds = locationRepository.findFuzzyMatchIds(normalized, SEARCH_LIMIT);
        if (orderedIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Location> byId = locationRepository
                .findAllWithAttributesByIdIn(orderedIds)
                .stream()
                .collect(Collectors.toMap(Location::getId, l -> l));

        return orderedIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(LocationMapper::toDto)
                .toList();
    }

}
