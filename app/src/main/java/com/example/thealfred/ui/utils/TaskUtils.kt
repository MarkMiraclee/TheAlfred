package com.example.thealfred.ui.utils

import com.example.thealfred.data.Task
import java.util.concurrent.TimeUnit

/**
 * Утилиты для работы с задачами
 */
object TaskUtils {
    
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
