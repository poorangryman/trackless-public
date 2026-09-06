package ru.otvykaniye.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.otvykaniye.tracker.TracklessState
import ru.otvykaniye.tracker.ui.theme.*

@Composable
fun SosDialog(state: TracklessState, onDismiss: () -> Unit) {
    val lang = state.language
    val inhaleStr = Strings.get(lang, "sos_inhale")
    val holdStr = Strings.get(lang, "sos_hold")
    val exhaleStr = Strings.get(lang, "sos_exhale")
    
    var phase by remember { mutableStateOf(inhaleStr) }
    var seconds by remember { mutableStateOf(4) }
    var totalTime by remember { mutableStateOf(180) }

    LaunchedEffect(Unit) {
        // 4-7-8 Breathing logic
        while (totalTime > 0) {
            phase = inhaleStr
            seconds = 4
            while (seconds > 0) { delay(1000); seconds--; totalTime-- }
            
            phase = holdStr
            seconds = 7
            while (seconds > 0) { delay(1000); seconds--; totalTime-- }
            
            phase = exhaleStr
            seconds = 8
            while (seconds > 0) { delay(1000); seconds--; totalTime-- }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = if (phase == inhaleStr) 0.5f else 1f,
        targetValue = if (phase == inhaleStr) 1f else if (phase == exhaleStr) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (phase == inhaleStr) 4000 else if (phase == exhaleStr) 8000 else 100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        title = { Text(Strings.get(lang, "sos_title"), color = Amber) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(Strings.get(lang, "sos_desc"), color = Color.LightGray)
                Spacer(Modifier.height(32.dp))
                
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Emerald.copy(alpha = 0.3f), radius = size.width / 2 * scale, center = Offset(size.width/2, size.height/2))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(phase, color = Emerald, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(seconds.toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                val m = totalTime / 60
                val s = totalTime % 60
                Text("${Strings.get(lang, "sos_remains")} ${String.format("%02d:%02d", m, s)}", color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Emerald)) {
                Text(Strings.get(lang, "sos_success"), color = BgDeep, fontWeight = FontWeight.Bold)
            }
        }
    )
}