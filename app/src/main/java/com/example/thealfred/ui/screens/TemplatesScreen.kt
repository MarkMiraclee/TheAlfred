package com.example.thealfred.ui.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thealfred.R
import com.example.thealfred.data.Template
import com.example.thealfred.ui.MissionViewModel
import com.example.thealfred.ui.utils.translate

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
                title = { Text(stringResource(id = R.string.templates_title)) },
                navigationIcon = {
                    IconButton(onClick = { debouncedAction { onNavigateBack() } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { debouncedAction { onNavigateToAddTask() } }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_template_button))
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        if (templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(stringResource(id = R.string.no_templates_message), fontSize = 18.sp)
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
                Text(stringResource(id = R.string.category_label, translate(category = template.category)), fontSize = 14.sp)
                Text(stringResource(id = R.string.threat_label, translate(threatLevel = template.threatLevel)), fontSize = 14.sp)
            }
            IconButton(onClick = { 
                debouncedAction {
                    viewModel.createTaskFromTemplate(template)
                    onNavigateBack()
                } 
            }) {
                Icon(Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.create_task_from_template_desc), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { debouncedAction { viewModel.deleteTemplate(template.id) } }) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.delete_template_desc), tint = Color.Gray)
            }
        }
    }
}