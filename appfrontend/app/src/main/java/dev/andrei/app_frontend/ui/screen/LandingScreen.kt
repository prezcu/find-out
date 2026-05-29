package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.ui.viewmodel.LandingScreenViewModel
import dev.andrei.app_frontend.ui.state.LocationUiState
import java.util.UUID

@Composable
fun LandingScreen(
    onLocationClick: (String) -> Unit,
    screenViewModel: LandingScreenViewModel = hiltViewModel()
) {
    val uiState by screenViewModel.screenState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is LocationUiState.Loading -> {
            Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "Locations loading ",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                CircularProgressIndicator()
            }
        }

        is LocationUiState.Success -> {
            if (state.locations.isEmpty()) {
                Text(text = "No locations found nearby.")
            } else {
                val deviceLocation by screenViewModel.getDeviceCurrentLocation()
                    .collectAsStateWithLifecycle()

                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        text = "Top 10 Rated Locations Nearby",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.locations) { location ->
                            LocationCard(
                                domainLocation = location,
                                onClick = { onLocationClick(location.id.toString()) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "Your current location", style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = deviceLocation?.latitude.toString(), style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = deviceLocation?.longitude.toString(), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        is LocationUiState.Error -> {
            Text(text = "Something went wrong: ${state.message}")
        }
    }
}

@Composable
fun LocationCard(domainLocation: LocationEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = domainLocation.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = domainLocation.category, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Rating: " + domainLocation.averageScore.toString(), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = domainLocation.latitude.toString(), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = domainLocation.longitude.toString(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
