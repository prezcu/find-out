package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.StateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.repository.LocationRepository
import dev.andrei.app_frontend.ui.navigation.WriteReviewRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject



@HiltViewModel
class WriteReviewScrenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: LocationRepository
) : ViewModel() {

    private val args = savedStateHandle.toRoute<WriteReviewRoute>()
    private val locationId = UUID.fromString(args.locationId)

    val location: StateFlow<LocationEntity?> = repository
        .getLocationById(locationId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

}