package com.example.thealfred.data

import java.util.Calendar
import java.util.TimeZone

/**
 * Утилиты недельного цикла. Неделя в приложении начинается в понедельник 00:00
 * по локальному времени — в этот момент сбрасывается XP и ранг.
 */
object WeekUtils {

    const val WEEK_MILLIS: Long = 7L * 24 * 60 * 60 * 1000

    /**
     * Начало недели (понедельник 00:00:00.000) для момента timeMillis
     * в указанной таймзоне. Календарные поля выставляются после сдвига,
     * поэтому переходы на летнее/зимнее время обрабатываются корректно.
     */
    fun startOfWeek(timeMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val cal = Calendar.getInstance(timeZone).apply { timeInMillis = timeMillis }
        // Calendar.DAY_OF_WEEK: SUNDAY=1, MONDAY=2, ..., SATURDAY=7;
        // приводим к «дней от понедельника»: ПН=0 ... ВС=6
        val daysSinceMonday = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        cal.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
