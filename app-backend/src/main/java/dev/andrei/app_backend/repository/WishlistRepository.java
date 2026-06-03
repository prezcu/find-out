package dev.andrei.app_backend.repository;

import dev.andrei.app_backend.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, UUID> {

    List<WishlistItem> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    boolean existsByUser_IdAndLocation_Id(UUID userId, UUID locationId);

    @Modifying
    void deleteByUser_IdAndLocation_Id(UUID userId, UUID locationId);
}