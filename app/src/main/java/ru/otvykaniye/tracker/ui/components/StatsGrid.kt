package ru.otvykaniye.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.otvykaniye.tracker.TracklessState
import ru.otvykaniye.tracker.domain.StatsCalculator
import ru.otvykaniye.tracker.ui.theme.*
import kotlinx.coroutines.flow.StateFlow


@Composable
fun SectionHeader(title: String, icon: ImageVector, iconTint: androidx.compose.ui.graphics.Color, action: @Composable () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, color = iconTint, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp)
        }
        action()
    }
}

@Composable
fun StatsGrid(state: TracklessState, timeSinceLastFlow: StateFlow<Long>, onDeleteEntry: (String) -> Unit, onCustomRecord: (Long) -> Unit = {}) {
    val timeSinceLast by timeSinceLastFlow.collectAsState()
    val profile = state.profiles[state.activeKind] ?: return
    val lang = state.language

    var showCustomTimePicker by remember { mutableStateOf(false) }

    if (showCustomTimePicker) {
        CustomTimePickerDialog(
            onDismiss = { showCustomTimePicker = false },
            onConfirm = { ts ->
                onCustomRecord(ts)
                showCustomTimePicker = false
            }
        )
    }

    val todayEntries = remember(profile.entries) {
        StatsCalculator.calculateTodayCount(profile.entries)
    }

    val healthStage = remember(timeSinceLast) {
        StatsCalculator.calculateHealthStage(timeSinceLast)
    }

    val spentSummary = remember(todayEntries, profile.entries.size, profile, state.activeKind) {
        StatsCalculator.calculateSpent(todayEntries, profile.entries.size, profile, state.activeKind)
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Health
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                SectionHeader(Strings.get(lang, "health"), Icons.Rounded.Favorite, PrimaryAccent)
                Spacer(Modifier.height(12.dp))

                val currentTitle = Strings.get(lang, healthStage.currentTitleKey)
                val currentDesc = Strings.get(lang, healthStage.currentDescKey)

                Text(currentTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text(currentDesc, color = TextDim, fontSize = 14.sp, lineHeight = 20.sp)

                if (healthStage.nextTitleKey != null) {
                    val nextTitle = Strings.get(lang, healthStage.nextTitleKey)

                    Spacer(Modifier.height(16.dp))
                    Text(Strings.get(lang, "next_stage").format(nextTitle), color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { healthStage.progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = PrimaryAccent.copy(alpha = 0.5f),
                        trackColor = BgDeep
                    )
                }
            }
        }

        // Stats summary
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                SectionHeader(Strings.get(lang, "stats"), Icons.Rounded.Timeline, PrimaryAccent)
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

                if (spentSummary.hasPrice) {
                    val spentToday = spentSummary.spentToday
                    val totalSpent = spentSummary.totalSpent

                    HorizontalDivider(
                        color = BgDeep,
                        thickness = 1.dp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                Strings.get(lang, "spent_today"),
                                color = TextDim,
                                fontSize = 11.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "$spentToday ${state.currency}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                Strings.get(lang, "spent_total"),
                                color = TextDim,
                                fontSize = 11.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "$totalSpent ${state.currency}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // History List
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                SectionHeader(
                    Strings.get(lang, "history"), 
                    Icons.Rounded.History, 
                    TextPrimary,
                    action = {
                        IconButton(onClick = { showCustomTimePicker = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add custom record", tint = TextDim, modifier = Modifier.size(24.dp))
                        }
                    }
                )
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(BgDeep).padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(entry.trigger, color = TextDim, fontSize = 12.sp)
                                }
                                IconButton(
                                    onClick = { onDeleteEntry(entry.id) }
                                ) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Coral, modifier = Modifier.size(24.dp))
                                }
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
