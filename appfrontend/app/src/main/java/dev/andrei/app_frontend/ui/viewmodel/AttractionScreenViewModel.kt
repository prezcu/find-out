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
import dev.andrei.app_frontend.data.remote.dto.preference.AttributeConceptDto
import dev.andrei.app_frontend.data.repository.AuthRepository
import dev.andrei.app_frontend.data.repository.PreferenceRepository
import dev.andrei.app_frontend.data.repository.ReviewRepository
import dev.andrei.app_frontend.data.repository.WishlistRepository
import dev.andrei.app_frontend.ui.components.LedgerEntry
import dev.andrei.app_frontend.ui.navigation.AttractionDetailRoute
import dev.andrei.app_frontend.ui.util.displayLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AttractionScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val locationRepository: LocationRepository,
    private val authRepository: AuthRepository,
    private val wishlistRepository: WishlistRepository,
    private val reviewRepository: ReviewRepository,
    private val preferenceRepository: PreferenceRepository
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

    // Absolute Google photo URLs for the hero carousel; empty when the location has no photos.
    private val _photoUrls = MutableStateFlow<List<String>>(emptyList())
    val photoUrls = _photoUrls.asStateFlow()

    // The signed-in user's taste weights (importance per concept); empty when logged out / unset.
    private val _concepts = MutableStateFlow<List<AttributeConceptDto>>(emptyList())

    val location: StateFlow<LocationEntity?> = locationRepository
        .getLocationById(locationId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * The attribute ledger: each taste concept paired with this venue's measured average for that
     * attribute (aggregated from its reviews) and the user's weight. Recomputed when either the
     * reviews or the weights change. Sorted by weight desc (then score desc).
     */
    val ledger: StateFlow<List<LedgerEntry>> = combine(_reviews, _concepts) { reviews, concepts ->
        buildLedger(reviews, concepts)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // A location's reviews and photos are public, so they can load regardless of auth.
        loadReviews()
        loadPhotos()
        loadPreferences()
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

    private fun loadPhotos() {
        viewModelScope.launch {
            _photoUrls.value = locationRepository.getPhotoUrls(locationIdArg)
        }
    }

    /** Best-effort: per-user taste weights. Returns empty on failure (e.g. logged out). */
    private fun loadPreferences() {
        viewModelScope.launch {
            preferenceRepository.getPreferences().onSuccess { _concepts.value = it }
        }
    }

    /**
     * Derives the ledger rows. With taste concepts present, every concept is a row (joining its
     * average score by [AttributeConceptDto.slug] == [AttributeScoreDto.attribute]); without them,
     * we fall back to whichever attributes the reviews actually scored.
     */
    private fun buildLedger(
        reviews: List<ReviewDto>,
        concepts: List<AttributeConceptDto>
    ): List<LedgerEntry> {
        val flat = reviews.flatMap { it.attributeScores }
        val avgByKey = flat.groupBy { it.attribute }
            .mapValues { (_, v) -> v.map { it.score }.average().toFloat() }

        return if (concepts.isNotEmpty()) {
            concepts.map { concept ->
                LedgerEntry(
                    name = displayLabel(concept.displayName, concept.slug),
                    weight = concept.importance,
                    score = avgByKey[concept.slug]
                )
            }.sortedWith(
                compareByDescending<LedgerEntry> { it.weight }.thenByDescending { it.score ?: -1f }
            )
        } else {
            val nameByKey = flat.associate { it.attribute to it.displayName }
            avgByKey.entries
                .map { (key, score) -> LedgerEntry(displayLabel(nameByKey[key], key), 0, score) }
                .sortedByDescending { it.score ?: -1f }
        }
    }
}
