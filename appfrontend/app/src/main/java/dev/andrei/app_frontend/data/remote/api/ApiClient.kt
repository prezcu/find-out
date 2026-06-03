package dev.andrei.app_frontend.data.remote.api

import dev.andrei.app_frontend.data.remote.dto.AddWishlistRequestDto
import dev.andrei.app_frontend.data.remote.dto.JustCoordinatesDto
import dev.andrei.app_frontend.data.remote.dto.LocationDto
import dev.andrei.app_frontend.data.remote.dto.MyReviewDto
import dev.andrei.app_frontend.data.remote.dto.ReviewDto
import dev.andrei.app_frontend.data.remote.dto.SubmitReviewRequestDto
import dev.andrei.app_frontend.data.remote.dto.WishlistStatusDto
import dev.andrei.app_frontend.data.remote.dto.auth.AuthResponse
import dev.andrei.app_frontend.data.remote.dto.auth.ChangePasswordRequest
import dev.andrei.app_frontend.data.remote.dto.auth.LoginRequest
import dev.andrei.app_frontend.data.remote.dto.auth.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
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

    @GET("/api/locations/search")
    suspend fun searchLocationsByName(
        @Query("q") query: String
    ): Response<List<LocationDto>>

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
