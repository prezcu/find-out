package dev.andrei.app_frontend.ui.screen

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.app_frontend.data.remote.dto.preference.AttributeConceptDto
import dev.andrei.app_frontend.ui.components.Hairline
import dev.andrei.app_frontend.ui.components.ImportanceBar
import dev.andrei.app_frontend.ui.components.ImportanceSteppers
import dev.andrei.app_frontend.ui.components.Kicker
import dev.andrei.app_frontend.ui.components.SectionRule
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.util.displayLabel
import dev.andrei.app_frontend.ui.viewmodel.CategorySection
import dev.andrei.app_frontend.ui.viewmodel.PreferencesViewModel

@Composable
fun PreferencesScreen(
    onBack: () -> Unit,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val concepts by viewModel.concepts.collectAsStateWithLifecycle()
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val c = FindoutTheme.colors

    LaunchedEffect(saved) { if (saved) onBack() }

    // Which category sections are open. First section opens once data lands, as a hint that the
    // rows are tucked under the headers; after that the user is in control.
    val expanded = remember { mutableStateListOf<String>() }
    var didInit by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(sections) {
        if (!didInit && sections.isNotEmpty()) {
            expanded.add(sections.first().label)
            didInit = true
        }
    }

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
                sections.forEach { section ->
                    val isOpen = section.label in expanded
                    item(key = "header:${section.label}") {
                        CategoryHeader(
                            section = section,
                            expanded = isOpen,
                            onToggle = {
                                if (isOpen) expanded.remove(section.label) else expanded.add(section.label)
                            }
                        )
                    }
                    if (isOpen) {
                        items(section.concepts, key = { it.conceptId }) { concept ->
                            ConceptPreferenceRow(
                                concept = concept,
                                onImportanceChange = { viewModel.setImportance(concept.conceptId, it) }
                            )
                        }
                    }
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

/** Collapsible category header: name + how many in the group are set + a rotating chevron. */
@Composable
private fun CategoryHeader(
    section: CategorySection,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val c = FindoutTheme.colors
    val chevronRotation by animateFloatAsState(if (expanded) 90f else 0f, label = "chevron")
    Column {
        Row(
            Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                section.label,
                style = FindoutType.cardNameSm.copy(fontSize = 19.sp),
                color = c.ink,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${section.setCount}/${section.total} set",
                style = FindoutType.mono.copy(fontSize = 11.sp),
                color = if (section.setCount > 0) c.accent else c.sub
            )
            Text(
                "›",
                style = FindoutType.hero.copy(fontSize = 22.sp),
                color = c.sub,
                modifier = Modifier.rotate(chevronRotation)
            )
        }
        Hairline()
    }
}

@Composable
private fun ConceptPreferenceRow(
    concept: AttributeConceptDto,
    onImportanceChange: (Int) -> Unit
) {
    val c = FindoutTheme.colors
    Column(Modifier.fillMaxWidth().padding(vertical = 13.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                displayLabel(concept.displayName, concept.slug),
                style = FindoutType.cardNameSm.copy(fontSize = 19.sp),
                color = c.ink,
                modifier = Modifier.weight(1f)
            )
            ImportanceSteppers(value = concept.importance, onValueChange = onImportanceChange)
        }
        Spacer(Modifier.height(10.dp))
        ImportanceBar(value = concept.importance, onValueChange = onImportanceChange)
        Spacer(Modifier.height(13.dp))
        Hairline()
    }
}
