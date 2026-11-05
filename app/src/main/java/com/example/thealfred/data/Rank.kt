package com.example.thealfred.data

/**
 * Единственный источник порогов рангов. Используется и для вычисления ранга по XP
 * (TaskRepository), и для отображения прогресса (ui.utils.RankUtils).
 */
object VigilanteRanks {

    /** Ранги по возрастанию: буква ранга и XP, с которого этот ранг начинается. */
    val ranks = listOf(
        "D" to 0,
        "C" to 500,
        "B" to 1500,
        "A" to 3000,
        "S" to 5000
    )

    /** Вычислить ранг по количеству XP. Отрицательный XP (после отмены задач) остаётся рангом D. */
    fun determineRank(xp: Int): String {
        return ranks.lastOrNull { xp >= it.second }?.first ?: "D"
    }
}
