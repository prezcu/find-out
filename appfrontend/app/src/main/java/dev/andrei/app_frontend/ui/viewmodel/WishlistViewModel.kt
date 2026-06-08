package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.repository.AuthRepository
import dev.andrei.app_frontend.data.repository.WishlistRepository
import dev.andrei.app_frontend.ui.state.WishlistUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState = _uiState.asStateFlow()

    /** Loads the signed-in user's saved locations. Safe to call on each screen entry. */
    fun refresh() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                _uiState.value = WishlistUiState(loggedIn = false, isLoading = false)
                return@launch
            }
            _uiState.update { it.copy(loggedIn = true, isLoading = true, errorMessage = null) }
            wishlistRepository.getWishlist().fold(
                onSuccess = { items ->
                    _uiState.value = WishlistUiState(
                        loggedIn = true,
                        isLoading = false,
                        items = items
                    )
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Couldn't load your saved places"
                        )
                    }
                }
            )
        }
    }

    /** Removes a saved place; optimistically drops it from the list, reloading if the call fails. */
    fun removeFromWishlist(locationId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(items = state.items.filterNot { it.id.toString() == locationId })
            }
            wishlistRepository.removeFromWishlist(locationId).onFailure { refresh() }
        }
    }
}
