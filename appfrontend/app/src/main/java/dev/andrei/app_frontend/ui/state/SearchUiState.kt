package dev.andrei.app_frontend.ui.state

import dev.andrei.app_frontend.data.local.entity.LocationEntity

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val results: List<LocationEntity>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
