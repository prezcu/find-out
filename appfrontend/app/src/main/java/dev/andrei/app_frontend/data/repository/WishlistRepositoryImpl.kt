package dev.andrei.app_frontend.data.repository

import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.remote.api.ApiService
import dev.andrei.app_frontend.data.remote.dto.AddWishlistRequestDto
import retrofit2.Response
import javax.inject.Inject

class WishlistRepositoryImpl @Inject constructor(
    private val api: ApiService
) : WishlistRepository {

    override suspend fun getWishlist(): Result<List<LocationEntity>> = runCatching {
        val response = api.getWishlist()
        if (!response.isSuccessful) error(response.toUserMessage())
        response.body().orEmpty().map { it.toEntity() }
    }

    override suspend fun addToWishlist(locationId: String): Result<Unit> = runCatching {
        val response = api.addToWishlist(AddWishlistRequestDto(locationId))
        if (!response.isSuccessful) error(response.toUserMessage())
        Unit
    }

    override suspend fun removeFromWishlist(locationId: String): Result<Unit> = runCatching {
        val response = api.removeFromWishlist(locationId)
        if (!response.isSuccessful) error(response.toUserMessage())
        Unit
    }

    override suspend fun isWishlisted(locationId: String): Result<Boolean> = runCatching {
        val response = api.getWishlistStatus(locationId)
        if (!response.isSuccessful) error(response.toUserMessage())
        response.body()?.wishlisted ?: false
    }

    private fun Response<*>.toUserMessage(): String = when (code()) {
        401 -> "You need to be signed in"
        404 -> "Location not found"
        409 -> "Already in your wishlist"
        in 400..499 -> "Request failed (${code()})"
        in 500..599 -> "Server error, please try again later"
        else -> "Unexpected error (${code()})"
    }
}