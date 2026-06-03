package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.remote.ApiConfig
import dev.andrei.app_frontend.ui.viewmodel.LandingScreenViewModel
import dev.andrei.app_frontend.ui.state.LocationUiState
import dev.andrei.app_frontend.ui.util.displayLabel
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

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

                // When any result carries a match score, the list is personalized.
                val isMatchRanked = state.locations.any { it.matchScore != null }

                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        text = if (isMatchRanked) "Best matches near you" else "Top 10 Rated Locations Nearby",
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
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFCF9F2) // Surface Dark Slate
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Subtle shadow like a physical postcard
    ) {
      Column {
        // Thumbnail: real Google photo when available, branded placeholder otherwise.
        // The image is layered over a cream Box + faint pin, so a missing photo (404) or a
        // still-loading request degrades gracefully instead of showing a blank gap.
        LocationThumbnail(domainLocation)

        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Title, Category, and Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = domainLocation.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A312B) // Deep Espresso
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = displayLabel(domainLocation.primaryCategoryDisplayName, domainLocation.category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8C7A6B) // Muted Earth/Clay
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Rating Block
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFEBA63F), // Roman Sunset Gold
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = domainLocation.averageScore.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A312B) // Deep Espresso
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Row: Coordinates & Match Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coordinates
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Coordinates",
                        tint = Color(0xFFD46A54).copy(alpha = 0.8f), // Soft Terracotta Pin
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${domainLocation.latitude}, ${domainLocation.longitude}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF94A3B8) // Subdued Gray
                    )
                }

                // Match Score Badge
                domainLocation.matchScore?.let { match ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFD46A54).copy(alpha = 0.1f), // 10% Terracotta
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFD46A54).copy(alpha = 0.3f), // 30% Terracotta outline
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${(match / 5.0 * 100).roundToInt()}% Match",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2533D) // Solid Terracotta Text
                        )
                    }
                }
            }
        }
      }
    }
}

/**
 * Card header image. Renders the location's first Google photo over a cream placeholder that
 * carries a faint pin — so locations without a photo (the backend returns 404) or a slow load
 * still look intentional rather than blank. The Card clips this to its rounded top corners.
 */
@Composable
private fun LocationThumbnail(domainLocation: LocationEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color(0xFFEFE7D8)) // faint cream, matches the card surface family
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color(0xFFD46A54).copy(alpha = 0.25f), // ghosted terracotta pin
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
        )
        AsyncImage(
            model = ApiConfig.photoUrl(domainLocation.id.toString()),
            contentDescription = domainLocation.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
