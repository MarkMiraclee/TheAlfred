package com.example.thealfred.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar

class TaskRepository(private val db: AppDatabase, private val templateDao: TemplateDao) {

    private val taskDao = db.taskDao()

    val activeTasksSortedByPriority: Flow<List<Task>> = taskDao.getActiveTasksSortedByPriority()
    val completedTasks: Flow<List<Task>> = taskDao.getCompletedTasks()
    val userProgress: Flow<Progress> = taskDao.getProgress().map { progress ->
        progress ?: Progress()
    }

    val allTemplates: Flow<List<Template>> = templateDao.getAllTemplates()

    /**
     * Чтение прогресса и запись обновлённой задачи выполняются в одной транзакции:
     * два быстрых переключения статуса не смогут перезаписать XP друг друга.
     */
    suspend fun updateTaskStatus(task: Task, isCompleted: Boolean) {
        db.withTransaction {
            val currentProgress = taskDao.getProgressOnce() ?: Progress()

            val updatedTask = task.copy(
                isCompleted = isCompleted,
                completedAt = when {
                    isCompleted && !task.isCompleted -> System.currentTimeMillis()
                    // Отмена выполнения сбрасывает дату завершения
                    !isCompleted -> null
                    else -> task.completedAt
                }
            )
            taskDao.updateTask(updatedTask)

            val xpChange = when {
                isCompleted && !task.isCompleted -> task.xpValue
                !isCompleted && task.isCompleted -> -task.xpValue
                else -> 0
            }

            if (xpChange != 0) {
                val newTotalXP = currentProgress.totalVigilancePoints + xpChange

                val updatedProgress = currentProgress.copy(
                    totalVigilancePoints = newTotalXP,
                    currentRank = VigilanteRanks.determineRank(newTotalXP),
                    operationalMissionsXp = currentProgress.operationalMissionsXp +
                        if (task.category == TaskCategory.OPERATIONAL_MISSIONS) xpChange else 0,
                    physicalConditioningXp = currentProgress.physicalConditioningXp +
                        if (task.category == TaskCategory.PHYSICAL_CONDITIONING) xpChange else 0,
                    intelligenceAndStrategyXp = currentProgress.intelligenceAndStrategyXp +
                        if (task.category == TaskCategory.INTELLIGENCE_AND_STRATEGY) xpChange else 0,
                    waynePersonaMaintenanceXp = currentProgress.waynePersonaMaintenanceXp +
                        if (task.category == TaskCategory.WAYNE_PERSONA_MAINTENANCE) xpChange else 0,
                    lastUpdated = System.currentTimeMillis()
                )

                taskDao.insertProgress(updatedProgress)
            }
        }
    }

    suspend fun completeMorningRitual() {
        db.withTransaction {
            val currentProgress = taskDao.getProgressOnce() ?: Progress()

            val ritualTask = Task(
                title = "50 Push-ups: Readiness Check",
                category = TaskCategory.PHYSICAL_CONDITIONING,
                threatLevel = ThreatLevel.MINOR_THREAT,
                isCompleted = true, // Сразу создаем как выполненную
                isMorningRitual = true,
                completedAt = System.currentTimeMillis()
            )
            taskDao.insertTask(ritualTask)

            val xpChange = ritualTask.xpValue
            val newTotalXP = currentProgress.totalVigilancePoints + xpChange

            val updatedProgress = currentProgress.copy(
                totalVigilancePoints = newTotalXP,
                currentRank = VigilanteRanks.determineRank(newTotalXP),
                physicalConditioningXp = currentProgress.physicalConditioningXp + xpChange,
                lastUpdated = System.currentTimeMillis()
            )

            taskDao.insertProgress(updatedProgress)
        }
    }

    suspend fun insertTask(task: Task) {
        val insertedId = taskDao.insertTask(task)
        // IGNORE-стратегия возвращает -1 при конфликте — сообщаем о сбое явно
        require(insertedId != -1L) { "Failed to insert task '${task.title}'" }
    }

    suspend fun deleteTask(taskId: Int) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun isRitualCompletedToday(): Boolean {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return taskDao.countTodayMorningRituals(startOfDay) > 0
    }

    suspend fun insertTemplate(template: Template) {
        val insertedId = templateDao.insertTemplate(template)
        require(insertedId != -1L) { "Failed to insert template '${template.title}'" }
    }

    suspend fun deleteTemplate(templateId: Int) {
        templateDao.deleteTemplateById(templateId)
    }

    suspend fun createTaskFromTemplate(template: Template) {
        val newTask = Task(
            title = template.title,
            category = template.category,
            threatLevel = template.threatLevel
        )
        insertTask(newTask)
    }

    val criticalThreatsCompletionRate: Flow<Float> = combine(
        taskDao.getTotalCriticalTasksInLastWeek(),
        taskDao.getCompletedCriticalTasksInLastWeek()
    ) { total, completed ->
        if (total > 0) completed.toFloat() / total.toFloat() else 0f
    }

    // Время от создания задач до их закрытия (не фактическое время работы)
    val timeToCompletePerCategory: Flow<List<CategoryDuration>> = taskDao.getTimeToCompletePerCategory()

    val weeklyProductivity: Flow<List<DailyProductivity>> =
        taskDao.getCompletedTasksInLastWeek().map { tasks ->
            tasks.groupBy {
                Instant.ofEpochMilli(it.completedAt!!).atZone(ZoneId.systemDefault()).toLocalDate()
            }.map { (date, tasksOnDate) ->
                DailyProductivity(date, tasksOnDate.sumOf { it.xpValue })
            }.sortedBy { it.date }
        }
}
