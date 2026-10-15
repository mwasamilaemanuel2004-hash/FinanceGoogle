package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun OnlineStatusBar(
    isConnected: Boolean,
    isApiReady: Boolean,
    balance: Double,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "blink"
    )

    Surface(
        color = Color.Black.copy(alpha = 0.8f),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isConnected && isApiReady) AccentNeonGreen else DangerRed)
                    .then(if (isConnected && isApiReady) Modifier.graphicsLayer(alpha = alpha) else Modifier)
            )
            
            Text(
                text = if (isConnected && isApiReady) "● ONLINE 24/7" else "● OFFLINE - API ERROR",
                color = if (isConnected && isApiReady) AccentNeonGreen else DangerRed,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
            
            Text(
                text = "WS: ${if (isConnected) "LIVE" else "DISCONNECTED"}",
                color = TextWhite,
                fontSize = 11.sp
            )
            
            Text(
                text = "Data: LIVE 24/7",
                color = TextWhite,
                fontSize = 11.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = if (isConnected && isApiReady) "Binance: CONNECTED $${String.format("%,.2f", balance)}" else "Binance: NOT CONNECTED",
                color = if (isConnected && isApiReady) AccentNeonGreen else DangerRed,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
