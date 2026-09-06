package ru.otvykaniye.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.otvykaniye.tracker.TracklessState
import ru.otvykaniye.tracker.ui.theme.*

@Composable
fun SectionHeader(title: String, icon: ImageVector, iconTint: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = iconTint, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp)
    }
}

@Composable
fun StatsGrid(state: TracklessState, timeSinceLast: Long) {
    val profile = state.profiles[state.activeKind] ?: return
    
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // Wishlist
        if (profile.wishlistTitle.isNotEmpty() && profile.wishlistCost > 0) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    SectionHeader("ЦЕЛЬ", Icons.Rounded.Star, Amber)
                    Spacer(Modifier.height(12.dp))
                    Text(profile.wishlistTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    
                    val saved = (profile.baseline * profile.price) - (profile.entries.size * (profile.price / profile.perPack))
                    val progress = (saved.coerceAtLeast(0.0) / profile.wishlistCost).toFloat().coerceIn(0f, 1f)
                    
                    Text("${saved.coerceAtLeast(0.0).toInt()} / ${profile.wishlistCost.toInt()} ${state.currency}", color = TextDim, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = Amber,
                        trackColor = BgDeep
                    )
                }
            }
        }

        // Health
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                SectionHeader("ЗДОРОВЬЕ", Icons.Rounded.Favorite, Emerald)
                Spacer(Modifier.height(12.dp))
                
                val hours = timeSinceLast / (1000 * 60 * 60)
                val stageTitle = when {
                    hours < 1 -> "Начало пути"
                    hours < 12 -> "Очищение крови"
                    hours < 24 -> "Снижение риска"
                    hours < 72 -> "Никотин выведен"
                    else -> "Глубокое восстановление"
                }
                Text(stageTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        // Stats summary
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                SectionHeader("СТАТИСТИКА", Icons.Rounded.Timeline, Cyan)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Сегодня", color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        val todayEntries = profile.entries.filter { it.ts > System.currentTimeMillis() - 86400000 }
                        Text("${todayEntries.size} раз", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Лимит", color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text("${profile.dailyLimit}", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // History List
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                SectionHeader("ИСТОРИЯ", Icons.Rounded.History, TextPrimary)
                Spacer(Modifier.height(16.dp))
                if (profile.entries.isEmpty()) {
                    Text("Пока нет записей", color = TextDim)
                } else {
                    profile.entries.takeLast(5).reversed().forEach { entry ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(formatDate(entry.ts), color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(BgDeep).padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(entry.trigger, color = TextDim, fontSize = 12.sp)
                            }
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