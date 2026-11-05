package com.example.thealfred.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Порядок по угрозам задаём явно: enum хранится строкой, и лексикографический
    // ORDER BY ставил бы MINOR_THREAT выше CRITICAL_THREAT
    @Query(
        """
        SELECT * FROM tasks WHERE isCompleted = 0
        ORDER BY CASE threatLevel
            WHEN 'CRITICAL_THREAT' THEN 0
            WHEN 'MEDIUM_THREAT' THEN 1
            ELSE 2
        END, createdAt ASC
        """
    )
    fun getActiveTasksSortedByPriority(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    // --- Прогресс ---
    @Query("SELECT * FROM progress WHERE id = 1")
    fun getProgress(): Flow<Progress?>

    // Немедленный (не Flow) запрос для чтения прогресса внутри транзакции
    @Query("SELECT * FROM progress WHERE id = 1")
    suspend fun getProgressOnce(): Progress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: Progress)

    // --- Утренний ритуал ---
    @Query("SELECT COUNT(*) FROM tasks WHERE isMorningRitual = 1 AND completedAt >= :startOfDay")
    suspend fun countTodayMorningRituals(startOfDay: Long): Int

    // --- Аналитика ---

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    fun getTotalCompletedCount(): Flow<Int>

    // Пропущенные дедлайны: задачи, закрытые позже дедлайна,
    // плюс активные, чей дедлайн уже прошёл. Просто констатация, без штрафов.
    @Query(
        "SELECT COUNT(*) FROM tasks WHERE dueDate IS NOT NULL AND (" +
            "(isCompleted = 1 AND completedAt > dueDate) OR " +
            "(isCompleted = 0 AND dueDate < strftime('%s','now') * 1000))"
    )
    fun getMissedDeadlinesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND completedAt >= :from AND completedAt < :to")
    suspend fun countCompletedBetween(from: Long, to: Long): Int
}
