package com.example.thealfred.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thealfred.data.DailyProductivity
import com.example.thealfred.data.Task
import com.example.thealfred.data.ThreatLevel
import com.example.thealfred.ui.MissionViewModel
import com.example.thealfred.ui.theme.AlertRed
import com.example.thealfred.ui.theme.ConditioningGreen
import com.example.thealfred.ui.utils.DEBOUNCE_INTERVAL_MS
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Палитра журнала: только индикаторы — зелёный успех, красный крит, серый текст
private val LogGray = Color(0xFF9E9E9E)
private val LogDim = Color(0xFF6E6E6E)
private val LogGreen = ConditioningGreen
private val LogRed = AlertRed

private enum class LogPeriod(val label: String) {
    TODAY("СЕГОДНЯ"),
    WEEK("НЕДЕЛЯ"),
    ALL("ВСЁ ВРЕМЯ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatcaveLogScreen(viewModel: MissionViewModel, onNavigateBack: () -> Unit) {
    val userProgress by viewModel.userProgress.collectAsState()
    val totalMissions by viewModel.totalCompletedMissions.collectAsState()
    val allTimeXp by viewModel.allTimeXp.collectAsState()
    val missedDeadlines by viewModel.missedDeadlines.collectAsState()
    val weekDailyXp by viewModel.currentWeekDailyXp.collectAsState()
    val weeklySummaries by viewModel.weeklySummaries.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()

    var period by remember { mutableStateOf(LogPeriod.TODAY) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var lastClickTime by remember { mutableStateOf(0L) }

    fun debouncedAction(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > DEBOUNCE_INTERVAL_MS) {
            lastClickTime = currentTime
            action()
        }
    }

    val logTimeFormat = remember { SimpleDateFormat("[dd.MM.yyyy HH:mm]", Locale.getDefault()) }
    val todayStart = remember { Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis }
    val weekStart = userProgress?.weekStart ?: 0L

    val logTasks = when (period) {
        LogPeriod.TODAY -> completedTasks.filter { (it.completedAt ?: 0L) >= todayStart }
        LogPeriod.WEEK -> completedTasks.filter { (it.completedAt ?: 0L) >= weekStart }
        LogPeriod.ALL -> completedTasks
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "BATCAVE LOG // TERMINAL",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { debouncedAction { onNavigateBack() } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── 1. ГЛОБАЛЬНАЯ СВОДКА ─────────────────────────────
            item { SectionHeader("GLOBAL STATS") }
            item {
                ConsoleCard {
                    StatRow("МИССИЙ ВЫПОЛНЕНО", totalMissions.toString())
                    StatRow("ОБЩИЙ XP", allTimeXp.toString(), valueColor = LogGreen)
                    StatRow("ТЕКУЩИЙ РАНГ", userProgress?.currentRank ?: "...")
                    StatRow(
                        "ПРОПУЩЕНО ДЕДЛАЙНОВ",
                        missedDeadlines.toString(),
                        valueColor = if (missedDeadlines == 0) LogGreen else LogRed
                    )
                }
            }

            // ── 2. РАСПРЕДЕЛЕНИЕ ПО КАТЕГОРИЯМ ───────────────────
            item { SectionHeader("CATEGORY BREAKDOWN") }
            val progress = userProgress
            if (progress != null && progress.totalVigilancePoints > 0) {
                val categories = listOf(
                    "OPERATIONAL_MISSIONS" to progress.operationalMissionsXp,
                    "PHYSICAL_CONDITIONING" to progress.physicalConditioningXp,
                    "INTELLIGENCE_AND_STRATEGY" to progress.intelligenceAndStrategyXp,
                    "WAYNE_PERSONA_MAINTENANCE" to progress.waynePersonaMaintenanceXp
                )
                item {
                    ConsoleCard {
                        categories.forEach { (name, xp) ->
                            CategoryBarRow(name, xp, progress.totalVigilancePoints)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            } else {
                item {
                    ConsoleCard {
                        Text(
                            "NO DATA THIS WEEK",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = LogDim
                        )
                    }
                }
            }

            // ── 4. ДИНАМИКА (график за неделю) ───────────────────
            item { SectionHeader("DYNAMICS (XP/DAY, MON-SUN)") }
            item {
                ConsoleCard {
                    WeekBarChart(weekDailyXp)
                }
            }

            // ── 3. ХРОНИКА ОПЕРАЦИЙ ──────────────────────────────
            item { SectionHeader("MISSION LOG") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LogPeriod.entries.forEach { p ->
                        Text(
                            text = p.label,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = if (period == p) FontWeight.Bold else FontWeight.Normal,
                            color = if (period == p) LogGreen else LogDim,
                            modifier = Modifier.clickable { period = p }
                        )
                    }
                }
            }
            if (logTasks.isEmpty()) {
                item {
                    Text(
                        "NO RECORDS IN THIS RANGE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = LogDim,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(logTasks, key = { it.id }) { task ->
                    MissionLogRow(task, logTimeFormat) { selectedTask = task }
                }
            }

            // ── 5. ИСТОРИЯ НЕДЕЛЬ (снимки из weekly_summaries) ───
            item { SectionHeader("WEEKLY HISTORY") }
            if (weeklySummaries.isEmpty()) {
                item {
                    Text(
                        "NO COMPLETED WEEKS YET",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = LogDim
                    )
                }
            } else {
                items(weeklySummaries, key = { it.id }) { week ->
                    Text(
                        text = "${formatWeekRange(week.weekStart)} | RANK ${week.currentRank} | ${week.totalXp} XP | ${week.tasksCompleted} TASKS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = LogGray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    selectedTask?.let { task ->
        TaskDetailsDialog(task, onDismiss = { selectedTask = null })
    }
}

// ── Компоненты консольного стиля ─────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            text = "── $title ──",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = LogGray
        )
    }
}

@Composable
private fun ConsoleCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) { content() }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = LogGray)
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun CategoryBarRow(name: String, xp: Int, weekTotalXp: Int) {
    val pct = if (weekTotalXp > 0) xp * 100 / weekTotalXp else 0
    val fraction = if (weekTotalXp > 0) (xp.toFloat() / weekTotalXp).coerceIn(0f, 1f) else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                name,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = LogGray,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "$xp XP ($pct%)",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = LogGreen,
            trackColor = Color(0xFF2A2A2A),
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}

@Composable
private fun WeekBarChart(data: List<DailyProductivity>) {
    val dayLabels = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
    val maxXp = data.maxOfOrNull { it.totalXp } ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = if (day.totalXp > 0) day.totalXp.toString() else "",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = LogGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                val fraction = if (maxXp > 0) day.totalXp.toFloat() / maxXp else 0f
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    // Серая дорожка-базлайн, чтобы день был виден даже с нулём
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(color = Color(0xFF222222))
                    }
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(if (fraction > 0f) fraction else 0f)
                    ) {
                        drawRect(color = LogGreen)
                    }
                }
                Text(
                    text = dayLabels.getOrElse(index) { "" },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = LogDim
                )
            }
        }
    }
}

