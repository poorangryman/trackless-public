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
import java.util.Calendar

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
    val lang = state.language

    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val endOfToday = Calendar.getInstance().apply {
        timeInMillis = startOfToday
        add(Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis

    val todayEntries = profile.entries.count { it.ts in startOfToday until endOfToday }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Wishlist
        if (profile.wishlistTitle.isNotEmpty() && profile.wishlistCost > 0) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    SectionHeader(Strings.get(lang, "target"), Icons.Rounded.Star, Amber)
                    Spacer(Modifier.height(12.dp))
                    Text(profile.wishlistTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(4.dp))

                    // Convert pack price to a single-piece price before applying
                    // consumption counts. Both baseline and entries are in pieces.
                    val pricePerPiece = if (profile.perPack > 0) {
                        profile.price / profile.perPack
                    } else {
                        0.0
                    }
                    val saved = (profile.baseline * pricePerPiece) - (profile.entries.size * pricePerPiece)
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
                SectionHeader(Strings.get(lang, "health"), Icons.Rounded.Favorite, Emerald)
                Spacer(Modifier.height(12.dp))

                val hours = timeSinceLast / (1000 * 60 * 60)
                val stageIdx = when {
                    hours < 1 -> 0
                    hours < 12 -> 1
                    hours < 24 -> 2
                    hours < 72 -> 3
                    else -> 4
                }

                val currentTitle = Strings.get(lang, "stage_$stageIdx")
                val currentDesc = Strings.get(lang, "stage_${stageIdx}_desc")

                Text(currentTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text(currentDesc, color = TextDim, fontSize = 14.sp, lineHeight = 20.sp)

                if (stageIdx < 4) {
                    val nextTitle = Strings.get(lang, "stage_${stageIdx + 1}")
                    val maxHours = when(stageIdx) { 0 -> 1; 1 -> 12; 2 -> 24; 3 -> 72; else -> 72 }.toFloat()
                    val prevHours = when(stageIdx) { 0 -> 0; 1 -> 1; 2 -> 12; 3 -> 24; else -> 24 }.toFloat()

                    val progress = ((hours - prevHours) / (maxHours - prevHours)).coerceIn(0f, 1f)

                    Spacer(Modifier.height(16.dp))
                    Text(Strings.get(lang, "next_stage").format(nextTitle), color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Emerald.copy(alpha = 0.5f),
                        trackColor = BgDeep
                    )
                }
            }
        }

        // Stats summary
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                SectionHeader(Strings.get(lang, "stats"), Icons.Rounded.Timeline, Cyan)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(Strings.get(lang, "today"), color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text("$todayEntries", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(Strings.get(lang, "limit"), color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text("${profile.dailyLimit}", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // History List
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                SectionHeader(Strings.get(lang, "history"), Icons.Rounded.History, TextPrimary)
                Spacer(Modifier.height(16.dp))
                if (profile.entries.isEmpty()) {
                    Text(Strings.get(lang, "no_entries"), color = TextDim)
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
