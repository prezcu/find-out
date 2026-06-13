package dev.andrei.app_frontend.ui.util

import dev.andrei.app_frontend.data.remote.dto.OpeningHourDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class OpeningHoursStatusTest {

    private val zone = ZoneId.of("Europe/Bucharest")
    // 2024-01-01 is a Monday, so ISO day N maps to baseMonday + (N-1) days.
    private val baseMonday = LocalDate.of(2024, 1, 1)

    private fun dt(isoDay: Int, hour: Int, minute: Int): ZonedDateTime =
        ZonedDateTime.of(baseMonday.plusDays((isoDay - 1).toLong()), LocalTime.of(hour, minute), zone)

    private fun hour(day: Int, open: String, close: String) = OpeningHourDto(day, open, close)

    @Test
    fun `same-day interval reports open with closing time`() {
        val hours = listOf(hour(1, "09:00", "17:00"))
        val status = openStatus(hours, dt(1, 12, 0))
        assertTrue(status is OpenStatus.Open)
        status as OpenStatus.Open
        assertEquals(LocalTime.of(17, 0), status.closesAt)
        assertFalse(status.open24h)
    }

    @Test
    fun `before opening reports closed opening later today`() {
        val hours = listOf(hour(1, "09:00", "17:00"))
        val status = openStatus(hours, dt(1, 8, 0))
        assertTrue(status is OpenStatus.Closed)
        status as OpenStatus.Closed
        assertEquals(LocalTime.of(9, 0), status.opensAt)
        assertEquals(baseMonday, status.opensDate)
    }

    @Test
    fun `after closing wraps to next week same day`() {
        val hours = listOf(hour(1, "09:00", "17:00"))
        val status = openStatus(hours, dt(1, 18, 0))
        assertTrue(status is OpenStatus.Closed)
        status as OpenStatus.Closed
        assertEquals(baseMonday.plusDays(7), status.opensDate) // next Monday
    }

    @Test
    fun `past-midnight interval carries over into next day`() {
        val hours = listOf(hour(5, "18:00", "02:00")) // Fri 18:00 -> Sat 02:00
        val open = openStatus(hours, dt(6, 1, 0)) // Sat 01:00
        assertTrue(open is OpenStatus.Open)
        assertEquals(LocalTime.of(2, 0), (open as OpenStatus.Open).closesAt)

        val closed = openStatus(hours, dt(6, 3, 0)) // Sat 03:00, after close
        assertTrue(closed is OpenStatus.Closed)
    }

    @Test
    fun `00 00 to 00 00 is open 24 hours`() {
        val hours = listOf(hour(1, "00:00", "00:00"))
        val status = openStatus(hours, dt(1, 3, 0))
        assertTrue(status is OpenStatus.Open)
        assertTrue((status as OpenStatus.Open).open24h)
    }

    @Test
    fun `empty hours is unknown and produces no badge`() {
        val status = openStatus(emptyList(), dt(1, 12, 0))
        assertEquals(OpenStatus.Unknown, status)
        assertNull(status.toBadge(dt(1, 12, 0)))
    }

    @Test
    fun `weekly schedule fills closed days and merges split hours`() {
        val hours = listOf(
            hour(1, "09:00", "13:00"),
            hour(1, "14:00", "18:00"),
            hour(7, "00:00", "00:00"),
        )
        val week = weeklySchedule(hours)
        assertEquals(7, week.size)
        assertEquals("09:00–13:00, 14:00–18:00", week[0].text) // Monday, split + sorted
        assertEquals("Closed", week[1].text)                    // Tuesday, no row
        assertEquals("Open 24 hours", week[6].text)             // Sunday
    }

    @Test
    fun `badge text reflects open and closed states`() {
        val openBadge = OpenStatus.Open(LocalTime.of(17, 0), open24h = false).toBadge(dt(1, 12, 0))!!
        assertTrue(openBadge.open)
        assertEquals("Open now · closes 17:00", openBadge.text)

        val closedBadge = OpenStatus.Closed(LocalTime.of(9, 0), baseMonday).toBadge(dt(1, 8, 0))!!
        assertFalse(closedBadge.open)
        assertEquals("Closed · opens today 09:00", closedBadge.text)
    }
}
