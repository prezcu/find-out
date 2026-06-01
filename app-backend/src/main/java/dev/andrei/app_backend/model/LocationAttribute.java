package dev.andrei.app_backend.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import java.util.UUID;

@Entity
@Immutable
@Table(
        name = "location_attribute"
)
@Getter
@Setter // sunt probleme cu immutable?
public class LocationAttribute {
    @Id
    private UUID id;

    @Column(name="average_score", nullable = false)
    private Double average_score;

    @Column(name="score_count", nullable = false)
    private Integer score_count;

    @ManyToOne
    private Location location;

    @ManyToOne
    private Attribute attribute;
}
