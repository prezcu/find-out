package dev.andrei.app_frontend.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType

/**
 * Field Edition bottom nav (HANDOFF §6): four text-only tabs, a 1dp top rule, and an 18×2dp amber
 * underline under the active tab. No icons.
 */
@Composable
fun BottomNavBar(navController: NavController) {
    val c = FindoutTheme.colors
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Column(Modifier.fillMaxWidth().background(c.bg)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
        Row(
            Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 13.dp, bottom = 16.dp)
        ) {
            topLevelRoutes.forEach { route ->
                val selected =
                    currentDestination?.hierarchy?.any { it.hasRoute(route::class) } == true
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(LandingRoute) { saveState = true }
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        route.label.uppercase(),
                        style = FindoutType.navLabel,
                        color = if (selected) c.accent else c.sub
                    )
                    Spacer(Modifier.height(5.dp))
                    Box(
                        Modifier
                            .width(18.dp)
                            .height(2.dp)
                            .background(if (selected) c.accent else Color.Transparent)
                    )
                }
            }
        }
    }
}
