package dev.andrei.app_backend.repository;

import dev.andrei.app_backend.model.AttributeConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttributeConceptRepository extends JpaRepository<AttributeConcept, UUID> {

    // Presentation order for the preference editor.
    List<AttributeConcept> findAllByOrderBySortOrderAsc();
}
