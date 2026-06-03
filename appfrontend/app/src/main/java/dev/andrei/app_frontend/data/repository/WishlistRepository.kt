package dev.andrei.app_frontend.data.repository

import dev.andrei.app_frontend.data.local.entity.LocationEntity

interface WishlistRepository {

    suspend fun getWishlist(): Result<List<LocationEntity>>

    suspend fun addToWishlist(locationId: String): Result<Unit>

    suspend fun removeFromWishlist(locationId: String): Result<Unit>

    suspend fun isWishlisted(locationId: String): Result<Boolean>
}