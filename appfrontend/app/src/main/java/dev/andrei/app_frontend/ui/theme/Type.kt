package dev.andrei.app_frontend.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import dev.andrei.app_frontend.R

// ── Downloadable Google Fonts (HANDOFF §3) ──────────────────────────────────────────────────
// Newsreader (display + numbers), Hanken Grotesk (UI + body), Spline Sans Mono (data only).
// These are NOT bundled — the GMS provider fetches & caches them; on a cold first run there may
// be a brief Roboto fallback before the typeface resolves.

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val newsreaderFont = GoogleFont("Newsreader")
private val hankenFont = GoogleFont("Hanken Grotesk")
private val splineMonoFont = GoogleFont("Spline Sans Mono")

/** Serif display + numbers. Includes italic faces (used for notes / empty states). */
val Newsreader = FontFamily(
    Font(newsreaderFont, googleFontProvider, FontWeight.Normal),
    Font(newsreaderFont, googleFontProvider, FontWeight.Medium),
    Font(newsreaderFont, googleFontProvider, FontWeight.SemiBold),
    Font(newsreaderFont, googleFontProvider, FontWeight.Normal, FontStyle.Italic),
    Font(newsreaderFont, googleFontProvider, FontWeight.Medium, FontStyle.Italic),
)

/** Sans UI + body. */
val Hanken = FontFamily(
    Font(hankenFont, googleFontProvider, FontWeight.Normal),
    Font(hankenFont, googleFontProvider, FontWeight.Medium),
    Font(hankenFont, googleFontProvider, FontWeight.SemiBold),
    Font(hankenFont, googleFontProvider, FontWeight.Bold),
    Font(hankenFont, googleFontProvider, FontWeight.ExtraBold),
)

/** Monospace data: CAT no., coords, ratings, scores, micro-labels. */
val SplineMono = FontFamily(
    Font(splineMonoFont, googleFontProvider, FontWeight.Normal),
    Font(splineMonoFont, googleFontProvider, FontWeight.Medium),
)

/** tabular-figures feature; applied to every numeric style. */
private const val TNUM = "tnum"

/**
 * Named Field Edition text styles (HANDOFF §3). For the many mono micro-labels at varying sizes,
 * use [SplineMono] directly or `FindoutType.mono.copy(fontSize = …)`.
 */
object FindoutType {
    // Trim default font padding + line height so the large numerals sit tight without clipping.
    private val tight = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )

    // The big match % on detail.
    val matchFigure = TextStyle(
        fontFamily = Newsreader, fontWeight = FontWeight.SemiBold,
        fontSize = 74.sp, lineHeight = 74.sp, letterSpacing = (-3).sp, fontFeatureSettings = TNUM,
        platformStyle = PlatformTextStyle(includeFontPadding = false), lineHeightStyle = tight
    )
    // Match % inside a card notch.
    val matchNotch = TextStyle(
        fontFamily = Newsreader, fontWeight = FontWeight.SemiBold,
        fontSize = 33.sp, lineHeight = 33.sp, letterSpacing = (-1.5).sp, fontFeatureSettings = TNUM,
        platformStyle = PlatformTextStyle(includeFontPadding = false), lineHeightStyle = tight
    )
    // Detail hero place name.
    val hero = TextStyle(
        fontFamily = Newsreader, fontWeight = FontWeight.Medium,
        fontSize = 42.sp, lineHeight = 41.sp, letterSpacing = (-1).sp
    )
    // Screen title.
    val h1 = TextStyle(
        fontFamily = Newsreader, fontWeight = FontWeight.Medium,
        fontSize = 34.sp, lineHeight = 34.sp, letterSpacing = (-0.9).sp
    )
    // List / card name.
    val cardName = TextStyle(
        fontFamily = Newsreader, fontWeight = FontWeight.Medium,
        fontSize = 27.sp, lineHeight = 28.sp, letterSpacing = (-0.4).sp
    )
    val cardNameSm = TextStyle(
        fontFamily = Newsreader, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 22.sp, letterSpacing = (-0.3).sp
    )
    // Body / note.
    val body = TextStyle(
        fontFamily = Newsreader, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 21.sp
    )
    val bodyItalic = TextStyle(
        fontFamily = Newsreader, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Italic,
        fontSize = 16.sp, lineHeight = 22.sp
    )
    // Button / UI label.
    val button = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.Bold,
        fontSize = 14.5.sp, letterSpacing = 0.3.sp
    )
    // Bottom nav tab label.
    val navLabel = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.Bold,
        fontSize = 11.sp, letterSpacing = 1.sp
    )
    // Mono kicker / data value (override fontSize as needed).
    val kicker = TextStyle(
        fontFamily = SplineMono, fontWeight = FontWeight.Normal,
        fontSize = 10.sp, letterSpacing = 2.sp
    )
    val mono = TextStyle(
        fontFamily = SplineMono, fontWeight = FontWeight.Normal,
        fontSize = 11.sp, fontFeatureSettings = TNUM
    )
}

// Material typography mapped onto the Field Edition families so any un-restyled Material text
// (TopAppBar titles, default Text, etc.) still uses our typefaces instead of Roboto.
private val base = Typography()
val FindoutTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Newsreader),
    displayMedium = base.displayMedium.copy(fontFamily = Newsreader),
    displaySmall = base.displaySmall.copy(fontFamily = Newsreader),
    headlineLarge = base.headlineLarge.copy(fontFamily = Newsreader),
    headlineMedium = base.headlineMedium.copy(fontFamily = Newsreader),
    headlineSmall = base.headlineSmall.copy(fontFamily = Newsreader),
    titleLarge = base.titleLarge.copy(fontFamily = Newsreader),
    titleMedium = base.titleMedium.copy(fontFamily = Hanken),
    titleSmall = base.titleSmall.copy(fontFamily = Hanken),
    bodyLarge = base.bodyLarge.copy(fontFamily = Newsreader),
    bodyMedium = base.bodyMedium.copy(fontFamily = Newsreader),
    bodySmall = base.bodySmall.copy(fontFamily = Hanken),
    labelLarge = base.labelLarge.copy(fontFamily = Hanken),
    labelMedium = base.labelMedium.copy(fontFamily = Hanken),
    labelSmall = base.labelSmall.copy(fontFamily = Hanken),
)
