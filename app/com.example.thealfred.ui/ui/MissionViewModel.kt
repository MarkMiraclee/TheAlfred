package com.example.thealfred.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thealfred.data.Progress
import com.example.thealfred.data.Task
import com.example.thealfred.data.Template
import com.example.thealfred.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MissionViewModel(private val repository: TaskRepository, application: Application) : ViewModel() {

    // --- STATE FLOWS --- 
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

    val userProgress: StateFlow<Progress> = repository.userProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Progress()
    )

    val showReadinessCheck: StateFlow<Boolean> = repository.showReadinessCheck.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val allTemplates: StateFlow<List<Template>> = repository.allTemplates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- TASK EVENTS --- 
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

    // --- TEMPLATE EVENTS ---
    fun insertTemplate(template: Template) {
        viewModelScope.launch {
            repository.insertTemplate(template)
        }
    }

    fun deleteTemplate(templateId: Int) {
        viewModelScope.launch {
            repository.deleteTemplate(templateId)
        }
    }

    fun createTaskFromTemplate(template: Template) {
        viewModelScope.launch {
            repository.createTaskFromTemplate(template)
        }
    }
}