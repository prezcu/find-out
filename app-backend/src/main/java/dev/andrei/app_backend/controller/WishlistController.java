package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.dto.LocationDto;
import dev.andrei.app_backend.dto.wishlist.AddWishlistRequest;
import dev.andrei.app_backend.dto.wishlist.WishlistStatusResponse;
import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<List<LocationDto>> getWishlist(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(wishlistService.getWishlist(user.getId()));
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<WishlistStatusResponse> status(@AuthenticationPrincipal User user,
                                                         @PathVariable UUID locationId) {
        return ResponseEntity.ok(
                new WishlistStatusResponse(wishlistService.isWishlisted(user.getId(), locationId)));
    }

    @PostMapping
    public ResponseEntity<Void> add(@AuthenticationPrincipal User user,
                                    @Valid @RequestBody AddWishlistRequest request) {
        wishlistService.addToWishlist(user.getId(), request.locationId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal User user,
                                       @PathVariable UUID locationId) {
        wishlistService.removeFromWishlist(user.getId(), locationId);
        return ResponseEntity.noContent().build();
    }
}