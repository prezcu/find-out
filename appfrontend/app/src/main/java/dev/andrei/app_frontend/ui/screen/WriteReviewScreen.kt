package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.ui.components.FindoutPrimaryButton
import dev.andrei.app_frontend.ui.components.FindoutTextField
import dev.andrei.app_frontend.ui.components.Kicker
import dev.andrei.app_frontend.ui.state.AttributeRating
import dev.andrei.app_frontend.ui.state.ReviewDraft
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.util.displayLabel
import dev.andrei.app_frontend.ui.viewmodel.WriteReviewScrenViewModel

@Composable
fun WriteReviewScreen(
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: WriteReviewScrenViewModel = hiltViewModel()
) {
    val location by viewModel.location.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors

    var reviewText by remember { mutableStateOf("") }
    val attributeRatings = remember { mutableStateMapOf<String, Float>() }

    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) onSubmitSuccess() }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        // Top bar
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(40.dp).clickable { onBack() }, contentAlignment = Alignment.CenterStart) {
                Text("←", style = FindoutType.hero.copy(fontSize = 24.sp), color = c.ink)
            }
            Kicker(
                "Write a Review",
                color = c.sub,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.weight(1f).padding(end = 40.dp)
            )
        }

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(location?.attributes ?: emptyList(), key = { it.name }) { attribute ->
                AttributeCard(
                    name = displayLabel(attribute.displayName, attribute.name),
                    rating = attributeRatings[attribute.name] ?: 0f,
                    onRatingChange = { attributeRatings[attribute.name] = it }
                )
            }
            item {
                Spacer(Modifier.height(4.dp))
                FindoutTextField(
                    label = "Write your review",
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    singleLine = false
                )
            }
        }

        Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 16.dp)) {
            uiState.errorMessage?.let {
                Text(it, style = FindoutType.mono.copy(fontSize = 10.5.sp), color = c.accent2)
                Spacer(Modifier.height(8.dp))
            }
            FindoutPrimaryButton(
                label = "Submit review",
                onClick = {
                    val draft = ReviewDraft(
                        attributeRatings = (location?.attributes ?: emptyList()).map { attr ->
                            AttributeRating(attr.name, attributeRatings[attr.name] ?: 0f)
                        },
                        reviewText = reviewText
                    )
                    viewModel.submitReview(draft)
                },
                enabled = location != null && !uiState.isSubmitting,
                loading = uiState.isSubmitting
            )
        }
    }
}

@Composable
private fun AttributeCard(name: String, rating: Float, onRatingChange: (Float) -> Unit) {
    val c = FindoutTheme.colors
    Column(
        Modifier.fillMaxWidth().border(1.dp, c.line).background(c.card).padding(horizontal = 15.dp, vertical = 13.dp)
    ) {
        Text(name, style = FindoutType.cardNameSm.copy(fontSize = 19.sp), color = c.ink)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HalfStarRatingBar(rating = rating, onRatingChange = onRatingChange)
            Text("%.1f".format(rating), style = FindoutType.mono.copy(fontSize = 12.sp), color = c.sub)
        }
    }
}

@Composable
private fun HalfStarRatingBar(rating: Float, onRatingChange: (Float) -> Unit, starSize: Dp = 34.dp) {
    val c = FindoutTheme.colors
    Row {
        for (i in 0 until 5) {
            val fill = (rating - i).coerceIn(0f, 1f)
            Box(Modifier.size(starSize)) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = c.faint,
                    modifier = Modifier.matchParentSize()
                )
                if (fill > 0f) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = c.accent,
                        modifier = Modifier
                            .matchParentSize()
                            .drawWithContent {
                                clipRect(right = size.width * fill) { this@drawWithContent.drawContent() }
                            }
                    )
                }
                Row(Modifier.matchParentSize()) {
                    Box(Modifier.weight(1f).fillMaxHeight().clickable { onRatingChange(i + 0.5f) })
                    Box(Modifier.weight(1f).fillMaxHeight().clickable { onRatingChange(i + 1f) })
                }
            }
        }
    }
}
