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
import com.example.thealfred.data.TaskCategory
import com.example.thealfred.data.Template
import com.example.thealfred.data.ThreatLevel
import com.example.thealfred.ui.MissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTemplateScreen(
    viewModel: MissionViewModel,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.OPERATIONAL_MISSIONS) }
    var selectedThreatLevel by remember { mutableStateOf(ThreatLevel.MEDIUM_THREAT) }
    var templateAdded by remember { mutableStateOf(false) }

    if (templateAdded) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NEW TEMPLATE", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
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
                label = { Text("Template Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )

            CategorySelector(selectedCategory = selectedCategory, onCategorySelected = { selectedCategory = it }, colors = textFieldColors)

            ThreatLevelSelector(selectedThreatLevel = selectedThreatLevel, onThreatSelected = { selectedThreatLevel = it }, colors = textFieldColors)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newTemplate = Template(
                            title = title,
                            category = selectedCategory,
                            threatLevel = selectedThreatLevel
                        )
                        viewModel.insertTemplate(newTemplate)
                        templateAdded = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && !templateAdded
            ) {
                Text("SAVE TEMPLATE")
            }
        }
    }
}