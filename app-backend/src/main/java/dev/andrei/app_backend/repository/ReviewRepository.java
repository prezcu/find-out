package dev.andrei.app_backend.repository;

import dev.andrei.app_backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Traverses review.user.id and review.location.id (the underscores make the path explicit).
    boolean existsByUser_IdAndLocation_Id(UUID userId, UUID locationId);
}