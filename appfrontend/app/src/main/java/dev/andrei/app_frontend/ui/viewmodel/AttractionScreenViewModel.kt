package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.repository.LocationRepository
import dev.andrei.app_frontend.ui.navigation.NavRoutes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AttractionScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: LocationRepository
) : ViewModel() {

    private val locationId: UUID = UUID.fromString(
        checkNotNull(savedStateHandle[NavRoutes.AttractionDetail.ARG_LOCATION_ID])
    )

    val location: StateFlow<LocationEntity?> = repository
        .getLocationById(locationId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
