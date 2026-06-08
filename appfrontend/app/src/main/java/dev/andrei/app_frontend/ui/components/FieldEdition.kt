package dev.andrei.app_frontend.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.theme.Hanken
import java.util.UUID
import kotlin.math.abs

// ── Field-guide chrome ─────────────────────────────────────────────────────────────────────

/** Mono uppercase label used for screen kickers and small data tags. */
@Composable
fun Kicker(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = FindoutTheme.colors.accent,
    fontSize: TextUnit = 10.sp,
    letterSpacing: TextUnit = 2.sp,
) {
    Text(
        text = text.uppercase(),
        style = FindoutType.kicker.copy(fontSize = fontSize, letterSpacing = letterSpacing),
        color = color,
        modifier = modifier
    )
}

/** Section rule: a near-solid ink hairline over a faint hairline (HANDOFF §4). */
@Composable
fun SectionRule(modifier: Modifier = Modifier) {
    val c = FindoutTheme.colors
    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.ink.copy(alpha = 0.85f)))
        Spacer(Modifier.height(3.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
    }
}

/** Standard screen header: kicker row + rule + serif H1. */
@Composable
fun ScreenHeader(
    title: String,
    kicker: String,
    modifier: Modifier = Modifier,
    kickerRight: String? = null,
) {
    val c = FindoutTheme.colors
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Kicker(kicker, color = c.accent)
            if (kickerRight != null) {
                Kicker(kickerRight, color = c.sub, fontSize = 9.5.sp, letterSpacing = 1.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        SectionRule()
        Spacer(Modifier.height(13.dp))
        Text(title, style = FindoutType.h1, color = c.ink)
    }
}

/** Hairline divider, ink at 16% (HANDOFF §1). */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(FindoutTheme.colors.line))
}

// ── Match figure / notch ───────────────────────────────────────────────────────────────────

/** The amber match number with a smaller trailing "%". [shadow] keeps it legible over photos. */
@Composable
fun MatchFigure(
    percent: Int,
    big: Boolean = false,
    modifier: Modifier = Modifier,
    shadow: Shadow? = null,
) {
    val c = FindoutTheme.colors
    val numStyle = (if (big) FindoutType.matchFigure else FindoutType.matchNotch).copy(shadow = shadow)
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Text(percent.toString(), style = numStyle, color = c.accent)
        Text(
            "%",
            style = numStyle.copy(fontSize = if (big) 24.sp else 13.sp),
            color = c.accent,
            modifier = Modifier.padding(bottom = if (big) 6.dp else 2.dp)
        )
    }
}

/**
 * Match % flush to a photo's top-right corner; hidden when [percent] is null. Instead of an opaque
 * chip it uses a translucent diagonal scrim (darkest at the corner, fading toward the bottom-left)
 * so the photo reads through, with a shadow on the digits for legibility.
 */
@Composable
fun MatchNotch(percent: Int?, modifier: Modifier = Modifier) {
    if (percent == null) return
    val c = FindoutTheme.colors
    val shadow = Shadow(color = Color.Black.copy(alpha = 0.7f), offset = Offset(0f, 1f), blurRadius = 10f)
    Box(
        modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(Color.Transparent, c.bg.copy(alpha = 0.7f)),
                    start = Offset(0f, Float.POSITIVE_INFINITY), // bottom-left → transparent
                    end = Offset(Float.POSITIVE_INFINITY, 0f),   // top-right corner → dark
                )
            )
            .padding(start = 22.dp, end = 11.dp, top = 6.dp, bottom = 10.dp)
    ) {
        MatchFigure(percent, big = false, shadow = shadow)
    }
}

// ── Bookmark glyph (custom; material-icons-core has no Bookmark) ─────────────────────────────

@Composable
fun BookmarkGlyph(filled: Boolean, modifier: Modifier = Modifier, size: Dp = 17.dp) {
    val c = FindoutTheme.colors
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension / 16f
        val path = Path().apply {
            moveTo(3.5f * s, 2.2f * s)
            lineTo(12.5f * s, 2.2f * s)
            lineTo(12.5f * s, 13.8f * s)
            lineTo(8f * s, 10.4f * s)
            lineTo(3.5f * s, 13.8f * s)
            close()
        }
        if (filled) drawPath(path, color = c.accent)
        else drawPath(path, color = c.sub, style = Stroke(width = 1.4f * s))
    }
}

// ── Buttons ────────────────────────────────────────────────────────────────────────────────

@Composable
fun FindoutPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val c = FindoutTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .background(if (enabled && !loading) c.accent else c.faint)
            .clickable(enabled = enabled && !loading) { onClick() }
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = c.onAccent, strokeWidth = 2.dp)
        } else {
            Text(label, style = FindoutType.button, color = c.onAccent)
        }
    }
}

