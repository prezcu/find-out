package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.remote.dto.ReviewDto
import dev.andrei.app_frontend.data.repository.LocationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.andrei.app_frontend.data.repository.AuthRepository
import dev.andrei.app_frontend.data.repository.ReviewRepository
import dev.andrei.app_frontend.data.repository.WishlistRepository
import dev.andrei.app_frontend.ui.navigation.AttractionDetailRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AttractionScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    locationRepository: LocationRepository,
    private val authRepository: AuthRepository,
    private val wishlistRepository: WishlistRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val args = savedStateHandle.toRoute<AttractionDetailRoute>()
    // The raw String id is what the wishlist/review APIs take; the UUID is for the local DB query.
    private val locationIdArg = args.locationId
    val locationId: UUID = UUID.fromString(locationIdArg)

    private val _logInState = MutableStateFlow(false)
    val logInState = _logInState.asStateFlow()

    private val _isWishlisted = MutableStateFlow(false)
    val isWishlisted = _isWishlisted.asStateFlow()

    // True while an add/remove request is in flight, so the screen can disable the toggle.
    private val _wishlistBusy = MutableStateFlow(false)
    val wishlistBusy = _wishlistBusy.asStateFlow()

    private val _reviews = MutableStateFlow<List<ReviewDto>>(emptyList())
    val reviews = _reviews.asStateFlow()

    val location: StateFlow<LocationEntity?> = locationRepository
        .getLocationById(locationId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // A location's reviews are public, so they can load regardless of auth.
        loadReviews()
    }

    /**
     * Refreshes auth-dependent state. Called by the screen on (re)composition so that returning
     * from the login flow updates both the login flag and the wishlist status. Wishlist status is
     * only fetched when signed in (the endpoint requires auth).
     */
    fun updateLogInState() {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn()
            _logInState.value = loggedIn
            _isWishlisted.value = if (loggedIn) {
                wishlistRepository.isWishlisted(locationIdArg).getOrDefault(false)
            } else {
                false
            }
        }
    }

    /** Adds or removes this location from the wishlist; flips local state only on success. */
    fun toggleWishlist() {
        if (_wishlistBusy.value) return
        viewModelScope.launch {
            _wishlistBusy.value = true
            val currentlyWishlisted = _isWishlisted.value
            val result = if (currentlyWishlisted) {
                wishlistRepository.removeFromWishlist(locationIdArg)
            } else {
                wishlistRepository.addToWishlist(locationIdArg)
            }
            if (result.isSuccess) {
                _isWishlisted.value = !currentlyWishlisted
            }
            _wishlistBusy.value = false
        }
    }

    private fun loadReviews() {
        viewModelScope.launch {
            reviewRepository.getLocationReviews(locationIdArg)
                .onSuccess { _reviews.value = it }
        }
    }
}
