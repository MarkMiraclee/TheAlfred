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
    private val weeklySummaryDao = db.weeklySummaryDao()

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

    /**
     * Недельный цикл: при первом запуске после границы недели (понедельник 00:00
     * локального времени) итог прошлой недели сохраняется в weekly_summaries,
     * пропущенные недели записываются нулевыми, а XP, ранг и категории сбрасываются.
     * Самое первое включение счётчика (weekStart = 0, после установки/обновления)
     * фиксирует текущую неделю без сброса уже накопленного XP.
     */
    suspend fun ensureWeeklyReset() {
        db.withTransaction {
            val progress = taskDao.getProgressOnce() ?: return@withTransaction
            val currentWeekStart = WeekUtils.startOfWeek(System.currentTimeMillis())

            when {
                // Первый запуск после установки/обновления — только фиксируем неделю
                progress.weekStart == 0L ->
                    taskDao.insertProgress(
                        progress.copy(weekStart = currentWeekStart, lastUpdated = System.currentTimeMillis())
                    )

                // Неделя сменилась — снимок, пропуски и сброс
                progress.weekStart != currentWeekStart -> {
                    val finishedWeekEnd = WeekUtils.startOfWeek(progress.weekStart + WeekUtils.WEEK_MILLIS)

                    weeklySummaryDao.insertWeeklySummary(
                        WeeklySummary(
                            weekStart = progress.weekStart,
                            totalXp = progress.totalVigilancePoints,
                            currentRank = progress.currentRank,
                            operationalMissionsXp = progress.operationalMissionsXp,
                            physicalConditioningXp = progress.physicalConditioningXp,
                            intelligenceAndStrategyXp = progress.intelligenceAndStrategyXp,
                            waynePersonaMaintenanceXp = progress.waynePersonaMaintenanceXp,
                            tasksCompleted = taskDao.countCompletedBetween(progress.weekStart, finishedWeekEnd)
                        )
                    )

                    // Недели, в которые приложение не открывалось, — нулевые записи
                    var skipped = WeekUtils.startOfWeek(progress.weekStart + WeekUtils.WEEK_MILLIS)
                    while (skipped < currentWeekStart) {
                        weeklySummaryDao.insertWeeklySummary(
                            WeeklySummary(
                                weekStart = skipped,
                                totalXp = 0,
                                currentRank = VigilanteRanks.determineRank(0),
                                operationalMissionsXp = 0,
                                physicalConditioningXp = 0,
                                intelligenceAndStrategyXp = 0,
                                waynePersonaMaintenanceXp = 0,
                                tasksCompleted = 0
                            )
                        )
                        skipped = WeekUtils.startOfWeek(skipped + WeekUtils.WEEK_MILLIS)
                    }

                    // Сброс прогресса на новую неделю
                    taskDao.insertProgress(
                        progress.copy(
                            totalVigilancePoints = 0,
                            currentRank = VigilanteRanks.determineRank(0),
                            operationalMissionsXp = 0,
                            physicalConditioningXp = 0,
                            intelligenceAndStrategyXp = 0,
                            waynePersonaMaintenanceXp = 0,
                            weekStart = currentWeekStart,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    val weeklySummaries: Flow<List<WeeklySummary>> = weeklySummaryDao.getWeeklySummaries()

    // Выполнено задач в текущей неделе (граница недели хранится в Progress)
    val tasksCompletedThisWeek: Flow<Int> = combine(
        taskDao.getCompletedTasks(),
        userProgress
    ) { tasks, progress ->
        tasks.count { (it.completedAt ?: 0L) >= progress.weekStart }
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
