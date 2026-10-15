package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PriceCandle
import com.example.ui.theme.*

/**
 * Themes for Candlestick charts with distinct Bull, Bear, and Light Edge colors.
 */
enum class CandleThemeMode(
    val id: String,
    val nameSwahili: String,
    val nameEn: String,
    val bullColor: Color,
    val bearColor: Color,
    val edgeBullColor: Color,
    val edgeBearColor: Color
) {
    CLASSIC(
        id = "classic",
        nameSwahili = "Kijani & Nyekundu",
        nameEn = "Classic Green / Red",
        bullColor = Color(0xFF00E676),      // Neon Green
        bearColor = Color(0xFFFF1744),      // Vivid Red
        edgeBullColor = Color(0xFFB9F6CA),  // Luminous Green Light Edge
        edgeBearColor = Color(0xFFFF8A80)   // Luminous Red Light Edge
    ),
    CYBERPUNK(
        id = "cyberpunk",
        nameSwahili = "Cyan & Magenta (Cyber)",
        nameEn = "Cyan / Neon Magenta",
        bullColor = Color(0xFF00E5FF),      // Vivid Cyan
        bearColor = Color(0xFFFF007F),      // Hot Neon Fuchsia
        edgeBullColor = Color(0xFFE0F7FA),  // Glowing Ice Cyan Edge
        edgeBearColor = Color(0xFFFF80AB)   // Glowing Magenta Edge
    ),
    EMERALD_GOLD(
        id = "emerald_gold",
        nameSwahili = "Zumaridi & Dhahabu",
        nameEn = "Emerald & Pure Gold",
        bullColor = Color(0xFF10B981),      // Emerald Green
        bearColor = Color(0xFFFFB300),      // Sovereign Amber Gold
        edgeBullColor = Color(0xFFA7F3D0),  // Light Emerald Edge
        edgeBearColor = Color(0xFFFFE082)   // Light Golden Edge
    ),
    PRO_WHITE(
        id = "pro_white",
        nameSwahili = "Nyeupe & Crimson (Pro)",
        nameEn = "Ice White / Crimson",
        bullColor = Color(0xFFF8FAFC),      // Crisp Ice White
        bearColor = Color(0xFFE11D48),      // Deep Rose Crimson
        edgeBullColor = Color(0xFFFFFFFF),  // Pure White Light Edge
        edgeBearColor = Color(0xFFFDA4AF)   // Light Rose Edge
    ),
    AQUA_PURPLE(
        id = "aqua_purple",
        nameSwahili = "Baharini & Zambarau",
        nameEn = "Aquamarine & Purple",
        bullColor = Color(0xFF14B8A6),      // Teal Aqua
        bearColor = Color(0xFFD946EF),      // Electric Violet
        edgeBullColor = Color(0xFF99F6E4),  // Aquamarine Light Edge
        edgeBearColor = Color(0xFFF5D0FE)   // Lilac Light Edge
    )
}

enum class ChartSizeMode(val labelSwahili: String, val labelEn: String, val heightDp: Int) {
    STANDARD("Kawaida", "Standard", 190),
    EXPANDED("Ndefu", "Tall", 350),
    FULL_PAGE("Jaza Ukurasa", "Full Page", -1)
}

