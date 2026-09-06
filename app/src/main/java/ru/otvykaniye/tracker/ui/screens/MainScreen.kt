package ru.otvykaniye.tracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.otvykaniye.tracker.TracklessViewModel
import ru.otvykaniye.tracker.ui.components.*
import ru.otvykaniye.tracker.ui.theme.*

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

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TrackLess", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Настройки", tint = TextDim)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroCard(
                timeSinceLast = timeSinceLast,
                onRecord = { viewModel.recordUse("habit") },
                onSos = { showSos = true }
            )
            
            StatsGrid(state, timeSinceLast)
            
            val profile = state.profiles[state.activeKind]
            if (profile != null) {
                ChartComponent(profile)
                HourlyStats(profile)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showSos) {
        SosDialog(onDismiss = { showSos = false })
    }
    
    if (showSettings) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettings = false })
    }
}

@Composable
fun HeroCard(timeSinceLast: Long, onRecord: () -> Unit, onSos: () -> Unit) {
    ru.otvykaniye.tracker.ui.components.GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("С последнего использования прошло", color = TextDim, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            val timeStr = formatTime(timeSinceLast)
            Text(timeStr, color = TextPrimary, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onRecord, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Emerald)) {
                    Text("Записать", color = BgDeep, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onSos, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber)) {
                    Text("Тяга SOS", fontWeight = FontWeight.Bold)
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