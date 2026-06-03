package dev.andrei.app_backend.dto.wishlist;

/** Whether the current user has the given location in their wishlist. */
public record WishlistStatusResponse(boolean wishlisted) {}