package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.EsmhDestination
import com.example.ui.navigation.NavigationGroup
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun EsmhBottomNav(
    currentDestination: EsmhDestination,
    isSwahili: Boolean,
    onNavigate: (EsmhDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryDestinations = listOf(
        EsmhDestination.HOME,
        EsmhDestination.TRADE,
        EsmhDestination.MARKETS,
        EsmhDestination.BOTS,
        EsmhDestination.PORTFOLIO,
        EsmhDestination.SETTINGS
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = modifier
    ) {
        primaryDestinations.forEach { dest ->
            val isSelected = currentDestination == dest
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(dest) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = if (isSwahili) dest.titleSw else dest.titleEn
                    )
                },
                label = {
                    Text(
                        text = if (isSwahili) dest.titleSw else dest.titleEn,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = SurfaceElevated
                )
            )
        }
    }
}
