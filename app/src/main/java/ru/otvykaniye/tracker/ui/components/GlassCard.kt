package ru.otvykaniye.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.otvykaniye.tracker.ui.theme.BgCard

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    
    // Glass specular effect (inner top glow) and subtle border
    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.25f), // Specular highlight at top
            Color.White.copy(alpha = 0.05f)  // Dimmer at bottom
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp, 
                shape = shape, 
                spotColor = Color.Black, 
                ambientColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(shape)
            .background(BgCard.copy(alpha = 0.85f))
            .border(1.dp, borderBrush, shape),
        content = content
    )
}