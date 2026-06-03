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
import androidx.navigation.toRoute
import dev.andrei.app_frontend.ui.screen.AttractionScreen
import dev.andrei.app_frontend.ui.screen.LandingScreen
import dev.andrei.app_frontend.ui.screen.LoginScreen
import dev.andrei.app_frontend.ui.screen.ProfileScreen
import dev.andrei.app_frontend.ui.screen.RegisterScreen
import dev.andrei.app_frontend.ui.screen.SearchScreen
import dev.andrei.app_frontend.ui.screen.WishlistScreen
import dev.andrei.app_frontend.ui.screen.WriteReviewScreen
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
                        // Return to the screen the user was on before authenticating;
                        // popping Login inclusively keeps it off the back stack.
                        navController.popBackStack(LoginRoute, inclusive = true)
                    },
                    onNavigateToRegister = {
                        navController.navigate(RegisterRoute)
                    }
                )
            }

            composable<RegisterRoute> {
                RegisterScreen(
                    onRegisterSuccess = {
                        // Drop both Register and Login, returning to the pre-auth screen.
                        navController.popBackStack(LoginRoute, inclusive = true)
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

            composable<SavedRoute> {
                WishlistScreen(
                    onLocationClick = { locationId ->
                        navController.navigate(AttractionDetailRoute(locationId))
                    },
                    onSignIn = {
                        navController.navigate(LoginRoute) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<ProfileRoute> {
                ProfileScreen(
                    onSignIn = {
                        navController.navigate(LoginRoute) {
                            launchSingleTop = true
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

            composable<AttractionDetailRoute> { backStackEntry ->
                val routeArgs = backStackEntry.toRoute<AttractionDetailRoute>()
                val locationId = routeArgs.locationId

                AttractionScreen(
                    onBack = { navController.popBackStack() },
                    onSignIn = {
                        navController.navigate(LoginRoute) {
                            launchSingleTop = true
                        }
                    },
                    onWriteReview = {
                        navController.navigate(WriteReviewRoute(locationId))
                    }
                )
            }

            composable<WriteReviewRoute> {
                WriteReviewScreen(
                    onBack = { navController.popBackStack() },
                    onSubmitSuccess = {
                        navController.navigate(LandingRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
