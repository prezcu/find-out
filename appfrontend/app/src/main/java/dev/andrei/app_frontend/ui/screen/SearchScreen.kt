package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.ui.state.SearchUiState
import dev.andrei.app_frontend.ui.viewmodel.SearchScreenViewModel
import java.util.UUID

@Composable
fun SearchScreen(
    onLocationClick: (String) -> Unit,
    viewModel: SearchScreenViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Search by name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.submit() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = viewModel::submit,
            enabled = query.isNotBlank() && uiState !is SearchUiState.Loading,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is SearchUiState.Idle -> {
                Text(
                    text = "Type a name and tap Search.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            is SearchUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is SearchUiState.Success -> {
                if (state.results.isEmpty()) {
                    Text(
                        text = "No matching locations.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.results) { location ->
                            LocationCard(
                                domainLocation = location,
                                onClick = { onLocationClick(location.id.toString()) }
                            )
                        }
                    }
                }
            }

            is SearchUiState.Error -> {
                Text(
                    text = "Something went wrong: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
