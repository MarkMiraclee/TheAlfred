package com.example.thealfred.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY threatLevel DESC, createdAt ASC")
    fun getActiveTasksSortedByPriority(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE threatLevel = 'CRITICAL_THREAT' AND isCompleted = 0")
    fun getCriticalTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL AND dueDate < :currentTime AND isCompleted = 0")
    fun getOverdueTasks(currentTime: Long = System.currentTimeMillis()): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    // --- Прогресс ---
    @Query("SELECT * FROM progress WHERE id = 1")
    fun getProgress(): Flow<Progress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: Progress)

    // --- Утренний ритуал ---
    @Query("SELECT COUNT(*) FROM tasks WHERE title = '50 Push-ups: Readiness Check' AND completedAt >= :startOfDay")
    suspend fun countTodayMorningRituals(startOfDay: Long): Int

    // --- Аналитика ---
    @Query("SELECT COUNT(*) FROM tasks WHERE threatLevel = 'CRITICAL_THREAT' AND createdAt >= :since")
    fun getTotalCriticalTasksInLastWeek(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE threatLevel = 'CRITICAL_THREAT' AND isCompleted = 1 AND completedAt >= :since")
    fun getCompletedCriticalTasksInLastWeek(since: Long): Flow<Int>

    // ИСПРАВЛЕНО: `duration` переименовано в `totalDuration`
    @Query("SELECT category, SUM(completedAt - createdAt) as totalDuration FROM tasks WHERE isCompleted = 1 AND completedAt IS NOT NULL GROUP BY category")
    fun getTimeSpentPerCategory(): Flow<List<CategoryDuration>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND completedAt >= :since")
    fun getCompletedTasksSince(since: Long): Flow<List<Task>>
}