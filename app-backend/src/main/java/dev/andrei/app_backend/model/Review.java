package dev.andrei.app_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "review",
        // One review per user per location. Backed by a service-side check (for a friendly 409)
        // and this DB constraint as the last line of defence against races / duplicate requests.
        uniqueConstraints = @UniqueConstraint(
                name = "uq_review_user_location",
                columnNames = {"user_id", "location_id"}
        )
)
@Getter
@Setter
public class Review {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Weighted mean of this review's attribute scores (score * attribute global_weight) normalised by
    // the total weight. Persisted (the column is NOT NULL) so it can be read back without recomputing.
    // numeric column kept exact; declare the JDBC type so validation expects NUMERIC, not float.
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "average_score", nullable = false)
    private double averageScore;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewAttributeScore> attributeScores = new ArrayList<>();

    public Review() {}

    /** Keeps both sides of the relationship in sync */
    public void addAttributeScore(ReviewAttributeScore attributeScore) {
        attributeScores.add(attributeScore);
        attributeScore.setReview(this);
    }
}
