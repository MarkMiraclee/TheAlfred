package com.example.thealfred.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import com.example.thealfred.data.Task
import com.example.thealfred.ui.theme.AlertRed
import com.example.thealfred.ui.theme.WarningOrange
import java.util.concurrent.TimeUnit

@Composable
private fun formatDueDate(dueDate: Long): Pair<String, Color> {
    val currentTime = System.currentTimeMillis()
    val remainingTime = dueDate - currentTime

    if (remainingTime < 0) {
        return "OVERDUE" to AlertRed
    }

    val days = TimeUnit.MILLISECONDS.toDays(remainingTime)
    val hours = TimeUnit.MILLISECONDS.toHours(remainingTime) % 24

    return when {
        days > 1 -> "$days days left" to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        days == 1L -> "1 day left" to WarningOrange
        hours > 0 -> "$hours hours left" to WarningOrange
        else -> "< 1 hour left" to WarningOrange
    }
}

@Composable
fun TaskRow(task: Task, onToggleComplete: (Task) -> Unit, onDelete: (Task) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggleComplete(task) }, // Защита от двойного клика обеспечивает транзакция XP в TaskRepository
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium
            )

            task.dueDate?.let {
                val (text, color) = formatDueDate(it)
                Text(
                    text = text,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "+${task.xpValue} XP",
            color = WarningOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = { onDelete(task) }) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Task",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
