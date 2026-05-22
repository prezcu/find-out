package dev.andrei.app_frontend.ui.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.repository.LocationRepository
import dev.andrei.app_frontend.data.repository.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import dev.andrei.app_frontend.ui.state.LocationUiState

@HiltViewModel
class LandingScreenViewModel @Inject constructor(
    private val screenRepository: LocationRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val screenState: StateFlow<LocationUiState> = sessionRepository.deviceLocation
        .onEach { Log.d("FlowDebug", "1. Raw Device Location emitted: $it") }
        .filterNotNull()
        .onEach { Log.d("FlowDebug", "2. Location passed filterNotNull: $it") }
        .flatMapLatest { deviceLocation ->
            Log.d("FlowDebug", "3. Triggering database query for top 10 locations...")

            screenRepository.getTop10CloseLocations(deviceLocation.longitude, deviceLocation.latitude)
                .onEach { Log.d("FlowDebug", "4. Database returned list of size: ${it.size}") }
                .map { locationsList ->
                    LocationUiState.Success(locationsList) as LocationUiState
                }
        }
        .catch { exception ->
            // This catches crashes in your database or repository
            Log.e("FlowDebug", "FATAL ERROR in Flow: ${exception.message}", exception)
            emit(LocationUiState.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LocationUiState.Loading
        )

    fun getDeviceCurrentLocation(): StateFlow<Location?>{
        return sessionRepository.deviceLocation
    }
}