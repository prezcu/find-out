package dev.andrei.app_frontend.ui.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.repository.AuthRepository
import dev.andrei.app_frontend.data.repository.LocationRepository
import dev.andrei.app_frontend.data.repository.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import dev.andrei.app_frontend.ui.state.LocationUiState

@HiltViewModel
class LandingScreenViewModel @Inject constructor(
    private val screenRepository: LocationRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val screenState: StateFlow<LocationUiState> = sessionRepository.deviceLocation
        .filterNotNull()
        .flatMapLatest { deviceLocation ->
            // Signed-in users get the personalized match-ranked list; everyone else gets the
            // public rating-sorted nearby list.
            val source = if (authRepository.isLoggedIn()) {
                screenRepository.getRecommendedLocations(deviceLocation.longitude, deviceLocation.latitude)
            } else {
                screenRepository.getTop10CloseLocations(deviceLocation.longitude, deviceLocation.latitude)
            }
            source.map { locationsList ->
                LocationUiState.Success(locationsList) as LocationUiState
            }
        }
        .catch { exception ->
            Log.e("LandingScreenViewModel", "Error loading landing locations: ${exception.message}", exception)
            emit(LocationUiState.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LocationUiState.Loading
        )

    fun getDeviceCurrentLocation(): StateFlow<Location?> {
        return sessionRepository.deviceLocation
    }
}
