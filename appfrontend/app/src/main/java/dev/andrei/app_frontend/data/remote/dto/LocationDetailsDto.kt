package dev.andrei.app_frontend.data.remote.dto

/**
 * On-demand details for the attraction screen: the venue's street address and weekly opening hours,
 * resolved server-side from Google Place Details on first view and cached. [address] is null when
 * unavailable; [openingHours] is empty when the venue has no hours.
 */
data class LocationDetailsDto(
    val address: String? = null,
    val openingHours: List<OpeningHourDto> = emptyList()
)

/**
 * One opening interval. [dayOfWeek] is ISO 1=Mon..7=Sun; [openTime]/[closeTime] are "HH:mm".
 * "00:00"–"00:00" means open 24 hours; a close earlier than open crosses midnight.
 */
data class OpeningHourDto(
    val dayOfWeek: Int,
    val openTime: String,
    val closeTime: String,
    val isClosed: Boolean = false
)
