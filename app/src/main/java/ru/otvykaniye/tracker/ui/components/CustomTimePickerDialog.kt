package ru.otvykaniye.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ru.otvykaniye.tracker.ui.theme.*
import java.util.Calendar

@Composable
fun CustomTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    var dayOffset by remember { mutableStateOf(0) } 

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Когда это было?", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickPill("-5 мин") {
                        calendar.timeInMillis = System.currentTimeMillis() - 5 * 60 * 1000
                        selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
                        selectedMinute = calendar.get(Calendar.MINUTE)
                        dayOffset = 0
                    }
                    QuickPill("-15 мин") {
                        calendar.timeInMillis = System.currentTimeMillis() - 15 * 60 * 1000
                        selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
                        selectedMinute = calendar.get(Calendar.MINUTE)
                        dayOffset = 0
                    }
                    QuickPill("-1 час") {
                        calendar.timeInMillis = System.currentTimeMillis() - 60 * 60 * 1000
                        selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
                        selectedMinute = calendar.get(Calendar.MINUTE)
                        dayOffset = 0
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberScroller(
                        range = 0..23,
                        selectedValue = selectedHour,
                        onValueChange = { selectedHour = it }
                    )
                    Text(":", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    NumberScroller(
                        range = 0..59,
                        selectedValue = selectedMinute,
                        onValueChange = { selectedMinute = it }
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePill("Сегодня", dayOffset == 0) { dayOffset = 0 }
                    DatePill("Вчера", dayOffset == -1) { dayOffset = -1 }
                    DatePill("Позавчера", dayOffset == -2) { dayOffset = -2 }
                }

                Spacer(Modifier.height(32.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена", color = TextDim)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val resultCal = Calendar.getInstance()
                            resultCal.add(Calendar.DAY_OF_YEAR, dayOffset)
                            resultCal.set(Calendar.HOUR_OF_DAY, selectedHour)
                            resultCal.set(Calendar.MINUTE, selectedMinute)
                            onConfirm(resultCal.timeInMillis)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
                    ) {
                        Text("Сохранить", color = BgDeep, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickPill(text: String, onClick: () -> Unit) {
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(text, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
}

@Composable
fun DatePill(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) PrimaryAccent.copy(alpha = 0.15f) else BgCard,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text,
            color = if (isSelected) PrimaryAccent else TextDim,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun NumberScroller(range: IntRange, selectedValue: Int, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextButton(onClick = { 
            var nv = selectedValue - 1
            if (nv < range.first) nv = range.last
            onValueChange(nv)
        }) { Text("▲", color = TextDim, fontSize = 20.sp) }
        
        Text(
            text = selectedValue.toString().padStart(2, '0'),
            color = TextPrimary,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold
        )
        
        TextButton(onClick = { 
            var nv = selectedValue + 1
            if (nv > range.last) nv = range.first
            onValueChange(nv)
        }) { Text("▼", color = TextDim, fontSize = 20.sp) }
    }
}
