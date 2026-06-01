package dev.andrei.app_backend.repository;

import dev.andrei.app_backend.model.Location;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
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
}
