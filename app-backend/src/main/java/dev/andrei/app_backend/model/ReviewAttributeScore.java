package dev.andrei.app_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/** The score one {@link Review} assigned to a single attribute, e.g. "Cleanliness" -> 4.5. */
@Entity
@Table(name = "review_attribute_score")
@Getter
@Setter
public class ReviewAttributeScore {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    // numeric column kept exact; declare the JDBC type so validation expects NUMERIC, not float.
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "score", nullable = false)
    private double score;

    public ReviewAttributeScore() {}
}
