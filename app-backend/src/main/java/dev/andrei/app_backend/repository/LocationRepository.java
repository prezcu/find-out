package dev.andrei.app_backend.repository;

import dev.andrei.app_backend.model.Location;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID>{

    //TODO: Possibly change to variable radius, last parameter in ST_DWithin
    @Query(value = """
        SELECT * FROM location
        WHERE ST_DWithin(
            coordinate_point::geography,
            ST_MakePoint(:longitude, :latitude)::geography,
            1500
        )
        ORDER BY average_score DESC
        LIMIT 10;
        """, nativeQuery = true)
    List<Location> findTop10CloseLocations(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude
    );

    /**
     * Substring search on the persisted, pre-normalised name. Caller is
     * expected to normalise {@code normalizedQuery} the same way the column
     * was populated (see TextNormalizer). Sort + limit are passed via Pageable.
     */
    List<Location> findByNormalizedNameContaining(String normalizedQuery, Pageable pageable);
}
