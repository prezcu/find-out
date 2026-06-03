package dev.andrei.app_backend.dto.wishlist;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddWishlistRequest(@NotNull UUID locationId) {}