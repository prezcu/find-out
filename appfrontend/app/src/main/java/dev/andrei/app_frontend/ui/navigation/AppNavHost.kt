package dev.andrei.app_frontend.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    val currentRoute = backStackEntry?.destination?.route

    val startDestination = if (authViewModel.isLoggedInOnStart) {
        NavRoutes.Landing.route
    } else {
        NavRoutes.Login.route
    }

    val showBottomBar = currentRoute != null &&
            currentRoute != NavRoutes.Login.route &&
            currentRoute != NavRoutes.Register.route &&
            !currentRoute.startsWith(NavRoutes.AttractionDetail.ROUTE_PREFIX)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavRoutes.Landing.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(NavRoutes.Register.route)
                    }
                )
            }

            composable(NavRoutes.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(NavRoutes.Landing.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Landing.route) {
                LandingScreen(
                    onLocationClick = { locationId ->
                        navController.navigate(NavRoutes.AttractionDetail.createRoute(locationId))
                    }
                )
            }

            composable(NavRoutes.Search.route) {
                SearchScreen(
                    onLocationClick = { locationId ->
                        navController.navigate(NavRoutes.AttractionDetail.createRoute(locationId))
                    }
                )
            }

            composable(NavRoutes.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = NavRoutes.AttractionDetail.route,
                arguments = listOf(
                    navArgument(NavRoutes.AttractionDetail.ARG_LOCATION_ID) {
                        type = NavType.StringType
                    }
                )
            ) {
                AttractionScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
