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
import com.example.thealfred.data.TaskCategory
import com.example.thealfred.data.Template
import com.example.thealfred.data.ThreatLevel
import com.example.thealfred.ui.MissionViewModel
import com.example.thealfred.ui.utils.translate

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
                title = { Text(stringResource(id = R.string.new_template_title), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
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
                label = { Text(stringResource(id = R.string.template_title_label)) },
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
                Text(stringResource(id = R.string.save_template_button))
            }
        }
    }
}