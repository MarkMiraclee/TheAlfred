package com.example.thealfred.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thealfred.data.Template
import com.example.thealfred.ui.MissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(viewModel: MissionViewModel, onNavigateToAddTask: () -> Unit, onNavigateBack: () -> Unit) {
    val templates by viewModel.allTemplates.collectAsState()
    var lastClickTime by remember { mutableStateOf(0L) }

    fun debouncedAction(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > DEBOUNCE_INTERVAL_MS) {
            lastClickTime = currentTime
            action()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PLANNING TOOL: TEMPLATES") },
                navigationIcon = {
                    IconButton(onClick = { debouncedAction { onNavigateBack() } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { debouncedAction { onNavigateToAddTask() } }) {
                Icon(Icons.Default.Add, contentDescription = "Add Template")
            }
        },
        containerColor = Color.Transparent // Делаем фон Scaffold прозрачным
    ) { paddingValues ->
        if (templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No templates created yet.", fontSize = 18.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(templates, key = { it.id }) { template ->
                    TemplateCard(template = template, viewModel = viewModel, onNavigateBack = onNavigateBack)
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(template: Template, viewModel: MissionViewModel, onNavigateBack: () -> Unit) {
    var lastClickTime by remember { mutableStateOf(0L) }

    fun debouncedAction(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > DEBOUNCE_INTERVAL_MS) {
            lastClickTime = currentTime
            action()
        }
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(template.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Category: ${template.category.name}", fontSize = 14.sp)
                Text("Threat: ${template.threatLevel.name}", fontSize = 14.sp)
            }
            // Кнопка "Создать задачу"
            IconButton(onClick = { 
                debouncedAction {
                    viewModel.createTaskFromTemplate(template)
                    onNavigateBack()
                } 
            }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Create Task from Template", tint = MaterialTheme.colorScheme.primary)
            }
            // Кнопка "Удалить"
            IconButton(onClick = { debouncedAction { viewModel.deleteTemplate(template.id) } }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Template", tint = Color.Gray)
            }
        }
    }
}
