package com.example.thealfred.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.thealfred.data.Task
import com.example.thealfred.data.TaskCategory
import com.example.thealfred.data.ThreatLevel
import com.example.thealfred.ui.MissionViewModel
import com.example.thealfred.ui.utils.TaskUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

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
                title = { Text("NEW THREAT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent // Делаем фон Scaffold прозрачным
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Black.copy(alpha = 0.5f), // Увеличена прозрачность до 50%
                unfocusedContainerColor = Color.Black.copy(alpha = 0.5f), // Увеличена прозрачность до 50%
                disabledContainerColor = Color.Black.copy(alpha = 0.5f),
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Threat Title") },
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
                    "Deadline: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it)}"
                } ?: "SET DEADLINE"
                Text(buttonText)
            }

            if (showDatePicker) {
                // Запрещаем выбирать прошедшие даты: DatePicker отдаёт полночь UTC выбранного дня,
                // поэтому границей берём полночь UTC календарного «сегодня» по локальному времени
                val todayUtcMidnightMillis = remember {
                    val now = Calendar.getInstance()
                    GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
                        clear()
                        set(
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                        )
                    }.timeInMillis
                }
                val datePickerState = rememberDatePickerState(
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                            utcTimeMillis >= todayUtcMidnightMillis
                    }
                )
                val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // DatePicker отдаёт полночь по UTC — переводим в конец выбранного дня по локальному времени
                                selectedDueDate = datePickerState.selectedDateMillis
                                    ?.let { TaskUtils.pickerDateToEndOfLocalDay(it) }
                                showDatePicker = false
                            },
                            enabled = confirmEnabled
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
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
                Text("ADD THREAT")
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
            value = selectedCategory.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Operational Protocol") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = colors
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskCategory.values().forEach { category ->
                DropdownMenuItem(text = { Text(category.name) }, onClick = { 
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
            value = selectedThreatLevel.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Threat Level") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = colors
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ThreatLevel.values().forEach { threatLevel ->
                DropdownMenuItem(text = { Text(threatLevel.name) }, onClick = { 
                    onThreatSelected(threatLevel)
                    expanded = false
                })
            }
        }
    }
}