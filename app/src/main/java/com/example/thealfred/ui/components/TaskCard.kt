package com.example.thealfred.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thealfred.data.Task
import com.example.thealfred.data.ThreatLevel
import com.example.thealfred.ui.theme.CriticalThreatColor
import com.example.thealfred.ui.theme.MediumThreatColor
import com.example.thealfred.ui.theme.MinorThreatColor

private fun getThreatColor(threatLevel: ThreatLevel) = when (threatLevel) {
    ThreatLevel.CRITICAL_THREAT -> CriticalThreatColor
    ThreatLevel.MEDIUM_THREAT -> MediumThreatColor
    ThreatLevel.MINOR_THREAT -> MinorThreatColor
}

@Composable
fun TaskCard(
    task: Task,
    onToggleComplete: ((Task) -> Unit)? // Сделаем коллбэк необязательным
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Плоский вид
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3A3A3A)) // Цвет фона
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = null, // Чекбокс всегда неактивен
                enabled = false, // Визуально делаем неактивным
                colors = CheckboxDefaults.colors(
                    checkedColor = getThreatColor(task.threatLevel),
                    disabledCheckedColor = getThreatColor(task.threatLevel)
                )
            )
            Spacer(modifier = Modifier.width(12.dp))

            Text(task.title, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Chip(label = task.threatLevel.name.replace("_", " "), color = getThreatColor(task.threatLevel))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+${task.xpValue} XP",
                    color = getThreatColor(task.threatLevel),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}