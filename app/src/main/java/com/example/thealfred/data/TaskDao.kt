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
    // Окно «последние 7 дней» вычисляем прямо в SQL через strftime('now'):
    // так отсечка пересчитывается при каждом исполнении запроса, а не
    // замораживается на момент создания Flow.

    @Query(
        "SELECT COUNT(*) FROM tasks WHERE threatLevel = 'CRITICAL_THREAT' " +
            "AND createdAt >= strftime('%s','now') * 1000 - 604800000"
    )
    fun getTotalCriticalTasksInLastWeek(): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM tasks WHERE threatLevel = 'CRITICAL_THREAT' AND isCompleted = 1 " +
            "AND completedAt >= strftime('%s','now') * 1000 - 604800000"
    )
    fun getCompletedCriticalTasksInLastWeek(): Flow<Int>

    // Это время от создания задачи до её закрытия (completedAt - createdAt),
    // а не фактически затраченное на задачу время
    @Query(
        "SELECT category, SUM(completedAt - createdAt) as totalDuration FROM tasks " +
            "WHERE isCompleted = 1 AND completedAt IS NOT NULL GROUP BY category"
    )
    fun getTimeToCompletePerCategory(): Flow<List<CategoryDuration>>

    @Query(
        "SELECT * FROM tasks WHERE isCompleted = 1 " +
            "AND completedAt >= strftime('%s','now') * 1000 - 604800000"
    )
    fun getCompletedTasksInLastWeek(): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND completedAt >= :from AND completedAt < :to")
    suspend fun countCompletedBetween(from: Long, to: Long): Int
}
