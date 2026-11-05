package com.example.thealfred.ui.utils

import com.example.thealfred.data.Progress
import com.example.thealfred.data.VigilanteRanks
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

    @Test
    fun determineRankBoundaries() {
        // Границы порогов: 500 -> C, 1500 -> B, 3000 -> A, 5000 -> S
        assertEquals("D", VigilanteRanks.determineRank(0))
        assertEquals("D", VigilanteRanks.determineRank(499))
        assertEquals("C", VigilanteRanks.determineRank(500))
        assertEquals("C", VigilanteRanks.determineRank(1499))
        assertEquals("B", VigilanteRanks.determineRank(1500))
        assertEquals("B", VigilanteRanks.determineRank(2999))
        assertEquals("A", VigilanteRanks.determineRank(3000))
        assertEquals("A", VigilanteRanks.determineRank(4999))
        assertEquals("S", VigilanteRanks.determineRank(5000))
        assertEquals("S", VigilanteRanks.determineRank(12000))
    }

    @Test
    fun negativeXpStaysRankD() {
        // После отмены выполненных задач XP может уйти в минус
        assertEquals("D", VigilanteRanks.determineRank(-50))
    }
}
