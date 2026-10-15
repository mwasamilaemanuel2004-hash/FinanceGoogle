package com.example.ui.components

import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*

@Composable
fun OnlineStatusBar(isOnline: Boolean, statusText: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "blink"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(if (isOnline) Color(0xFF00FF00) else Color.Red, CircleShape)
                .let { if (isOnline) it.alpha(alpha) else it }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isOnline) "● ONLINE 24/7 | WS: LIVE | Data: LIVE 24/7" else "● OFFLINE - API NOT CONNECTED",
            style = MaterialTheme.typography.labelSmall,
            color = if (isOnline) Color(0xFF00FF00) else Color.Red,
            fontSize = 12.sp
        )
    }
}
