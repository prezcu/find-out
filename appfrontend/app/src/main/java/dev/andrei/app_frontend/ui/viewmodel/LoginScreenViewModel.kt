package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.repository.AuthRepository
import dev.andrei.app_frontend.data.repository.AuthResult
import dev.andrei.app_frontend.ui.state.LoginFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(LoginFormState())
    val formState = _formState.asStateFlow()

    fun onEmailChange(value: String) =
        _formState.update { it.copy(email = value, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _formState.update { it.copy(password = value, errorMessage = null) }

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
            when (val result = authRepository.login(state.email.trim(), state.password)) {
                is AuthResult.Success ->
                    _formState.update { it.copy(isLoading = false, isSuccess = true) }
                is AuthResult.Error ->
                    _formState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    private fun validate(state: LoginFormState): String? {
        if (state.email.isBlank()) return "Email is required"
        if (state.password.isBlank()) return "Password is required"
        return null
    }
}
