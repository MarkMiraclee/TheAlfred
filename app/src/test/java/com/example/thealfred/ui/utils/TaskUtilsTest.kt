package com.example.thealfred.ui.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class TaskUtilsTest {

    /** То, что отдаёт Material3 DatePicker: полночь UTC выбранной даты. */
    private fun pickerMillis(year: Int, month: Int, day: Int): Long =
        GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day)
        }.timeInMillis

    private fun assertEndOfDay(tzId: String, year: Int, month: Int, day: Int) {
        val tz = TimeZone.getTimeZone(tzId)
        val result = TaskUtils.pickerDateToEndOfLocalDay(pickerMillis(year, month, day), tz)
        val local = GregorianCalendar(tz).apply { timeInMillis = result }

        assertEquals("$tzId year", year, local.get(Calendar.YEAR))
        assertEquals("$tzId month", month, local.get(Calendar.MONTH))
        assertEquals("$tzId day", day, local.get(Calendar.DAY_OF_MONTH))
        assertEquals("$tzId hour", 23, local.get(Calendar.HOUR_OF_DAY))
        assertEquals("$tzId minute", 59, local.get(Calendar.MINUTE))
        assertEquals("$tzId second", 59, local.get(Calendar.SECOND))
        assertEquals("$tzId millis", 999, local.get(Calendar.MILLISECOND))
    }

    @Test
    fun deadlineIsEndOfPickedDayInEastOfUtc() {
        assertEndOfDay("Europe/Moscow", 2026, Calendar.OCTOBER, 25)
        assertEndOfDay("Pacific/Kiritimati", 2026, Calendar.OCTOBER, 25) // UTC+14
    }

    @Test
    fun deadlineIsEndOfPickedDayInWestOfUtc() {
        assertEndOfDay("America/Los_Angeles", 2026, Calendar.OCTOBER, 25)
        assertEndOfDay("Pacific/Honolulu", 2026, Calendar.OCTOBER, 25) // UTC-10
    }

    @Test
    fun deadlineIsEndOfPickedDayInUtcAndAroundDstChange() {
        assertEndOfDay("UTC", 2026, Calendar.OCTOBER, 25)
        assertEndOfDay("Europe/Berlin", 2026, Calendar.OCTOBER, 25) // день смены на зимнее время
        assertEndOfDay("Europe/Berlin", 2026, Calendar.DECEMBER, 31)
    }

    @Test
    fun deadlineIsNotReachedBeforeTheEndOfThePickedDay() {
        // Регрессия: раньше значение сохранялось как 00:00 UTC, и в Москве задача
        // становилась просроченной уже в 03:00 выбранного дня.
        val moscow = TimeZone.getTimeZone("Europe/Moscow")
        val picked = pickerMillis(2026, Calendar.OCTOBER, 25)
        val result = TaskUtils.pickerDateToEndOfLocalDay(picked, moscow)

        val noonOnPickedDay = GregorianCalendar(moscow).apply {
            clear()
            set(2026, Calendar.OCTOBER, 25, 12, 0, 0)
        }.timeInMillis
        assertTrue(result > noonOnPickedDay)
        assertTrue(result > picked + 20 * 60 * 60 * 1000L)
    }
}
