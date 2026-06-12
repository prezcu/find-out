package dev.andrei.app_backend.repository;

import dev.andrei.app_backend.model.Location;
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
        LIMIT :limit
        """, nativeQuery = true)
    List<UUID> findTop10CloseLocationIds(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("limit") int limit
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
    // Each attribute's concept is fetched too so the mapper can expose its slug
    // (used client-side to join the user's per-concept weight) without an N+1.
    // Result order is undefined -- the service reorders to match step 1.
    @Query("""
        SELECT DISTINCT l
        FROM Location l
        LEFT JOIN FETCH l.locationAttributes la
        LEFT JOIN FETCH la.attribute a
        LEFT JOIN FETCH a.concept
        WHERE l.id IN :ids
        """)
    List<Location> findAllWithAttributesByIdIn(@Param("ids") List<UUID> ids);

    /**
     * Typo-tolerant ranked search over name + category. Returns matching ids in display order;
     * the caller hydrates them via {@link #findAllWithAttributesByIdIn} (same two-step pattern as
     * {@link #findTop10CloseLocationIds}, because native queries cannot hydrate JPA associations).
     * <p>
     * A row matches when the query is a substring of the name OR is trigram-similar (pg_trgm `%`,
     * default threshold 0.3) to the name or category. Ordering: prefix matches first, then highest
     * trigram similarity across name/category, then global rating. {@code normalizedQuery} must be
     * normalised the same way the {@code normalized_name} column was populated (see TextNormalizer).
     */
    @Query(value = """
        SELECT l.id
        FROM location l
        WHERE l.normalized_name LIKE '%' || :q || '%'
           OR l.normalized_name % :q
           OR lower(l.primary_category) % :q
           OR lower(coalesce(l.primary_category_display_name, '')) % :q
        ORDER BY
           (l.normalized_name LIKE :q || '%') DESC,
           GREATEST(
               similarity(l.normalized_name, :q),
               similarity(lower(l.primary_category), :q),
               similarity(lower(coalesce(l.primary_category_display_name, '')), :q)
           ) DESC,
           l.average_score DESC NULLS LAST
        LIMIT :limit
        """, nativeQuery = true)
    List<UUID> findFuzzyMatchIds(@Param("q") String normalizedQuery, @Param("limit") int limit);

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
