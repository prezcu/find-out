package dev.andrei.app_backend.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.util.Set;
import java.util.UUID;

@Entity
@Immutable
@Table(
        name = "attribute"
)
@Getter
@Setter // sunt probleme cu immutable?
public class Attribute {
    @Id
    private UUID id;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="global_weight", nullable = false)
    private double global_weight;
}
