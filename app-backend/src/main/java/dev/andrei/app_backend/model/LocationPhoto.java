package dev.andrei.app_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * One resolved Google Place photo for a {@link Location}, e.g. resource name
 * "places/&lt;id&gt;/photos/&lt;ref&gt;" at a given position. Written by
 * {@code GooglePlacesPhotoService}; the client fetches the image by index via the controller.
 *
 * <p>created_at / updated_at exist in the table with a DB default and are intentionally left unmapped
 * so inserts don't have to populate them.
 */
@Entity
@Table(name = "location_photo")
@Getter
@Setter
public class LocationPhoto {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    // smallint in the DB (photo indices are tiny: 0..max-photos).
    @Column(name = "photo_index", nullable = false)
    private Short photoIndex;

    @Column(name = "photo_name", length = 1024)
    private String photoName;

    public LocationPhoto() {}
}
