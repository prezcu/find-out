package dev.andrei.app_backend.repository;

import dev.andrei.app_backend.model.Location;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    // Step 1: PostGIS distance filter. Native because ST_DWithin / ST_MakePoint /
    // the ::geography cast have no portable JPQL equivalent. Returns IDs only --
    // native queries cannot hydrate JPA associations.
    //TODO: Possibly change to variable radius, last parameter in ST_DWithin
    @Query(value = """
        SELECT l.id
        FROM location l
        WHERE ST_DWithin(
            l.coordinate_point::geography,
            ST_MakePoint(:longitude, :latitude)::geography,
            1500
        )
        ORDER BY l.average_score DESC
        LIMIT 10
        """, nativeQuery = true)
    List<UUID> findTop10CloseLocationIds(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude
    );

    // Match scoring: all candidate ids within a variable radius (meters). No average_score
    // pre-sort/limit here -- the service ranks by per-user match score, not global rating.
    @Query(value = """
        SELECT l.id
        FROM location l
        WHERE ST_DWithin(
            l.coordinate_point::geography,
            ST_MakePoint(:longitude, :latitude)::geography,
            :radiusMeters
        )
        """, nativeQuery = true)
    List<UUID> findLocationIdsWithinRadius(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters
    );

    // Like findAllWithAttributesByIdIn but also fetches each attribute's concept,
    // needed to map location attributes onto the user's concept-level preferences.
    @Query("""
        SELECT DISTINCT l
        FROM Location l
        LEFT JOIN FETCH l.locationAttributes la
        LEFT JOIN FETCH la.attribute a
        LEFT JOIN FETCH a.concept
        WHERE l.id IN :ids
        """)
    List<Location> findAllWithAttributesAndConceptByIdIn(@Param("ids") List<UUID> ids);

    // Step 2: hydrate locations + their attribute graph in one SELECT.
    // LEFT JOIN so locations without attributes still come back. DISTINCT
    // collapses the row multiplication caused by the one-to-many fetch.
    // Result order is undefined -- the service reorders to match step 1.
    @Query("""
        SELECT DISTINCT l
        FROM Location l
        LEFT JOIN FETCH l.locationAttributes la
        LEFT JOIN FETCH la.attribute
        WHERE l.id IN :ids
        """)
    List<Location> findAllWithAttributesByIdIn(@Param("ids") List<UUID> ids);

    /**
     * Substring search on the persisted, pre-normalised name. Caller is
     * expected to normalise {@code normalizedQuery} the same way the column
     * was populated (see TextNormalizer). Sort + limit are passed via Pageable.
     */
    @EntityGraph(attributePaths = {"locationAttributes", "locationAttributes.attribute"})
    List<Location> findByNormalizedNameContaining(String normalizedQuery, Pageable pageable);

    // Single location with its attribute aggregates fetched, so a review submission can validate
    // attribute names and update the running averages in one managed graph. Mirrors
    // findAllWithAttributesByIdIn but for one id.
    @Query("""
        SELECT l
        FROM Location l
        LEFT JOIN FETCH l.locationAttributes la
        LEFT JOIN FETCH la.attribute
        WHERE l.id = :id
        """)
    Optional<Location> findWithAttributesById(@Param("id") UUID id);
}
