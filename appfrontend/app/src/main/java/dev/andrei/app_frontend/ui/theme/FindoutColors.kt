package dev.andrei.app_frontend.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Field Edition colour tokens (HANDOFF §2). These are the single source of truth for the dark
 * theme; Material's [androidx.compose.material3.ColorScheme] is derived from them in [FindoutTheme]
 * only so un-restyled Material surfaces inherit the palette. Read these via [LocalFindoutColors]
 * (or the `FindoutTheme.colors` convenience accessor) rather than `MaterialTheme.colorScheme`.
 */
@Immutable
data class FindoutColors(
    val bg: Color,        // background / base surface
    val card: Color,      // raised surface
    val cardHi: Color,    // pressed / highlighted surface
    val ink: Color,       // primary text
    val sub: Color,       // secondary text
    val faint: Color,     // tertiary text, empty ticks
    val accent: Color,    // amber — match, active, primary action
    val accentDeep: Color,// amber pressed
    val accent2: Color,   // brick — secondary mark
    val sage: Color,      // positive / validation
    val onAccent: Color,  // text on amber
    val line: Color,      // hairline divider
    val hair: Color,      // faint track
    val imgTone: Color,   // photo placeholder fill
    val onImg: Color,     // text over photos
)

val FindoutColorsDark = FindoutColors(
    bg = Color(0xFF1E1915),
    card = Color(0xFF262019),
    cardHi = Color(0xFF2E2720),
    ink = Color(0xFFF1E8DA),
    sub = Color(0xFF9D8E7B),
    faint = Color(0xFF6B5E4E),
    accent = Color(0xFFE2A24F),
    accentDeep = Color(0xFFC9842F),
    accent2 = Color(0xFFBE5E3C),
    sage = Color(0xFF8C9A6E),
    onAccent = Color(0xFF1E1915),
    line = Color(0xFFF1E8DA).copy(alpha = 0.16f),
    hair = Color(0xFFF1E8DA).copy(alpha = 0.09f),
    imgTone = Color(0xFF4C3A29),
    onImg = Color(0xFFFBF6EE),
)

/** Swaps the whole tree's palette in one place; defaults to the dark Field Edition. */
val LocalFindoutColors = staticCompositionLocalOf { FindoutColorsDark }
