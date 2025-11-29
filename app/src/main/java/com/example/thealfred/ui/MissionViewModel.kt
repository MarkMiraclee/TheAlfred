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

sealed class ReadinessCheckState {
    object Loading : ReadinessCheckState()
    object Show : ReadinessCheckState()
    object Hide : ReadinessCheckState()
}

class MissionViewModel(private val repository: TaskRepository, application: Application) : AndroidViewModel(application) {

    private val _dailyQuote = MutableStateFlow("")
    val dailyQuote: StateFlow<String> = _dailyQuote.asStateFlow()

    private val _readinessCheckState = MutableStateFlow<ReadinessCheckState>(ReadinessCheckState.Loading)
    val readinessCheckState: StateFlow<ReadinessCheckState> = _readinessCheckState.asStateFlow()

    init {
        viewModelScope.launch {
            _dailyQuote.value = DailyQuoteManager.getQuote(getApplication<Application>().applicationContext)
            _readinessCheckState.value = if (repository.isRitualCompletedToday()) ReadinessCheckState.Hide else ReadinessCheckState.Show
        }
    }

    val allTasks: StateFlow<List<Task>> = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeTasks: StateFlow<List<Task>> = repository.activeTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val criticalTasks: StateFlow<List<Task>> = repository.criticalTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val overdueTasks: StateFlow<List<Task>> = repository.overdueTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val completedTasks: StateFlow<List<Task>> = repository.completedTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeTasksSortedByPriority: StateFlow<List<Task>> = repository.activeTasksSortedByPriority.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
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
    val timeSpentPerCategory: StateFlow<List<CategoryDuration>> = repository.timeSpentPerCategory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val weeklyProductivity: StateFlow<List<DailyProductivity>> = repository.weeklyProductivity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ОБРАБОТЧИКИ СОБЫТИЙ
    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            val currentProgress = userProgress.value ?: return@launch
            repository.updateTaskStatus(
                task = task,
                isCompleted = !task.isCompleted,
                currentProgress = currentProgress
            )
        }
    }

    fun addNewTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun completeRitual() {
        viewModelScope.launch {
            val currentProgress = userProgress.value ?: return@launch
            repository.completeMorningRitual(currentProgress)
            _readinessCheckState.value = ReadinessCheckState.Hide
        }
    }
}