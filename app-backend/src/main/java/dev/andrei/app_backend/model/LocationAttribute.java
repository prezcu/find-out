package dev.andrei.app_backend.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
// Mutable: submitting a review updates average_score and score_count.
@Table(
        name = "location_attribute"
)
@Getter
@Setter // sunt probleme cu immutable?
public class LocationAttribute {
    @Id
    private UUID id;

    // numeric column kept exact; declare the JDBC type so validation expects NUMERIC, not float.
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name="average_score", nullable = false)
    private Double average_score;

    @Column(name="score_count", nullable = false)
    private Integer score_count;

    @ManyToOne
    private Location location;

    @ManyToOne
    private Attribute attribute;
}
