package com.example.thealfred.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: TaskCategory,
    val threatLevel: ThreatLevel
)