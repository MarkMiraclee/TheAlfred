package com.example.thealfred.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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

    // Значение XP берется из ThreatLevel
    val xpValue: Int = threatLevel.xpValue,

    val isCompleted: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)