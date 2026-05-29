package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.repository.LocationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.andrei.app_frontend.ui.navigation.AttractionDetailRoute
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AttractionScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: LocationRepository
) : ViewModel() {

    private val args = savedStateHandle.toRoute<AttractionDetailRoute>()
    val locationId = UUID.fromString(args.locationId)

    val location: StateFlow<LocationEntity?> = repository
        .getLocationById(locationId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
