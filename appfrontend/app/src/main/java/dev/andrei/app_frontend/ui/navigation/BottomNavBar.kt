package dev.andrei.app_frontend.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue

private data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: @Composable () -> Unit
)

private val bottomNavItems = listOf(
    BottomNavItem("Home", NavRoutes.Landing.route) {
        Icon(Icons.Filled.Home, contentDescription = "Home")
    },
    BottomNavItem("Search", NavRoutes.Search.route) {
        Icon(Icons.Filled.Search, contentDescription = "Search")
    },
    BottomNavItem("Profile", NavRoutes.Profile.route) {
        Icon(Icons.Filled.Person, contentDescription = "Profile")
    },
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(NavRoutes.Landing.route) { saveState = true }
                    }
                },
                icon = item.icon,
                label = { Text(item.label) }
            )
        }
    }
}
