package com.example.thealfred.ui.utils

import com.example.thealfred.data.Progress

object RankUtils {

    private val rankThresholds = mapOf(
        "D" to 500,
        "C" to 1500,
        "B" to 3000,
        "A" to 5000,
        "S" to Int.MAX_VALUE // S-ранг - максимальный
    )

    fun getNextRankThreshold(currentRank: String?): Int {
        return if (currentRank == null) 0 else rankThresholds[currentRank] ?: Int.MAX_VALUE
    }

    fun getProgressToNextRank(progress: Progress): Float {
        val currentRank = progress.currentRank
        val nextRankThreshold = getNextRankThreshold(currentRank)
        
        val previousRankThreshold = rankThresholds.entries
            .firstOrNull { it.value == nextRankThreshold }?.key?.let { key ->
                rankThresholds.filterKeys { it < key }.values.maxOrNull() 
            } ?: 0

        val rankXpRange = (nextRankThreshold - previousRankThreshold).toFloat()
        val userXpInRank = (progress.totalVigilancePoints - previousRankThreshold).toFloat()

        return if (rankXpRange > 0) userXpInRank / rankXpRange else 0f
    }
}
