package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.ui.components.FindoutOutlineButton
import dev.andrei.app_frontend.ui.components.ScreenHeader
import dev.andrei.app_frontend.ui.components.SpecimenCard
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.viewmodel.WishlistViewModel

@Composable
fun WishlistScreen(
    onLocationClick: (String) -> Unit,
    onSignIn: () -> Unit,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors

    // Reload whenever the screen (re)enters composition, e.g. after saving an item elsewhere.
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Your shortlist",
            kicker = "Saved · ${state.items.size}",
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 14.dp)
        )

        when {
            !state.loggedIn -> EmptyState(
                message = "Sign in to see the places you've saved.",
                actionLabel = "Sign in",
                onAction = onSignIn
            )

            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }

            state.errorMessage != null -> EmptyState(
                message = state.errorMessage!!,
                actionLabel = "Retry",
                onAction = viewModel::refresh
            )

            state.items.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(horizontal = 22.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    "Nothing saved yet. Tap the bookmark on any place to file it here.",
                    style = FindoutType.bodyItalic,
                    color = c.sub,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.items, key = { it.id }) { location ->
                    SpecimenCard(
                        location = location,
                        onOpen = { onLocationClick(location.id.toString()) },
                        bookmarkFilled = true,
                        onToggleBookmark = { viewModel.removeFromWishlist(location.id.toString()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, actionLabel: String?, onAction: () -> Unit) {
    val c = FindoutTheme.colors
    Box(Modifier.fillMaxSize().padding(horizontal = 22.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = FindoutType.bodyItalic, color = c.sub)
            if (actionLabel != null) {
                Spacer(Modifier.height(16.dp))
                Box(Modifier.width(200.dp)) {
                    FindoutOutlineButton(label = actionLabel, onClick = onAction)
                }
            }
        }
    }
}
