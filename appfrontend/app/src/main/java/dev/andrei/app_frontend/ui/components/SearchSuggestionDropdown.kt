package dev.andrei.app_frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.util.displayLabel

/**
 * Autocomplete dropdown of venue suggestions, shown beneath the search field while typing. Rendered
 * as a [Popup] so it floats over the live result cards. Anchored at its composition position (place
 * it directly under the text field). [focusable] is false so the keyboard stays up as the user types.
 *
 * Each row opens the venue via [onPick]. Width is matched to the screen content width (22.dp side
 * padding) since a Popup escapes the parent's layout constraints.
 */
@Composable
fun SearchSuggestionDropdown(
    suggestions: List<LocationEntity>,
    onPick: (LocationEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (suggestions.isEmpty()) return
    val c = FindoutTheme.colors
    val contentWidth = (LocalConfiguration.current.screenWidthDp - 44).dp

    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(0, 0),
        properties = PopupProperties(focusable = false),
    ) {
        Column(
            modifier
                .width(contentWidth)
                .border(1.dp, c.line)
                .background(c.card)
                .heightIn(max = 320.dp)
                .verticalScroll(rememberScrollState())
        ) {
            suggestions.forEachIndexed { index, location ->
                if (index > 0) Hairline()
                SuggestionRow(location, onClick = { onPick(location) })
            }
        }
    }
}

@Composable
private fun SuggestionRow(location: LocationEntity, onClick: () -> Unit) {
    val c = FindoutTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                location.name,
                style = FindoutType.cardNameSm.copy(fontSize = 16.sp),
                color = c.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Kicker(
                displayLabel(location.primaryCategoryDisplayName, location.category),
                color = c.sub,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Text("›", style = FindoutType.mono.copy(fontSize = 15.sp), color = c.faint)
    }
}
