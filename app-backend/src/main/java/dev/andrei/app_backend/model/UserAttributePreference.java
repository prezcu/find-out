package dev.andrei.app_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * A user's importance (0–5) for an {@link AttributeConcept}. 0 means "don't care" and
 * contributes nothing to the match score. Logical uniqueness of (user, concept) is
 * enforced in the service (find-then-update), not via a DB constraint.
 */
@Entity
@Table(name = "user_attribute_preference")
@Getter
@Setter
public class UserAttributePreference {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "concept_id", nullable = false)
    private AttributeConcept concept;

    @Column(name = "importance", nullable = false)
    private int importance;

    public UserAttributePreference() {}
}
