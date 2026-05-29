package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.ui.viewmodel.AttractionScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionScreen(
    // locationId: String,
    onBack: () -> Unit,
    viewModel: AttractionScreenViewModel = hiltViewModel()
) {
    val location by viewModel.location.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(location?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        when (val loc = location) {
            null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> AttractionDetail(loc, Modifier.padding(padding))
        }
    }
}

@Composable
private fun AttractionDetail(location: LocationEntity, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Hero ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            AssistChip(
                onClick = {},
                label = { Text(location.category) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Place,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

        HorizontalDivider()

        // --- Score ---
        DetailSection(title = "Rating") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "%.1f / 5.0".format(location.averageScore),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        HorizontalDivider()

        // --- Coordinates ---
        DetailSection(title = "Location") {
            DetailRow(
                icon = { Icon(Icons.Filled.Place, contentDescription = null) },
                label = "Latitude",
                value = "%.5f".format(location.latitude)
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(
                icon = { Icon(Icons.Filled.Place, contentDescription = null) },
                label = "Longitude",
                value = "%.5f".format(location.longitude)
            )
        }

        HorizontalDivider()

        // --- Amenities ---
        DetailSection(title = "Amenities") {
            AmenityRow(
                label = "Accessible features",
                available = location.hasAccessibleFeatures
            )
            Spacer(modifier = Modifier.height(8.dp))
            AmenityRow(
                label = "Toilets available",
                available = location.hasToilets
            )
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun DetailRow(icon: @Composable () -> Unit, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            icon()
        }
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun AmenityRow(label: String, available: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            imageVector = if (available) Icons.Filled.CheckCircle else Icons.Filled.Close,
            contentDescription = null,
            tint = if (available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
