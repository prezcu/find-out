package dev.andrei.app_frontend.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.data.remote.dto.preference.AttributeConceptDto
import dev.andrei.app_frontend.ui.components.Hairline
import dev.andrei.app_frontend.ui.components.Kicker
import dev.andrei.app_frontend.ui.components.SectionRule
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.util.displayLabel
import dev.andrei.app_frontend.ui.viewmodel.PreferencesViewModel

@Composable
fun PreferencesScreen(
    onBack: () -> Unit,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val concepts by viewModel.concepts.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors

    LaunchedEffect(saved) { if (saved) onBack() }

    Column(Modifier.fillMaxSize()) {
        // Header (edit mode: back arrow + kicker)
        Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(48.dp).clickable { onBack() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("←", style = FindoutType.hero.copy(fontSize = 24.sp), color = c.ink)
                }
                Kicker("Edit Taste", color = c.sub, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.width(48.dp))
            }
            Spacer(Modifier.height(2.dp))
            SectionRule()
            Spacer(Modifier.height(13.dp))
            Text("Your preferences", style = FindoutType.h1, color = c.ink)
            Spacer(Modifier.height(4.dp))
            Text(
                "How much does each matter? 0 = don't care, 5 = essential.",
                style = FindoutType.bodyItalic,
                color = c.sub
            )
            Spacer(Modifier.height(8.dp))
        }

        if (loading && concepts.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp)
            ) {
                items(concepts, key = { it.conceptId }) { concept ->
                    ConceptPreferenceRow(
                        concept = concept,
                        onImportanceChange = { viewModel.setImportance(concept.conceptId, it) }
                    )
                }
            }
        }

        error?.let {
            Text(
                it,
                style = FindoutType.mono.copy(fontSize = 11.sp),
                color = c.accent2,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
            )
        }

        // Footer: status + compact Save
        Hairline()
        val setCount = concepts.count { it.importance > 0 }
        val totalCount = concepts.size
        Row(
            Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 13.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "$setCount/$totalCount set · keep tuning to sharpen your matches.",
                style = FindoutType.bodyItalic.copy(fontSize = 14.5.sp),
                color = c.sub,
                modifier = Modifier.weight(1f)
            )
            Box(
                Modifier
                    .background(if (saving) c.faint else c.accent)
                    .clickable(enabled = !saving) { viewModel.save() }
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(if (saving) "Saving…" else "Save →", style = FindoutType.button, color = c.onAccent)
            }
        }
    }
}

@Composable
private fun ConceptPreferenceRow(
    concept: AttributeConceptDto,
    onImportanceChange: (Int) -> Unit
) {
    val c = FindoutTheme.colors
    val w = concept.importance
    Column(Modifier.fillMaxWidth().padding(vertical = 13.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                displayLabel(concept.displayName, concept.slug),
                style = FindoutType.cardNameSm.copy(fontSize = 19.sp),
                color = c.ink,
                modifier = Modifier.weight(1f)
            )
            Stepper(glyph = "–", borderColor = c.line, glyphColor = if (w > 0) c.ink else c.faint) {
                onImportanceChange((w - 1).coerceAtLeast(0))
            }
            Text(
                w.toString(),
                style = FindoutType.cardName.copy(fontSize = 26.sp, fontFeatureSettings = "tnum"),
                color = c.accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(24.dp)
            )
            Stepper(glyph = "+", borderColor = c.accent, glyphColor = c.accent) {
                onImportanceChange((w + 1).coerceAtMost(5))
            }
        }
        Spacer(Modifier.height(10.dp))
        // tappable 5-segment 0..5 bar
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (j in 0 until 5) {
                val on = j < w
                Box(
                    Modifier
                        .weight(1f)
                        .height(9.dp)
                        .background(if (on) c.accent else Color.Transparent)
                        .border(1.dp, if (on) c.accent else c.line)
                        .clickable { onImportanceChange(if (j + 1 == w) j else j + 1) }
                )
            }
        }
        Spacer(Modifier.height(13.dp))
        Hairline()
    }
}

@Composable
private fun Stepper(glyph: String, borderColor: Color, glyphColor: Color, onClick: () -> Unit) {
    // 48dp touch target around a compact 26dp visual square (HANDOFF §4).
    Box(
        Modifier.size(48.dp).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(26.dp).border(1.dp, borderColor),
            contentAlignment = Alignment.Center
        ) {
            Text(glyph, style = FindoutType.mono.copy(fontSize = 16.sp), color = glyphColor)
        }
    }
}
