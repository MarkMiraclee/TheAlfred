package com.example.thealfred.ui.utils

import com.example.thealfred.data.Task
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Утилиты для работы с задачами
 */
object TaskUtils {

    /**
     * Material3 DatePicker отдаёт выбранную дату как полночь по UTC. Если сохранить это значение как есть,
     * дедлайн наступит в 00:00 UTC (в Москве это 03:00 выбранного дня), а в поясах западнее UTC
     * выбранная дата покажется на день раньше.
     *
     * Берём календарный день, который выбрал пользователь (он записан в UTC),
     * и возвращаем конец этого дня (23:59:59.999) по локальному времени.
     */
    fun pickerDateToEndOfLocalDay(
        pickerUtcMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        val picked = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply { timeInMillis = pickerUtcMillis }
        return GregorianCalendar(timeZone).apply {
            clear()
            set(
                picked.get(Calendar.YEAR),
                picked.get(Calendar.MONTH),
                picked.get(Calendar.DAY_OF_MONTH),
                23, 59, 59
            )
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * Проверить, просрочена ли задача
     */
    fun isOverdue(task: Task): Boolean {
        if (task.isCompleted || task.dueDate == null) return false
        return task.dueDate < System.currentTimeMillis()
    }
    
    /**
     * Проверить, скоро ли истечет дедлайн (менее 24 часов)
     */
    fun isDueSoon(task: Task): Boolean {
        if (task.isCompleted || task.dueDate == null) return false
        val hoursUntilDue = TimeUnit.MILLISECONDS.toHours(task.dueDate - System.currentTimeMillis())
        return hoursUntilDue in 1..24
    }
    
    /**
     * Получить количество часов до дедлайна
     */
    fun getHoursUntilDue(task: Task): Long? {
        if (task.dueDate == null) return null
        return TimeUnit.MILLISECONDS.toHours(task.dueDate - System.currentTimeMillis())
    }
    
    /**
     * Получить строку с временем до дедлайна
     */
    fun getDueDateString(task: Task): String? {
        if (task.dueDate == null) return null
        
        val hoursUntilDue = getHoursUntilDue(task) ?: return null
        
        return when {
            hoursUntilDue < 0 -> "OVERDUE"
            hoursUntilDue < 1 -> "< 1 HOUR"
            hoursUntilDue < 24 -> "$hoursUntilDue HOURS"
            else -> {
                val days = hoursUntilDue / 24
                "$days DAYS"
            }
        }
    }
}
