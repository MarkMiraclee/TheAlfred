package com.example.thealfred.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thealfred.data.Progress

@Composable
fun VigilanteRankDisplay(progress: Progress) {

    // Логика определения XP для следующего уровня (для UI)
    val xpForNextLevel = when {
        progress.totalVigilancePoints < 500 -> 500
        progress.totalVigilancePoints < 2000 -> 2000
        progress.totalVigilancePoints < 5000 -> 5000
        progress.totalVigilancePoints < 10000 -> 10000
        progress.totalVigilancePoints < 25000 -> 25000
        else -> progress.totalVigilancePoints
    }

    val xpForPreviousLevel = when {
        progress.totalVigilancePoints < 500 -> 0
        progress.totalVigilancePoints < 2000 -> 500
        progress.totalVigilancePoints < 5000 -> 2000
        progress.totalVigilancePoints < 10000 -> 5000
        progress.totalVigilancePoints < 25000 -> 10000
        else -> 25000
    }

    val xpSinceLastLevel = progress.totalVigilancePoints - xpForPreviousLevel
    val xpNeededForNextLevel = xpForNextLevel - xpForPreviousLevel

    val currentProgressFraction =
        if (xpNeededForNextLevel > 0) {
            xpSinceLastLevel.toFloat() / xpNeededForNextLevel.toFloat()
        } else {
            1f // Максимальное значение, если уже Легенда
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1C)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ТЕКУЩИЙ ПРОТОКОЛ:",
                color = Color.LightGray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = progress.currentRank.uppercase(),
                color = Color(0xFF00BFFF),
                fontSize = 22.sp,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = currentProgressFraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFFFA500),
                trackColor = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Очки Бдительности: ${progress.totalVigilancePoints} / $xpForNextLevel XP",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}