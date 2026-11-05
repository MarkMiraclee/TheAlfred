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

    val isCompleted: Boolean = false,

    // Флаг утреннего ритуала (Readiness Check) — используется аналитикой,
    // поэтому ритуал опознаётся не по названию задачи
    val isMorningRitual: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),

    // Дата дедлайна (для системы срочности)
    val dueDate: Long? = null,

    // Дата завершения (для аналитики)
    val completedAt: Long? = null
) {
    // Вычисляемое свойство без backing field — Room его не сохраняет
    val xpValue: Int
        get() = threatLevel.xpValue
}
