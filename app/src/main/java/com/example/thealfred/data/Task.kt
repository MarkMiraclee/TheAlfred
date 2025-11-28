package com.example.thealfred.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Основная модель данных для задачи
@Entity(tableName = "tasks")
data class Task(
    // ID задачи. autoGenerate = true позволяет Room автоматически присваивать ID.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    // Используем Enum
    val category: TaskCategory,

    // Используем Enum
    val threatLevel: ThreatLevel,

    // Значение XP (хранится в БД, но по умолчанию берется из threatLevel)
    val xpValue: Int = 0,

    val isCompleted: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    
    // Дата дедлайна (для системы срочности)
    val dueDate: Long? = null,
    
    // Дата завершения (для аналитики)
    val completedAt: Long? = null
) {
    // Вычисляемое свойство для получения реального значения XP
    val effectiveXpValue: Int
        get() = if (xpValue > 0) xpValue else threatLevel.xpValue
}
