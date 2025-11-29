package com.example.thealfred.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class TaskRepository(private val taskDao: TaskDao, private val templateDao: TemplateDao) {

    // --- 1. ПЕРЕМЕННЫЕ ДЛЯ ЧТЕНИЯ ДАННЫХ (ЗАДАЧИ) ---

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val activeTasks: Flow<List<Task>> = taskDao.getAllTasks().map { tasks -> tasks.filter { !it.isCompleted } }
    val userProgress: Flow<Progress> = taskDao.getProgress().map { it ?: Progress() }
    val showReadinessCheck: Flow<Boolean> = allTasks.map { tasks ->
        val today = Calendar.getInstance()
        val startOfToday = today.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        tasks.none {
            it.title == "50 Отжиманий: Проверка Готовности" && it.createdAt >= startOfToday
        }
    }

    // --- 2. ПЕРЕМЕННЫЕ ДЛЯ ЧТЕНИЯ ДАННЫХ (ШАБЛОНЫ) ---

    val allTemplates: Flow<List<Template>> = templateDao.getAllTemplates()

    // --- 3. ФУНКЦИИ УПРАВЛЕНИЯ ЗАДАЧАМИ (CRUD) ---

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun deleteTask(taskId: Int) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun createTaskFromTemplate(template: Template) {
        val task = Task(
            title = template.title,
            category = template.category,
            threatLevel = template.threatLevel
        )
        taskDao.insertTask(task)
    }

    // --- 4. ФУНКЦИИ УПРАВЛЕНИЯ ШАБЛОНАМИ (CRUD) ---

    suspend fun insertTemplate(template: Template) {
        templateDao.insertTemplate(template)
    }

    suspend fun deleteTemplate(templateId: Int) {
        templateDao.deleteTemplateById(templateId)
    }

    // --- 5. ЛОГИКА: ОБНОВЛЕНИЕ СТАТУСА И НАЧИСЛЕНИЕ XP ---

    suspend fun updateTaskStatus(task: Task, isCompleted: Boolean, currentProgress: Progress) {
        val updatedTask = task.copy(isCompleted = isCompleted)
        taskDao.updateTask(updatedTask)

        val xpChange = if (isCompleted && !task.isCompleted) {
            task.xpValue
        } else if (!isCompleted && task.isCompleted) {
            -task.xpValue
        } else {
            0
        }

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

    // --- 6. ФУНКЦИЯ ДЛЯ УТРЕННЕГО РИТУАЛА ---

    suspend fun completeMorningRitual(currentProgress: Progress) {
        val ritualTask = Task(
            title = "50 Отжиманий: Проверка Готовности",
            category = TaskCategory.PHYSICAL_CONDITIONING,
            threatLevel = ThreatLevel.MINOR_THREAT,
            isCompleted = true,
        )
        taskDao.insertTask(ritualTask)

        val newTotalXP = currentProgress.totalVigilancePoints + ritualTask.xpValue
        val newRank = determineRank(newTotalXP)
        val updatedProgress = currentProgress.copy(
            totalVigilancePoints = newTotalXP,
            currentRank = newRank,
            lastUpdated = System.currentTimeMillis()
        )
        taskDao.insertProgress(updatedProgress)
    }

    // --- 7. ЛОГИКА ОПРЕДЕЛЕНИЯ РАНГА ---

    private fun determineRank(xp: Int): String {
        return when {
            xp < 500 -> "D"
            xp < 1500 -> "C"
            xp < 3000 -> "B"
            xp < 5000 -> "A"
            else -> "S"
        }
    }
}