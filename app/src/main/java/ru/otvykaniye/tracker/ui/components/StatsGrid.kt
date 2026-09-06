package ru.otvykaniye.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.otvykaniye.tracker.TracklessState
import ru.otvykaniye.tracker.ui.theme.*

@Composable
fun StatsGrid(state: TracklessState, timeSinceLast: Long) {
    val profile = state.profiles[state.activeKind] ?: return
    
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // Wishlist
        if (profile.wishlistTitle.isNotEmpty() && profile.wishlistCost > 0) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("🎯 Цель: ${profile.wishlistTitle}", color = Amber, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Стоимость: ${profile.wishlistCost} ${state.currency}", color = TextPrimary)
                    // Basic progress
                    val saved = (profile.baseline * profile.price) - (profile.entries.size * (profile.price / profile.perPack))
                    val progress = (saved.coerceAtLeast(0.0) / profile.wishlistCost).toFloat().coerceIn(0f, 1f)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Emerald,
                        trackColor = BgDeep
                    )
                    Text("Накоплено: ${saved.coerceAtLeast(0.0)} / ${profile.wishlistCost}", color = TextDim, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        // Health
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("💚 Здоровье и Восстановление", color = Emerald, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                
                val hours = timeSinceLast / (1000 * 60 * 60)
                val stageTitle = when {
                    hours < 1 -> "Начало пути"
                    hours < 12 -> "Очищение крови"
                    hours < 24 -> "Снижение риска"
                    hours < 72 -> "Никотин выведен"
                    else -> "Глубокое восстановление"
                }
                Text("Текущий этап: $stageTitle", color = TextPrimary)
            }
        }

        // Stats summary
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("📊 Статистика", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Сегодня", color = TextDim)
                        val todayEntries = profile.entries.filter { it.ts > System.currentTimeMillis() - 86400000 }
                        Text("${todayEntries.size} раз", color = TextPrimary, fontSize = 20.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Лимит", color = TextDim)
                        Text("${profile.dailyLimit}", color = TextPrimary, fontSize = 20.sp)
                    }
                }
            }
        }
        
        // History List
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("🕒 История", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                if (profile.entries.isEmpty()) {
                    Text("Пока нет записей", color = TextDim)
                } else {
                    profile.entries.takeLast(5).reversed().forEach { entry ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatDate(entry.ts), color = TextPrimary)
                            Text(entry.trigger, color = TextDim)
                        }
                    }
                }
            }
        }
    }
}

fun formatDate(ts: Long): String {
    val sdf = java.text.SimpleDateFormat("dd.MM HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(ts))
}
