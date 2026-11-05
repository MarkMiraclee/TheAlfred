package com.example.thealfred.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Уровень XP и Ранг Бэтмена, а также прогресс по категориям
@Entity(tableName = "progress")
data class Progress(
    @PrimaryKey
    val id: Int = 1,

    // --- Общий прогресс ---
    val totalVigilancePoints: Int = 0,
    val currentRank: String = "D",

    // --- Прогресс по категориям (Локальные прогресс-бары) ---
    val operationalMissionsXp: Int = 0,
    val physicalConditioningXp: Int = 0,
    val intelligenceAndStrategyXp: Int = 0,
    val waynePersonaMaintenanceXp: Int = 0,

    val lastUpdated: Long = System.currentTimeMillis(),

    // Понедельник 00:00 локального времени текущей недели.
    // 0 = ещё не инициализировано (первый запуск после установки/обновления):
    // тогда неделя фиксируется без сброса уже накопленного XP.
    val weekStart: Long = 0
)
