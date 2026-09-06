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
import androidx.compose.ui.unit.dp
import ru.otvykaniye.tracker.ui.theme.BgCard
import ru.otvykaniye.tracker.ui.theme.GlassBorder

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .shadow(elevation = 10.dp, shape = shape, spotColor = androidx.compose.ui.graphics.Color.Black, ambientColor = androidx.compose.ui.graphics.Color.Black)
            .clip(shape)
            .background(BgCard.copy(alpha = 0.82f))
            .border(1.dp, GlassBorder, shape),
        content = content
    )
}

