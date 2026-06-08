package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.ui.components.ScreenHeader
import dev.andrei.app_frontend.ui.components.SpecimenCard
import dev.andrei.app_frontend.ui.state.LocationUiState
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.viewmodel.LandingScreenViewModel

@Composable
fun LandingScreen(
    onLocationClick: (String) -> Unit,
    screenViewModel: LandingScreenViewModel = hiltViewModel()
) {
    val uiState by screenViewModel.screenState.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors

    when (val state = uiState) {
        is LocationUiState.Loading -> CenterMessage { CircularProgressIndicator(color = c.accent) }

        is LocationUiState.Error ->
            CenterMessage {
                Text("Something went wrong.\n${state.message}", style = FindoutType.bodyItalic, color = c.sub)
            }

        is LocationUiState.Success -> {
            // A personalised feed is one where the backend attached match scores.
            val matchRanked = state.locations.any { it.matchScore != null }
            Column(Modifier.fillMaxSize()) {
                ScreenHeader(
                    title = if (matchRanked) "Matched nearby" else "Top rated nearby",
                    kicker = "Field Guide",
                    kickerRight = "Bucharest · ${state.locations.size}",
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 14.dp)
                )
                if (state.locations.isEmpty()) {
                    CenterMessage {
                        Text("No locations found nearby.", style = FindoutType.bodyItalic, color = c.sub)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.locations, key = { it.id }) { location ->
                            SpecimenCard(
                                location = location,
                                onOpen = { onLocationClick(location.id.toString()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenterMessage(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(22.dp), contentAlignment = Alignment.Center) { content() }
}
