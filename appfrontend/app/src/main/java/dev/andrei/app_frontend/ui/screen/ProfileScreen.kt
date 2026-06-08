package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.data.remote.dto.MyReviewDto
import dev.andrei.app_frontend.ui.components.FindoutOutlineButton
import dev.andrei.app_frontend.ui.components.FindoutPrimaryButton
import dev.andrei.app_frontend.ui.components.Kicker
import dev.andrei.app_frontend.ui.components.ScreenHeader
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.util.displayLabel
import dev.andrei.app_frontend.ui.viewmodel.ProfileScreenViewModel

@Composable
fun ProfileScreen(
    onSignIn: () -> Unit,
    onLogout: () -> Unit,
    onEditPreferences: () -> Unit,
    viewModel: ProfileScreenViewModel = hiltViewModel()
) {
    val loggedIn by viewModel.logInState.collectAsStateWithLifecycle()
    val myReviews by viewModel.myReviews.collectAsStateWithLifecycle()
    val reviewsLoading by viewModel.reviewsLoading.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors

    LaunchedEffect(Unit) { viewModel.updateLogInState() }

    Column(
        Modifier.fillMaxSize().padding(start = 22.dp, end = 22.dp, top = 4.dp)
    ) {
        ScreenHeader(title = "Profile", kicker = "Your Dossier")
        Spacer(Modifier.height(16.dp))

        if (!loggedIn) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sign in to see your reviews.", style = FindoutType.bodyItalic, color = c.sub)
                    Spacer(Modifier.height(16.dp))
                    FindoutPrimaryButton(label = "Sign in", onClick = onSignIn)
                }
            }
        } else {
            Text(
                "${myReviews.size} ${if (myReviews.size == 1) "review" else "reviews"} written",
                style = FindoutType.mono.copy(fontSize = 10.5.sp),
                color = c.sub
            )
            Spacer(Modifier.height(18.dp))
            Kicker("My Reviews", color = c.faint, fontSize = 9.5.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(8.dp))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    reviewsLoading && myReviews.isEmpty() ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = c.accent)
                        }

                    myReviews.isEmpty() ->
                        Text(
                            "You haven't written any reviews yet.",
                            style = FindoutType.bodyItalic,
                            color = c.sub
                        )

                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(myReviews, key = { it.id }) { review -> MyReviewCard(review) }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            FindoutPrimaryButton(label = "Edit preferences", onClick = onEditPreferences)
            Spacer(Modifier.height(10.dp))
            FindoutOutlineButton(label = "Log out", onClick = onLogout)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MyReviewCard(review: MyReviewDto) {
    val c = FindoutTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .border(1.dp, c.line)
            .background(c.card)
            .padding(horizontal = 15.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                review.locationName,
                style = FindoutType.cardNameSm,
                color = c.ink,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.height(0.dp))
            Text(
                "★ %.1f".format(review.overallScore),
                style = FindoutType.cardNameSm.copy(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
                color = c.accent
            )
        }
        if (review.attributeScores.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                review.attributeScores.forEach { attr ->
                    Row {
                        Text(
                            displayLabel(attr.displayName, attr.attribute) + " ",
                            style = FindoutType.mono.copy(fontSize = 10.5.sp),
                            color = c.sub
                        )
                        Text(
                            "%.1f".format(attr.score),
                            style = FindoutType.mono.copy(fontSize = 10.5.sp),
                            color = c.ink
                        )
                    }
                }
            }
        }
        if (review.content.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(review.content, style = FindoutType.body, color = c.ink)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            review.createdAt.take(10),
            style = FindoutType.mono.copy(fontSize = 9.5.sp),
            color = c.faint
        )
    }
}