@Composable
fun FindoutOutlineButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = FindoutTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .border(1.dp, c.line)
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = FindoutType.button, color = c.ink)
    }
}

/** Detail footer: a split Save | Directions bar (HANDOFF §6). */
@Composable
fun SplitActionBar(
    saved: Boolean,
    onSave: () -> Unit,
    onDirections: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = FindoutTheme.colors
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(
            Modifier
                .weight(1f)
                .clickable { onSave() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (saved) "✓ Saved" else "Save",
                style = FindoutType.button,
                color = if (saved) c.accent else c.ink
            )
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(c.line))
        Box(
            Modifier
                .weight(1.3f)
                .background(c.accent)
                .clickable { onDirections() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Directions →", style = FindoutType.button, color = c.onAccent)
        }
    }
}

// ── Text field ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FindoutTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null,
) {
    val c = FindoutTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .border(1.dp, c.line)
            .background(c.card)
            .padding(start = 14.dp, end = 14.dp, top = 9.dp, bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                label.uppercase(),
                style = FindoutType.kicker.copy(fontSize = 9.sp, letterSpacing = 1.5.sp),
                color = c.sub
            )
            Spacer(Modifier.height(3.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = singleLine,
                textStyle = TextStyle(
                    color = c.ink,
                    fontFamily = Hanken,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(c.accent),
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                keyboardActions = KeyboardActions(
                    onSearch = { onImeAction?.invoke() },
                    onDone = { onImeAction?.invoke() },
                    onGo = { onImeAction?.invoke() },
                    onNext = { onImeAction?.invoke() },
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            trailing()
        }
    }
}

/** Convenience: a password visual transformation toggle is the caller's job; this just helps. */
fun passwordTransform(visible: Boolean): VisualTransformation =
    if (visible) VisualTransformation.None else PasswordVisualTransformation()

// ── Attribute ledger (HANDOFF §5/§6) ────────────────────────────────────────────────────────

/** One row of the detail ledger. [score] is null when no review has rated this attribute. */
data class LedgerEntry(val name: String, val weight: Int, val score: Float?)

@Composable
fun LedgerHeader(modifier: Modifier = Modifier) {
    val c = FindoutTheme.colors
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Kicker("Attribute", color = c.faint, fontSize = 9.sp, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
            Kicker("Weight", color = c.faint, fontSize = 9.sp, letterSpacing = 1.sp, modifier = Modifier.width(70.dp))
            Kicker("Score", color = c.faint, fontSize = 9.sp, letterSpacing = 1.sp, modifier = Modifier.width(40.dp))
        }
        Hairline()
    }
}

@Composable
fun LedgerRow(entry: LedgerEntry, modifier: Modifier = Modifier) {
    val c = FindoutTheme.colors
    val hi = entry.weight >= 4
    Column(modifier.fillMaxWidth().padding(vertical = 11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                if (hi) {
                    Text(
                        "›",
                        style = FindoutType.mono.copy(fontSize = 12.sp),
                        color = c.accent,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                }
                Text(
                    entry.name,
                    style = FindoutType.cardNameSm.copy(fontSize = 17.sp),
                    color = if (hi) c.ink else c.sub
                )
            }
            // weight dots
            Row(
                Modifier.width(70.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { j ->
                    val on = j < entry.weight
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(if (on) c.accent else Color.Transparent)
                            .border(1.dp, if (on) c.accent else c.faint)
                    )
                }
            }
            Text(
                entry.score?.let { "%.1f".format(it) } ?: "—",
                style = FindoutType.mono.copy(fontSize = 13.sp),
                color = c.ink,
                textAlign = TextAlign.End,
                modifier = Modifier.width(40.dp)
            )
        }
        Spacer(Modifier.height(9.dp))
        // track + fill
        Box(Modifier.fillMaxWidth().height(3.dp).background(c.line)) {
            val frac = ((entry.score ?: 0f) / 5f).coerceIn(0f, 1f)
            if (frac > 0f) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(frac)
                        .background(if (hi) c.accent else c.faint)
                )
            }
        }
    }
}

// ── Misc helpers ───────────────────────────────────────────────────────────────────────────

/**
 * A deterministic field-guide catalogue code (e.g. "044-A") derived from a venue id. Purely
 * decorative chrome to carry the "specimen" identity; stable per venue, not real data.
 */
fun catalogCode(id: UUID): String {
    val h = abs(id.hashCode())
    val num = h % 1000
    val letter = 'A' + (h / 1000 % 6)
    return "%03d-%s".format(num, letter)
}
