package dev.andrei.app_frontend.data.remote.dto.auth

data class RegisterRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class AuthResponse(
    val token: String,
    val expiresAt: Long
)

data class ErrorResponse(
    val error: String
)
