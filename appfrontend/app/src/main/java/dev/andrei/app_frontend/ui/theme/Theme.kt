package dev.andrei.app_frontend.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

/**
 * Field Edition theme — dark only (HANDOFF). Hard 90° corners everywhere, no elevation, palette
 * driven by [FindoutColors]. Material's scheme is derived from the tokens purely so any
 * un-restyled Material surface inherits the look. Access tokens via `FindoutTheme.colors`.
 */
@Composable
fun FindoutTheme(content: @Composable () -> Unit) {
    val colors = FindoutColorsDark

    val scheme = darkColorScheme(
        primary = colors.accent,
        onPrimary = colors.onAccent,
        secondary = colors.accent2,
        onSecondary = colors.onAccent,
        tertiary = colors.sage,
        background = colors.bg,
        onBackground = colors.ink,
        surface = colors.card,
        onSurface = colors.ink,
        surfaceVariant = colors.cardHi,
        onSurfaceVariant = colors.sub,
        outline = colors.line,
        outlineVariant = colors.hair,
        error = colors.accent2,
        onError = colors.onAccent,
    )

    // Radius 0 everywhere (HANDOFF §4): hard-edged corners on all Material surfaces.
    val square = RoundedCornerShape(0.dp)
    val shapes = Shapes(
        extraSmall = square,
        small = square,
        medium = square,
        large = square,
        extraLarge = square,
    )

    CompositionLocalProvider(LocalFindoutColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = FindoutTypography,
            shapes = shapes,
            content = content
        )
    }
}

/** Ergonomic accessor: `FindoutTheme.colors.accent`. */
object FindoutTheme {
    val colors: FindoutColors
        @Composable @ReadOnlyComposable get() = LocalFindoutColors.current
}
