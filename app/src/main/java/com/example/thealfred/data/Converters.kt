package com.example.thealfred.data

import androidx.room.TypeConverter

// Класс для преобразования Enum'ов в String и обратно
class Converters {

    // --- ThreatLevel Converter ---
    @TypeConverter
    fun fromThreatLevel(value: ThreatLevel): String {
        return value.name // Храним Enum как его имя (строка)
    }

    @TypeConverter
    fun toThreatLevel(value: String): ThreatLevel {
        return ThreatLevel.valueOf(value) // Преобразуем строку обратно в Enum
    }

    // --- TaskCategory Converter ---
    @TypeConverter
    fun fromTaskCategory(value: TaskCategory): String {
        return value.name
    }

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory {
        return TaskCategory.valueOf(value)
    }
}
