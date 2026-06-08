package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.ui.components.FindoutTextField
import dev.andrei.app_frontend.ui.components.Kicker
import dev.andrei.app_frontend.ui.components.SectionRule
import dev.andrei.app_frontend.ui.components.SpecimenCard
import dev.andrei.app_frontend.ui.state.SearchUiState
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.viewmodel.SearchScreenViewModel

@Composable
fun SearchScreen(
    onLocationClick: (String) -> Unit,
    viewModel: SearchScreenViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp)) {
            Kicker("Search", color = c.accent)
            Spacer(Modifier.height(8.dp))
            SectionRule()
            Spacer(Modifier.height(14.dp))
            FindoutTextField(
                label = "Search by name, type or area",
                value = query,
                onValueChange = viewModel::onQueryChange,
                imeAction = ImeAction.Search,
                onImeAction = viewModel::submit,
                trailing = if (query.isNotEmpty()) {
                    {
                        Text(
                            "×",
                            style = FindoutType.mono.copy(fontSize = 18.sp),
                            color = c.sub,
                            modifier = Modifier.clickable {
                                viewModel.onQueryChange("")
                                viewModel.submit()
                            }
                        )
                    }
                } else null
            )
            Spacer(Modifier.height(16.dp))
        }

        when (val state = uiState) {
            is SearchUiState.Idle ->
                Hint("Type a name, type or area, then search.")

            is SearchUiState.Loading ->
                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.accent)
                }

            is SearchUiState.Error ->
                Hint("Something went wrong: ${state.message}")

            is SearchUiState.Success -> {
                val count = state.results.size
                Text(
                    text = buildString {
                        append("$count ")
                        append(if (count == 1) "RESULT" else "RESULTS")
                        if (query.isNotBlank()) append(" · “$query”")
                    },
                    style = FindoutType.kicker.copy(fontSize = 9.5.sp, letterSpacing = 1.5.sp),
                    color = c.faint,
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp)
                )
                if (state.results.isEmpty()) {
                    Hint("Nothing matches “$query”. Try a café name or a neighbourhood.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.results, key = { it.id }) { location ->
                            SpecimenCard(
                                location = location,
                                onOpen = { onLocationClick(location.id.toString()) },
                                photoHeight = 170.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        style = FindoutType.bodyItalic,
        color = FindoutTheme.colors.sub,
        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
    )
}
