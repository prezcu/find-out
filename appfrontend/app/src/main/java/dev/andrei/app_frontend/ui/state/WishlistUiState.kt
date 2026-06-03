package dev.andrei.app_frontend.ui.state

import dev.andrei.app_frontend.data.local.entity.LocationEntity

data class WishlistUiState(
    val loggedIn: Boolean = true,
    val isLoading: Boolean = true,
    val items: List<LocationEntity> = emptyList(),
    val errorMessage: String? = null
)
