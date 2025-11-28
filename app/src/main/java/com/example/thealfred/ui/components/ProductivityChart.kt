package com.example.thealfred.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thealfred.data.DailyProductivity
import com.example.thealfred.ui.theme.BatmanBlueLight
import com.example.thealfred.ui.theme.BatcaveLightGray
import java.time.format.DateTimeFormatter

@Composable
fun ProductivityChart(productivityData: List<DailyProductivity>) {
    val maxProductivity = productivityData.maxOfOrNull { it.totalXp } ?: 1
    val formatter = DateTimeFormatter.ofPattern("dd.MM")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        productivityData.forEach {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // XP Text
                Text(
                    text = it.totalXp.toString(),
                    fontSize = 10.sp,
                    color = BatcaveLightGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Bar
                Canvas(modifier = Modifier.fillMaxHeight(it.totalXp.toFloat() / maxProductivity).width(24.dp)) { 
                    drawRect(color = BatmanBlueLight)
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Date Text
                Text(
                    text = it.date.format(formatter),
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        }
    }
}
