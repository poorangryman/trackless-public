package ru.otvykaniye.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.otvykaniye.tracker.TracklessViewModel
import ru.otvykaniye.tracker.ui.theme.*

@Composable
fun MainScreen(viewModel: TracklessViewModel) {
    val state by viewModel.state.collectAsState()
    val timeSinceLast by viewModel.timeSinceLastEntry.collectAsState()

    var showSos by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TrackLess",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                // Settings button etc.
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
                timeSinceLast = timeSinceLast,
                onRecord = { viewModel.recordUse("habit") },
                onSos = { showSos = true }
            )
            StatsGrid()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showSos) {
        SosDialog(onDismiss = { showSos = false })
    }
}

@Composable
fun HeroCard(
    timeSinceLast: Long,
    onRecord: () -> Unit,
    onSos: () -> Unit
) {
    ru.otvykaniye.tracker.ui.components.GlassCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("— последнего использовани€ прошло", color = TextDim, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            val timeStr = formatTime(timeSinceLast)
            Text(timeStr, color = TextPrimary, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRecord,
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                ) {
                    Text("«аписать", color = BgDeep, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onSos,
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber)
                ) {
                    Text("“€га SOS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatsGrid() {
    // Placeholder for stats grid
}

@Composable
fun SosDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("—правитьс€ с т€гой", color = Amber) },
        text = { Text("ќстра€ т€га длитс€ всего 3-5 минут. —делайте дыхательное упражнение и переждите волну.") },
        confirmButton = {
            Button(onClick = onDismiss) { Text("“€га отступила!") }
        },
        containerColor = BgCard
    )
}

fun formatTime(ms: Long): String {
    if (ms <= 0) return "Ч : Ч : Ч"
    val s = ms / 1000
    val h = s / 3600
    val m = (s % 3600) / 60
    val q = s % 60
    return String.format("%02d:%02d:%02d", h, m, q)
}

