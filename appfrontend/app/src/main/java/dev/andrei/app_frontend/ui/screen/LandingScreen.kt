package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.ui.components.FindoutOutlineButton
import dev.andrei.app_frontend.ui.components.ScreenHeader
import dev.andrei.app_frontend.ui.components.SpecimenCard
import dev.andrei.app_frontend.ui.state.LandingData
import dev.andrei.app_frontend.ui.state.LocationUiState
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.viewmodel.LandingScreenViewModel

// "View more" reveals the lists in client-side pages (the backend already returns a larger batch).
private const val INITIAL_VISIBLE = 5
private const val PAGE_STEP = 5

@Composable
fun LandingScreen(
    onLocationClick: (String) -> Unit,
    screenViewModel: LandingScreenViewModel = hiltViewModel()
) {
    val uiState by screenViewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by screenViewModel.isRefreshing.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors

    when (val state = uiState) {
        is LocationUiState.Loading -> CenterMessage { CircularProgressIndicator(color = c.accent) }

        is LocationUiState.Error ->
            CenterMessage {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Something went wrong.\n${state.message}",
                        style = FindoutType.bodyItalic,
                        color = c.sub,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(18.dp))
                    FindoutOutlineButton(label = "Try again", onClick = screenViewModel::refresh)
                }
            }

        is LocationUiState.Success ->
            LandingContent(
                data = state.data,
                isRefreshing = isRefreshing,
                onRefresh = screenViewModel::refresh,
                onLocationClick = onLocationClick
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandingContent(
    data: LandingData,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLocationClick: (String) -> Unit,
) {
    val c = FindoutTheme.colors
    val hasBestMatches = data.bestMatches.isNotEmpty()

    // Hoisted out of the LazyColumn builder: rememberSaveable is @Composable and the builder is not.
    var bestVisible by rememberSaveable { mutableIntStateOf(INITIAL_VISIBLE) }
    var topVisible by rememberSaveable { mutableIntStateOf(INITIAL_VISIBLE) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        // A single LazyColumn so the whole page scrolls (and the pull gesture works) even when a
        // section is empty.
        LazyColumn(
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!hasBestMatches && data.topRated.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No locations found nearby.", style = FindoutType.bodyItalic, color = c.sub)
                    }
                }
            } else {
                if (hasBestMatches) {
                    locationSection(
                        title = "Best matches",
                        countLabel = "Bucharest · ${data.bestMatches.size}",
                        locations = data.bestMatches,
                        visibleCount = bestVisible,
                        onViewMore = { bestVisible += PAGE_STEP },
                        onLocationClick = onLocationClick
                    )
                }
                locationSection(
                    title = "Top rated nearby",
                    countLabel = "Bucharest · ${data.topRated.size}",
                    locations = data.topRated,
                    visibleCount = topVisible,
                    onViewMore = { topVisible += PAGE_STEP },
                    onLocationClick = onLocationClick
                )
            }
        }
    }
}

private fun LazyListScope.locationSection(
    title: String,
    countLabel: String,
    locations: List<LocationEntity>,
    visibleCount: Int,
    onViewMore: () -> Unit,
    onLocationClick: (String) -> Unit,
) {
    item(key = "header:$title") {
        ScreenHeader(
            title = title,
            kicker = "Field Guide",
            kickerRight = countLabel,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )
    }
    items(locations.take(visibleCount), key = { it.id }) { location ->
        SpecimenCard(
            location = location,
            onOpen = { onLocationClick(location.id.toString()) }
        )
    }
    if (visibleCount < locations.size) {
        item(key = "more:$title") {
            FindoutOutlineButton(label = "View more", onClick = onViewMore)
        }
    }
}

@Composable
private fun CenterMessage(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(22.dp), contentAlignment = Alignment.Center) { content() }
}
