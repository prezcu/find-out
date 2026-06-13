package dev.andrei.app_frontend.data.remote.api

import dev.andrei.app_frontend.data.remote.dto.AddWishlistRequestDto
import dev.andrei.app_frontend.data.remote.dto.JustCoordinatesDto
import dev.andrei.app_frontend.data.remote.dto.LocationDetailsDto
import dev.andrei.app_frontend.data.remote.dto.LocationDto
import dev.andrei.app_frontend.data.remote.dto.LocationPhotosDto
import dev.andrei.app_frontend.data.remote.dto.MyReviewDto
import dev.andrei.app_frontend.data.remote.dto.ReviewDto
import dev.andrei.app_frontend.data.remote.dto.SubmitReviewRequestDto
import dev.andrei.app_frontend.data.remote.dto.WishlistStatusDto
import dev.andrei.app_frontend.data.remote.dto.auth.AuthResponse
import dev.andrei.app_frontend.data.remote.dto.auth.ChangePasswordRequest
import dev.andrei.app_frontend.data.remote.dto.auth.LoginRequest
import dev.andrei.app_frontend.data.remote.dto.auth.RegisterRequest
import dev.andrei.app_frontend.data.remote.dto.preference.AttributeConceptDto
import dev.andrei.app_frontend.data.remote.dto.preference.UpdatePreferencesRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// data class for test endpoint
data class ApiResponse(val message: String)


interface ApiService {
    @GET("/api/hello")
    suspend fun getHelloMessage(): ApiResponse

    @POST("/api/locations/nearby")
    suspend fun fetchTop10CloseLocations(
        @Body request: JustCoordinatesDto
    ): Response<List<LocationDto>>

    @POST("/api/locations/recommended")
    suspend fun fetchRecommendedLocations(
        @Body request: JustCoordinatesDto
    ): Response<List<LocationDto>>

    @POST("/api/locations/discover")
    suspend fun fetchDiscoveryLocations(
        @Body request: JustCoordinatesDto
    ): Response<List<LocationDto>>

    @GET("/api/preferences")
    suspend fun getPreferences(): Response<List<AttributeConceptDto>>

    @PUT("/api/preferences")
    suspend fun updatePreferences(
        @Body request: UpdatePreferencesRequestDto
    ): Response<Unit>

    @GET("/api/locations/search")
    suspend fun searchLocationsByName(
        @Query("q") query: String
    ): Response<List<LocationDto>>

    // Public: how many Google photos this location has. The client builds /photo?index=N URLs from it.
    @GET("/api/locations/{id}/photos")
    suspend fun getLocationPhotoCount(
        @Path("id") id: String
    ): Response<LocationPhotosDto>

    // Public: street address + weekly opening hours. Resolved from Google Place Details on first hit
    // (server-cached), so re-opens make no Google call. Open-now is derived on the client.
    @GET("/api/locations/{id}/details")
    suspend fun getLocationDetails(
        @Path("id") id: String
    ): Response<LocationDetailsDto>

    @POST("/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    @POST("/api/reviews")
    suspend fun submitReview(
        @Body request: SubmitReviewRequestDto
    ): Response<Unit>

    @GET("/api/locations/{locationId}/reviews")
    suspend fun getLocationReviews(
        @Path("locationId") locationId: String
    ): Response<List<ReviewDto>>

    @GET("/api/reviews/me")
    suspend fun getMyReviews(): Response<List<MyReviewDto>>

    @GET("/api/wishlist")
    suspend fun getWishlist(): Response<List<LocationDto>>

    @GET("/api/wishlist/{locationId}")
    suspend fun getWishlistStatus(
        @Path("locationId") locationId: String
    ): Response<WishlistStatusDto>

    @POST("/api/wishlist")
    suspend fun addToWishlist(
        @Body request: AddWishlistRequestDto
    ): Response<Unit>

    @DELETE("/api/wishlist/{locationId}")
    suspend fun removeFromWishlist(
        @Path("locationId") locationId: String
    ): Response<Unit>
}