@Composable
fun CandlestickChartView(
    candles: List<PriceCandle>,
    isSwahili: Boolean,
    modifier: Modifier = Modifier,
    initialTheme: CandleThemeMode = CandleThemeMode.CLASSIC,
    showControls: Boolean = true,
    symbol: String = "BTC/USDT",
    currentPrice: Double = 0.0,
    onQuickBuy: (() -> Unit)? = null,
    onQuickSell: (() -> Unit)? = null
) {
    var selectedTheme by remember { mutableStateOf(initialTheme) }
    var enableLightEdge by remember { mutableStateOf(true) }
    var isHollowBull by remember { mutableStateOf(false) }
    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }
    var showColorMenu by remember { mutableStateOf(false) }
    var chartSize by remember { mutableStateOf(ChartSizeMode.STANDARD) }
    var showFullPageModal by remember { mutableStateOf(false) }

    // Indicator states
    var showEma by remember { mutableStateOf(true) }
    var showBollinger by remember { mutableStateOf(false) }
    var showVolume by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Controls Row
        if (showControls) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color Theme Pill button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, selectedTheme.bullColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { showColorMenu = !showColorMenu }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Candle Color",
                        tint = selectedTheme.bullColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isSwahili) selectedTheme.nameSwahili else selectedTheme.nameEn,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(selectedTheme.bullColor)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(selectedTheme.bearColor)
                    )
                }

                // Chart Size & Fullscreen Action Group ("JAZA UKURASA")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Light Edge Toggle Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (enableLightEdge) AccentCyan.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (enableLightEdge) AccentCyan else BorderSubtle,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { enableLightEdge = !enableLightEdge }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Flare,
                                contentDescription = "Light Edge",
                                tint = if (enableLightEdge) AccentCyan else TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Glow",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (enableLightEdge) FontWeight.Black else FontWeight.Normal,
                                color = if (enableLightEdge) AccentCyan else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Height Toggle (Standard <-> Tall)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (chartSize == ChartSizeMode.EXPANDED) AccentGold.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (chartSize == ChartSizeMode.EXPANDED) AccentGold else BorderSubtle,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                chartSize = if (chartSize == ChartSizeMode.STANDARD) {
                                    ChartSizeMode.EXPANDED
                                } else {
                                    ChartSizeMode.STANDARD
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (chartSize == ChartSizeMode.EXPANDED) (if (isSwahili) "Ndefu" else "Tall") else (if (isSwahili) "Fupi" else "Compact"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (chartSize == ChartSizeMode.EXPANDED) AccentGold else TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    // Prominent JAZA UKURASA (Full Page Mode) Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentNeonGreen.copy(alpha = 0.22f))
                            .border(
                                width = 1.dp,
                                color = AccentNeonGreen,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { showFullPageModal = true }
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Jaza Ukurasa",
                                tint = AccentNeonGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isSwahili) "Jaza Ukurasa" else "Full Page",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentNeonGreen,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Quick Theme Selector Strip (Horizontally Scrollable Themes)
            AnimatedVisibility(visible = showColorMenu) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    items(CandleThemeMode.values()) { theme ->
                        val isChosen = theme == selectedTheme
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) theme.bullColor.copy(alpha = 0.18f) else SurfaceElevated)
                                .border(
                                    width = if (isChosen) 1.5.dp else 1.dp,
                                    color = if (isChosen) theme.bullColor else BorderSubtle,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedTheme = theme
                                    showColorMenu = false
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(theme.bullColor)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(theme.bearColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSwahili) theme.nameSwahili else theme.nameEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isChosen) FontWeight.Black else FontWeight.Medium,
                                    color = if (isChosen) TextWhite else TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Inspected Candle HUD
        val inspectedCandle = selectedCandleIndex?.let { idx ->
            candles.getOrNull(idx)
        } ?: candles.lastOrNull()

        if (inspectedCandle != null) {
            val isBull = inspectedCandle.close >= inspectedCandle.open
            val candleDelta = inspectedCandle.close - inspectedCandle.open
            val deltaPct = if (inspectedCandle.open > 0) (candleDelta / inspectedCandle.open) * 100.0 else 0.0

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "O: ${formatPrice(inspectedCandle.open)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "H: ${formatPrice(inspectedCandle.high)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "L: ${formatPrice(inspectedCandle.low)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "C: ${formatPrice(inspectedCandle.close)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isBull) selectedTheme.bullColor else selectedTheme.bearColor,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = "${if (deltaPct >= 0) "+" else ""}${String.format("%.2f", deltaPct)}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (isBull) selectedTheme.bullColor else selectedTheme.bearColor,
                    fontSize = 10.sp
                )
            }
        }

        // CANDLESTICK CANVAS WITH DYNAMIC HEIGHT
        val actualHeight = if (chartSize == ChartSizeMode.EXPANDED) 350.dp else 190.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(actualHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF070B14))
                .border(1.dp, BorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        ) {
            CandleCanvasCore(
                candles = candles,
                selectedTheme = selectedTheme,
                enableLightEdge = enableLightEdge,
                isHollowBull = isHollowBull,
                showEma = showEma,
                showBollinger = showBollinger,
                showVolume = showVolume,
                selectedCandleIndex = selectedCandleIndex,
                onSelectCandleIndex = { selectedCandleIndex = it }
            )

            // Floating Fullscreen Trigger in bottom-right of chart
            IconButton(
                onClick = { showFullPageModal = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Expand to Full Page",
                    tint = AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // FULL PAGE MODAL OVERLAY ("JAZA UKURASA / SKRINI KAMILI")
    if (showFullPageModal) {
        FullPageCandleDialog(
            candles = candles,
            symbol = symbol,
            currentPrice = if (currentPrice > 0.0) currentPrice else (candles.lastOrNull()?.close ?: 0.0),
            isSwahili = isSwahili,
            selectedTheme = selectedTheme,
            onThemeChange = { selectedTheme = it },
            enableLightEdge = enableLightEdge,
            onToggleLightEdge = { enableLightEdge = !enableLightEdge },
            isHollowBull = isHollowBull,
            onToggleHollow = { isHollowBull = !isHollowBull },
            onClose = { showFullPageModal = false },
            onQuickBuy = onQuickBuy,
            onQuickSell = onQuickSell
        )
    }
}

/**
 * Fullscreen / Full Page Trading Dialog ("Jaza Ukurasa").
 * Takes up 100% of the screen width and height.
 */
@Composable
fun FullPageCandleDialog(
    candles: List<PriceCandle>,
    symbol: String,
    currentPrice: Double,
    isSwahili: Boolean,
    selectedTheme: CandleThemeMode,
    onThemeChange: (CandleThemeMode) -> Unit,
    enableLightEdge: Boolean,
    onToggleLightEdge: () -> Unit,
    isHollowBull: Boolean,
    onToggleHollow: () -> Unit,
    onClose: () -> Unit,
    onQuickBuy: (() -> Unit)? = null,
    onQuickSell: (() -> Unit)? = null
) {
    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }
    var showEma by remember { mutableStateOf(true) }
    var showBollinger by remember { mutableStateOf(false) }
    var showVolume by remember { mutableStateOf(true) }
    var selectedTimeframe by remember { mutableStateOf("1m") }
    var showColorMenu by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF060911)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Top Fullscreen Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "Exit Fullscreen",
                                tint = AccentCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = symbol,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentNeonGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isSwahili) "UKURASA MZIMA" else "FULL PAGE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = AccentNeonGreen,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Text(
                                text = "$${formatPrice(currentPrice)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                        }
                    }

                    // Top Action Controls (Indicators & Close)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Palette Button
                        IconButton(
                            onClick = { showColorMenu = !showColorMenu },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Colors",
                                tint = selectedTheme.bullColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Light Edge Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (enableLightEdge) AccentCyan.copy(alpha = 0.2f) else SurfaceElevated)
                                .border(1.dp, if (enableLightEdge) AccentCyan else BorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { onToggleLightEdge() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Glow",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (enableLightEdge) AccentCyan else TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        // Close button
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Color Theme Selector Drawer in Fullscreen
                AnimatedVisibility(visible = showColorMenu) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D1424))
                            .padding(8.dp)
                    ) {
                        items(CandleThemeMode.values()) { theme ->
                            val isChosen = theme == selectedTheme
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) theme.bullColor.copy(alpha = 0.25f) else SurfaceElevated)
                                    .border(
                                        width = if (isChosen) 2.dp else 1.dp,
                                        color = if (isChosen) theme.bullColor else BorderSubtle,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onThemeChange(theme)
                                        showColorMenu = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(theme.bullColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(theme.bearColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSwahili) theme.nameSwahili else theme.nameEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isChosen) FontWeight.Black else FontWeight.Normal,
                                        color = if (isChosen) TextWhite else TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Timeframe Bar & Indicator Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0F1D))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timeframes
                    val tfs = listOf("1m", "5m", "15m", "1h", "4h", "1D", "1W")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        tfs.forEach { tf ->
                            val isSel = tf == selectedTimeframe
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) AccentCyan else SurfaceElevated)
                                    .clickable { selectedTimeframe = tf }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = tf,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Indicator Toggle Chips (EMA, BB, VOL)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IndicatorChip(label = "EMA", active = showEma, color = AccentGold) {
                            showEma = !showEma
                        }
                        IndicatorChip(label = "BB", active = showBollinger, color = AccentLavender) {
                            showBollinger = !showBollinger
                        }
                        IndicatorChip(label = "VOL", active = showVolume, color = AccentEmerald) {
                            showVolume = !showVolume
                        }
                    }
                }

                // Active Inspected Candle Telemetry Bar
                val inspected = selectedCandleIndex?.let { candles.getOrNull(it) } ?: candles.lastOrNull()
                if (inspected != null) {
                    val isBull = inspected.close >= inspected.open
                    val delta = inspected.close - inspected.open
                    val pct = if (inspected.open > 0) (delta / inspected.open) * 100.0 else 0.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090D18))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("O: ${formatPrice(inspected.open)}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("H: ${formatPrice(inspected.high)}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("L: ${formatPrice(inspected.low)}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                "C: ${formatPrice(inspected.close)}",
                                color = if (isBull) selectedTheme.bullColor else selectedTheme.bearColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "${if (pct >= 0) "+" else ""}${String.format("%.2f", pct)}%",
                            color = if (isBull) selectedTheme.bullColor else selectedTheme.bearColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // THE FULL-PAGE CANVAS TAKES UP ALL AVAILABLE HEIGHT!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF050811))
                ) {
                    CandleCanvasCore(
                        candles = candles,
                        selectedTheme = selectedTheme,
                        enableLightEdge = enableLightEdge,
                        isHollowBull = isHollowBull,
                        showEma = showEma,
                        showBollinger = showBollinger,
                        showVolume = showVolume,
                        selectedCandleIndex = selectedCandleIndex,
                        onSelectCandleIndex = { selectedCandleIndex = it },
                        showRightPriceAxis = true
                    )
                }

                // Bottom Trading Bar in Full Page Mode
                Surface(
                    color = SurfaceElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onQuickBuy?.invoke() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = "Buy", tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "NUNUA (BUY)" else "BUY ORDER",
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Button(
                            onClick = { onQuickSell?.invoke() },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Icon(Icons.Default.TrendingDown, contentDescription = "Sell", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "UZA (SELL)" else "SELL ORDER",
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "Exit",
                                tint = AccentCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IndicatorChip(label: String, active: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) color.copy(alpha = 0.2f) else SurfaceElevated)
            .border(1.dp, if (active) color else BorderSubtle, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (active) FontWeight.Black else FontWeight.Normal,
            color = if (active) color else TextMuted,
            fontSize = 9.sp
        )
    }
}

/**
 * Core Canvas Drawing for Candlesticks, Wicks, Light Edges, Indicators (EMA, Bollinger, Volume), and Price Scale.
 */
@Composable
private fun CandleCanvasCore(
    candles: List<PriceCandle>,
    selectedTheme: CandleThemeMode,
    enableLightEdge: Boolean,
    isHollowBull: Boolean,
    showEma: Boolean,
    showBollinger: Boolean,
    showVolume: Boolean,
    selectedCandleIndex: Int?,
    onSelectCandleIndex: (Int?) -> Unit,
    showRightPriceAxis: Boolean = false
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(candles) {
                detectTapGestures { offset ->
                    if (candles.isNotEmpty()) {
                        val cWidth = size.width / candles.size
                        val idx = (offset.x / cWidth).toInt().coerceIn(0, candles.size - 1)
                        onSelectCandleIndex(idx)
                    }
                }
            }
            .pointerInput(candles) {
                detectHorizontalDragGestures(
                    onDragEnd = { /* keep selection */ },
                    onHorizontalDrag = { change, _ ->
                        if (candles.isNotEmpty()) {
                            val cWidth = size.width / candles.size
                            val idx = (change.position.x / cWidth).toInt().coerceIn(0, candles.size - 1)
                            onSelectCandleIndex(idx)
                        }
                    }
                )
            }
    ) {
        if (candles.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height

        // Volume area occupies the bottom 18% of canvas if enabled
        val chartBottomH = if (showVolume) h * 0.82f else h

        val minPrice = candles.minOf { it.low }
        val maxPrice = candles.maxOf { it.high }
        val rawRange = maxPrice - minPrice
        val priceRange = if (rawRange > 0.0) rawRange * 1.10 else 1.0
        val bottomBound = minPrice - (priceRange * 0.05)

        val candleWidth = w / candles.size.coerceAtLeast(1)

        // 1. Draw subtle background price grid levels (20%, 40%, 60%, 80%)
        val gridLevels = listOf(0.20f, 0.40f, 0.60f, 0.80f)
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

        gridLevels.forEach { lvl ->
            val y = chartBottomH * lvl
            drawLine(
                color = Color.White.copy(alpha = 0.06f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )
        }

        // 2. Draw Volume Profile Bars at the bottom
        if (showVolume) {
            val maxVol = candles.maxOfOrNull { it.volume } ?: 1.0
            val volAreaTop = chartBottomH + (h * 0.02f)
            val volAreaHeight = h - volAreaTop

            // Divider line above volume
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, chartBottomH),
                end = Offset(w, chartBottomH),
                strokeWidth = 1f
            )

            candles.forEachIndexed { i, c ->
                val isBull = c.close >= c.open
                val volRatio = if (maxVol > 0.0) (c.volume / maxVol).toFloat().coerceIn(0.05f, 1f) else 0.2f
                val barH = volAreaHeight * volRatio
                val x = i * candleWidth + (candleWidth * 0.15f)
                val barW = candleWidth * 0.70f
                val volColor = if (isBull) selectedTheme.bullColor.copy(alpha = 0.40f) else selectedTheme.bearColor.copy(alpha = 0.40f)

                drawRect(
                    color = volColor,
                    topLeft = Offset(x, h - barH),
                    size = Size(barW, barH)
                )
            }
        }

        // 3. Draw Bollinger Bands if enabled
        if (showBollinger && candles.size >= 5) {
            val period = 10
            val upperPoints = mutableListOf<Offset>()
            val lowerPoints = mutableListOf<Offset>()

            for (i in period - 1 until candles.size) {
                val sub = candles.subList(i - period + 1, i + 1).map { it.close }
                val sma = sub.average()
                val variance = sub.map { (it - sma) * (it - sma) }.average()
                val stdDev = Math.sqrt(variance)
                val upper = sma + 2 * stdDev
                val lower = sma - 2 * stdDev

                val x = i * candleWidth + candleWidth / 2f
                val yUp = (chartBottomH - ((upper - bottomBound) / priceRange * chartBottomH)).toFloat().coerceIn(0f, chartBottomH)
                val yLow = (chartBottomH - ((lower - bottomBound) / priceRange * chartBottomH)).toFloat().coerceIn(0f, chartBottomH)

                upperPoints.add(Offset(x, yUp))
                lowerPoints.add(Offset(x, yLow))
            }

            for (k in 0 until upperPoints.size - 1) {
                drawLine(
                    color = Color(0xFFC084FC).copy(alpha = 0.6f),
                    start = upperPoints[k],
                    end = upperPoints[k + 1],
                    strokeWidth = 1.2f
                )
                drawLine(
                    color = Color(0xFFC084FC).copy(alpha = 0.6f),
                    start = lowerPoints[k],
                    end = lowerPoints[k + 1],
                    strokeWidth = 1.2f
                )
            }
        }

        // 4. Draw EMA (Exponential Moving Average) line if enabled
        if (showEma && candles.size >= 3) {
            val period = minOf(9, candles.size)
            val mult = 2.0 / (period + 1.0)
            var currentEma = candles.take(period).map { it.close }.average()
            val emaOffsets = mutableListOf<Offset>()

            for (i in candles.indices) {
                if (i >= period - 1) {
                    val price = candles[i].close
                    currentEma = (price - currentEma) * mult + currentEma
                    val x = i * candleWidth + candleWidth / 2f
                    val y = (chartBottomH - ((currentEma - bottomBound) / priceRange * chartBottomH)).toFloat().coerceIn(0f, chartBottomH)
                    emaOffsets.add(Offset(x, y))
                }
            }

            for (k in 0 until emaOffsets.size - 1) {
                drawLine(
                    color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                    start = emaOffsets[k],
                    end = emaOffsets[k + 1],
                    strokeWidth = 1.8f
                )
            }
        }

        // 5. Draw Candlesticks with Wicks, Body, and Light Edges
        candles.forEachIndexed { i, c ->
            val isBull = c.close >= c.open
            val bodyColor = if (isBull) selectedTheme.bullColor else selectedTheme.bearColor
            val edgeColor = if (isBull) selectedTheme.edgeBullColor else selectedTheme.edgeBearColor
            val x = i * candleWidth + candleWidth / 2f

            // Vertical wick
            val highY = (chartBottomH - ((c.high - bottomBound) / priceRange * chartBottomH)).toFloat().coerceIn(0f, chartBottomH)
            val lowY = (chartBottomH - ((c.low - bottomBound) / priceRange * chartBottomH)).toFloat().coerceIn(0f, chartBottomH)

            drawLine(
                color = bodyColor.copy(alpha = 0.9f),
                start = Offset(x, highY),
                end = Offset(x, lowY),
                strokeWidth = 2.0f
            )

            // Body coordinates
            val openY = (chartBottomH - ((c.open - bottomBound) / priceRange * chartBottomH)).toFloat().coerceIn(0f, chartBottomH)
            val closeY = (chartBottomH - ((c.close - bottomBound) / priceRange * chartBottomH)).toFloat().coerceIn(0f, chartBottomH)
            val top = minOf(openY, closeY)
            val bottom = maxOf(openY, closeY)
            val bodyHeight = maxOf(bottom - top, 2.5f)
            val candleBodyWidth = candleWidth * 0.72f
            val left = x - candleBodyWidth / 2f

            // Candle Body Fill
            if (isBull && isHollowBull) {
                drawRect(
                    color = bodyColor.copy(alpha = 0.20f),
                    topLeft = Offset(left, top),
                    size = Size(candleBodyWidth, bodyHeight)
                )
            } else {
                drawRect(
                    color = bodyColor,
                    topLeft = Offset(left, top),
                    size = Size(candleBodyWidth, bodyHeight)
                )
            }

            // LIGHT EDGE (ON TOP OF BODY FOR LUMINOUS GLOW)
            if (enableLightEdge) {
                drawRect(
                    color = edgeColor.copy(alpha = 0.95f),
                    topLeft = Offset(left, top),
                    size = Size(candleBodyWidth, bodyHeight),
                    style = Stroke(width = 1.5f)
                )

                // Reflection sheen
                drawLine(
                    color = Color.White.copy(alpha = 0.50f),
                    start = Offset(left, top),
                    end = Offset(left + candleBodyWidth, top),
                    strokeWidth = 1.2f
                )
            } else {
                drawRect(
                    color = Color.Black.copy(alpha = 0.4f),
                    topLeft = Offset(left, top),
                    size = Size(candleBodyWidth, bodyHeight),
                    style = Stroke(width = 0.8f)
                )
            }
        }

        // 6. Current Latest Price Dotted Guide Line
        val latestCandle = candles.lastOrNull()
        if (latestCandle != null) {
            val latestY = (chartBottomH - ((latestCandle.close - bottomBound) / priceRange * chartBottomH)).toFloat().coerceIn(0f, chartBottomH)
            val latestColor = if (latestCandle.close >= latestCandle.open) selectedTheme.bullColor else selectedTheme.bearColor

            drawLine(
                color = latestColor.copy(alpha = 0.85f),
                start = Offset(0f, latestY),
                end = Offset(w, latestY),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )
        }

        // 7. Selected / Inspected Crosshair
        selectedCandleIndex?.let { idx ->
            if (idx in candles.indices) {
                val c = candles[idx]
                val x = idx * candleWidth + candleWidth / 2f
                val closeY = (chartBottomH - ((c.close - bottomBound) / priceRange * chartBottomH)).toFloat().coerceIn(0f, chartBottomH)

                // Vertical Crosshair
                drawLine(
                    color = Color.White.copy(alpha = 0.45f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )

                // Horizontal Crosshair
                drawLine(
                    color = Color.White.copy(alpha = 0.45f),
                    start = Offset(0f, closeY),
                    end = Offset(w, closeY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )

                // Intersection point dot
                drawCircle(
                    color = Color.White,
                    radius = 3.5f,
                    center = Offset(x, closeY)
                )
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 1000.0) {
        String.format("%,.1f", price)
    } else if (price >= 1.0) {
        String.format("%,.2f", price)
    } else {
        String.format("%.4f", price)
    }
}
