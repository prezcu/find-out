package dev.andrei.app_frontend.ui.state

import dev.andrei.app_frontend.data.local.entity.LocationEntity

/**
 * Landing feed. [LandingData.bestMatches] is empty for anonymous users and for signed-in users with no
 * effective preferences (where the recommended list would just mirror [LandingData.topRated]); in those
 * cases only the "Top rated nearby" section is shown.
 */
data class LandingData(
    val bestMatches: List<LocationEntity>,
    val topRated: List<LocationEntity>,
)

sealed interface LocationUiState {
    data object Loading : LocationUiState
    data class Success(val data: LandingData) : LocationUiState
    data class Error(val message: String) : LocationUiState
}
