package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileScreenViewmodel @Inject constructor(
    private val repository : AuthRepository
) : ViewModel() {

    private val _logInState = MutableStateFlow(false)
    val logInState = _logInState.asStateFlow()

    fun updateLogInState(){
        viewModelScope.launch {
            _logInState.value = repository.isLoggedIn()
        }
    }


}