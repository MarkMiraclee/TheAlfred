package com.example.thealfred.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.thealfred.data.TaskRepository

// Фабрика, необходимая для передачи TaskRepository в конструктор ViewModel
class MissionViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MissionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MissionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}