package dev.andrei.app_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** A user's saved (wishlisted) location. Independent of Review — see the plan's diamond note. */
@Entity
@Table(
        name = "wishlist_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_wishlist_user_location",
                columnNames = {"user_id", "location_id"}
        )
)
@Getter
@Setter
public class WishlistItem {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public WishlistItem() {}
}
