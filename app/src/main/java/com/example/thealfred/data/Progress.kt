package com.example.thealfred.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
data class Progress(
    @PrimaryKey
    val id: Int = 1, // Всегда один объект прогресса
    val totalVigilancePoints: Int = 0,
    val currentRank: String = "D", // Ранг по умолчанию "D"
    val lastUpdated: Long = 0L,

    // XP по категориям
    val operationalMissionsXp: Int = 0,
    val physicalConditioningXp: Int = 0,
    val intelligenceAndStrategyXp: Int = 0,
    val waynePersonaMaintenanceXp: Int = 0
)