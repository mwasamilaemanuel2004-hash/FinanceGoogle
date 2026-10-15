package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketTick
import com.example.data.model.MarketType
import com.example.engine.AgenticRagCagEngine
import com.example.engine.AgenticSystemDecision
import com.example.engine.MotherBrainEngine
import com.example.ui.components.AgenticRagCagDialog
import com.example.ui.components.AnyPairSelectorDialog
import com.example.ui.components.CandlestickChartView
import com.example.ui.components.IndicatorConfig
import com.example.ui.components.IndicatorSetupDialog
import com.example.ui.components.LivePriceTickView
import com.example.ui.components.MasterAgentConsoleDialog
import com.example.ui.components.RealAccountConnectionDialog
import com.example.ui.theme.*
import java.util.Locale

// Distinct timeframe configuration with vibrant colors ("rangi nyingine ya time")
data class TimeframeColorOption(
    val label: String,
    val displayName: String,
    val color: Color
)

val AllColoredTimeframes = listOf(
    TimeframeColorOption("1s", "1s", Color(0xFF00E5FF)),   // Electric Cyan
    TimeframeColorOption("1m", "1m", Color(0xFF00E676)),   // Emerald Green
    TimeframeColorOption("5m", "5m", Color(0xFFFFD600)),   // Vivid Gold/Yellow
    TimeframeColorOption("15m", "15m", Color(0xFFFF6D00)), // Flame Orange
    TimeframeColorOption("1h", "1h", Color(0xFF7C4DFF)),   // Deep Violet
    TimeframeColorOption("4h", "4h", Color(0xFFFF4081)),   // Neon Pink/Magenta
    TimeframeColorOption("1D", "1D", Color(0xFF40C4FF)),   // Sky Blue
    TimeframeColorOption("1W", "1W", Color(0xFFFF5252))    // Coral Red
)

