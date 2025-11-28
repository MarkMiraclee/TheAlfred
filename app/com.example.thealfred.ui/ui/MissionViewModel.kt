package com.example.thealfred.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thealfred.data.Progress
import com.example.thealfred.data.Task
import com.example.thealfred.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel принимает Репозиторий как зависимость
class MissionViewModel(private val repository: TaskRepository) : ViewModel() {

    // ЭКСПОЗИЦИЯ ДАННЫХ В UI (СОСТОЯНИЕ)
    val allTasks: StateFlow<List<Task>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val activeTasks: StateFlow<List<Task>> = repository.activeTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Текущий прогресс (XP и Ранг)
    val userProgress: StateFlow<Progress> = repository.userProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Progress()
    )

    // ОБРАБОТЧИКИ СОБЫТИЙ
    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            val currentProgress = userProgress.value
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
            val currentProgress = userProgress.value
            repository.completeMorningRitual(currentProgress)
        }
    }
}