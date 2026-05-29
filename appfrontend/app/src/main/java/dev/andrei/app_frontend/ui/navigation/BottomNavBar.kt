package dev.andrei.app_frontend.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute


@Composable
fun BottomNavBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        topLevelRoutes.forEach { route ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.hasRoute(route::class) } == true,
                onClick = {
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(LandingRoute) { saveState = true }
                    }
                },
                icon = { Icon(route.icon, contentDescription = route.label) },
                label = { Text(route.label) }
            )
        }
    }
}
