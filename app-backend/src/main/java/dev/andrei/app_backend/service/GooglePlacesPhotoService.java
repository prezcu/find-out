package dev.andrei.app_backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.andrei.app_backend.model.Location;
import dev.andrei.app_backend.model.LocationPhoto;
import dev.andrei.app_backend.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves Google Place Photos for a {@link Location} and serves their image URLs.
 *
 * <p>Google's Place Photos API can't be queried by raw coordinates: we first run a
 * <em>Text Search (New)</em> with the location's name biased to its coordinates to obtain a place id
 * and photo <em>resource names</em>, persist those once, then exchange a resource name for a fresh,
 * key-less image URL via <em>Place Photo (New)</em> on demand.
 *
 * <p>The API key stays server-side. When no key is configured the service no-ops (returns no photos)
 * so the rest of the app keeps working.
 */
@Service
public class GooglePlacesPhotoService {

    private static final Logger log = LoggerFactory.getLogger(GooglePlacesPhotoService.class);
    private static final String BASE_URL = "https://places.googleapis.com";
    // Bias the text search to roughly the location's neighbourhood (metres).
    private static final double SEARCH_BIAS_RADIUS_M = 500.0;

    private final LocationRepository locationRepository;
    private final RestClient restClient;
    private final String apiKey;
    private final int maxPhotos;
    private final int photoMaxWidthPx;

    public GooglePlacesPhotoService(
            LocationRepository locationRepository,
            @Value("${google.places.api-key:}") String apiKey,
            @Value("${google.places.max-photos:8}") int maxPhotos,
            @Value("${google.places.photo-max-width-px:800}") int photoMaxWidthPx) {
        this.locationRepository = locationRepository;
        this.apiKey = apiKey;
        this.maxPhotos = maxPhotos;
        this.photoMaxWidthPx = photoMaxWidthPx;
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    /** Photo features are active only when an API key is configured. */
    public boolean isEnabled() {
        return StringUtils.hasText(apiKey);
    }

    /**
     * Returns the Google photo resource names for a location, resolving + persisting them on first
     * use. Already-resolved locations (incl. those that resolved to no photos) are served from the
     * persisted state without another Google call.
     */
    @Transactional
    public List<String> ensureResolvedPhotoNames(UUID locationId) {
        Optional<Location> maybe = locationRepository.findById(locationId);
        if (maybe.isEmpty()) {
            return List.of();
        }
        Location location = maybe.get();

        if (location.getPhotosFetchedAt() != null) {
            // resolved before (possibly empty); ordered by photo_index via @OrderBy
            return location.getPhotos().stream().map(LocationPhoto::getPhotoName).toList();
        }
        if (!isEnabled()) {
            return List.of(); // leave unresolved so a later key configuration still works
        }

        try {
            ResolvedPlace resolved = searchPlace(location.getName(),
                    location.getCoordinate_point().getY(),  // latitude
                    location.getCoordinate_point().getX());  // longitude

            location.setGooglePlaceId(resolved.placeId());

            // Replace any existing rows (orphanRemoval cleans them up) with the freshly resolved set.
            location.getPhotos().clear();
            List<String> names = resolved.photoNames();
            for (short i = 0; i < names.size(); i++) {
                LocationPhoto photo = new LocationPhoto();
                photo.setId(UUID.randomUUID());
                photo.setLocation(location);
                photo.setPhotoIndex(i);
                photo.setPhotoName(names.get(i));
                location.getPhotos().add(photo);
            }
            location.setPhotosFetchedAt(Instant.now()); // also marks an empty result (negative cache)
            locationRepository.save(location);
            return names;
        } catch (Exception e) {
            // Transient failure: don't poison the negative cache, just serve nothing this time.
            log.warn("Google Places photo resolution failed for location {} ({}): {}",
                    locationId, location.getName(), e.toString());
            return List.of();
        }
    }

    /**
     * Exchanges a stored photo resource name for a fresh, key-less image URL
     * (an {@code lh3.googleusercontent.com} link). Returns {@code null} on failure.
     */
    public String resolvePhotoUri(String photoName, int maxWidthPx) {
        if (!isEnabled() || !StringUtils.hasText(photoName)) {
            return null;
        }
        try {
            // photoName already contains slashes ("places/.../photos/..."); build the URI directly so
            // template expansion never percent-encodes them.
            URI uri = URI.create(BASE_URL + "/v1/" + photoName + "/media"
                    + "?maxWidthPx=" + maxWidthPx + "&skipHttpRedirect=true");
            PhotoMediaResponse body = restClient.get()
                    .uri(uri)
                    .header("X-Goog-Api-Key", apiKey)
                    .retrieve()
                    .body(PhotoMediaResponse.class);
            return body != null ? body.photoUri() : null;
        } catch (Exception e) {
            log.warn("Google Places photo media fetch failed for {}: {}", photoName, e.toString());
            return null;
        }
    }

    public int getPhotoMaxWidthPx() {
        return photoMaxWidthPx;
    }

    // --- Google Places (New) Text Search ---

    private record ResolvedPlace(String placeId, List<String> photoNames) {}

    private ResolvedPlace searchPlace(String name, double latitude, double longitude) {
        Map<String, Object> request = Map.of(
                "textQuery", name,
                "maxResultCount", 1,
                "locationBias", Map.of(
                        "circle", Map.of(
                                "center", Map.of("latitude", latitude, "longitude", longitude),
                                "radius", SEARCH_BIAS_RADIUS_M)));

        TextSearchResponse response = restClient.post()
                .uri("/v1/places:searchText")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", "places.id,places.photos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TextSearchResponse.class);

        if (response == null || response.places() == null || response.places().isEmpty()) {
            return new ResolvedPlace(null, List.of());
        }
        Place place = response.places().get(0);
        List<String> names = place.photos() == null ? List.of() : place.photos().stream()
                .map(Photo::name)
                .filter(StringUtils::hasText)
                .limit(maxPhotos)
                .toList();
        return new ResolvedPlace(place.id(), names);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TextSearchResponse(List<Place> places) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Place(String id, List<Photo> photos) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Photo(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PhotoMediaResponse(String name, String photoUri) {}
}
