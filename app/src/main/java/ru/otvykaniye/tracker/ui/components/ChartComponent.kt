package ru.otvykaniye.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.otvykaniye.tracker.TracklessState
import ru.otvykaniye.tracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ChartComponent(state: TracklessState) {
    val profile = state.profiles[state.activeKind] ?: return
    val entries = profile.entries
    val dailyLimit = profile.dailyLimit
    val lang = state.language

    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val days = (0..13).map { index ->
        val start = (startOfToday.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -(13 - index))
        }
        val end = (start.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val startMs = start.timeInMillis
        val endMs = end.timeInMillis
        val count = entries.count { it.ts in startMs until endMs }
        DayData(startMs, count)
    }

    val maxCount = (days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(dailyLimit).coerceAtLeast(1)

    var selectedDay by remember { mutableStateOf<DayData?>(null) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            SectionHeader(Strings.get(lang, "chart_14_days"), Icons.Rounded.BarChart, Emerald)
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgDeep)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (selectedDay != null) {
                    val locale = if (lang == "en") Locale.ENGLISH else Locale("ru")
                    val sdf = SimpleDateFormat("dd MMM", locale)
                    val dateStr = sdf.format(selectedDay!!.ts)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(dateStr, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("${selectedDay!!.count} ${Strings.get(lang, "pcs")}", color = if (selectedDay!!.count > dailyLimit) Coral else Emerald, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(Strings.get(lang, "tap_bar"), color = TextDim, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
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
                val barWidth = (canvasWidth / 14) * 0.65f
                val spacing = (canvasWidth / 14) * 0.35f

                // Draw limit line
                val limitY = canvasHeight - (dailyLimit.toFloat() / maxCount) * canvasHeight
                drawLine(
                    color = Coral.copy(alpha = 0.4f),
                    start = Offset(0f, limitY),
                    end = Offset(canvasWidth, limitY),
                    strokeWidth = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                days.forEachIndexed { index, day ->
                    val x = index * (canvasWidth / 14) + spacing / 2
                    val barHeight = (day.count.toFloat() / maxCount) * canvasHeight
                    val y = canvasHeight - barHeight

                    val color = if (day.count > dailyLimit) Coral else Emerald
                    val alpha = if (selectedDay == day) 1f else 0.5f

                    drawRoundRect(
                        color = color.copy(alpha = alpha),
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

@Composable
fun HourlyStats(state: TracklessState) {
    val profile = state.profiles[state.activeKind] ?: return
    val entries = profile.entries
    val lang = state.language

    var morning = 0
    var day = 0
    var eve = 0
    var night = 0

    val calendar = Calendar.getInstance()
    entries.forEach { entry ->
        calendar.timeInMillis = entry.ts
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 6..11 -> morning++
            in 12..17 -> day++
            in 18..23 -> eve++
            else -> night++
        }
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            SectionHeader(Strings.get(lang, "time_of_use"), Icons.Rounded.AccessTime, Cyan)
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HourlyBlock(Strings.get(lang, "morning"), "6-12", morning)
                HourlyBlock(Strings.get(lang, "day"), "12-18", day)
                HourlyBlock(Strings.get(lang, "evening"), "18-24", eve)
                HourlyBlock(Strings.get(lang, "night"), "0-6", night)
            }
        }
    }
}

@Composable
fun HourlyBlock(title: String, time: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(time, color = TextDim, fontSize = 11.sp)
        Spacer(Modifier.height(12.dp))
        Text(count.toString(), color = Cyan, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
    }
}
