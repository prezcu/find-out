package dev.andrei.app_frontend.data.remote.api

import dev.andrei.app_frontend.data.remote.dto.JustCoordinatesDto
import dev.andrei.app_frontend.data.remote.dto.LocationDto
import dev.andrei.app_frontend.data.remote.dto.auth.AuthResponse
import dev.andrei.app_frontend.data.remote.dto.auth.ChangePasswordRequest
import dev.andrei.app_frontend.data.remote.dto.auth.LoginRequest
import dev.andrei.app_frontend.data.remote.dto.auth.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
}
