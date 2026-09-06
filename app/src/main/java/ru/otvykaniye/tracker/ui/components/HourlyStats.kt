package ru.otvykaniye.tracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.otvykaniye.tracker.ProfileState
import ru.otvykaniye.tracker.ui.theme.Cyan
import ru.otvykaniye.tracker.ui.theme.TextDim
import ru.otvykaniye.tracker.ui.theme.TextPrimary
import java.util.Calendar

@Composable
fun HourlyStats(profile: ProfileState) {
    val entries = profile.entries

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
        Column(Modifier.padding(16.dp)) {
            Text("⏰ Время употребления (всего)", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HourlyBlock("Утро", "6-12", morning)
                HourlyBlock("День", "12-18", day)
                HourlyBlock("Вечер", "18-24", eve)
                HourlyBlock("Ночь", "0-6", night)
            }
        }
    }
}

@Composable
fun HourlyBlock(title: String, time: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(time, color = TextDim, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text(count.toString(), color = Cyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}