package dev.andrei.app_frontend.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import kotlinx.serialization.Serializable
import androidx.compose.ui.graphics.vector.ImageVector

// interface for bottom navigation bar
// routes care metadata but this makes route ssot
sealed interface TopLevelRoute {
    val label: String
    val icon: ImageVector
}

@Serializable
data object LandingRoute : TopLevelRoute {
    override val label get() = "Home"
    override val icon get() = Icons.Filled.Home
}

@Serializable
data object SearchRoute :  TopLevelRoute {
    override val label get() = "Search"
    override val icon get() = Icons.Filled.Search
}

@Serializable
data object ProfileRoute :  TopLevelRoute {
    override val label get() = "Profile"
    override val icon get() = Icons.Filled.Person
}

@Serializable object LoginRoute

@Serializable object RegisterRoute

// Its usually easiest to pass ID as String through navigation
// and parse it back to UUID in your ViewModel
@Serializable data class AttractionDetailRoute (val locationId: String) {}


val topLevelRoutes = listOf(LandingRoute, SearchRoute, ProfileRoute)
