package dev.andrei.app_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

/**
 * One opening interval for a {@link Location}, e.g. "Mon 09:00–18:00". Written by
 * {@code GooglePlacesDetailsService} from Google's {@code regularOpeningHours}; the client renders
 * the weekly schedule and derives a live open-now status from these rows.
 *
 * <p>{@code dayOfWeek} is ISO 1=Mon..7=Sun. A row is always an <em>open</em> interval; days with no
 * row are treated as closed. An interval whose {@code closeTime} is earlier than its {@code openTime}
 * crosses midnight; {@code 00:00–00:00} means open 24 hours.
 *
 * <p>created_at / updated_at exist in the table with a DB default and are intentionally left unmapped
 * so inserts don't have to populate them.
 */
@Entity
@Table(name = "location_hours")
@Getter
@Setter
public class LocationHours {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    // ISO day of week: 1=Monday .. 7=Sunday.
    @Column(name = "day_of_week", nullable = false)
    private Short dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "is_closed")
    private Boolean isClosed;

    public LocationHours() {}
}
