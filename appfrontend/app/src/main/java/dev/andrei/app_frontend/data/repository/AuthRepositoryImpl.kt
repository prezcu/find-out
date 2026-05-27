package dev.andrei.app_frontend.data.repository

import com.google.gson.Gson
import dev.andrei.app_frontend.data.local.AuthTokenStore
import dev.andrei.app_frontend.data.remote.api.ApiService
import dev.andrei.app_frontend.data.remote.dto.auth.AuthResponse
import dev.andrei.app_frontend.data.remote.dto.auth.ChangePasswordRequest
import dev.andrei.app_frontend.data.remote.dto.auth.ErrorResponse
import dev.andrei.app_frontend.data.remote.dto.auth.LoginRequest
import dev.andrei.app_frontend.data.remote.dto.auth.RegisterRequest
import retrofit2.Response
import javax.inject.Inject
import kotlin.jvm.java

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

    private fun Response<AuthResponse>.handleAuthResponse(): AuthResult {
        if (!isSuccessful)
            return AuthResult.Error(errorMessage())

        val body = body() ?: return AuthResult.Error("Empty response from server")
        tokenStore.saveToken(body.token, body.expiresAt)
        return AuthResult.Success
    }

    private fun Response<*>.errorMessage(): String {
        // try to read the exact error from the backend (with gson)
        try {
            val errorJsonString = errorBody()?.string()
            if (errorJsonString != null) {
                val parsedError = Gson().fromJson(errorJsonString, ErrorResponse::class.java)
                if (parsedError?.error != null) {
                    // it should read "EMAIL_TAKEN" for already registered email
                    return when (parsedError.error) {
                        "EMAIL_TAKEN" -> "The email you have entered is already registered"
                        "INVALID_CREDENTIALS" -> "The username or password you have entered is incorrect"
                        "VALIDATION_FAILED" -> "The request you have sent could not be validated"
                        else -> "An exception has occured in the internal server"
                    }
                }
            }
        } catch (e: Exception) {
            // ignored: JSON was missing or malformed, and we fall back to status codes
        }

        return when (code()) {
            401 -> "Invalid credentials or unauthorized access to this resource"
            409 -> "A server conflict has occured with your request. Please try again"
            in 400..499 -> "Request rejected (${code()})"
            in 500..599 -> "Server error, please try again later"
            else -> "Unexpected error (${code()})"
        }
    }
}
