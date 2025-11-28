package com.example.thealfred.ui.utils

import androidx.compose.ui.graphics.Color
import com.example.thealfred.data.ThreatLevel
import com.example.thealfred.ui.theme.*

/**
 * Утилиты для работы с уровнями угроз
 */
object ThreatLevelUtils {
    
    /**
     * Получить цвет уровня угрозы
     */
    fun getThreatLevelColor(threatLevel: ThreatLevel): Color {
        return when (threatLevel) {
            ThreatLevel.MINOR_THREAT -> MinorThreatColor
            ThreatLevel.MEDIUM_THREAT -> MediumThreatColor
            ThreatLevel.CRITICAL_THREAT -> CriticalThreatColor
        }
    }
    
    /**
     * Получить название уровня угрозы для отображения
     */
    fun getThreatLevelDisplayName(threatLevel: ThreatLevel): String {
        return when (threatLevel) {
            ThreatLevel.MINOR_THREAT -> "МЕЛКАЯ УГРОЗА"
            ThreatLevel.MEDIUM_THREAT -> "СРЕДНЯЯ УГРОЗА"
            ThreatLevel.CRITICAL_THREAT -> "КРИТИЧЕСКАЯ УГРОЗА"
        }
    }
    
    /**
     * Получить короткое название уровня угрозы
     */
    fun getThreatLevelShortName(threatLevel: ThreatLevel): String {
        return when (threatLevel) {
            ThreatLevel.MINOR_THREAT -> "МИНОР"
            ThreatLevel.MEDIUM_THREAT -> "СРЕДНЯЯ"
            ThreatLevel.CRITICAL_THREAT -> "КРИТИЧЕСКАЯ"
        }
    }
}

