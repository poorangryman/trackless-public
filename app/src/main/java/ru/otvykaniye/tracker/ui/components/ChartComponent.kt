package ru.otvykaniye.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.otvykaniye.tracker.ProfileState
import ru.otvykaniye.tracker.ui.theme.BgCard
import ru.otvykaniye.tracker.ui.theme.Emerald
import ru.otvykaniye.tracker.ui.theme.TextDim
import ru.otvykaniye.tracker.ui.theme.TextPrimary
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChartComponent(profile: ProfileState) {
    val entries = profile.entries
    val dailyLimit = profile.dailyLimit

    // Prepare data for the last 14 days
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis

    val days = (0..13).map { i ->
        val start = startOfToday - (13 - i) * 86400000L
        val end = start + 86400000L
        val count = entries.count { it.ts in start until end }
        DayData(start, count)
    }

    val maxCount = (days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(dailyLimit).coerceAtLeast(1)

    var selectedDay by remember { mutableStateOf<DayData?>(null) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("📈 График за 14 дней", color = Emerald, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            
            if (selectedDay != null) {
                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                val dateStr = sdf.format(selectedDay!!.ts)
                Text("$dateStr: ${selectedDay!!.count} шт", color = TextPrimary, fontWeight = FontWeight.Bold)
            } else {
                Text("Нажмите на столбец", color = TextDim, fontSize = 12.sp)
            }
            
            Spacer(Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .pointerInput(days) {
                        detectTapGestures { offset ->
                            val canvasWidth = size.width
                            val barWidth = canvasWidth / 14
                            val index = (offset.x / barWidth).toInt().coerceIn(0, 13)
                            selectedDay = days[index]
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barWidth = (canvasWidth / 14) * 0.7f
                val spacing = (canvasWidth / 14) * 0.3f

                // Draw limit line
                val limitY = canvasHeight - (dailyLimit.toFloat() / maxCount) * canvasHeight
                drawLine(
                    color = Color.Red.copy(alpha = 0.5f),
                    start = Offset(0f, limitY),
                    end = Offset(canvasWidth, limitY),
                    strokeWidth = 2f
                )

                days.forEachIndexed { index, day ->
                    val x = index * (canvasWidth / 14) + spacing / 2
                    val barHeight = (day.count.toFloat() / maxCount) * canvasHeight
                    val y = canvasHeight - barHeight

                    val color = if (day.count > dailyLimit) Color(0xFFFB7185) else Emerald

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }
    }
}

data class DayData(val ts: Long, val count: Int)