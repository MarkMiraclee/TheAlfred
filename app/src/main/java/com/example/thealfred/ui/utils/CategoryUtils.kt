package com.example.thealfred.ui.utils

import androidx.compose.ui.graphics.Color
import com.example.thealfred.data.TaskCategory
import com.example.thealfred.ui.theme.*

/**
 * Утилиты для работы с категориями задач
 */
object CategoryUtils {
    
    /**
     * Получить цвет категории
     */
    fun getCategoryColor(category: TaskCategory): Color {
        return when (category) {
            TaskCategory.OPERATIONAL_MISSIONS -> OperationalBlue
            TaskCategory.PHYSICAL_CONDITIONING -> ConditioningGreen
            TaskCategory.INTELLIGENCE_AND_STRATEGY -> IntelligenceYellow
            TaskCategory.WAYNE_PERSONA_MAINTENANCE -> PersonaPurple
        }
    }
    
    /**
     * Получить название категории для отображения
     */
    fun getCategoryDisplayName(category: TaskCategory): String {
        return when (category) {
            TaskCategory.OPERATIONAL_MISSIONS -> "ОПЕРАЦИОННЫЕ МИССИИ"
            TaskCategory.PHYSICAL_CONDITIONING -> "ФИЗИЧЕСКОЕ КОНДИЦИОНИРОВАНИЕ"
            TaskCategory.INTELLIGENCE_AND_STRATEGY -> "ИНТЕЛЛЕКТ & СТРАТЕГИЯ"
            TaskCategory.WAYNE_PERSONA_MAINTENANCE -> "ПОДДЕРЖАНИЕ ПЕРСОНЫ УЭЙНА"
        }
    }
    
    /**
     * Получить короткое название категории
     */
    fun getCategoryShortName(category: TaskCategory): String {
        return when (category) {
            TaskCategory.OPERATIONAL_MISSIONS -> "МИССИИ"
            TaskCategory.PHYSICAL_CONDITIONING -> "КОНДИЦИОНИРОВАНИЕ"
            TaskCategory.INTELLIGENCE_AND_STRATEGY -> "ИНТЕЛЛЕКТ"
            TaskCategory.WAYNE_PERSONA_MAINTENANCE -> "ПЕРСОНА"
        }
    }
}

