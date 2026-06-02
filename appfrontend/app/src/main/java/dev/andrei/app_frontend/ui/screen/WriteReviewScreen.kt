package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.ui.state.AttributeRating
import dev.andrei.app_frontend.ui.state.ReviewDraft
import dev.andrei.app_frontend.ui.viewmodel.WriteReviewScrenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: WriteReviewScrenViewModel = hiltViewModel()
) {
    val location by viewModel.location.collectAsStateWithLifecycle()
    val reviewTextState = rememberTextFieldState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // UI state for the rating of each attribute card, keyed by attribute name.
    // Hoisted here (instead of inside AttributeCard) so it can be collected on submit.
    val attributeRatings = remember { mutableStateMapOf<String, Float>() }

    // Navigate away exactly once, after composition settles, when the submit succeeds.
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onSubmitSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Write a review") },
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
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(location?.attributes ?: emptyList()) { attribute ->
                        AttributeCard(
                            attributeName = attribute,
                            rating = attributeRatings[attribute] ?: 0f,
                            onRatingChange = { attributeRatings[attribute] = it }
                        )
                    }

                    item {
                        OutlinedTextField(
                            state = reviewTextState,
                            label = { Text("Write your review") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 15.dp),
                            lineLimits = TextFieldLineLimits.MultiLine(
                                minHeightInLines = 3,
                                maxHeightInLines = 8
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        val draft = ReviewDraft(
                            attributeRatings = (location?.attributes ?: emptyList()).map { attribute ->
                                AttributeRating(
                                    attribute = attribute,
                                    rating = attributeRatings[attribute] ?: 0f
                                )
                            },
                            reviewText = reviewTextState.text.toString()
                        )
                        viewModel.submitReview(draft)
                    },
                    enabled = location != null && !uiState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Submit review")
                    }
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

    }

}


@Composable
fun AttributeCard(
    attributeName: String,
    rating: Float,
    onRatingChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = attributeName, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HalfStarRatingBar(
                    rating = rating,
                    onRatingChange = onRatingChange
                )
                Text(
                    text = "%.1f".format(rating),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun HalfStarRatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    starSize: Dp = 36.dp,
) {
    val filledColor = Color(0xFFFFC107)
    val emptyColor = MaterialTheme.colorScheme.outlineVariant
    Row {
        for (i in 0 until 5) {
            // How much of this star is filled: 0f (empty), 0.5f (half), or 1f (full).
            val fill = (rating - i).coerceIn(0f, 1f)
            Box(modifier = Modifier.size(starSize)) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = emptyColor,
                    modifier = Modifier.matchParentSize()
                )
                if (fill > 0f) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = filledColor,
                        modifier = Modifier
                            .matchParentSize()
                            .drawWithContent {
                                clipRect(right = size.width * fill) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                    )
                }
                // Two invisible tap zones on top: left half -> +0.5, right half -> +1.
                Row(Modifier.matchParentSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onRatingChange(i + 0.5f) }
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onRatingChange(i + 1f) }
                    )
                }
            }
        }
    }
}
