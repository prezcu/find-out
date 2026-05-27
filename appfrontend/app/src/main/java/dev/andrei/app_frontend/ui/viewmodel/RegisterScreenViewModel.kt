package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.repository.AuthRepository
import dev.andrei.app_frontend.data.repository.AuthResult
import dev.andrei.app_frontend.ui.state.RegisterFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(RegisterFormState())
    val formState = _formState.asStateFlow()

    fun onEmailChange(value: String) =
        _formState.update { it.copy(email = value, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _formState.update { it.copy(password = value, errorMessage = null) }

    fun onConfirmPasswordChange(value: String) =
        _formState.update { it.copy(confirmPassword = value, errorMessage = null) }

    fun togglePasswordVisibility() =
        _formState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun submit() {
        val state = _formState.value
        val validationError = validate(state)
        if (validationError != null) {
            _formState.update { it.copy(errorMessage = validationError) }
            return
        }

        _formState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.register(state.email.trim(), state.password)) {
                is AuthResult.Success ->
                    _formState.update { it.copy(isLoading = false, isSuccess = true) }
                is AuthResult.Error ->
                    _formState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    private fun validate(state: RegisterFormState): String? {
        if (state.email.isBlank()) return "Email is required"
        //TODO: Additional email validation
        if (!state.email.contains("@")) return "Enter a valid email"
        if (state.password.length < 8) return "Password must be at least 8 characters"
        if (state.password != state.confirmPassword) return "Passwords do not match"
        return null
    }
}
