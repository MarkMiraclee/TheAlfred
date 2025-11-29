package com.example.thealfred.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thealfred.data.ThreatLevel
import com.example.thealfred.ui.MissionViewModel
import com.example.thealfred.ui.components.TaskRow
import com.example.thealfred.ui.components.VigilanteRankDisplay
import com.example.thealfred.ui.theme.*
import com.example.thealfred.ui.utils.TaskUtils

const val DEBOUNCE_INTERVAL_MS = 1000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyBriefingScreen(
    viewModel: MissionViewModel,
    onNavigateToCompleted: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToAddTask: () -> Unit
) {
    val activeTasks by viewModel.activeTasksSortedByPriority.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()
    val dailyQuote by viewModel.dailyQuote.collectAsState()
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
                title = { Text("DAILY BRIEFING", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { debouncedAction { onNavigateToJournal() } }) {
                        Icon(Icons.Filled.Book, contentDescription = "Journal")
                    }
                    IconButton(onClick = { debouncedAction { onNavigateToTemplates() } }) {
                        Icon(Icons.Default.Build, contentDescription = "Planning Tools")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { debouncedAction { onNavigateToAddTask() } }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = dailyQuote.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(1000))
                ) {
                    QuoteOfTheDay(quote = dailyQuote)
                }
            }

            item {
                VigilanteRankDisplay(progress = userProgress)
            }

            val overdueTasks = activeTasks.filter { TaskUtils.isOverdue(it) }
            val criticalTasks = activeTasks.filter { it.threatLevel == ThreatLevel.CRITICAL_THREAT && !TaskUtils.isOverdue(it) }
            val mediumTasks = activeTasks.filter { it.threatLevel == ThreatLevel.MEDIUM_THREAT && !TaskUtils.isOverdue(it) }
            val minorTasks = activeTasks.filter { it.threatLevel == ThreatLevel.MINOR_THREAT && !TaskUtils.isOverdue(it) }

            if (overdueTasks.isNotEmpty()) {
                item {
                    val infiniteTransition = rememberInfiniteTransition(label = "blink_alert")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha_alert"
                    )

                    Card(modifier = Modifier.fillMaxWidth().alpha(alpha)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("⚠ CAVE ALERT", color = AlertRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            overdueTasks.forEach { task ->
                                TaskRow(task = task, onToggleComplete = { viewModel.toggleTaskStatus(it) })
                            }
                        }
                    }
                }
            }

            if (criticalTasks.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("CRITICAL THREATS", color = CriticalThreatColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            criticalTasks.forEach { task ->
                                TaskRow(task = task, onToggleComplete = { viewModel.toggleTaskStatus(it) })
                            }
                        }
                    }
                }
            }

            if (mediumTasks.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("MEDIUM THREATS", color = MediumThreatColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            mediumTasks.forEach { task ->
                                TaskRow(task = task, onToggleComplete = { viewModel.toggleTaskStatus(it) })
                            }
                        }
                    }
                }
            }

            if (minorTasks.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("MINOR THREATS", color = MinorThreatColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            minorTasks.forEach { task ->
                                TaskRow(task = task, onToggleComplete = { viewModel.toggleTaskStatus(it) })
                            }
                        }
                    }
                }
            }
            
            item {
                 Button(
                    onClick = { debouncedAction { onNavigateToCompleted() } },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("COMPLETED THREATS", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QuoteOfTheDay(quote: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = quote,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Cursive,
                fontSize = 22.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "- Альфред",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}