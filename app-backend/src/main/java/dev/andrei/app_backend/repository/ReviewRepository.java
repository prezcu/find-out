package dev.andrei.app_backend.repository;

import dev.andrei.app_backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Traverses review.user.id and review.location.id (the underscores make the path explicit).
    boolean existsByUser_IdAndLocation_Id(UUID userId, UUID locationId);

    // Reviews of a location, newest first, with the reviewer + attribute scores fetched in one go.
    // Only one collection (attributeScores) is fetched, so no MultipleBagFetchException.
    @Query("""
        SELECT DISTINCT r
        FROM Review r
        JOIN FETCH r.user
        LEFT JOIN FETCH r.attributeScores s
        LEFT JOIN FETCH s.attribute
        WHERE r.location.id = :locationId
        ORDER BY r.createdAt DESC
        """)
    List<Review> findByLocationIdWithScores(@Param("locationId") UUID locationId);

    // The current user's reviews, newest first, with the location + attribute scores fetched.
    @Query("""
        SELECT DISTINCT r
        FROM Review r
        JOIN FETCH r.location
        LEFT JOIN FETCH r.attributeScores s
        LEFT JOIN FETCH s.attribute
        WHERE r.user.id = :userId
        ORDER BY r.createdAt DESC
        """)
    List<Review> findByUserIdWithScores(@Param("userId") UUID userId);
}