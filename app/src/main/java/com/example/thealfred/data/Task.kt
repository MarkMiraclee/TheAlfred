package com.example.thealfred.data

import androidx.room.Entity
import androidx.room.Ignore
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

    val createdAt: Long = System.currentTimeMillis(),
    
    // Дата дедлайна (для системы срочности)
    val dueDate: Long? = null,
    
    // Дата завершения (для аналитики)
    val completedAt: Long? = null
) {
    // Теперь XP всегда вычисляются на основе уровня угрозы
    val xpValue: Int
        @Ignore // Говорим Room не пытаться сохранить это поле в БД
        get() = threatLevel.xpValue
}