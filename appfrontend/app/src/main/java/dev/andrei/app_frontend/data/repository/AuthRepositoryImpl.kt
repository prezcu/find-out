package dev.andrei.app_frontend.data.repository

import dev.andrei.app_frontend.data.local.AuthTokenStore
import dev.andrei.app_frontend.data.remote.api.ApiService
import dev.andrei.app_frontend.data.remote.dto.auth.ChangePasswordRequest
import dev.andrei.app_frontend.data.remote.dto.auth.LoginRequest
import dev.andrei.app_frontend.data.remote.dto.auth.RegisterRequest
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val tokenStore: AuthTokenStore
) : AuthRepository {

    override suspend fun register(email: String, password: String): AuthResult =
        runCatching { api.register(RegisterRequest(email, password)) }
            .fold(
                onSuccess = { it.handleAuthResponse() },
                onFailure = { AuthResult.Error(it.message ?: "Network error") }
            )

    override suspend fun login(email: String, password: String): AuthResult =
        runCatching { api.login(LoginRequest(email, password)) }
            .fold(
                onSuccess = { it.handleAuthResponse() },
                onFailure = { AuthResult.Error(it.message ?: "Network error") }
            )

    override suspend fun changePassword(oldPassword: String, newPassword: String): AuthResult =
        runCatching { api.changePassword(ChangePasswordRequest(oldPassword, newPassword)) }
            .fold(
                onSuccess = { response ->
                    if (response.isSuccessful) AuthResult.Success
                    else AuthResult.Error(response.errorMessage())
                },
                onFailure = { AuthResult.Error(it.message ?: "Network error") }
            )

    override fun isLoggedIn(): Boolean = tokenStore.isLoggedIn()

    override fun logout() = tokenStore.clear()

    private fun Response<dev.andrei.app_frontend.data.remote.dto.auth.AuthResponse>.handleAuthResponse(): AuthResult {
        if (!isSuccessful) return AuthResult.Error(errorMessage())
        val body = body() ?: return AuthResult.Error("Empty response from server")
        tokenStore.saveToken(body.token, body.expiresAt)
        return AuthResult.Success
    }

    private fun Response<*>.errorMessage(): String = when (code()) {
        401 -> "Invalid credentials"
        409 -> "An account with this email already exists"
        in 400..499 -> "Request rejected (${code()})"
        in 500..599 -> "Server error, please try again later"
        else -> "Unexpected error (${code()})"
    }
}
