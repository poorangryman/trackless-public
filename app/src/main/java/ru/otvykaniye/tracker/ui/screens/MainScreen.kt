package ru.otvykaniye.tracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.otvykaniye.tracker.TracklessState
import ru.otvykaniye.tracker.TracklessViewModel
import ru.otvykaniye.tracker.ui.components.*
import ru.otvykaniye.tracker.ui.theme.*

@Composable
fun AmbientBackground() {
    val gradient = Brush.radialGradient(
        colors = listOf(
            Emerald.copy(alpha = 0.15f),
            Color.Transparent,
            Amber.copy(alpha = 0.08f),
            Cyan.copy(alpha = 0.05f)
        ),
        radius = 1500f
    )
    Box(modifier = Modifier.fillMaxSize().background(BgDeep))
    Box(modifier = Modifier.fillMaxSize().background(gradient))
}

@Composable
fun MainScreen(viewModel: TracklessViewModel) {
    val state by viewModel.state.collectAsState()
    val timeSinceLast by viewModel.timeSinceLastEntry.collectAsState()

    var showSos by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    if (showSettings) {
        BackHandler { showSettings = false }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TRACKER", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(
                            text = "TrackLess",
                            style = TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White, Color(0xFFB6D8CA))
                                )
                            )
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Kind Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(13.dp))
                                .background(BgCard.copy(alpha = 0.8f))
                                .clickable { 
                                    viewModel.setKind(if (state.activeKind == "snus") "cigarette" else "snus")
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (state.activeKind == "snus") Strings.get(state.language, "snus") else Strings.get(state.language, "cigarettes"),
                                color = Emerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeroCard(
                    state = state,
                    timeSinceLast = timeSinceLast,
                    onRecord = { viewModel.recordUse("habit") },
                    onSos = { showSos = true },
                    onUndo = { viewModel.undoLastUse() }
                )
                
                StatsGrid(state, timeSinceLast, onDeleteEntry = { id -> viewModel.deleteEntry(id) })
                
                val profile = state.profiles[state.activeKind]
                if (profile != null) {
                    ChartComponent(state)
                    HourlyStats(state)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showSos) {
        SosDialog(state, onDismiss = { showSos = false })
    }
    
    if (showSettings) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettings = false })
    }
}

@Composable
fun HeroCard(state: TracklessState, timeSinceLast: Long, onRecord: () -> Unit, onSos: () -> Unit, onUndo: () -> Unit) {
    val lang = state.language
    ru.otvykaniye.tracker.ui.components.GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(Strings.get(lang, "time_passed"), color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                if ((state.profiles[state.activeKind]?.entries?.size ?: 0) > 0) {
                    IconButton(onClick = onUndo, modifier = Modifier.size(24.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Rounded.Undo, contentDescription = "Undo", tint = TextDim)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            val timeStr = formatTime(timeSinceLast)
            Text(timeStr, color = TextPrimary, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onRecord, 
                    modifier = Modifier.weight(1f).height(52.dp), 
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                ) {
                    Text(Strings.get(lang, "record"), color = BgDeep, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                OutlinedButton(
                    onClick = onSos, 
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Amber.copy(alpha = 0.5f))
                ) {
                    Text(Strings.get(lang, "craving_sos"), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    if (ms <= 0) return "— : — : —"
    val s = ms / 1000
    val h = s / 3600
    val m = (s % 3600) / 60
    val q = s % 60
    return String.format("%02d:%02d:%02d", h, m, q)
}