package dev.andrei.app_frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.remote.ApiConfig
import dev.andrei.app_frontend.ui.theme.FindoutTheme
import dev.andrei.app_frontend.ui.theme.FindoutType
import dev.andrei.app_frontend.ui.util.displayLabel
import kotlin.math.roundToInt

/**
 * Specimen card (HANDOFF §6): hard-edged bordered surface with a cropped hero photo, a CAT. kicker
 * + match notch overlaid, the name/category over a scrim, and a footer with the ★ rating and an
 * optional bookmark. The match notch is hidden when this venue carries no [LocationEntity.matchScore]
 * (i.e. it came from a non-personalised list). STANDOUT is intentionally omitted — per-attribute
 * scores aren't available on list rows.
 */
@Composable
fun SpecimenCard(
    location: LocationEntity,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    photoHeight: Dp = 190.dp,
    bookmarkFilled: Boolean? = null,
    onToggleBookmark: () -> Unit = {},
) {
    val c = FindoutTheme.colors
    val percent = location.matchScore?.let { (it / 5.0 * 100).roundToInt() }
    val onImgShadow = Shadow(color = Color.Black.copy(alpha = 0.6f), offset = Offset(0f, 2f), blurRadius = 12f)

    Column(
        modifier
            .fillMaxWidth()
            .border(1.dp, c.line)
            .background(c.card)
            .clickable { onOpen() }
    ) {
        Box(Modifier.fillMaxWidth().height(photoHeight)) {
            AsyncImage(
                model = ApiConfig.photoUrl(location.id.toString()),
                contentDescription = location.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(c.imgTone)
            )
            // Bottom scrim so the name stays legible over any photo.
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0.4f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.5f)
                    )
                )
            )
            Kicker(
                "CAT. ${catalogCode(location.id)}",
                color = c.onImg.copy(alpha = 0.85f),
                fontSize = 9.5.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 13.dp, top = 11.dp)
            )
            MatchNotch(percent, Modifier.align(Alignment.TopEnd))
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, end = 78.dp, bottom = 12.dp)
            ) {
                Text(
                    displayLabel(location.primaryCategoryDisplayName, location.category).uppercase(),
                    style = FindoutType.kicker.copy(
                        fontSize = 9.5.sp,
                        letterSpacing = 1.sp,
                        shadow = onImgShadow
                    ),
                    color = c.onImg.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    location.name,
                    style = FindoutType.cardName.copy(shadow = onImgShadow),
                    color = c.onImg
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 11.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "★ %.2f".format(location.averageScore),
                style = FindoutType.mono.copy(fontSize = 11.sp),
                color = c.accent
            )
            Spacer(Modifier.weight(1f))
            if (bookmarkFilled != null) {
                Box(
                    modifier = Modifier.size(44.dp).clickable { onToggleBookmark() },
                    contentAlignment = Alignment.Center
                ) {
                    BookmarkGlyph(filled = bookmarkFilled)
                }
            }
        }
    }
}
