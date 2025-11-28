package com.example.thealfred.data

import java.time.LocalDate

/**
 * Data class для хранения данных для графика продуктивности.
 * Хранит дату и суммарное количество XP, заработанное в этот день.
 */
data class DailyProductivity(
    val date: LocalDate,
    val totalXp: Int
)
