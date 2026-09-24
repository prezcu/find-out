package dev.andrei.app_backend.service;

import dev.andrei.app_backend.dto.LocationDto;
import dev.andrei.app_backend.model.Location;
import dev.andrei.app_backend.model.WishlistItem;
import dev.andrei.app_backend.repository.LocationRepository;
import dev.andrei.app_backend.repository.UserRepository;
import dev.andrei.app_backend.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    /// no autowired notation because the class only has 1 constructor so it is implicitly autowired
    WishlistService (WishlistRepository wishlistRepository, UserRepository userRepository,
                     LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addToWishlist(UUID userId, UUID locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new LocationNotFoundException();
        }

        if (wishlistRepository.existsByUser_IdAndLocation_Id(userId, locationId)) {
            throw new AlreadyWishlistedException();
        }

        WishlistItem item = new WishlistItem();
        item.setId(UUID.randomUUID());
        item.setUser(userRepository.getReferenceById(userId));
        item.setLocation(locationRepository.getReferenceById(locationId));
        item.setCreatedAt(Instant.now());
        wishlistRepository.save(item);


    }

    @Transactional
    public void removeFromWishlist(UUID userId, UUID locationId) {
        // idempotent: deleting a missing row is a no-op (controller still returns 204)
        wishlistRepository.deleteByUser_IdAndLocation_Id(userId, locationId);
    }

    @Transactional(readOnly = true)
    public List<LocationDto> getWishlist(UUID userId) {
        // Reading getLocation().getId() on the lazy proxy uses the FK only (no extra query);
        // the locations + attributes are then fetched in one shot, mirroring LocationService.
        List<UUID> orderedIds = wishlistRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(item -> item.getLocation().getId())
                .toList();
        if (orderedIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Location> byId = locationRepository.findAllWithAttributesByIdIn(orderedIds).stream()
                .collect(Collectors.toMap(Location::getId, l -> l));

        return orderedIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(LocationMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isWishlisted(UUID userId, UUID locationId) {
        return wishlistRepository.existsByUser_IdAndLocation_Id(userId, locationId);
    }

    public static class LocationNotFoundException extends RuntimeException {}
    public static class AlreadyWishlistedException extends RuntimeException {}
}
