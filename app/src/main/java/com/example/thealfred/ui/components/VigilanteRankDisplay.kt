package com.example.thealfred.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thealfred.data.Progress
import com.example.thealfred.ui.theme.*
import com.example.thealfred.ui.utils.RankUtils

@Composable
fun VigilanteRankDisplay(progress: Progress) {
    var isExpanded by remember { mutableStateOf(false) }
    val nextRankThreshold = RankUtils.getNextRankThreshold(progress.currentRank)
    val progressPercentage = RankUtils.getProgressToNextRank(progress)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { isExpanded = !isExpanded }, // Делаем карточку кликабельной
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // --- Основная информация о ранге ---
            Text(
                text = "CURRENT PROTOCOL:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = progress.currentRank,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progressPercentage },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                // Убираем точку-ограничитель и зазор в конце трека (поведение M3 по умолчанию)
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Vigilance Points: ${progress.totalVigilancePoints}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = if (nextRankThreshold < Int.MAX_VALUE) "$nextRankThreshold XP" else "MAX",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // --- Раскрывающаяся часть с категориями ---
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))
                    CategoryProgressCard(
                        title = "OPERATIONAL MISSIONS",
                        currentXp = progress.operationalMissionsXp,
                        totalXp = progress.totalVigilancePoints, // Добавлено
                        color = OperationalBlue
                    )
                    CategoryProgressCard(
                        title = "PHYSICAL CONDITIONING",
                        currentXp = progress.physicalConditioningXp,
                        totalXp = progress.totalVigilancePoints, // Добавлено
                        color = ConditioningGreen
                    )
                    CategoryProgressCard(
                        title = "INTELLIGENCE & STRATEGY",
                        currentXp = progress.intelligenceAndStrategyXp,
                        totalXp = progress.totalVigilancePoints, // Добавлено
                        color = IntelligenceYellow
                    )
                    CategoryProgressCard(
                        title = "WAYNE PERSONA MAINTENANCE",
                        currentXp = progress.waynePersonaMaintenanceXp,
                        totalXp = progress.totalVigilancePoints, // Добавлено
                        color = PersonaPurple
                    )
                }
            }
        }
    }
}