@Composable
fun HomeScreen(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    selectedSymbol: String,
    onSelectSymbol: (String) -> Unit,
    onNavigateToTrade: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val ticks by engine.tickEngine.ticks.collectAsState()
    val isLiveFeedActive by engine.tickEngine.isLiveFeedActive.collectAsState()
    val pingLatency by engine.tickEngine.pingLatencyMs.collectAsState()
    val brainState by engine.brainState.collectAsState()
    val telemetry by engine.telemetry.collectAsState()
    val userCredentials by engine.userCredentials.collectAsState()
    val bots by engine.bots.collectAsState()

    var showPairSelectorDialog by remember { mutableStateOf(false) }
    var selectedTimeframe by remember { mutableStateOf("1m") }
    var showCandlePreview by remember { mutableStateOf(true) }
    var showAgenticDialog by remember { mutableStateOf(false) }
    var showIndicatorDialog by remember { mutableStateOf(false) }
    var showMasterAgentDialog by remember { mutableStateOf(false) }
    var currentIndicatorConfig by remember { mutableStateOf(IndicatorConfig()) }
    var activeAgenticDecision by remember { mutableStateOf<AgenticSystemDecision?>(null) }

    val candles = remember(selectedSymbol, ticks) {
        engine.tickEngine.getCandlesForSymbol(selectedSymbol)
    }

    val currentTick = ticks[selectedSymbol]
        ?: MarketTick(selectedSymbol, MarketType.CRYPTO, 83789.00, 0.89, 84500.0, 82800.0, 20000.0, 50.0)

    val btcTick = ticks["BTC/USDT"]
    val goldTick = ticks["XAU/USD (Gold)"]

    val categories = listOf(
        MarketType.FOREX,
        MarketType.CRYPTO,
        MarketType.COMMODITIES,
        MarketType.DERIV,
        MarketType.STOCKS,
        MarketType.METALS
    )
    val groupedTicks = ticks.values.groupBy { it.marketType }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. LIVE REAL-TIME FEED BANNER (Authentic Live Prices)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isLiveFeedActive) AccentNeonGreen else DangerRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSwahili) "BEI HALISI LIVE" else "REAL-TIME FEED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = if (isLiveFeedActive) AccentNeonGreen else DangerRed
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "⚡ ${pingLatency}ms",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentCyan,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        btcTick?.let { btc ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("BTC: ", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                LivePriceTickView(price = btc.price, textStyle = MaterialTheme.typography.labelSmall, showDirectionArrow = false)
                            }
                        }
                        goldTick?.let { gold ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Gold: ", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                LivePriceTickView(price = gold.price, textStyle = MaterialTheme.typography.labelSmall, showDirectionArrow = false)
                            }
                        }
                        // Instant manual sync button
                        IconButton(
                            onClick = { engine.tickEngine.forceRefreshNow() },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Instant Refresh",
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 1.5 PROMINENT REAL BROKER / API SETUP & ACCOUNT STATUS CARD (Right above Chart)
        item {
            val isConfigured = userCredentials.any { it.apiKey.isNotEmpty() }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isConfigured) AccentNeonGreen else AccentGold
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isConfigured) AccentNeonGreen.copy(alpha = 0.2f) else AccentGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.VpnKey,
                                    contentDescription = "API",
                                    tint = if (isConfigured) AccentNeonGreen else AccentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isSwahili) "HALI YA MADALALI & EXCHANGE" else "BROKER & EXCHANGE CONNECTIVITY",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isConfigured) AccentNeonGreen else AccentGold
                                )
                                Text(
                                    text = if (isConfigured) 
                                        (if (isSwahili) "✅ Akaunti Imeunganishwa • Salio: $${String.format("%,.2f", telemetry.totalEquityUsd)} • Boti ${bots.count { it.isActive }} Active" else "✅ Connected • Equity: $${String.format("%,.2f", telemetry.totalEquityUsd)} • ${bots.count { it.isActive }} Active Bots")
                                    else 
                                        (if (isSwahili) "⚠️ Hakuna API. Weka taarifa za broker kwenye ukurasa wa Mipangilio." else "⚠️ No broker connected. Configure API keys in Settings hub."),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Button(
                            onClick = { onNavigateToSettings?.invoke() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConfigured) SurfaceElevated else AccentGold
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isConfigured) AccentCyan else Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSwahili) "Mipangilio ya API" else "API Settings",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isConfigured) AccentCyan else Color.Black
                            )
                        }
                    }
                }
            }
        }

        // 2. ACTIVE PAIR SELECTION CARD WITH DOWN BUTTON (DOWN ARROW)
        item {
            val themeColor = getMarketThemeColor(currentTick.marketType)
            val isPositive = currentTick.change24h >= 0.0

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(2.dp, themeColor.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header row with Pair Name, DOWN BUTTON (ArrowDropDown), and Market Type Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pair Name Clickable Pill with Down Button
                        Surface(
                            onClick = { showPairSelectorDialog = true },
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentTick.symbol,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                // Down Button / Down Arrow to switch pair
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Pair Down Button",
                                    tint = themeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Category Pill & Switch Button
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(themeColor.copy(alpha = 0.2f))
                                    .border(1.dp, themeColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = currentTick.marketType.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = themeColor
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            FilledTonalButton(
                                onClick = { showPairSelectorDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = SurfaceElevated)
                            ) {
                                Text(
                                    text = if (isSwahili) "Badili ▾" else "Switch ▾",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real Price and 24h Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = if (isSwahili) "Bei Halisi ya Sasa (Live Tick):" else "Current Live Price (Live Flash):",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Visual color flash on price change (Green on rise, Red on fall)
                            LivePriceTickView(
                                price = currentTick.price,
                                textStyle = MaterialTheme.typography.headlineMedium,
                                showDirectionArrow = true,
                                showBackgroundFlash = true
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background((if (isPositive) AccentNeonGreen else DangerRed).copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${if (isPositive) "+" else ""}${String.format("%.2f", currentTick.change24h)}% (24h)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isPositive) AccentNeonGreen else DangerRed
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "High: $${String.format("%,.1f", currentTick.high24h)} • Low: $${String.format("%,.1f", currentTick.low24h)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. MULTI-COLORED TIMEFRAME SELECTOR ("ya rangi nyingine ya time")
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSwahili) "CHAGUA TIME FRAME (KILA TIME INA RANGI YAKE):" else "SELECT TIME FRAME (DISTINCT TIME COLORS):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = TextMuted
                            )
                            val activeTf = AllColoredTimeframes.find { it.label == selectedTimeframe }
                            Text(
                                text = "Tf: ${selectedTimeframe}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = activeTf?.color ?: AccentCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(AllColoredTimeframes) { tf ->
                                val isSelected = tf.label == selectedTimeframe
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) tf.color else tf.color.copy(alpha = 0.12f))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color.White else tf.color.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedTimeframe = tf.label }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tf.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) Color.Black else tf.color
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Optional Quick Candlestick Chart Toggle with selectable colors & light edges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { showCandlePreview = !showCandlePreview }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Candle Chart",
                                tint = AccentCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "Chati ya Mishumaa (Candlestick)" else "Candlestick Chart Preview",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Text(
                            text = if (showCandlePreview) {
                                if (isSwahili) "Ficha Chati ▴" else "Hide Chart ▴"
                            } else {
                                if (isSwahili) "Onyesha Chati ▾" else "Show Chart ▾"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }

                    // Candlestick View (Shown when expanded) with Jaza Ukurasa (Full Page Mode)
                    AnimatedVisibility(visible = showCandlePreview) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            CandlestickChartView(
                                candles = candles,
                                isSwahili = isSwahili,
                                showControls = true,
                                symbol = selectedSymbol,
                                currentPrice = currentTick.price,
                                onQuickBuy = { onNavigateToTrade(selectedSymbol) },
                                onQuickSell = { onNavigateToTrade(selectedSymbol) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Agentic RAG & CAG + Indicator Setup Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { showMasterAgentDialog = true },
                            modifier = Modifier.weight(1.1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = SurfaceElevated)
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = AccentNeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isSwahili) "Wakala Mkuu" else "Master AI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentNeonGreen
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                activeAgenticDecision = AgenticRagCagEngine.evaluateTradeOpportunity(
                                    symbol = selectedSymbol,
                                    currentPrice = currentTick.price,
                                    candles = candles
                                )
                                showAgenticDialog = true
                            },
                            modifier = Modifier.weight(1.1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = SurfaceElevated)
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isSwahili) "RAG & CAG" else "RAG/CAG",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentCyan
                            )
                        }

                        FilledTonalButton(
                            onClick = { showIndicatorDialog = true },
                            modifier = Modifier.weight(0.9f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = SurfaceElevated)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isSwahili) "Viashiria" else "Indicators",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Open in Trade Terminal Button
                    Button(
                        onClick = { onNavigateToTrade(currentTick.symbol) },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Trade", tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSwahili) "Fungua Terminal ya Trade ($selectedSymbol)" else "Open in Trading Terminal ($selectedSymbol)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }

                    // Dynamic Pair AI Recommendation Banner (Volume, Depth, Capital & Profit Alignment)
                    val topRecommendations by engine.masterAgent.pairRecommendations.collectAsState()
                    val bestPair = topRecommendations.firstOrNull()
                    if (bestPair != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            onClick = { showMasterAgentDialog = true },
                            color = AccentCyan.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = if (isSwahili) "Chaguo la Wakala: ${bestPair.symbol} (Score: ${bestPair.compositeScore}%)" else "AI Pair Pick: ${bestPair.symbol} (${bestPair.compositeScore}% Match)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = if (isSwahili) "Kina Bora: ${bestPair.bestExchangeForLiquidity} • Lot: ${bestPair.recommendedLotSize}" else "Top Depth: ${bestPair.bestExchangeForLiquidity} • Lot: ${bestPair.recommendedLotSize}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AccentCyan,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Text(
                                    text = if (isSwahili) "Tazama Matrix ›" else "View Matrix ›",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = AccentCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. REINFORCEMENT LEARNING QUICK STATUS (Compact & Clean, No Huge Canvas)
        item {
            val decisionColor = when (brainState.agentDecision) {
                "STRONG BUY" -> AccentNeonGreen
                "STRONG SELL" -> DangerRed
                else -> AccentCyan
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, decisionColor.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "RL", tint = decisionColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "WAKALA WA RL (AI DECISION):" else "RL AGENT DECISION:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${brainState.agentDecision} (${brainState.regime})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = decisionColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Reward: ${String.format("%+.1f", brainState.totalReward)} pts",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (brainState.totalReward >= 0) AccentNeonGreen else DangerRed
                        )
                        Text(
                            text = "Episodes: ${brainState.trainedEpisodes} | Mastery: ${brainState.learningProgress}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // 4.5. SOVEREIGN AI CONSOLIDATED INTELLIGENCE MATRIX (Indicators + Specialized Agents + LLM + ML Ensemble)
        item {
            var matrixExpanded by remember { mutableStateOf(false) }
            val decisionColor = when (brainState.agentDecision) {
                "STRONG BUY" -> AccentNeonGreen
                "STRONG SELL" -> DangerRed
                else -> AccentCyan
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable { matrixExpanded = !matrixExpanded }
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "MATRIKSI JUMUISHI YA AI (CONSOLIDATED CORE)" else "SOVEREIGN AI CONSOLIDATED MATRIX",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentGold,
                                fontSize = 10.sp
                            )
                        }
                        Surface(
                            color = AccentGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "INTEGRATED INTEL",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold,
                                fontSize = 8.sp
                            )
                        }
                    }

                    Text(
                        text = if (isSwahili)
                            "Muunganisho mkuu wa Viashiria vya AI (Indicators), Mawakala (Agentic Agents), LLM Rationale, na ML Ensemble 22-mifano."
                            else "Unified sovereign matrix integrating AI technical indicators, specialized agentic consensus, LLM reasoning context, and 22-Model ML Ensemble forecasts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

                    // 4 Layers Grid Preview
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "1. AI INDICATORS", style = MaterialTheme.typography.labelSmall, color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            Text(text = "EMA Cross, Bollinger, RSI, CVD", style = MaterialTheme.typography.bodySmall, color = TextWhite, fontSize = 10.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "2. SPECIALIZED AGENTS", style = MaterialTheme.typography.labelSmall, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            Text(text = "Market Intel, Quant, Supervisor", style = MaterialTheme.typography.bodySmall, color = TextWhite, fontSize = 10.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "3. QUANT LLM RATIONALE", style = MaterialTheme.typography.labelSmall, color = AccentNeonGreen, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            Text(text = "SMC, Liquidity, ATR Risk Guard", style = MaterialTheme.typography.bodySmall, color = TextWhite, fontSize = 10.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "4. ML FORECAST ENSEMBLE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            Text(text = "Confidence: ${brainState.learningProgress}% (BULLISH)", style = MaterialTheme.typography.bodySmall, color = TextWhite, fontSize = 10.sp)
                        }
                    }

                    if (matrixExpanded) {
                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                        // Expanded view with detailed metrics
                        Text(
                            text = if (isSwahili) "MCHANGANUO MAHIRE WA TEKNOLOJIA (AI DEEP-DIVE):" else "HIGH-FIDELITY COGNITIVE ANALYSIS REPORT:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold,
                            fontSize = 9.sp
                        )
                        Surface(
                            color = DarkCharcoal,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val textValue = if (isSwahili) {
                                    "• [VIASHIRIA VYA AI]: Bollinger squeeze iko kwenye 14% contraction (Breakout imminent). EMA 9 iko juu ya EMA 21 (BULLISH).\n" +
                                    "• [MAWAKALA WA AI]: Wakala wa Ujasusi (Agent 1) anakisia mtiririko chanya wa CVD (+224K USD). Wakala wa ML (Agent 2) anakadiria uwezekano wa ushindi kuwa 87%.\n" +
                                    "• [QUANT LLM DECISION]: Mapengo ya bei (FVG) na Liquidity Pools zimefichuliwa kwenye chati ya $selectedSymbol. Entry Zone imethibitishwa na circuit breaker yetu.\n" +
                                    "• [MACHINE LEARNING]: Mifano yote 22 ya ML imehitimu consensus ya 91% ya ujasusi bila kuwa na lookahead bias au data leakage."
                                } else {
                                    "• [AI INDICATORS]: Bollinger band width contracted to 14% (Breakout imminent). EMA 9/21 cross confirmed positive trend momentum.\n" +
                                    "• [AGENTIC CONSENSUS]: Agent 1 (Market Intel) signals positive CVD delta (+224K USD). Agent 2 (Quant ML) computes combined Win Probability of 87%.\n" +
                                    "• [QUANT LLM RATIONALE]: Fair Value Gap (FVG) and Liquidity pools successfully swept on $selectedSymbol. Hard stop and circuit breaker gates pre-validated.\n" +
                                    "• [ML ENSEMBLE FORECAST]: 22-Model consensus resolved at 91% bullish confidence score with zero lookahead bias or drift detected."
                                }
                                Text(
                                    text = textValue,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        Text(
                            text = if (isSwahili) "Bofya kufunga mchanganuo ‹" else "Click to collapse detailed matrix ‹",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 9.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Text(
                            text = if (isSwahili) "Bofya hapa kufungua mchanganuo wa kina wa AI ›" else "Click to expand detailed AI analytical breakdown ›",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentGold,
                            fontSize = 9.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // 5. THE 6 CATEGORIZED MARKET BOXES WITH RADIUS 20 AT EDGE AND COLOR APPLET
        // ("tenganisha pair za forex, crypto, commodities, deriv, stock , metal, kwa box za radus 20 at edge with collor applet")
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSwahili) "MASOKO YOTE 6 (BOXES ZA RADIUS 20 & COLOR APPLET):" else "ALL 6 MARKET SEGMENTS (RADIUS 20 BOXES):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = AccentCyan
                )
                Text(
                    text = "${ticks.size} Pairs",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        items(categories) { marketType ->
            val marketTicks = groupedTicks[marketType] ?: emptyList()
            if (marketTicks.isNotEmpty()) {
                HomeMarketCategoryRadius20Box(
                    marketType = marketType,
                    ticks = marketTicks,
                    selectedSymbol = selectedSymbol,
                    isSwahili = isSwahili,
                    onSelectPair = { onSelectSymbol(it) },
                    onOpenTrade = { onNavigateToTrade(it) }
                )
            }
        }
    }

    // Pair Selector Dialog when Down Button / Search is clicked
    if (showPairSelectorDialog) {
        AnyPairSelectorDialog(
            ticks = ticks,
            currentSelectedSymbol = selectedSymbol,
            isSwahili = isSwahili,
            onSelectPair = { chosen ->
                onSelectSymbol(chosen)
                showPairSelectorDialog = false
            },
            onAddCustomPair = { sym, type, price ->
                val newTick = engine.tickEngine.addCustomPair(sym, type, price)
                onSelectSymbol(newTick.symbol)
                showPairSelectorDialog = false
            },
            onDismiss = { showPairSelectorDialog = false }
        )
    }

    // Modal Agentic RAG & CAG Dialog
    if (showAgenticDialog && activeAgenticDecision != null) {
        AgenticRagCagDialog(
            decision = activeAgenticDecision!!,
            isSwahili = isSwahili,
            onApplyToOrder = { side, tp, sl ->
                onNavigateToTrade(selectedSymbol)
            },
            onDismiss = { showAgenticDialog = false }
        )
    }

    // Modal Indicator Setup Dialog
    if (showIndicatorDialog) {
        IndicatorSetupDialog(
            currentConfig = currentIndicatorConfig,
            isSwahili = isSwahili,
            onSaveConfig = { updated ->
                currentIndicatorConfig = updated
            },
            onDismiss = { showIndicatorDialog = false }
        )
    }

    // Modal Master Agent Console Dialog (Dynamic Pair AI & Autonomous Reasoning)
    if (showMasterAgentDialog) {
        val candidateCandles = remember(ticks) {
            listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "XRP/USDT", "DOGE/USDT", "XAU/USD", "EUR/USD", "VOLATILITY 75", "CRASH 1000", "NVDA")
                .associateWith { sym -> engine.tickEngine.getCandlesForSymbol(sym) }
        }
        MasterAgentConsoleDialog(
            agent = engine.masterAgent,
            currentSymbol = selectedSymbol,
            isSwahili = isSwahili,
            ticks = ticks,
            candlesMap = candidateCandles,
            initialTab = 0,
            onSelectPair = { sym ->
                onSelectSymbol(sym)
                showMasterAgentDialog = false
            },
            onExecuteTrade = { sym, side, lot, tp, sl ->
                onSelectSymbol(sym)
                showMasterAgentDialog = false
                onNavigateToTrade(sym)
            },
            onDismiss = { showMasterAgentDialog = false }
        )
    }
}

@Composable
fun HomeMarketCategoryRadius20Box(
    marketType: MarketType,
    ticks: List<MarketTick>,
    selectedSymbol: String,
    isSwahili: Boolean,
    onSelectPair: (String) -> Unit,
    onOpenTrade: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (marketType) {
        MarketType.FOREX -> Color(0xFF00E676)       // Emerald Green
        MarketType.CRYPTO -> Color(0xFFFF9100)      // Neon Orange / Gold
        MarketType.COMMODITIES -> Color(0xFFFFAB00) // Amber Bronze
        MarketType.DERIV -> Color(0xFFD500F9)       // Vibrant Magenta / Electric Purple
        MarketType.STOCKS -> Color(0xFF2979FF)      // Electric Cobalt Blue
        MarketType.METALS -> Color(0xFFFFD700)      // Sovereign Pure Gold
        MarketType.INDICES -> Color(0xFF00E5FF)     // Electric Cyan
        MarketType.ETFS -> Color(0xFFE040FB)        // Purple Orchid
    }

    val categoryTitle = when (marketType) {
        MarketType.FOREX -> if (isSwahili) "FOREX (Fedha za Kigeni)" else "FOREX / CURRENCY PAIRS"
        MarketType.CRYPTO -> if (isSwahili) "CRYPTO (Sarafu za Kidijitali)" else "CRYPTO ASSETS"
        MarketType.COMMODITIES -> if (isSwahili) "COMMODITIES (Bidhaa za Nishati)" else "COMMODITIES & ENERGY"
        MarketType.DERIV -> if (isSwahili) "DERIV (Synthetics za Volatility & Crash/Boom)" else "DERIV SYNTHETICS & VOLATILITY"
        MarketType.STOCKS -> if (isSwahili) "STOCKS (Hisa za Kimataifa & DSE Tanzania)" else "GLOBAL & DSE EQUITIES"
        MarketType.METALS -> if (isSwahili) "METALS (Madini ya Dhahabu na Fedha)" else "PRECIOUS METALS (GOLD & SILVER)"
        MarketType.INDICES -> if (isSwahili) "INDICES (Fahirisi za Masoko)" else "GLOBAL INDICES"
        MarketType.ETFS -> if (isSwahili) "ETFs (Mifuko ya Pamoja ya Uwekezaji)" else "EXCHANGE TRADED FUNDS (ETFs)"
    }

    // Box with Radius 20dp at edge and Color Applet Indicator
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(categoryColor.copy(alpha = 0.07f))
            .border(
                width = 2.dp,
                color = categoryColor.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        // Category Header with Color Applet Accent
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color Applet Indicator
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(categoryColor)
                        .border(1.5.dp, Color.White, RoundedCornerShape(7.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = categoryColor
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(categoryColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${ticks.size} Pairs",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = categoryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Scroll list of pairs inside this box
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ticks) { t ->
                val isSelected = t.symbol == selectedSymbol
                val isPositive = t.change24h >= 0.0
                val cleanSym = t.symbol
                    .replace(" (Deriv)", "")
                    .replace(" (DSE)", "")
                    .replace(" (Gold)", "")
                    .replace(" (Silver)", "")

                Box(
                    modifier = Modifier
                        .widthIn(min = 120.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) categoryColor.copy(alpha = 0.25f) else SurfaceElevated)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) categoryColor else BorderSubtle,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectPair(t.symbol) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cleanSym,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) categoryColor else TextWhite
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(categoryColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (t.price >= 1.0) "$${String.format("%,.2f", t.price)}" else "$${String.format("%.4f", t.price)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (isPositive) AccentNeonGreen else DangerRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${if (isPositive) "+" else ""}${String.format("%.1f", t.change24h)}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) AccentNeonGreen else DangerRed
                            )
                        }
                    }
                }
            }
        }
    }
}
