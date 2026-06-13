package dev.andrei.app_frontend.ui.util

import dev.andrei.app_frontend.data.remote.dto.OpeningHourDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Derives a live open-now status and a weekly schedule from a venue's regular opening hours.
 *
 * Pure (java.time only, no Android deps) so it's unit-testable. All reasoning happens in
 * "minutes since Monday 00:00" (a 0..10080 week ring), which makes past-midnight intervals and the
 * Sunday→Monday wrap fall out naturally. The caller passes [ZonedDateTime.now] in the app's zone
 * (Europe/Bucharest) so DST is handled by java.time — we never trust a cached open-now flag.
 *
 * Convention (matches the backend): [OpeningHourDto.dayOfWeek] is ISO 1=Mon..7=Sun, times are "HH:mm";
 * a row is always an open interval (days with no row are closed); "00:00"–"00:00" means open 24h; a
 * close earlier than open crosses midnight.
 */

private const val MIN_PER_DAY = 24 * 60
private const val MIN_PER_WEEK = 7 * MIN_PER_DAY

sealed interface OpenStatus {
    /** Open right now; [closesAt] is the active interval's close (ignored when [open24h]). */
    data class Open(val closesAt: LocalTime, val open24h: Boolean) : OpenStatus
    /** Closed now; next opens at [opensAt] on [opensDate]. */
    data class Closed(val opensAt: LocalTime, val opensDate: LocalDate) : OpenStatus
    /** No usable hours data. */
    data object Unknown : OpenStatus
}

/** A one-line badge: [text] to show and whether the venue is currently [open] (drives colour). */
data class OpenBadge(val text: String, val open: Boolean)

/** One day's row for the weekly schedule, e.g. ("Mon", "09:00–18:00") or ("Sun", "Closed"). */
data class DayHours(val label: String, val text: String)

private data class Interval(
    val startMin: Int,
    val lengthMin: Int,
    val openTime: LocalTime,
    val closeTime: LocalTime,
    val open24h: Boolean,
)

fun openStatus(hours: List<OpeningHourDto>, now: ZonedDateTime): OpenStatus {
    val intervals = hours.mapNotNull { it.toInterval() }
    if (intervals.isEmpty()) return OpenStatus.Unknown

    val nowMin = (now.dayOfWeek.value - 1) * MIN_PER_DAY + now.hour * 60 + now.minute

    // Open now? An interval contains `now` if the forward distance from its start is within its length.
    for (iv in intervals) {
        val offset = Math.floorMod(nowMin - iv.startMin, MIN_PER_WEEK)
        if (offset < iv.lengthMin) {
            return OpenStatus.Open(iv.closeTime, iv.open24h)
        }
    }

    // Closed: the next opening is the interval with the smallest positive distance from now.
    val next = intervals.minByOrNull { iv ->
        val dist = Math.floorMod(iv.startMin - nowMin, MIN_PER_WEEK)
        if (dist == 0) MIN_PER_WEEK else dist
    } ?: return OpenStatus.Unknown

    val dist = Math.floorMod(next.startMin - nowMin, MIN_PER_WEEK).let { if (it == 0) MIN_PER_WEEK else it }
    val opensAt = now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(dist.toLong())
    return OpenStatus.Closed(next.openTime, opensAt.toLocalDate())
}

/** Formats an [OpenStatus] into a badge, or null when unknown. [now] supplies "today"/"tomorrow". */
fun OpenStatus.toBadge(now: ZonedDateTime): OpenBadge? = when (this) {
    is OpenStatus.Open ->
        OpenBadge(if (open24h) "Open 24 hours" else "Open now · closes ${fmt(closesAt)}", true)
    is OpenStatus.Closed ->
        OpenBadge("Closed · opens ${dayLabel(opensDate, now.toLocalDate())} ${fmt(opensAt)}", false)
    OpenStatus.Unknown -> null
}

/** ISO Mon..Sun rows for the schedule table; days without hours read "Closed". */
fun weeklySchedule(hours: List<OpeningHourDto>): List<DayHours> {
    val byDay = hours.groupBy { it.dayOfWeek }
    return (1..7).map { d ->
        val items = byDay[d].orEmpty().sortedBy { it.openTime }
        val text = if (items.isEmpty()) "Closed" else items.joinToString(", ") { formatInterval(it) }
        DayHours(DayOfWeek.of(d).getDisplayName(TextStyle.SHORT, Locale.ENGLISH), text)
    }
}

private fun OpeningHourDto.toInterval(): Interval? {
    if (dayOfWeek !in 1..7) return null
    val open = parseTime(openTime) ?: return null
    val close = parseTime(closeTime) ?: return null
    val openMin = open.hour * 60 + open.minute
    val closeMin = close.hour * 60 + close.minute
    val is24 = openMin == 0 && closeMin == 0
    val length = when {
        is24 -> MIN_PER_DAY
        closeMin > openMin -> closeMin - openMin
        else -> (MIN_PER_DAY - openMin) + closeMin // crosses midnight
    }
    if (length <= 0) return null
    return Interval((dayOfWeek - 1) * MIN_PER_DAY + openMin, length, open, close, is24)
}

private fun formatInterval(h: OpeningHourDto): String =
    if (h.openTime == "00:00" && h.closeTime == "00:00") "Open 24 hours"
    else "${h.openTime}–${h.closeTime}"

private fun dayLabel(opensDate: LocalDate, today: LocalDate): String =
    when (ChronoUnit.DAYS.between(today, opensDate)) {
        0L -> "today"
        1L -> "tomorrow"
        else -> opensDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    }

private fun fmt(t: LocalTime): String = "%02d:%02d".format(t.hour, t.minute)

private fun parseTime(value: String): LocalTime? = try {
    val parts = value.split(":")
    LocalTime.of(parts[0].toInt(), parts[1].toInt())
} catch (e: Exception) {
    null
}
