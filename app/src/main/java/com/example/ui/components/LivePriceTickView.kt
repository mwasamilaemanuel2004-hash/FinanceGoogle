package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

/**
 * Visual Real-Time Tick Color Flashing Component.
 * Flashes vivid Neon Green when price ticks UP, and Vivid Crimson Red when price ticks DOWN.
 */
@Composable
fun LivePriceTickView(
    price: Double,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    showDirectionArrow: Boolean = true,
    showBackgroundFlash: Boolean = true
) {
    var previousPrice by remember { mutableStateOf(price) }
    var tickDirection by remember { mutableStateOf(0) } // 1: Up, -1: Down, 0: Neutral

    val animatedColor by animateColorAsState(
        targetValue = when (tickDirection) {
            1 -> Color(0xFF00E676)  // Bright Emerald Neon Green Flash
            -1 -> Color(0xFFFF1744) // Bright Vivid Crimson Red Flash
            else -> TextWhite
        },
        animationSpec = tween(durationMillis = 350),
        label = "tickFlashColor"
    )

    val animatedBg by animateColorAsState(
        targetValue = when (tickDirection) {
            1 -> Color(0xFF00E676).copy(alpha = 0.18f)
            -1 -> Color(0xFFFF1744).copy(alpha = 0.18f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 350),
        label = "tickFlashBg"
    )

    LaunchedEffect(price) {
        if (previousPrice > 0.0) {
            if (price > previousPrice) {
                tickDirection = 1
                delay(450L)
                tickDirection = 0
            } else if (price < previousPrice) {
                tickDirection = -1
                delay(450L)
                tickDirection = 0
            }
        }
        previousPrice = price
    }

    val formattedPrice = when {
        price >= 1000.0 -> String.format("%,.2f", price)
        price >= 1.0 -> String.format("%,.4f", price)
        else -> String.format("%.6f", price)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (showBackgroundFlash) animatedBg else Color.Transparent)
            .border(
                width = if (tickDirection != 0 && showBackgroundFlash) 1.dp else 0.dp,
                color = if (showBackgroundFlash) animatedColor.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = if (showBackgroundFlash) 6.dp else 0.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$$formattedPrice",
                style = textStyle,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = animatedColor
            )

            if (showDirectionArrow) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = if (tickDirection >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = animatedColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
