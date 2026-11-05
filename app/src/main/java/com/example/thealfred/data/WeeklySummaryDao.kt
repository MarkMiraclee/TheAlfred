package com.example.thealfred.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklySummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklySummary(summary: WeeklySummary)

    @Query("SELECT * FROM weekly_summaries ORDER BY weekStart DESC")
    fun getWeeklySummaries(): Flow<List<WeeklySummary>>
}
