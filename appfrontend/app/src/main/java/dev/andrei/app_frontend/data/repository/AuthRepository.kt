package dev.andrei.app_frontend.data.repository

interface AuthRepository {

    suspend fun register(email: String, password: String): AuthResult

    suspend fun login(email: String, password: String): AuthResult

    suspend fun changePassword(oldPassword: String, newPassword: String): AuthResult

    fun isLoggedIn(): Boolean

    fun logout()
}

sealed interface AuthResult {
    data object Success : AuthResult
    data class Error(val message: String) : AuthResult
}
