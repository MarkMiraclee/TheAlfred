package com.example.thealfred.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// TaskRepository получает DAO и предоставляет методы для управления данными.
// Он будет Singleton, то есть будет существовать в единственном экземпляре.
class TaskRepository(private val taskDao: TaskDao) {

    // --- 1. ПЕРЕМЕННЫЕ ДЛЯ ЧТЕНИЯ ДАННЫХ ---

    // Получает все задачи, отсортированные по дате создания.
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    // Flow, который возвращает только НЕЗАВЕРШЕННЫЕ задачи
    val activeTasks: Flow<List<Task>> = taskDao.getAllTasks()
        .map { tasks -> tasks.filter { !it.isCompleted } }

    // Реактивное получение текущего прогресса (XP, Ранг)
    val userProgress: Flow<Progress> = taskDao.getProgress().map { it ?: Progress() }

    // --- 2. ФУНКЦИИ УПРАВЛЕНИЯ ЗАДАЧАМИ (CRUD) ---

    // Вставка новой задачи.
    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    // Удаление задачи по ID
    suspend fun deleteTask(taskId: Int) {
        taskDao.deleteTaskById(taskId)
    }

    // --- 3. ЛОГИКА: ОБНОВЛЕНИЕ СТАТУСА И НАЧИСЛЕНИЕ XP ---

    /**
     * Обновляет статус задачи (выполнена/не выполнена) и начисляет/вычитает XP.
     * * @param task Задача для обновления.
     * @param isCompleted Новый статус выполнения.
     * @param currentProgress Текущий объект Progress пользователя (XP, Ранг).
     */
    suspend fun updateTaskStatus(task: Task, isCompleted: Boolean, currentProgress: Progress) {

        // 1. Обновляем статус задачи в базе данных
        val updatedTask = task.copy(isCompleted = isCompleted)
        taskDao.updateTask(updatedTask)

        // 2. Рассчитываем изменение XP
        // Проверяем: если задача уже была завершена (чтобы избежать двойного начисления),
        // или если статус не изменился, не начисляем XP.
        val xpChange = if (isCompleted && !task.isCompleted) {
            task.xpValue // Если выполнена впервые, добавляем XP
        } else if (!isCompleted && task.isCompleted) {
            -task.xpValue // Если отменена (снова активна), вычитаем XP
        } else {
            0 // Нет изменения статуса, нет изменения XP
        }

        // 3. Обновляем общий прогресс, только если XP изменились
        if (xpChange != 0) {
            val newTotalXP = currentProgress.totalVigilancePoints + xpChange
            val newRank = determineRank(newTotalXP)

            val updatedProgress = currentProgress.copy(
                totalVigilancePoints = newTotalXP,
                currentRank = newRank,
                lastUpdated = System.currentTimeMillis()
            )

            taskDao.insertProgress(updatedProgress)
        }
    }


    // --- 4. ФУНКЦИЯ ДЛЯ УТРЕННЕГО РИТУАЛА (MVP) ---

    /**
     * Создает и немедленно завершает задачу "Утренний Ритуал" и обновляет прогресс.
     * @param currentProgress Текущий объект Progress пользователя (XP, Ранг).
     */
    suspend fun completeMorningRitual(currentProgress: Progress) {
        val ritualTask = Task(
            title = "50 Отжиманий: Проверка Готовности",
            category = TaskCategory.PHYSICAL_CONDITIONING,
            threatLevel = ThreatLevel.MINOR_THREAT,
            isCompleted = true, // Сразу выполнена
        )
        // Вставляем задачу
        taskDao.insertTask(ritualTask)

        // Обновляем прогресс (простое начисление XP)
        val newTotalXP = currentProgress.totalVigilancePoints + ritualTask.xpValue
        val newRank = determineRank(newTotalXP)

        val updatedProgress = currentProgress.copy(
            totalVigilancePoints = newTotalXP,
            currentRank = newRank,
            lastUpdated = System.currentTimeMillis()
        )
        taskDao.insertProgress(updatedProgress)
    }

    // --- 5. ЛОГИКА ОПРЕДЕЛЕНИЯ РАНГА ---

    /**
     * Определяет текущий ранг Бэтмена на основе набранных XP.
     */
    private fun determineRank(xp: Int): String {
        return when {
            xp < 500 -> "D"
            xp < 1500 -> "C"
            xp < 3000 -> "B"
            xp < 5000 -> "A"
            else -> "S" // 5000+ XP
        }
    }
}