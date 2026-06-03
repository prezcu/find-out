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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.remote.dto.ReviewDto
import dev.andrei.app_frontend.ui.util.displayLabel
import dev.andrei.app_frontend.ui.viewmodel.AttractionScreenViewModel
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionScreen(
    onSignIn: () -> Unit,
    onWriteReview: () -> Unit,
    onBack: () -> Unit,
    viewModel: AttractionScreenViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.updateLogInState() }
    val location by viewModel.location.collectAsStateWithLifecycle()
    val logInState by viewModel.logInState.collectAsStateWithLifecycle()
    val isWishlisted by viewModel.isWishlisted.collectAsStateWithLifecycle()
    val wishlistBusy by viewModel.wishlistBusy.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()

    val routeDestination = if (logInState) onWriteReview else onSignIn

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(location?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (logInState) {
                        IconButton(onClick = viewModel::toggleWishlist, enabled = !wishlistBusy) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isWishlisted) "Remove from wishlist" else "Add to wishlist"
                            )
                        }
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
            else -> AttractionDetail(loc, logInState, routeDestination, reviews, Modifier.padding(padding))
        }
    }
}

@Composable
private fun AttractionDetail(
    location: LocationEntity,
    loggedIn: Boolean,
    route: () -> Unit,
    reviews: List<ReviewDto>,
    modifier: Modifier = Modifier
) {
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
                label = { Text(displayLabel(location.primaryCategoryDisplayName, location.category)) },
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

        HorizontalDivider()

        // --Review
        FilledTonalButton(
            onClick = route,
            modifier = Modifier
                .padding(top = 16.dp)
                .height(48.dp)
        ) {
            Text(if (loggedIn) "Write a review" else "Sign in to review")
        }

        HorizontalDivider()

        // --- Reviews ---
        DetailSection(title = "Reviews") {
            if (reviews.isEmpty()) {
                Text(
                    text = "No reviews yet. Be the first to review.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                reviews.forEach { review ->
                    ReviewCard(review)
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = review.reviewerDisplayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "%.1f".format(review.overallScore),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (review.attributeScores.isNotEmpty()) {
                Text(
                    text = review.attributeScores.joinToString("   •   ") {
                        "${displayLabel(it.displayName, it.attribute)} ${"%.1f".format(it.score)}"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (review.content.isNotBlank()) {
                Text(
                    text = review.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = review.createdAt.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
