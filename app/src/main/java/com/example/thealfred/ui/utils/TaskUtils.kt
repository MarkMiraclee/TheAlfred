package com.example.thealfred.ui.utils

import com.example.thealfred.data.Task
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

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
}
