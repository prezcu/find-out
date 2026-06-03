package dev.andrei.app_backend.repository;

import dev.andrei.app_backend.model.UserAttributePreference;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAttributePreferenceRepository extends JpaRepository<UserAttributePreference, UUID> {

    // concept fetched so the editor can render display names without N+1.
    @EntityGraph(attributePaths = "concept")
    List<UserAttributePreference> findByUser_Id(UUID userId);

    Optional<UserAttributePreference> findByUser_IdAndConcept_Id(UUID userId, UUID conceptId);
}
