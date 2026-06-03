package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.ui.viewmodel.WishlistViewModel

@Composable
fun WishlistScreen(
    onLocationClick: (String) -> Unit,
    onSignIn: () -> Unit,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Reload whenever the screen (re)enters composition, e.g. after saving an item elsewhere.
    LaunchedEffect(Unit) { viewModel.refresh() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            !state.loggedIn -> EmptyState(
                message = "Sign in to see the places you've saved.",
                actionLabel = "Sign in",
                onAction = onSignIn
            )

            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            state.errorMessage != null -> EmptyState(
                message = state.errorMessage!!,
                actionLabel = "Retry",
                onAction = viewModel::refresh
            )

            state.items.isEmpty() -> EmptyState(
                message = "No saved places yet.\nTap the bookmark on a place to save it.",
                actionLabel = null,
                onAction = {}
            )

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.items) { location ->
                    LocationCard(
                        domainLocation = location,
                        onClick = { onLocationClick(location.id.toString()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, actionLabel: String?, onAction: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onAction, modifier = Modifier.height(48.dp)) {
                    Text(actionLabel)
                }
            }
        }
    }
}
