package com.example.thealfred.ui.utils

import com.example.thealfred.data.Progress
import com.example.thealfred.data.VigilanteRanks

object RankUtils {

    /** XP, с которого начинается следующий ранг. Для максимального (или неизвестного) ранга — Int.MAX_VALUE. */
    fun getNextRankThreshold(currentRank: String): Int {
        val index = VigilanteRanks.ranks.indexOfFirst { it.first == currentRank }
        if (index < 0) return Int.MAX_VALUE
        return VigilanteRanks.ranks.getOrNull(index + 1)?.second ?: Int.MAX_VALUE
    }

    /** Прогресс внутри текущего ранга от 0f до 1f. На максимальном (или неизвестном) ранге — 0f/1f. */
    fun getProgressToNextRank(progress: Progress): Float {
        val index = VigilanteRanks.ranks.indexOfFirst { it.first == progress.currentRank }
        if (index < 0) return 0f

        val rankStart = VigilanteRanks.ranks[index].second
        val nextRankStart = VigilanteRanks.ranks.getOrNull(index + 1)?.second ?: return 1f

        val xpInRank = (progress.totalVigilancePoints - rankStart).toFloat()
        val rankXpRange = (nextRankStart - rankStart).toFloat()
        return (xpInRank / rankXpRange).coerceIn(0f, 1f)
    }
}
