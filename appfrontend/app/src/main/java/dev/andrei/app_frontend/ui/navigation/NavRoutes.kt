package dev.andrei.app_frontend.ui.navigation

import java.util.UUID

sealed class NavRoutes(val route: String) {

    data object Login : NavRoutes("login")

    data object Register : NavRoutes("register")

    data object Landing : NavRoutes("landing")

    data object Search : NavRoutes("search")

    data object Profile : NavRoutes("profile")

    data object AttractionDetail : NavRoutes("attraction/{locationId}") {
        const val ARG_LOCATION_ID = "locationId"
        const val ROUTE_PREFIX = "attraction/"
        fun createRoute(id: UUID): String = "$ROUTE_PREFIX$id"
    }
}
