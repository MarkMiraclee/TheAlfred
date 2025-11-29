package com.example.thealfred.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thealfred.R
import com.example.thealfred.data.Task
import com.example.thealfred.data.TaskCategory
import com.example.thealfred.data.ThreatLevel
import com.example.thealfred.ui.MissionViewModel
import com.example.thealfred.ui.components.CategoryProgressCard
import com.example.thealfred.ui.theme.*
import com.example.thealfred.ui.utils.translate
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
    return format.format(date).uppercase()
}

private fun getCategoryColor(category: TaskCategory): Color {
    return when (category) {
        TaskCategory.OPERATIONAL_MISSIONS -> OperationalBlue
        TaskCategory.PHYSICAL_CONDITIONING -> ConditioningGreen
        TaskCategory.INTELLIGENCE_AND_STRATEGY -> IntelligenceYellow
        TaskCategory.WAYNE_PERSONA_MAINTENANCE -> PersonaPurple
    }
}

private fun getThreatColor(threatLevel: ThreatLevel): Color {
    return when (threatLevel) {
        ThreatLevel.CRITICAL_THREAT -> CriticalThreatColor
        ThreatLevel.MEDIUM_THREAT -> MediumThreatColor
        ThreatLevel.MINOR_THREAT -> MinorThreatColor
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CompletedTasksScreen(viewModel: MissionViewModel, onNavigateBack: () -> Unit) {
    val completedTasks by viewModel.completedTasks.collectAsState()
    var lastClickTime by remember { mutableStateOf(0L) }

    fun debouncedAction(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > DEBOUNCE_INTERVAL_MS) {
            lastClickTime = currentTime
            action()
        }
    }

    val groupedByDay = completedTasks
        .filter { it.completedAt != null }
        .groupBy { formatDate(it.completedAt!!) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.completed_threats_title), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { debouncedAction { onNavigateBack() } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = groupedByDay.entries.toList(),
                key = { (date, _) -> date }
            ) { (date, tasksOnDay) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), 
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Divider(color = Color.Gray.copy(alpha = 0.5f))
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        val groupedByCategory = tasksOnDay.groupBy { it.category }
                        val totalXpOnDay = tasksOnDay.sumOf { it.xpValue }

                        groupedByCategory.forEach { (category, tasks) ->
                            ExpandableCategoryGroup(
                                category = category,
                                tasks = tasks,
                                totalXpOnDay = totalXpOnDay
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableCategoryGroup(
    category: TaskCategory,
    tasks: List<Task>,
    totalXpOnDay: Int
) {
    var isExpanded by remember { mutableStateOf(false) }
    val categoryXpOnDay = tasks.sumOf { it.xpValue }

    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            CategoryProgressCard(
                title = translate(category = category),
                currentXp = categoryXpOnDay,
                totalXp = totalXpOnDay,
                color = getCategoryColor(category)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp)) {
                val tasksByThreat = tasks.groupBy { it.threatLevel }
                val threatLevelsOrder = listOf(ThreatLevel.CRITICAL_THREAT, ThreatLevel.MEDIUM_THREAT, ThreatLevel.MINOR_THREAT)

                threatLevelsOrder.forEach { threatLevel ->
                    tasksByThreat[threatLevel]?.let { tasksInThreat ->
                        Text(
                            text = translate(threatLevel = threatLevel),
                            color = getThreatColor(threatLevel),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                        
                        tasksInThreat.forEach { task ->
                            CompletedTaskRow(task = task)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedTaskRow(task: Task) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = task.title,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "+${task.xpValue} XP",
            color = getThreatColor(task.threatLevel),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
    Divider(color = Color.Gray.copy(alpha = 0.2f))
}