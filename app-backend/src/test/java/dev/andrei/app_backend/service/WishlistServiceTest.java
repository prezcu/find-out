package dev.andrei.app_backend.service;

import dev.andrei.app_backend.dto.LocationDto;
import dev.andrei.app_backend.model.Location;
import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.model.WishlistItem;
import dev.andrei.app_backend.repository.LocationRepository;
import dev.andrei.app_backend.repository.UserRepository;
import dev.andrei.app_backend.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WishlistService wishlistService;

    private final UUID userId = UUID.randomUUID();
    private final UUID locationId = UUID.randomUUID();

    @Test
    void addToWishlist_whenNew_savesItem() {
        Mockito.when(locationRepository.existsById(locationId)).thenReturn(true);
        Mockito.when(wishlistRepository.existsByUser_IdAndLocation_Id(userId, locationId)).thenReturn(false);
        Mockito.when(userRepository.getReferenceById(userId))
                .thenReturn(new User(userId, "u@t.com", "h", Instant.now()));
        Mockito.when(locationRepository.getReferenceById(locationId)).thenReturn(location(locationId, "Park"));

        wishlistService.addToWishlist(userId, locationId);

        ArgumentCaptor<WishlistItem> captor = ArgumentCaptor.forClass(WishlistItem.class);
        Mockito.verify(wishlistRepository).saveAndFlush(captor.capture());
        WishlistItem saved = captor.getValue();
        assertNotNull(saved.getId());
        assertEquals(userId, saved.getUser().getId());
        assertEquals(locationId, saved.getLocation().getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void addToWishlist_whenLocationMissing_throwsLocationNotFound() {
        Mockito.when(locationRepository.existsById(locationId)).thenReturn(false);

        assertThrows(WishlistService.LocationNotFoundException.class,
                () -> wishlistService.addToWishlist(userId, locationId));
        Mockito.verify(wishlistRepository, Mockito.never()).saveAndFlush(any());
    }

    @Test
    void addToWishlist_whenDuplicate_throwsAlreadyWishlisted() {
        Mockito.when(locationRepository.existsById(locationId)).thenReturn(true);
        Mockito.when(wishlistRepository.existsByUser_IdAndLocation_Id(userId, locationId)).thenReturn(true);

        assertThrows(WishlistService.AlreadyWishlistedException.class,
                () -> wishlistService.addToWishlist(userId, locationId));
        Mockito.verify(wishlistRepository, Mockito.never()).saveAndFlush(any());
    }

    @Test
    void addToWishlist_whenInsertHitsUniqueConstraint_throwsAlreadyWishlisted() {
        Mockito.when(locationRepository.existsById(locationId)).thenReturn(true);
        Mockito.when(wishlistRepository.existsByUser_IdAndLocation_Id(userId, locationId)).thenReturn(false);

        Mockito.when(wishlistRepository.saveAndFlush(any()))
                .thenThrow(DataIntegrityViolationException.class);

        assertThrows(WishlistService.AlreadyWishlistedException.class,
                () -> wishlistService.addToWishlist(userId, locationId));
    }

    @Test
    void isWishlisted_delegatesToRepository() {
        Mockito.when(wishlistRepository.existsByUser_IdAndLocation_Id(userId, locationId)).thenReturn(true);
        assertTrue(wishlistService.isWishlisted(userId, locationId));
    }

    @Test
    void removeFromWishlist_deletesRow() {
        wishlistService.removeFromWishlist(userId, locationId);
        Mockito.verify(wishlistRepository).deleteByUser_IdAndLocation_Id(userId, locationId);
    }

    @Test
    void getWishlist_mapsLocationsPreservingWishlistOrder() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Mockito.when(wishlistRepository.findByUser_IdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(wishlistItem(id1, "Newest"), wishlistItem(id2, "Older")));
        // Repository returns them out of order; the service must reorder to match the wishlist.
        Mockito.when(locationRepository.findAllWithAttributesByIdIn(List.of(id1, id2)))
                .thenReturn(List.of(location(id2, "Older"), location(id1, "Newest")));

        List<LocationDto> result = wishlistService.getWishlist(userId);

        assertEquals(2, result.size());
        assertEquals(id1, result.get(0).id());
        assertEquals(id2, result.get(1).id());
    }

    @Test
    void getWishlist_whenEmpty_returnsEmpty() {
        Mockito.when(wishlistRepository.findByUser_IdOrderByCreatedAtDesc(userId)).thenReturn(List.of());
        assertTrue(wishlistService.getWishlist(userId).isEmpty());
    }

    private WishlistItem wishlistItem(UUID locId, String name) {
        WishlistItem item = new WishlistItem();
        item.setId(UUID.randomUUID());
        item.setLocation(location(locId, name));
        item.setCreatedAt(Instant.now());
        return item;
    }

    private Location location(UUID id, String name) {
        Location l = new Location();
        l.setId(id);
        l.setName(name);
        l.setPrimary_category("park");
        l.setCoordinate_point(new GeometryFactory().createPoint(new Coordinate(1.0, 2.0)));
        l.setHas_toilets(true);
        l.setHas_accessibility_features(false);
        l.setAverage_score(4.0);
        l.setLocationAttributes(new HashSet<>());
        return l;
    }
}
