package com.example.thealfred.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar

class TaskRepository(private val taskDao: TaskDao, private val templateDao: TemplateDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val activeTasks: Flow<List<Task>> = taskDao.getAllTasks().map { tasks -> tasks.filter { !it.isCompleted } }
    val criticalTasks: Flow<List<Task>> = taskDao.getCriticalTasks()
    val overdueTasks: Flow<List<Task>> = taskDao.getOverdueTasks()
    val activeTasksSortedByPriority: Flow<List<Task>> = taskDao.getActiveTasksSortedByPriority()
    val completedTasks: Flow<List<Task>> = taskDao.getCompletedTasks()
    val userProgress: Flow<Progress> = taskDao.getProgress().map { progress ->
        progress ?: Progress()
    }

    val allTemplates: Flow<List<Template>> = templateDao.getAllTemplates()

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun deleteTask(taskId: Int) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun updateTaskStatus(task: Task, isCompleted: Boolean, currentProgress: Progress) {
        val updatedTask = task.copy(
            isCompleted = isCompleted,
            completedAt = if (isCompleted && !task.isCompleted) System.currentTimeMillis() else task.completedAt
        )
        taskDao.updateTask(updatedTask)

        val xpChange = when {
            isCompleted && !task.isCompleted -> task.effectiveXpValue
            !isCompleted && task.isCompleted -> -task.effectiveXpValue
            else -> 0
        }

        if (xpChange != 0) {
            val newTotalXP = currentProgress.totalVigilancePoints + xpChange
            val newRank = determineRank(newTotalXP)

            val newOperationalMissionsXp = currentProgress.operationalMissionsXp + if (task.category == TaskCategory.OPERATIONAL_MISSIONS) xpChange else 0
            val newPhysicalConditioningXp = currentProgress.physicalConditioningXp + if (task.category == TaskCategory.PHYSICAL_CONDITIONING) xpChange else 0
            val newIntelligenceAndStrategyXp = currentProgress.intelligenceAndStrategyXp + if (task.category == TaskCategory.INTELLIGENCE_AND_STRATEGY) xpChange else 0
            val newWaynePersonaMaintenanceXp = currentProgress.waynePersonaMaintenanceXp + if (task.category == TaskCategory.WAYNE_PERSONA_MAINTENANCE) xpChange else 0

            val updatedProgress = currentProgress.copy(
                totalVigilancePoints = newTotalXP,
                currentRank = newRank,
                operationalMissionsXp = newOperationalMissionsXp,
                physicalConditioningXp = newPhysicalConditioningXp,
                intelligenceAndStrategyXp = newIntelligenceAndStrategyXp,
                waynePersonaMaintenanceXp = newWaynePersonaMaintenanceXp,
                lastUpdated = System.currentTimeMillis()
            )

            taskDao.insertProgress(updatedProgress)
        }
    }

    suspend fun completeMorningRitual(currentProgress: Progress) {
        val ritualTask = Task(
            title = "50 Push-ups: Readiness Check",
            category = TaskCategory.PHYSICAL_CONDITIONING,
            threatLevel = ThreatLevel.MINOR_THREAT,
            isCompleted = true, // Сразу создаем как выполненную
            completedAt = System.currentTimeMillis()
        )
        taskDao.insertTask(ritualTask)

        val xpChange = ritualTask.effectiveXpValue
        val newTotalXP = currentProgress.totalVigilancePoints + xpChange
        val newRank = determineRank(newTotalXP)
        val newPhysicalConditioningXp = currentProgress.physicalConditioningXp + xpChange

        val updatedProgress = currentProgress.copy(
            totalVigilancePoints = newTotalXP,
            currentRank = newRank,
            physicalConditioningXp = newPhysicalConditioningXp,
            lastUpdated = System.currentTimeMillis()
        )

        taskDao.insertProgress(updatedProgress)
    }

    private fun determineRank(xp: Int): String {
        return when {
            xp < 500 -> "D"
            xp < 1500 -> "C"
            xp < 3000 -> "B"
            xp < 5000 -> "A"
            else -> "S"
        }
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
        templateDao.insertTemplate(template)
    }

    suspend fun deleteTemplate(templateId: Int) {
        templateDao.deleteTemplateById(templateId)
    }

    suspend fun createTaskFromTemplate(template: Template) {
        val newTask = Task(
            title = template.title,
            category = template.category,
            threatLevel = template.threatLevel,
            xpValue = template.effectiveXpValue
        )
        taskDao.insertTask(newTask)
    }

    val criticalThreatsCompletionRate: Flow<Float> = combine(
        taskDao.getTotalCriticalTasksInLastWeek(getWeekAgoTimestamp()),
        taskDao.getCompletedCriticalTasksInLastWeek(getWeekAgoTimestamp())
    ) { total, completed ->
        if (total > 0) completed.toFloat() / total.toFloat() else 0f
    }

    val timeSpentPerCategory: Flow<List<CategoryDuration>> = taskDao.getTimeSpentPerCategory()

    val weeklyProductivity: Flow<List<DailyProductivity>> = 
        taskDao.getCompletedTasksSince(getWeekAgoTimestamp()).map { tasks ->
            tasks.groupBy { 
                Instant.ofEpochMilli(it.completedAt!!).atZone(ZoneId.systemDefault()).toLocalDate()
            }.map { (date, tasksOnDate) ->
                DailyProductivity(date, tasksOnDate.sumOf { it.effectiveXpValue })
            }.sortedBy { it.date }
        }

    private fun getWeekAgoTimestamp(): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.timeInMillis
    }
}