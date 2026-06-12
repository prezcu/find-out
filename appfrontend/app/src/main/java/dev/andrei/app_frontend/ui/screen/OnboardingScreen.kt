package dev.andrei.app_frontend.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.data.remote.dto.preference.AttributeConceptDto
import dev.andrei.app_frontend.ui.components.FindoutOutlineButton
import dev.andrei.app_frontend.ui.components.FindoutPrimaryButton
import dev.andrei.app_frontend.ui.components.ImportanceBar
import dev.andrei.app_frontend.ui.components.ImportanceSteppers
import dev.andrei.app_frontend.ui.components.Kicker
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.util.displayLabel
import dev.andrei.app_frontend.ui.viewmodel.OnboardingViewModel

private val IMPORTANCE_CAPTIONS = listOf(
    "Don't care", "A little", "Somewhat", "Matters", "Really matters", "Essential"
)

@Composable
fun OnboardingScreen(
    onFinishToPreferences: () -> Unit,
    onFinishToLanding: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val concepts by viewModel.concepts.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val step by viewModel.step.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors

    // Where to go once the curated picks have been saved (set when the user commits on the
    // final step). Skipping bypasses this and leaves immediately.
    var pendingNav by remember { mutableStateOf<(() -> Unit)?>(null) }
    LaunchedEffect(saved) { if (saved) pendingNav?.invoke() }

    val total = concepts.size
    val onFinalStep = step >= total

    Column(
        Modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (loading && concepts.isEmpty()) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
            return@Column
        }

        // Top row: progress dots + Skip (hidden on the final, already-committed screen).
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressDots(total = total, current = step)
            if (!onFinalStep) {
                Text(
                    "Skip →",
                    style = FindoutType.button.copy(fontSize = 13.5.sp),
                    color = c.sub,
                    modifier = Modifier.clickable { onFinishToLanding() }
                )
            } else {
                Spacer(Modifier.size(0.dp))
            }
        }

        Spacer(Modifier.height(40.dp))

        if (onFinalStep) {
            FinalStep(
                saving = saving,
                error = error,
                onStartExploring = {
                    pendingNav = onFinishToLanding
                    viewModel.finish()
                },
                onFineTuneMore = {
                    pendingNav = onFinishToPreferences
                    viewModel.finish()
                }
            )
        } else {
            val concept = concepts[step]
            AttributeStep(
                concept = concept,
                onImportanceChange = { viewModel.setImportance(concept.conceptId, it) },
                onBack = viewModel::back,
                onNext = { viewModel.next(total) },
                showBack = step > 0
            )
        }
    }
}

@Composable
private fun ProgressDots(total: Int, current: Int) {
    val c = FindoutTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until total) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (i <= current) c.accent else c.faint)
            )
        }
    }
}

@Composable
private fun AttributeStep(
    concept: AttributeConceptDto,
    onImportanceChange: (Int) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    showBack: Boolean,
) {
    val c = FindoutTheme.colors
    val value = concept.importance

    Kicker("Tune your taste", color = c.accent)
    Spacer(Modifier.height(12.dp))
    Text("How much does", style = FindoutType.bodyItalic.copy(fontSize = 18.sp), color = c.sub)
    Text(
        displayLabel(concept.displayName, concept.slug),
        style = FindoutType.hero,
        color = c.ink
    )
    Text("matter to you?", style = FindoutType.bodyItalic.copy(fontSize = 18.sp), color = c.sub)

    Spacer(Modifier.height(36.dp))

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImportanceSteppers(value = value, onValueChange = onImportanceChange, numberFontSize = 40.sp, numberWidth = 44.dp)
    }
    Spacer(Modifier.height(14.dp))
    ImportanceBar(value = value, onValueChange = onImportanceChange, segmentHeight = 12.dp)
    Spacer(Modifier.height(10.dp))
    Text(
        IMPORTANCE_CAPTIONS[value],
        style = FindoutType.mono.copy(fontSize = 12.sp),
        color = c.sub,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(44.dp))

    FindoutPrimaryButton(label = "Next →", onClick = onNext)
    Spacer(Modifier.height(14.dp))
    if (showBack) {
        Text(
            "← Back",
            style = FindoutType.button.copy(fontSize = 13.5.sp),
            color = c.sub,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable { onBack() }
        )
    }
}

@Composable
private fun FinalStep(
    saving: Boolean,
    error: String?,
    onStartExploring: () -> Unit,
    onFineTuneMore: () -> Unit,
) {
    val c = FindoutTheme.colors

    Kicker("All set", color = c.accent)
    Spacer(Modifier.height(12.dp))
    Text("You're ready to explore.", style = FindoutType.hero, color = c.ink)
    Spacer(Modifier.height(8.dp))
    Text(
        "We'll use these to surface places you'll love. Add the rest or fine-tune anytime in Settings.",
        style = FindoutType.bodyItalic.copy(fontSize = 17.sp),
        color = c.sub
    )

    Spacer(Modifier.height(36.dp))

    FindoutPrimaryButton(label = "Start exploring →", onClick = onStartExploring, loading = saving)
    Spacer(Modifier.height(14.dp))
    FindoutOutlineButton(label = "Fine-tune more", onClick = onFineTuneMore, enabled = !saving)

    error?.let {
        Spacer(Modifier.height(12.dp))
        Text(
            it,
            style = FindoutType.mono.copy(fontSize = 11.sp),
            color = c.accent2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
