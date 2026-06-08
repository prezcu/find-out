package dev.andrei.app_frontend.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.remote.dto.ReviewDto
import dev.andrei.app_frontend.ui.components.BookmarkGlyph
import dev.andrei.app_frontend.ui.components.Hairline
import dev.andrei.app_frontend.ui.components.Kicker
import dev.andrei.app_frontend.ui.components.LedgerHeader
import dev.andrei.app_frontend.ui.components.LedgerRow
import dev.andrei.app_frontend.ui.components.MatchFigure
import dev.andrei.app_frontend.ui.components.SplitActionBar
import dev.andrei.app_frontend.ui.components.catalogCode
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.util.displayLabel
import dev.andrei.app_frontend.ui.viewmodel.AttractionScreenViewModel
import kotlin.math.roundToInt
import androidx.core.net.toUri

@Composable
fun AttractionScreen(
    onSignIn: () -> Unit,
    onWriteReview: () -> Unit,
    onBack: () -> Unit,
    viewModel: AttractionScreenViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.updateLogInState() }
    val location by viewModel.location.collectAsStateWithLifecycle()
    val loggedIn by viewModel.logInState.collectAsStateWithLifecycle()
    val isWishlisted by viewModel.isWishlisted.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val photoUrls by viewModel.photoUrls.collectAsStateWithLifecycle()
    val ledger by viewModel.ledger.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors
    val context = LocalContext.current

    val loc = location
    if (loc == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = c.accent)
        }
        return
    }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        // Top bar: ← / CAT. / bookmark
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(40.dp).clickable { onBack() }, contentAlignment = Alignment.CenterStart) {
                Text("←", style = FindoutType.hero.copy(fontSize = 24.sp), color = c.ink)
            }
            Text(
                "CAT. ${catalogCode(loc.id)}",
                style = FindoutType.mono.copy(fontSize = 10.sp, letterSpacing = 1.5.sp),
                color = c.sub,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Box(
                Modifier.size(40.dp).clickable {
                    if (loggedIn) viewModel.toggleWishlist() else onSignIn()
                },
                contentAlignment = Alignment.CenterEnd
            ) {
                BookmarkGlyph(filled = loggedIn && isWishlisted, size = 18.dp)
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Hero(loc, photoUrls)

            // Match band
            val percent = loc.matchScore?.let { (it / 5.0 * 100).roundToInt() }
            Row(
                Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (percent != null) {
                    MatchFigure(percent, big = true)
                } else {
                    Text("—", style = FindoutType.matchFigure, color = c.faint)
                }
                Column(Modifier.weight(1f).padding(top = 4.dp)) {
                    Kicker("Match for you", color = c.sub, fontSize = 9.5.sp, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (percent != null) "Ranked against your taste profile."
                        else "Set your preferences to see how this scores for you.",
                        style = FindoutType.body,
                        color = c.ink
                    )
                }
            }
            Hairline()

            // Ledger
            Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 15.dp)) {
                LedgerHeader()
                if (ledger.isEmpty()) {
                    Text(
                        "No attribute data yet.",
                        style = FindoutType.bodyItalic,
                        color = c.sub,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    ledger.forEach { entry ->
                        LedgerRow(entry)
                        Hairline()
                    }
                }
            }

            // Coordinates + taste-profile note
            Row(Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 6.dp)) {
                Box(Modifier.width(2.dp).height(40.dp).background(c.accent2))
                Spacer(Modifier.width(12.dp))
                Text(
                    "%.5f, %.5f · weighting from your taste profile.".format(loc.latitude, loc.longitude),
                    style = FindoutType.bodyItalic.copy(fontSize = 15.sp),
                    color = c.sub
                )
            }

            // Amenities (compact)
            Text(
                buildString {
                    append("ACCESSIBLE ").append(if (loc.hasAccessibleFeatures) "✓" else "✗")
                    append("   ·   TOILETS ").append(if (loc.hasToilets) "✓" else "✗")
                },
                style = FindoutType.mono.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                color = c.faint,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 18.dp)
            )

            Hairline()

            // Field notes (reviews)
            Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 18.dp)) {
                Kicker(
                    "Field Notes · ${reviews.size}",
                    color = c.faint,
                    fontSize = 9.5.sp,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, c.line)
                        .clickable { if (loggedIn) onWriteReview() else onSignIn() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (loggedIn) "Write a review" else "Sign in to review",
                        style = FindoutType.button,
                        color = c.ink
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (reviews.isEmpty()) {
                    Text(
                        "No reviews yet. Be the first to note this place.",
                        style = FindoutType.bodyItalic,
                        color = c.sub
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        reviews.forEach { ReviewCard(it) }
                    }
                }
            }
        }

        // Footer
        Hairline()
        SplitActionBar(
            saved = loggedIn && isWishlisted,
            onSave = { if (loggedIn) viewModel.toggleWishlist() else onSignIn() },
            onDirections = {
                val uri =
                    ("geo:${loc.latitude},${loc.longitude}?q=${loc.latitude},${loc.longitude}" +
                            "(${Uri.encode(loc.name)})").toUri()
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
            }
        )
    }
}

@Composable
private fun Hero(location: LocationEntity, photoUrls: List<String>) {
    val c = FindoutTheme.colors
    val shadow = Shadow(color = Color.Black.copy(alpha = 0.55f), offset = Offset(0f, 2f), blurRadius = 16f)
    Box(Modifier.fillMaxWidth().height(296.dp).background(c.imgTone)) {
        if (photoUrls.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { photoUrls.size })
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = photoUrls[page],
                    contentDescription = location.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.5f))
            )
        )
        Column(
            Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 20.dp, bottom = 18.dp)
        ) {
            Text(
                "${displayLabel(location.primaryCategoryDisplayName, location.category).uppercase()} · ★ %.2f".format(location.averageScore),
                style = FindoutType.kicker.copy(fontSize = 10.sp, letterSpacing = 1.sp, shadow = shadow),
                color = c.onImg.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(4.dp))
            Text(location.name, style = FindoutType.hero.copy(shadow = shadow), color = c.onImg)
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewDto) {
    val c = FindoutTheme.colors
    Column(
        Modifier.fillMaxWidth().border(1.dp, c.line).background(c.card).padding(horizontal = 15.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                review.reviewerDisplayName,
                style = FindoutType.cardNameSm.copy(fontSize = 18.sp),
                color = c.ink,
                modifier = Modifier.weight(1f)
            )
            Text("★ %.1f".format(review.overallScore), style = FindoutType.mono.copy(fontSize = 12.sp), color = c.accent)
        }
        if (review.attributeScores.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                review.attributeScores.joinToString("   ·   ") {
                    "${displayLabel(it.displayName, it.attribute)} ${"%.1f".format(it.score)}"
                },
                style = FindoutType.mono.copy(fontSize = 10.5.sp),
                color = c.sub
            )
        }
        if (review.content.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(review.content, style = FindoutType.body, color = c.ink)
        }
        Spacer(Modifier.height(8.dp))
        Text(review.createdAt.take(10), style = FindoutType.mono.copy(fontSize = 9.5.sp), color = c.faint)
    }
}
