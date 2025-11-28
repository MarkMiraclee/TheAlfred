package com.example.thealfred.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow // Используем Flow для реактивных данных

@Dao
interface TaskDao {

    // 1. ПОЛУЧЕНИЕ ВСЕХ ЗАДАЧ
    // Flow позволяет UI автоматически обновляться при добавлении новых задач
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    // 2. ПОЛУЧЕНИЕ ЗАДАЧ ПО КАТЕГОРИИ
    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY createdAt DESC")
    fun getTasksByCategory(category: TaskCategory): Flow<List<Task>>

    // 3. ДОБАВЛЕНИЕ НОВОЙ ЗАДАЧИ
    // OnConflictStrategy.IGNORE означает, что если ID уже есть, мы игнорируем вставку
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)

    // 4. ОБНОВЛЕНИЕ ЗАДАЧИ
    // Используется для отметки isCompleted = true
    @Update
    suspend fun updateTask(task: Task)

    // 5. УДАЛЕНИЕ ЗАДАЧИ
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    // 6. ПОЛУЧЕНИЕ ПРОГРЕССА
// Получаем только одну запись (id=1), Flow для реактивного обновления
    @Query("SELECT * FROM progress WHERE id = 1")
    fun getProgress(): Flow<Progress?>

    // 7. ВСТАВКА/ОБНОВЛЕНИЕ ПРОГРЕССА
// OnConflictStrategy.REPLACE гарантирует, что мы заменим существующую (единственную) строку
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: Progress)
}