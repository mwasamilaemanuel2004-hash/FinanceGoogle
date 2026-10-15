package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern Glassmorphism Styling for Production Fintech UI
 * Creates clean semi-translucent frosted glass effect with glowing borders.
 */
object GlassmorphismDefaults {
    val GlassSurface = Color(0xCC0E1626) // 80% opacity dark obsidian/navy
    val GlassSurfaceElevated = Color(0xD9141F36) // 85% opacity elevated
    val GlassSurfaceLight = Color(0x332F81F7) // 20% electric blue glow
    val GlassBorder = Color(0x40388BFD) // subtle cyan/blue glow edge
    val GlassBorderGold = Color(0x59EAB308) // subtle gold glow edge
    val GlassBorderSubtle = Color(0x26FFFFFF) // 15% white frost edge
}

fun Modifier.glassmorphism(
    cornerRadius: Dp = 12.dp,
    backgroundColor: Color = GlassmorphismDefaults.GlassSurface,
    borderColor: Color = GlassmorphismDefaults.GlassBorder,
    borderWidth: Dp = 1.dp
): Modifier = composed {
    this
        .clip(RoundedCornerShape(cornerRadius))
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    backgroundColor,
                    backgroundColor.copy(alpha = (backgroundColor.alpha * 0.85f).coerceIn(0.1f, 1f))
                )
            )
        )
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = listOf(
                    borderColor,
                    borderColor.copy(alpha = 0.2f),
                    borderColor.copy(alpha = 0.5f)
                )
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
}
