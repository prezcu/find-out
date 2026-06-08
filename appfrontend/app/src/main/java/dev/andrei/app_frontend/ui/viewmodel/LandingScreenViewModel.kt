package dev.andrei.app_frontend.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.repository.AuthRepository
import dev.andrei.app_frontend.data.repository.LocationRepository
import dev.andrei.app_frontend.data.repository.SessionRepository
import dev.andrei.app_frontend.ui.state.LandingData
import dev.andrei.app_frontend.ui.state.LocationUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingScreenViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LocationUiState>(LocationUiState.Loading)
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var loadJob: Job? = null

    init {
        // Auto-load on the first GPS fix (and any later location change).
        sessionRepository.deviceLocation
            .filterNotNull()
            .onEach { load(showSpinner = true) }
            .launchIn(viewModelScope)
    }

    /** Re-read GPS and re-fetch the lists. Used by pull-to-refresh and the error "Try again" button. */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Same path MainActivity uses; updates sessionRepository.deviceLocation.
            sessionRepository.updateDeviceLocation()
            // Call load directly so a refresh re-fetches even when the coordinates are unchanged
            // (a same-value StateFlow emission wouldn't retrigger the init collector).
            load(showSpinner = false)
        }
    }

    private fun load(showSpinner: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val location = sessionRepository.deviceLocation.value ?: return@launch
                if (showSpinner && _uiState.value !is LocationUiState.Success) {
                    _uiState.value = LocationUiState.Loading
                }
                val lng = location.longitude
                val lat = location.latitude

                // Fetch concurrently. Anonymous users only get the public top-rated list.
                val topRatedDeferred = async { locationRepository.getTopRatedLocations(lng, lat) }
                val loggedIn = authRepository.isLoggedIn()
                val bestMatchesDeferred = async {
                    if (loggedIn) {
                        locationRepository.getRecommendedLocations(lng, lat)
                    } else {
                        Result.success(emptyList())
                    }
                }
                val discoveryDeferred = async {
                    if (loggedIn) {
                        locationRepository.getDiscoveryLocations(lng, lat)
                    } else {
                        Result.success(emptyList())
                    }
                }

                val topRated = topRatedDeferred.await().getOrThrow()
                val bestMatches = bestMatchesDeferred.await().getOrThrow()
                // The backend already returns empty when the user has no effective preferences.
                val discoveryPicks = discoveryDeferred.await().getOrThrow()

                _uiState.value = LocationUiState.Success(
                    LandingData(
                        bestMatches = collapseIfUnranked(bestMatches),
                        topRated = topRated,
                        discoveryPicks = discoveryPicks,
                    )
                )
            } catch (e: Exception) {
                Log.e("LandingScreenViewModel", "Error loading landing locations: ${e.message}", e)
                _uiState.value = LocationUiState.Error(e.message ?: "Unknown error")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // When the user has no effective preferences the backend falls back to a rating-sorted list with no
    // match scores, which is identical to "Top rated nearby". Drop it so we don't show two identical sections.
    private fun collapseIfUnranked(bestMatches: List<LocationEntity>): List<LocationEntity> =
        if (bestMatches.any { it.matchScore != null }) bestMatches else emptyList()
}
