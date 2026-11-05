package com.example.thealfred.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thealfred.ui.MissionViewModel
import com.example.thealfred.ui.components.ProductivityChart
import com.example.thealfred.ui.utils.DEBOUNCE_INTERVAL_MS
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatcaveLogScreen(viewModel: MissionViewModel, onNavigateBack: () -> Unit) {
    val completionRate by viewModel.criticalThreatsCompletionRate.collectAsState()
    val timeToCompletePerCategory by viewModel.timeToCompletePerCategory.collectAsState()
    val weeklyProductivity by viewModel.weeklyProductivity.collectAsState()
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
                title = { Text("BATCAVE LOG", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { debouncedAction { onNavigateBack() } }) {
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Карточка с графиком
            if (weeklyProductivity.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CRIME REDUCTION (XP/DAY)",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            ProductivityChart(productivityData = weeklyProductivity)
                        }
                    }
                }
            }

            // Карточка с KPI 1
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Critical Threats Neutralized (Weekly)",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(completionRate * 100).toInt()}%",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Карточка с KPI 2
            if (timeToCompletePerCategory.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                // Это время от создания задачи до её закрытия, а не время работы над ней
                                text = "TIME FROM CREATION TO COMPLETION",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            timeToCompletePerCategory.forEach {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = it.category.name,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = formatDuration(it.totalDuration),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательная функция для форматирования времени
private fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
