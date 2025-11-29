package com.example.thealfred.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.thealfred.R
import com.example.thealfred.data.Task
import com.example.thealfred.data.TaskCategory
import com.example.thealfred.data.ThreatLevel
import com.example.thealfred.ui.MissionViewModel
import com.example.thealfred.ui.utils.translate
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: MissionViewModel,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.OPERATIONAL_MISSIONS) }
    var selectedThreatLevel by remember { mutableStateOf(ThreatLevel.MEDIUM_THREAT) }
    var selectedDueDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var taskAdded by remember { mutableStateOf(false) }

    if (taskAdded) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.new_threat_title), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.5f),
                disabledContainerColor = Color.Black.copy(alpha = 0.5f),
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(id = R.string.threat_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )

            CategorySelector(selectedCategory = selectedCategory, onCategorySelected = { selectedCategory = it }, colors = textFieldColors)

            ThreatLevelSelector(selectedThreatLevel = selectedThreatLevel, onThreatSelected = { selectedThreatLevel = it }, colors = textFieldColors)

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                val buttonText = selectedDueDate?.let {
                    stringResource(id = R.string.deadline_format, SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it))
                } ?: stringResource(id = R.string.set_deadline_button)
                Text(buttonText)
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedDueDate = datePickerState.selectedDateMillis
                                showDatePicker = false
                            },
                            enabled = confirmEnabled
                        ) {
                            Text(stringResource(id = R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newTask = Task(
                            title = title,
                            category = selectedCategory,
                            threatLevel = selectedThreatLevel,
                            dueDate = selectedDueDate
                        )
                        viewModel.addNewTask(newTask)
                        taskAdded = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && !taskAdded
            ) {
                Text(stringResource(id = R.string.add_threat_button))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(selectedCategory: TaskCategory, onCategorySelected: (TaskCategory) -> Unit, colors: TextFieldColors) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = translate(category = selectedCategory),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.operational_protocol_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = colors
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskCategory.values().forEach { category ->
                DropdownMenuItem(text = { Text(translate(category = category)) }, onClick = { 
                    onCategorySelected(category)
                    expanded = false
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatLevelSelector(selectedThreatLevel: ThreatLevel, onThreatSelected: (ThreatLevel) -> Unit, colors: TextFieldColors) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = translate(threatLevel = selectedThreatLevel),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.threat_level_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = colors
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ThreatLevel.values().forEach { threatLevel ->
                DropdownMenuItem(text = { Text(translate(threatLevel = threatLevel)) }, onClick = { 
                    onThreatSelected(threatLevel)
                    expanded = false
                })
            }
        }
    }
}