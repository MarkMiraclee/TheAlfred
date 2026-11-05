package com.example.thealfred.ui.utils

import com.example.thealfred.data.WeekUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class WeekUtilsTest {

    /** Понедельник 00:00 указанной недели в таймзоне. */
    private fun mondayOf(tzId: String, year: Int, month: Int, day: Int): Long =
        GregorianCalendar(TimeZone.getTimeZone(tzId)).apply {
            clear()
            set(year, month, day, 0, 0, 0)
        }.timeInMillis

    /** Произвольный момент в таймзоне. */
    private fun at(tzId: String, year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        GregorianCalendar(TimeZone.getTimeZone(tzId)).apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }.timeInMillis

    @Test
    fun weekStartsOnMondayMidnight() {
        val tz = "Europe/Moscow"
        val monday = mondayOf(tz, 2026, Calendar.NOVEMBER, 2) // понедельник

        // Среда полдень -> понедельник той же недели
        assertEquals(monday, WeekUtils.startOfWeek(at(tz, 2026, Calendar.NOVEMBER, 4, 12, 0), TimeZone.getTimeZone(tz)))
        // Сам понедельник 00:00 -> неизменен
        assertEquals(monday, WeekUtils.startOfWeek(monday, TimeZone.getTimeZone(tz)))
    }

    @Test
    fun sundayBelongsToPreviousWeek() {
        val tz = "Europe/Moscow"
        // Воскресенье 01.11.2026 принадлежит неделе, начавшейся 26.10
        assertEquals(
            mondayOf(tz, 2026, Calendar.OCTOBER, 26),
            WeekUtils.startOfWeek(at(tz, 2026, Calendar.NOVEMBER, 1, 23, 59), TimeZone.getTimeZone(tz))
        )
        // Понедельник 02.11 уже новая неделя
        assertEquals(
            mondayOf(tz, 2026, Calendar.NOVEMBER, 2),
            WeekUtils.startOfWeek(at(tz, 2026, Calendar.NOVEMBER, 2, 0, 0), TimeZone.getTimeZone(tz))
        )
    }

    @Test
    fun weekBoundaryIsHandledInWestOfUtc() {
        val tz = "America/Los_Angeles"
        // Неделя 26.10 – 01.11 в локальном времени, а не UTC
        assertEquals(
            mondayOf(tz, 2026, Calendar.OCTOBER, 26),
            WeekUtils.startOfWeek(at(tz, 2026, Calendar.NOVEMBER, 1, 16, 0), TimeZone.getTimeZone(tz))
        )
    }

    @Test
    fun dstTransitionWeekStillStartsOnMondayMidnight() {
        val tz = "Europe/Berlin"
        // Неделя с переходом на зимнее время (25.10.2026 — воскресенье):
        // понедельник этой недели — 19.10, должен быть ровно 00:00 локального
        val monday = mondayOf(tz, 2026, Calendar.OCTOBER, 19)
        assertEquals(monday, WeekUtils.startOfWeek(at(tz, 2026, Calendar.OCTOBER, 21, 15, 30), TimeZone.getTimeZone(tz)))

        val local = GregorianCalendar(TimeZone.getTimeZone(tz)).apply { timeInMillis = monday }
        assertEquals(0, local.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, local.get(Calendar.MINUTE))
        assertEquals(0, local.get(Calendar.SECOND))
        assertEquals(0, local.get(Calendar.MILLISECOND))
    }

    @Test
    fun nextWeekStartIsExactlySevenDaysLater() {
        val tz = TimeZone.getTimeZone("Europe/Moscow")
        val week1 = WeekUtils.startOfWeek(at("Europe/Moscow", 2026, Calendar.NOVEMBER, 4, 10, 0), tz)
        val week2 = WeekUtils.startOfWeek(week1 + WeekUtils.WEEK_MILLIS, tz)
        // Москва без перехода на летнее время: неделя ровно 7 дней
        assertEquals(week1 + WeekUtils.WEEK_MILLIS, week2)
    }
}