@Composable
private fun MissionLogRow(task: Task, format: SimpleDateFormat, onClick: (Task) -> Unit) {
    val threatCode = when (task.threatLevel) {
        ThreatLevel.CRITICAL_THREAT -> "CRIT"
        ThreatLevel.MEDIUM_THREAT -> "MED"
        ThreatLevel.MINOR_THREAT -> "MIN"
    }
    val threatColor = when (task.threatLevel) {
        ThreatLevel.CRITICAL_THREAT -> LogRed
        else -> LogGray
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(task) }
            .padding(vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = format.format(Date(task.completedAt ?: task.createdAt)),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = LogDim
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = threatCode,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = threatColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = task.title,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "+${task.xpValue} XP",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LogGreen
            )
        }
        HorizontalDivider(color = Color(0xFF222222), thickness = 0.5.dp)
    }
}

@Composable
private fun TaskDetailsDialog(task: Task, onDismiss: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", fontFamily = FontFamily.Monospace)
            }
        },
        title = {
            Text(
                task.title,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                DetailLine("КАТЕГОРИЯ", task.category.name)
                DetailLine("УГРОЗА", task.threatLevel.name)
                DetailLine("НАГРАДА", "+${task.xpValue} XP", LogGreen)
                DetailLine("СОЗДАНА", dateFormat.format(Date(task.createdAt)))
                task.completedAt?.let {
                    DetailLine("ЗАКРЫТА", dateFormat.format(Date(it)))
                }
                when {
                    task.dueDate == null -> DetailLine("ДЕДЛАЙН", "—")
                    task.isCompleted && (task.completedAt ?: 0) > task.dueDate ->
                        DetailLine("ДЕДЛАЙН", SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(task.dueDate)) + " (ПРОПУЩЕН)", LogRed)
                    else ->
                        DetailLine("ДЕДЛАЙН", SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(task.dueDate)), LogGreen)
                }
            }
        }
    )
}

@Composable
private fun DetailLine(label: String, value: String, valueColor: Color = LogGray) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = LogDim)
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

// Диапазон недели: «03.11 – 09.11»
private fun formatWeekRange(weekStartMillis: Long): String {
    val format = SimpleDateFormat("dd.MM", Locale.getDefault())
    val end = weekStartMillis + TimeUnit.DAYS.toMillis(6) // воскресенье той же недели
    return "${format.format(Date(weekStartMillis))} – ${format.format(Date(end))}"
}
