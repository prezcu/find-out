package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AppAuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val isLoggedInOnStart: Boolean = authRepository.isLoggedIn()

    fun logout() = authRepository.logout()
}
