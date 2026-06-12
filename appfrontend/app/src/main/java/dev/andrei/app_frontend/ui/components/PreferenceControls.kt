package dev.andrei.app_frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType

/**
 * Shared 0–5 importance controls used by both the Preferences editor and the onboarding wizard,
 * so the two stay visually identical (HANDOFF §4). [value] is clamped 0..5 by the controls;
 * [onValueChange] receives the new value.
 */

/** A 48dp touch target around a compact visual square with a +/– glyph. */
@Composable
fun Stepper(
    glyph: String,
    borderColor: Color,
    glyphColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier.size(48.dp).clickable { onClick() },
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

/** `–  [n]  +` cluster. The minus dims at 0; both steppers clamp to 0..5. */
@Composable
fun ImportanceSteppers(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    numberFontSize: TextUnit = 26.sp,
    numberWidth: Dp = 24.dp,
) {
    val c = FindoutTheme.colors
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Stepper(glyph = "–", borderColor = c.line, glyphColor = if (value > 0) c.ink else c.faint) {
            onValueChange((value - 1).coerceAtLeast(0))
        }
        Text(
            value.toString(),
            style = FindoutType.cardName.copy(fontSize = numberFontSize, fontFeatureSettings = "tnum"),
            color = c.accent,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(numberWidth)
        )
        Stepper(glyph = "+", borderColor = c.accent, glyphColor = c.accent) {
            onValueChange((value + 1).coerceAtMost(5))
        }
    }
}

/** Tappable 5-segment 0..5 bar. Tapping the last filled segment toggles it back off. */
@Composable
fun ImportanceBar(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    segmentHeight: Dp = 9.dp,
) {
    val c = FindoutTheme.colors
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (j in 0 until 5) {
            val on = j < value
            Box(
                Modifier
                    .weight(1f)
                    .height(segmentHeight)
                    .background(if (on) c.accent else Color.Transparent)
                    .border(1.dp, if (on) c.accent else c.line)
                    .clickable { onValueChange(if (j + 1 == value) j else j + 1) }
            )
        }
    }
}
