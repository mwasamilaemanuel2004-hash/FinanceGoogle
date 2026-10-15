package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.data.model.MarketType
import java.util.Locale

// High-Tech Cyber Obsidian Dark Palette
val DeepObsidian = Color(0xFF070B14)
val DarkCharcoal = Color(0xFF0E1626)
val SurfaceCard = Color(0xFF141F36)
val SurfaceElevated = Color(0xFF1A2A4A)
val BorderSubtle = Color(0xFF233558)
val BorderGlow = Color(0xFF388BFD)

val TextWhite = Color(0xFFF0F6FC)
val TextMuted = Color(0xFF8B949E)
val TextDim = Color(0xFF586069)

// Accent Colors
val AccentElectricBlue = Color(0xFF2F81F7)
val AccentCyan = Color(0xFF00E5FF)
val AccentNeonGreen = Color(0xFF2EA043)
val AccentRed = Color(0xFFF85149)
val AccentOrange = Color(0xFFD29922)
val AccentLime = Color(0xFFA3E635)
val AccentEmerald = Color(0xFF10B981)
val AccentCoral = Color(0xFFF43F5E)
val AccentLavender = Color(0xFFC084FC)
val AccentTerracotta = Color(0xFFEA580C)
val AccentGold = Color(0xFFEAB308)
val AccentRose = Color(0xFFFB7185)
val AccentOcean = Color(0xFF0284C7)
val AccentAmber = Color(0xFFF59E0B)

// Status & Warnings
val DangerRed = Color(0xFFDA3633)
val DangerRedGlow = Color(0x33DA3633)
val WarningOrange = Color(0xFFD29922)
val WarningOrangeGlow = Color(0x33D29922)
val PrimaryEmerald = Color(0xFF238636)
val EmeraldGreen = Color(0xFF3FB950)
val BackgroundDark = Color(0xFF050811)
val BackgroundSurface = Color(0xFF0A0F1D)

// Lavender theme specials
val LavenderGlow = Color(0xFFD8B4FE)
val LavenderPrimary = Color(0xFFA855F7)
val LavenderDark = Color(0xFF1A102F)
val LavenderGlowAlpha = Color(0x33A855F7)

// Electric Blue theme specials
val ElectricBlue = Color(0xFF3B82F6)
val ElectricCyan = Color(0xFF06B6D4)
val ElectricBlueNeon = Color(0xFF60A5FA)
val ElectricBlueGlowAlpha = Color(0x333B82F6)

val EmeraldNeon = Color(0xFF34D399)
val FinnovaBlue = Color(0xFF2563EB)
val FinnovaCobalt = Color(0xFF1D4ED8)
val FinnovaCyan = Color(0xFF38BDF8)
val HeliosAmber = Color(0xFFF59E0B)

// Market-Specific Distinct Colors
val MarketForexGreen = Color(0xFF00E676)
val MarketForexGreenGlow = Color(0x4000E676)
val MarketCryptoDarkPurple = Color(0xFF9C27B0)
val MarketCryptoDarkPurpleDeep = Color(0xFF4A148C)
val MarketCryptoDarkPurpleGlow = Color(0x409C27B0)
val MarketStocksBlue = Color(0xFF2979FF)
val MarketStocksBlueGlow = Color(0x402979FF)
val MarketCommoditiesViolet = Color(0xFF7C4DFF)
val MarketCommoditiesWhite = Color(0xFFEDE7F6)
val MarketCommoditiesVioletGlow = Color(0x407C4DFF)
val MarketDerivOrange = Color(0xFFFF6D00)
val MarketDerivOrangeGlow = Color(0x40FF6D00)

data class AccentColorItem(val name: String, val color: Color)
data class MarketColorItem(val market: String, val color: Color, val glow: Color)

val All14AccentColors = listOf(
    AccentColorItem("Electric Blue", AccentElectricBlue),
    AccentColorItem("Cyan Glow", AccentCyan),
    AccentColorItem("Neon Green", AccentNeonGreen),
    AccentColorItem("Crimson Red", AccentRed),
    AccentColorItem("Amber Orange", AccentOrange),
    AccentColorItem("Lime Pulse", AccentLime),
    AccentColorItem("Emerald Alpha", AccentEmerald),
    AccentColorItem("Coral Flare", AccentCoral),
    AccentColorItem("Lavender Dream", AccentLavender),
    AccentColorItem("Terracotta Warmth", AccentTerracotta),
    AccentColorItem("Sovereign Gold", AccentGold),
    AccentColorItem("Rose Horizon", AccentRose),
    AccentColorItem("Ocean Deep", AccentOcean),
    AccentColorItem("Amber Flame", AccentAmber)
)

val All5MarketColors = listOf(
    MarketColorItem("Forex", MarketForexGreen, MarketForexGreenGlow),
    MarketColorItem("Crypto", MarketCryptoDarkPurple, MarketCryptoDarkPurpleGlow),
    MarketColorItem("Stocks", MarketStocksBlue, MarketStocksBlueGlow),
    MarketColorItem("Commodities", MarketCommoditiesViolet, MarketCommoditiesVioletGlow),
    MarketColorItem("Deriv Synthetics", MarketDerivOrange, MarketDerivOrangeGlow)
)

fun getMarketThemeColor(marketType: MarketType): Color = when (marketType) {
    MarketType.FOREX -> MarketForexGreen
    MarketType.CRYPTO -> Color(0xFF8B5CF6) // Vibrant Violet/Purple for Crypto
    MarketType.STOCKS -> MarketStocksBlue
    MarketType.METALS -> AccentGold // Gold for Metals
    MarketType.COMMODITIES -> AccentAmber // Amber for Commodities
    MarketType.DERIV -> MarketDerivOrange // Orange for Deriv Synthetics
    MarketType.INDICES -> AccentCyan
    MarketType.ETFS -> AccentElectricBlue // Electric Blue for ETFs
}

fun getMarketThemeColorByName(marketSymbolOrName: String): Color {
    val upper = marketSymbolOrName.uppercase(Locale.ROOT)
    if (upper.contains("BTC") || upper.contains("ETH") || upper.contains("SOL") || upper.contains("CRYPTO") || upper.contains("USDT")) {
        return MarketCryptoDarkPurple
    }
    if (upper.contains("EUR") || upper.contains("USD") || upper.contains("GBP") || upper.contains("JPY") || upper.contains("FOREX")) {
        return MarketForexGreen
    }
    if (upper.contains("AAPL") || upper.contains("TSLA") || upper.contains("NVDA") || upper.contains("STOCK") || upper.contains("EQUITY") || upper.contains("CRDB") || upper.contains("NMB") || upper.contains("DSE")) {
        return MarketStocksBlue
    }
    if (upper.contains("XAU") || upper.contains("GOLD") || upper.contains("SILVER") || upper.contains("OIL") || upper.contains("COMMODITY")) {
        return MarketCommoditiesViolet
    }
    if (upper.contains("VOLATILITY") || upper.contains("CRASH") || upper.contains("BOOM") || upper.contains("DERIV") || upper.contains("SYNTHETIC") || upper.contains("INDEX")) {
        return MarketDerivOrange
    }
    return AccentElectricBlue
}
