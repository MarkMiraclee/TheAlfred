package com.example.thealfred.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Итог одной завершённой недели. Записывается в момент недельного сброса,
 * чтобы журнал хранил статистику за все недели использования приложения.
 * Недели, в которые приложение не открывалось, сохраняются нулевыми записями.
 */
@Entity(
    tableName = "weekly_summaries",
    indices = [Index(value = ["weekStart"], unique = true)]
)
data class WeeklySummary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Понедельник 00:00 локального времени — начало недели
    val weekStart: Long,

    // Итог недели на момент сброса
    val totalXp: Int,
    val currentRank: String,
    val operationalMissionsXp: Int,
    val physicalConditioningXp: Int,
    val intelligenceAndStrategyXp: Int,
    val waynePersonaMaintenanceXp: Int,
    val tasksCompleted: Int,

    val savedAt: Long = System.currentTimeMillis()
)
