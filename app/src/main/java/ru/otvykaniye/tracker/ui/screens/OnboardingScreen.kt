package ru.otvykaniye.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.otvykaniye.tracker.TracklessState
import ru.otvykaniye.tracker.ui.theme.*

@Composable
fun OnboardingScreen(state: TracklessState, onComplete: (TracklessState) -> Unit) {
    var setupStep by remember { mutableStateOf(1) }
    var activeKind by remember { mutableStateOf(state.activeKind) }

    Box(modifier = Modifier.fillMaxSize().background(BgDeep).padding(16.dp)) {
        if (setupStep == 1) {
            Column(Modifier.align(Alignment.Center).fillMaxWidth()) {
                Text("TrackLess", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text("Базовые настройки", fontSize = 20.sp, color = TextPrimary)
                Spacer(Modifier.height(24.dp))
                
                Text("Что отслеживать:", color = TextDim)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChoiceButton("Снюс", activeKind == "snus", Modifier.weight(1f)) { activeKind = "snus" }
                    ChoiceButton("Сигареты", activeKind == "cigarette", Modifier.weight(1f)) { activeKind = "cigarette" }
                }
                
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { setupStep = 2 },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                ) {
                    Text("Далее", color = BgDeep, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(Modifier.align(Alignment.Center).fillMaxWidth()) {
                Text("Настройка трекера", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(24.dp))
                // Just a simplified onboarding for now to get it compiling and working.
                Button(
                    onClick = { onComplete(state.copy(activeKind = activeKind, onboarded = true)) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                ) {
                    Text("Начать отслеживание", color = BgDeep, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChoiceButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(50.dp)
            .background(if (selected) BgCard else BgDeep, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Emerald else TextDim, fontWeight = FontWeight.Bold)
    }
}