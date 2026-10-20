package com.example.thealfred.ui.utils

import com.example.thealfred.data.Progress
import org.junit.Assert.assertEquals
import org.junit.Test

class RankUtilsTest {

    private fun progress(rank: String, xp: Int) =
        Progress(totalVigilancePoints = xp, currentRank = rank)

    @Test
    fun progressInsideEachRank() {
        assertEquals(0.5f, RankUtils.getProgressToNextRank(progress("D", 250)), 0.0001f)
        assertEquals(0.5f, RankUtils.getProgressToNextRank(progress("C", 1000)), 0.0001f)
        assertEquals(1f / 3f, RankUtils.getProgressToNextRank(progress("B", 2000)), 0.0001f)
        assertEquals(0.5f, RankUtils.getProgressToNextRank(progress("A", 4000)), 0.0001f)
    }

    @Test
    fun progressAtRankStartIsZero() {
        assertEquals(0f, RankUtils.getProgressToNextRank(progress("D", 0)), 0.0001f)
        assertEquals(0f, RankUtils.getProgressToNextRank(progress("C", 500)), 0.0001f)
        assertEquals(0f, RankUtils.getProgressToNextRank(progress("B", 1500)), 0.0001f)
        assertEquals(0f, RankUtils.getProgressToNextRank(progress("A", 3000)), 0.0001f)
    }

    @Test
    fun maxRankIsAlwaysFull() {
        assertEquals(1f, RankUtils.getProgressToNextRank(progress("S", 5000)), 0.0001f)
        assertEquals(1f, RankUtils.getProgressToNextRank(progress("S", 12000)), 0.0001f)
    }

    @Test
    fun progressIsClampedToZeroAndOne() {
        // Отрицательный XP (после отмены выполненных задач) и XP выше порога не должны ломать индикатор
        assertEquals(0f, RankUtils.getProgressToNextRank(progress("D", -50)), 0.0001f)
        assertEquals(1f, RankUtils.getProgressToNextRank(progress("D", 900)), 0.0001f)
    }

    @Test
    fun unknownRankHasNoProgress() {
        assertEquals(0f, RankUtils.getProgressToNextRank(progress("X", 100)), 0.0001f)
        assertEquals(Int.MAX_VALUE, RankUtils.getNextRankThreshold("X"))
    }

    @Test
    fun nextRankThresholds() {
        assertEquals(500, RankUtils.getNextRankThreshold("D"))
        assertEquals(1500, RankUtils.getNextRankThreshold("C"))
        assertEquals(3000, RankUtils.getNextRankThreshold("B"))
        assertEquals(5000, RankUtils.getNextRankThreshold("A"))
        assertEquals(Int.MAX_VALUE, RankUtils.getNextRankThreshold("S"))
    }
}
