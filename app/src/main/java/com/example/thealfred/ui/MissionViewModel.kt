package com.example.thealfred.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thealfred.data.*
import com.example.thealfred.ui.utils.DailyQuoteManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MissionViewModel(private val repository: TaskRepository, application: Application) : AndroidViewModel(application) {

    private val _dailyQuote = MutableStateFlow("")
    val dailyQuote: StateFlow<String> = _dailyQuote.asStateFlow()

    /**
     * null — проверка ритуала в БД ещё идёт (показываем только фон),
     * true — ритуал за сегодня не выполнен, false — выполнен.
     */
    private val _showReadinessCheck = MutableStateFlow<Boolean?>(null)
    val showReadinessCheck: StateFlow<Boolean?> = _showReadinessCheck.asStateFlow()

    init {
        refreshDayState()
    }

    /**
     * Полная проверка календарного состояния: недельный сброс XP/ранга,
     * цитата дня и утренний ритуал. Вызывается при создании ViewModel
     * и на каждый ON_START (возврат приложения на передний план),
     * поэтому переход через полночь или понедельник подхватывается,
     * даже если процесс прожил несколько дней.
     */
    fun refreshDayState() {
        viewModelScope.launch {
            repository.ensureWeeklyReset()
            _dailyQuote.value = DailyQuoteManager.getQuote(getApplication<Application>().applicationContext)
            _showReadinessCheck.value = !repository.isRitualCompletedToday()
        }
    }

    val activeTasksSortedByPriority: StateFlow<List<Task>> = repository.activeTasksSortedByPriority.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val completedTasks: StateFlow<List<Task>> = repository.completedTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * null — прогресс ещё грузится из БД. До загрузки не рисуем ни фон ранга, ни контент,
     * чтобы не мигал фон дефолтного ранга D перед показом актуального ранга.
     */
    val userProgress: StateFlow<Progress?> = repository.userProgress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Шаблоны ---
    val allTemplates: StateFlow<List<Template>> = repository.allTemplates.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertTemplate(template: Template) = viewModelScope.launch {
        repository.insertTemplate(template)
    }

    fun createTaskFromTemplate(template: Template) = viewModelScope.launch {
        repository.createTaskFromTemplate(template)
    }

    fun deleteTemplate(templateId: Int) = viewModelScope.launch {
        repository.deleteTemplate(templateId)
    }

    // --- Аналитика ---
    val criticalThreatsCompletionRate: StateFlow<Float> = repository.criticalThreatsCompletionRate.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)
    val timeToCompletePerCategory: StateFlow<List<CategoryDuration>> = repository.timeToCompletePerCategory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val weeklyProductivity: StateFlow<List<DailyProductivity>> = repository.weeklyProductivity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val weeklySummaries: StateFlow<List<WeeklySummary>> = repository.weeklySummaries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasksCompletedThisWeek: StateFlow<Int> = repository.tasksCompletedThisWeek.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ОБРАБОТЧИКИ СОБЫТИЙ
    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            repository.updateTaskStatus(
                task = task,
                isCompleted = !task.isCompleted
            )
        }
    }

    fun addNewTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun completeRitual() {
        viewModelScope.launch {
            repository.completeMorningRitual()
            _showReadinessCheck.value = false
        }
    }
}
