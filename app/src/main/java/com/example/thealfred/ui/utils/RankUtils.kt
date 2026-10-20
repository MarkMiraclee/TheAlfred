package com.example.thealfred.ui.utils

import com.example.thealfred.data.Progress

object RankUtils {

    /**
     * Ранги по возрастанию: буква ранга и XP, с которого этот ранг начинается.
     * Должно совпадать с порогами в TaskRepository.determineRank().
     */
    private val ranks = listOf(
        "D" to 0,
        "C" to 500,
        "B" to 1500,
        "A" to 3000,
        "S" to 5000
    )

    /** XP, с которого начинается следующий ранг. Для максимального (или неизвестного) ранга — Int.MAX_VALUE. */
    fun getNextRankThreshold(currentRank: String): Int {
        val index = ranks.indexOfFirst { it.first == currentRank }
        if (index < 0) return Int.MAX_VALUE
        return ranks.getOrNull(index + 1)?.second ?: Int.MAX_VALUE
    }

    /** Прогресс внутри текущего ранга от 0f до 1f. На максимальном ранге всегда 1f. */
    fun getProgressToNextRank(progress: Progress): Float {
        val index = ranks.indexOfFirst { it.first == progress.currentRank }
        if (index < 0) return 0f

        val rankStart = ranks[index].second
        val nextRankStart = ranks.getOrNull(index + 1)?.second ?: return 1f

        val xpInRank = (progress.totalVigilancePoints - rankStart).toFloat()
        val rankXpRange = (nextRankStart - rankStart).toFloat()
        return (xpInRank / rankXpRange).coerceIn(0f, 1f)
    }
}
