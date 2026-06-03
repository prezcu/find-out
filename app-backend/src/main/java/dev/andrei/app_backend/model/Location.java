package dev.andrei.app_backend.model;

import dev.andrei.app_backend.service.TextNormalizer;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
// Mutable: submitting a review updates average_score and the per-attribute aggregates.
@Table(
        name = "location",
        indexes = {
                // B-tree helps prefix lookups (LIKE 'foo%') and exact matches.
                // Substring search (LIKE '%foo%') still scans, but the row-by-row
                // LOWER(name) call is gone -- normalisation happens once at write.
                @Index(name = "idx_location_normalized_name", columnList = "normalized_name")
        }
)
@Getter
@Setter // sunt probleme cu immutable?
public class Location {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "normalized_name")
    private String normalizedName;

    @Column(name = "primary_category", nullable = false)
    private String primary_category;

    // UI-friendly category label. Nullable: the client falls back to a prettified
    // primary_category when absent.
    @Column(name = "primary_category_display_name")
    private String primaryCategoryDisplayName;

    @Column(name = "coordinate_point", columnDefinition = "geography(Point, 4326)", nullable = false)
    private Point coordinate_point;

    @Column(name = "has_accessibility_features", nullable = false)
    private boolean has_accessibility_features;

    @Column(name = "has_toilets", nullable = false)
    private boolean has_toilets;

    @Column(name = "created_at", nullable = false)
    private Instant created_at;

    @Column(name = "updated_at", nullable = false)
    private Instant updated_at;

    @Column(name = "average_score")
    private Double average_score;

    @Column(name = "price_tier")
    private String price_tier;

    @Column(name = "website_url")
    private String website_url;

    @OneToMany(mappedBy = "location")
    private Set<LocationAttribute> locationAttributes;

    public Location() {}

    @PrePersist
    @PreUpdate
    private void syncNormalizedName() {
        this.normalizedName = TextNormalizer.normalize(this.name);
    }

    public boolean has_toilets() {
        return has_toilets;
    }

    public boolean has_accessibility_features() {
    return has_accessibility_features;
    }
}
