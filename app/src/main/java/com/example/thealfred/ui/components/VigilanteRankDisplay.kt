package com.example.thealfred.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thealfred.R
import com.example.thealfred.data.Progress
import com.example.thealfred.data.TaskCategory
import com.example.thealfred.ui.theme.*
import com.example.thealfred.ui.utils.RankUtils
import com.example.thealfred.ui.utils.translate

@Composable
fun VigilanteRankDisplay(progress: Progress) {
    var isExpanded by remember { mutableStateOf(false) }
    val currentRank = progress.currentRank ?: return // Если ранг еще не загружен, ничего не показываем
    val nextRankThreshold = RankUtils.getNextRankThreshold(currentRank)
    val progressPercentage = RankUtils.getProgressToNextRank(progress)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.current_protocol),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentRank,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = progressPercentage,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.vigilance_points, progress.totalVigilancePoints),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = if (nextRankThreshold < Int.MAX_VALUE) "$nextRankThreshold XP" else stringResource(id = R.string.max_rank),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(4.dp))
                    CategoryProgressCard(
                        title = translate(TaskCategory.OPERATIONAL_MISSIONS),
                        currentXp = progress.operationalMissionsXp,
                        totalXp = progress.totalVigilancePoints, 
                        color = OperationalBlue
                    )
                    CategoryProgressCard(
                        title = translate(TaskCategory.PHYSICAL_CONDITIONING),
                        currentXp = progress.physicalConditioningXp,
                        totalXp = progress.totalVigilancePoints, 
                        color = ConditioningGreen
                    )
                    CategoryProgressCard(
                        title = translate(TaskCategory.INTELLIGENCE_AND_STRATEGY),
                        currentXp = progress.intelligenceAndStrategyXp,
                        totalXp = progress.totalVigilancePoints, 
                        color = IntelligenceYellow
                    )
                    CategoryProgressCard(
                        title = translate(TaskCategory.WAYNE_PERSONA_MAINTENANCE),
                        currentXp = progress.waynePersonaMaintenanceXp,
                        totalXp = progress.totalVigilancePoints, 
                        color = PersonaPurple
                    )
                }
            }
        }
    }
}