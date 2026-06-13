package dev.andrei.app_backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.andrei.app_backend.dto.LocationDetailsDto;
import dev.andrei.app_backend.dto.OpeningHoursDto;
import dev.andrei.app_backend.model.Location;
import dev.andrei.app_backend.model.LocationHours;
import dev.andrei.app_backend.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves a {@link Location}'s street address and weekly opening hours via Google
 * <em>Place Details (New)</em>, persisting them on first use so the paid call is made at most once
 * per location and never refreshed.
 *
 * <p>Opening hours ({@code regularOpeningHours}) is an Enterprise SKU field, so this runs only when a
 * user opens an attraction screen. Already-resolved locations (incl. those that resolved to no
 * address/hours) are served from the persisted state without another Google call — the
 * {@code details_fetched_at} timestamp is the negative cache, mirroring {@link GooglePlacesPhotoService}.
 *
 * <p>The cached hours are the <em>regular weekly schedule</em>; the live "open now" status is derived
 * on the client, never from a cached {@code openNow} boolean (which would be frozen at fetch time).
 */
@Service
public class GooglePlacesDetailsService {

    private static final Logger log = LoggerFactory.getLogger(GooglePlacesDetailsService.class);
    private static final String BASE_URL = "https://places.googleapis.com";

    private final LocationRepository locationRepository;
    private final GooglePlacesPhotoService photoService;
    private final RestClient restClient;
    private final String apiKey;

    public GooglePlacesDetailsService(
            LocationRepository locationRepository,
            GooglePlacesPhotoService photoService,
            @Value("${google.places.api-key:}") String apiKey) {
        this.locationRepository = locationRepository;
        this.photoService = photoService;
        this.apiKey = apiKey;
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    /** Details features are active only when an API key is configured. */
    public boolean isEnabled() {
        return StringUtils.hasText(apiKey);
    }

    /**
     * Returns the address + weekly hours for a location, resolving + persisting them on first use.
     * Subsequent calls (and the no-key / not-found cases) are served from the persisted state without
     * another Google call.
     */
    @Transactional
    public LocationDetailsDto ensureResolvedDetails(UUID locationId) {
        Optional<Location> maybe = locationRepository.findById(locationId);
        if (maybe.isEmpty()) {
            return new LocationDetailsDto(null, List.of());
        }
        Location location = maybe.get();

        if (location.getDetailsFetchedAt() != null) {
            return toDto(location); // resolved before (possibly empty)
        }
        if (!isEnabled()) {
            return toDto(location); // leave unresolved so a later key configuration still works
        }

        // Place Details needs a place id. Photo resolution stores it as a side effect; reuse that so
        // the cheap photo Text Search (not the Enterprise field mask) does the lookup.
        if (!StringUtils.hasText(location.getGooglePlaceId())) {
            photoService.ensureResolvedPhotoNames(locationId); // same tx: updates the managed entity
        }
        String placeId = location.getGooglePlaceId();
        if (!StringUtils.hasText(placeId)) {
            return toDto(location); // couldn't resolve a place id; don't poison the cache, allow retry
        }

        try {
            PlaceDetails details = restClient.get()
                    .uri("/v1/places/{placeId}", placeId)
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "formattedAddress,regularOpeningHours")
                    .retrieve()
                    .body(PlaceDetails.class);

            location.setAddress(details != null ? details.formattedAddress() : null);

            // Replace any existing rows (orphanRemoval cleans them up) with the freshly resolved set.
            location.getHours().clear();
            if (details != null && details.regularOpeningHours() != null
                    && details.regularOpeningHours().periods() != null) {
                for (Period period : details.regularOpeningHours().periods()) {
                    LocationHours row = toRow(location, period);
                    if (row != null) {
                        location.getHours().add(row);
                    }
                }
            }
            location.setDetailsFetchedAt(Instant.now()); // also marks an empty result (negative cache)
            locationRepository.save(location);
            return toDto(location);
        } catch (Exception e) {
            // Transient failure: don't poison the negative cache, just serve what we have this time.
            log.warn("Google Places details resolution failed for location {} ({}): {}",
                    locationId, location.getName(), e.toString());
            return toDto(location);
        }
    }

    /** Builds one weekly-hours row from a Google period, or null when the period has no open time. */
    private LocationHours toRow(Location location, Period period) {
        if (period == null || period.open() == null) {
            return null;
        }
        LocationHours row = new LocationHours();
        row.setId(UUID.randomUUID());
        row.setLocation(location);
        row.setDayOfWeek(toIsoDay(period.open().day()));
        row.setOpenTime(LocalTime.of(period.open().hour(), period.open().minute()));
        // A period with no close means open 24h; represent as 00:00 (open is already 00:00 too).
        row.setCloseTime(period.close() == null
                ? LocalTime.MIDNIGHT
                : LocalTime.of(period.close().hour(), period.close().minute()));
        row.setIsClosed(false);
        return row;
    }

    /** Google numbers days 0=Sunday..6=Saturday; store ISO 1=Monday..7=Sunday. */
    private static short toIsoDay(int googleDay) {
        return (short) (googleDay == 0 ? 7 : googleDay);
    }

    private LocationDetailsDto toDto(Location location) {
        List<OpeningHoursDto> hours = location.getHours().stream()
                .map(h -> new OpeningHoursDto(
                        h.getDayOfWeek(),
                        formatTime(h.getOpenTime()),
                        formatTime(h.getCloseTime()),
                        Boolean.TRUE.equals(h.getIsClosed())))
                .toList();
        return new LocationDetailsDto(location.getAddress(), hours);
    }

    private static String formatTime(LocalTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    // --- Google Places (New) Place Details response (only the fields we mask for) ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PlaceDetails(String formattedAddress, RegularOpeningHours regularOpeningHours) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RegularOpeningHours(List<Period> periods) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Period(TimePoint open, TimePoint close) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TimePoint(int day, int hour, int minute) {}
}
