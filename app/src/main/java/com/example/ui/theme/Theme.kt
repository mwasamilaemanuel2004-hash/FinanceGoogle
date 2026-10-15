package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class DashboardThemeMode(
    val id: Int,
    val title: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val isDark: Boolean = true
) {
    NAVY_ELECTRIC_BLUE(1, "Navy + Electric Blue", AccentElectricBlue, Color(0xFF00E5FF), Color(0xFF070B14), Color(0xFF0E1626)),
    CHARCOAL_LIME(2, "Charcoal + Lime", AccentLime, AccentNeonGreen, Color(0xFF111827), Color(0xFF1F2937)),
    EMERALD_MINT(3, "Emerald + Mint", AccentNeonGreen, AccentEmerald, Color(0xFF062016), Color(0xFF0B3324)),
    TEAL_CORAL(4, "Teal + Coral", AccentCoral, Color(0xFF00B4D8), Color(0xFF021B2B), Color(0xFF052A42)),
    PLUM_LAVENDER(5, "Plum + Lavender", AccentLavender, LavenderGlow, Color(0xFF170D28), Color(0xFF261541)),
    TERRACOTTA_CREAM(6, "Terracotta + Cream", AccentTerracotta, Color(0xFFFFB703), Color(0xFF1A120B), Color(0xFF2C1D11)),
    MIDNIGHT_GOLD(7, "Midnight + Gold", AccentGold, Color(0xFFFFE66D), Color(0xFF0F0E17), Color(0xFF1F1D2B)),
    ROSE_GRAPHITE(8, "Rose + Graphite", AccentRose, Color(0xFFFF758F), Color(0xFF1A141A), Color(0xFF281E29)),
    OCEAN_SAND(9, "Ocean + Sand", AccentOcean, Color(0xFF38BDF8), Color(0xFF081B2B), Color(0xFF0F2D48)),
    FOREST_AMBER(10, "Forest + Amber", AccentAmber, AccentGold, Color(0xFF0A1810), Color(0xFF142B1D));

    companion object {
        val STUDIO_DARK = NAVY_ELECTRIC_BLUE
        val STUDIO_LIGHT = TERRACOTTA_CREAM
        val STUDIO_LAVENDER_ELECTRIC = PLUM_LAVENDER
        val CYBER_EMERALD = EMERALD_MINT
        val FINNOVA_BLUE = OCEAN_SAND
        val HELIOS_OBSIDIAN = MIDNIGHT_GOLD
    }
}

val LocalThemeMode = staticCompositionLocalOf { DashboardThemeMode.NAVY_ELECTRIC_BLUE }

@Composable
fun MwasaAiTheme(
    currentTheme: DashboardThemeMode = DashboardThemeMode.NAVY_ELECTRIC_BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = currentTheme.primaryColor,
        onPrimary = Color.White,
        secondary = currentTheme.secondaryColor,
        onSecondary = Color.Black,
        background = currentTheme.backgroundColor,
        onBackground = TextWhite,
        surface = currentTheme.surfaceColor,
        onSurface = TextWhite,
        surfaceVariant = currentTheme.surfaceColor.copy(alpha = 0.7f),
        onSurfaceVariant = TextMuted,
        outline = BorderSubtle,
        error = DangerRed,
        onError = Color.White
    )

    CompositionLocalProvider(LocalThemeMode provides currentTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
