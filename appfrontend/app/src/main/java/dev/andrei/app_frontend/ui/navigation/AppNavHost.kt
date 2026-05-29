package dev.andrei.app_frontend.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.andrei.app_frontend.ui.screen.AttractionScreen
import dev.andrei.app_frontend.ui.screen.LandingScreen
import dev.andrei.app_frontend.ui.screen.LoginScreen
import dev.andrei.app_frontend.ui.screen.ProfileScreen
import dev.andrei.app_frontend.ui.screen.RegisterScreen
import dev.andrei.app_frontend.ui.screen.SearchScreen
import dev.andrei.app_frontend.ui.viewmodel.AppAuthViewModel

@Composable
fun AppNavHost(authViewModel: AppAuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        topLevelRoutes.any { dest.hasRoute(it::class) }
    } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LandingRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<LoginRoute> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(LandingRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(RegisterRoute)
                    }
                )
            }

            composable<RegisterRoute> {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(LandingRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable<LandingRoute> {
                LandingScreen(
                    onLocationClick = { locationId ->
                        navController.navigate(AttractionDetailRoute(locationId))
                    }
                )
            }

            composable<SearchRoute> {
                SearchScreen(
                    onLocationClick = { locationId ->
                        navController.navigate(AttractionDetailRoute(locationId))
                    }
                )
            }

            composable<ProfileRoute> {
                ProfileScreen(
                    onLogin = {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(LandingRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable<AttractionDetailRoute> { //backStackEntry ->
                // val routeArgs = backStackEntry.toRoute<AttractionDetailRoute>()

                AttractionScreen(
                    //locationId = routeArgs.locationId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
