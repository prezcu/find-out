package dev.andrei.app_frontend.ui.state

import dev.andrei.app_frontend.data.local.entity.LocationEntity

sealed interface LocationUiState {
    data object Loading : LocationUiState
    data class Success(val locations: List<LocationEntity>) : LocationUiState
    data class Error(val message: String) : LocationUiState
}