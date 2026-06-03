package dev.andrei.app_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

/**
 * A user-facing "concept" that one or more category-specific {@link Attribute}s map to
 * (e.g. "cleanliness"). Preferences attach to concepts; the match formula maps a
 * concept-level preference onto whichever attribute row a location actually has.
 * Read-only reference data — seeded directly in the database.
 */
@Entity
@Immutable
@Table(name = "attribute_concept")
@Getter
@Setter
public class AttributeConcept {

    @Id
    private UUID id;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "group_label")
    private String groupLabel;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
