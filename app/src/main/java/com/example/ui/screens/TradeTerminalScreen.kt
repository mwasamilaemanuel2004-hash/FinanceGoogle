package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountHealthStatus
import com.example.data.model.MarketTick
import com.example.data.model.MarketType
import com.example.data.model.OrderSide
import com.example.data.model.PriceCandle
import com.example.engine.AgenticRagCagEngine
import com.example.engine.AgenticSystemDecision
import com.example.engine.AiMarketPrediction
import com.example.engine.MarketAnalysisEngine
import com.example.engine.MotherBrainEngine
import com.example.engine.StrategyDefinition
import com.example.ui.components.AgenticRagCagDialog
import com.example.ui.components.AnyPairSelectorDialog
import com.example.ui.components.CandlestickChartView
import com.example.ui.components.IndicatorConfig
import com.example.ui.components.IndicatorSetupDialog
import com.example.ui.components.InstallAndDownloadDialog
import com.example.ui.components.LivePriceTickView
import com.example.ui.components.MarketAnalysisDialog
import com.example.ui.components.MasterAgentConsoleDialog
import com.example.ui.components.RealAccountConnectionDialog
import com.example.ui.components.StrategySkillsDialog
import com.example.ui.theme.*
import com.example.util.ExternalPlatformManager
import kotlin.math.abs

@Composable
fun TradeTerminalScreen(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    initialSymbol: String? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ticks by engine.tickEngine.ticks.collectAsState()
    val pingLatency by engine.tickEngine.pingLatencyMs.collectAsState()
    val activeTrades by engine.activeTrades.collectAsState()
    val primaryConnector by engine.primaryConnector.collectAsState()
    val activeRealAccount by engine.realAccountBridge.activeRealAccount.collectAsState()
    val isLiveAuthorized by engine.realAccountBridge.isLiveTradingAuthorized.collectAsState()
    val userCredentials by engine.userCredentials.collectAsState()
    val isConfigured = userCredentials.any { it.apiKey.isNotBlank() }

    var selectedSymbol by remember { mutableStateOf(initialSymbol ?: "BTC/USDT") }
    var selectedSide by remember { mutableStateOf(OrderSide.BUY) }
    var lotSize by remember { mutableStateOf(1.0) }
    var isLiveExecutionMode by remember { mutableStateOf(false) }

    // Dialog controllers
    var showPairSelectorDialog by remember { mutableStateOf(false) }
    var showAnalysisDialog by remember { mutableStateOf(false) }
    var showInstallDialog by remember { mutableStateOf(false) }
    var showHelpGuide by remember { mutableStateOf(false) }
    var showStrategySkillsDialog by remember { mutableStateOf(false) }
    var showAgenticRagCagDialog by remember { mutableStateOf(false) }
    var showIndicatorSetupDialog by remember { mutableStateOf(false) }
    var showMasterAgentDialog by remember { mutableStateOf(false) }
    var showAddBotDialog by remember { mutableStateOf(false) }
    var showBinanceIndicatorsDialog by remember { mutableStateOf(false) }
    var currentIndicatorConfig by remember { mutableStateOf(IndicatorConfig()) }
    var activePrediction by remember { mutableStateOf<AiMarketPrediction?>(null) }
    var activeAgenticDecision by remember { mutableStateOf<AgenticSystemDecision?>(null) }

    // Take Profit & Stop Loss inputs
    var takeProfitInput by remember { mutableStateOf("") }
    var stopLossInput by remember { mutableStateOf("") }
    var rlFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var selectedTimeframe by remember { mutableStateOf("1m") }

    // If initialSymbol changed from parent
    LaunchedEffect(initialSymbol) {
        if (!initialSymbol.isNullOrBlank() && ticks.containsKey(initialSymbol)) {
            selectedSymbol = initialSymbol
        }
    }

    val currentTick = ticks[selectedSymbol]
        ?: MarketTick(selectedSymbol, MarketType.CRYPTO, 0.0, 0.0, 0.0, 0.0, 0.0, 50.0)

    val isLoading = currentTick.price == 0.0

    val candles = remember(selectedSymbol, ticks) {
        engine.tickEngine.getCandlesForSymbol(selectedSymbol)
    }

    // Auto-populate mathematically sound default TP and SL whenever symbol or side changes
    LaunchedEffect(selectedSymbol, selectedSide) {
        val price = currentTick.price
        if (selectedSide == OrderSide.BUY) {
            val defaultTp = price * 1.025
            val defaultSl = price * 0.985
            takeProfitInput = String.format("%.2f", defaultTp)
            stopLossInput = String.format("%.2f", defaultSl)
        } else {
            val defaultTp = price * 0.975
            val defaultSl = price * 1.015
            takeProfitInput = String.format("%.2f", defaultTp)
            stopLossInput = String.format("%.2f", defaultSl)
        }
    }

    // Mathematical Validation for Stop Loss and Take Profit
    val parsedTp = takeProfitInput.toDoubleOrNull()
    val parsedSl = stopLossInput.toDoubleOrNull()
    val isBuy = selectedSide == OrderSide.BUY

    // For BUY: TP must be > Price, SL must be < Price
    // For SELL: TP must be < Price, SL must be > Price
    val isTpValid = parsedTp != null && if (isBuy) parsedTp > currentTick.price else parsedTp < currentTick.price
    val isSlValid = parsedSl != null && if (isBuy) parsedSl < currentTick.price else parsedSl > currentTick.price
    val isOrderValid = isTpValid && isSlValid

    // Risk / Reward Calculations
    val profitDollar = if (parsedTp != null && currentTick.price > 0) {
        (abs(parsedTp - currentTick.price) / currentTick.price) * (lotSize * 1500.0)
    } else 0.0

    val lossDollar = if (parsedSl != null && currentTick.price > 0) {
        (abs(parsedSl - currentTick.price) / currentTick.price) * (lotSize * 1500.0)
    } else 0.0

    val profitPct = if (parsedTp != null && currentTick.price > 0) {
        (abs(parsedTp - currentTick.price) / currentTick.price) * 100.0
    } else 0.0

    val lossPct = if (parsedSl != null && currentTick.price > 0) {
        (abs(parsedSl - currentTick.price) / currentTick.price) * 100.0
    } else 0.0

    val rrRatioStr = if (lossDollar > 0) String.format("1:%.2f", profitDollar / lossDollar) else "1:2.0"

    fun applyPreset(presetType: String) {
        val price = currentTick.price
        when (presetType) {
            "RR_1_2" -> {
                val dist = price * 0.015
                if (isBuy) {
                    stopLossInput = String.format("%.2f", price - dist)
                    takeProfitInput = String.format("%.2f", price + (dist * 2.0))
                } else {
                    stopLossInput = String.format("%.2f", price + dist)
                    takeProfitInput = String.format("%.2f", price - (dist * 2.0))
                }
            }
            "RR_1_3" -> {
                val dist = price * 0.015
                if (isBuy) {
                    stopLossInput = String.format("%.2f", price - dist)
                    takeProfitInput = String.format("%.2f", price + (dist * 3.0))
                } else {
                    stopLossInput = String.format("%.2f", price + dist)
                    takeProfitInput = String.format("%.2f", price - (dist * 3.0))
                }
            }
            "SCALP" -> {
                val dist = price * 0.006
                if (isBuy) {
                    stopLossInput = String.format("%.2f", price - dist)
                    takeProfitInput = String.format("%.2f", price + (dist * 2.0))
                } else {
                    stopLossInput = String.format("%.2f", price + dist)
                    takeProfitInput = String.format("%.2f", price - (dist * 2.0))
                }
            }
            "SWING" -> {
                val dist = price * 0.025
                if (isBuy) {
                    stopLossInput = String.format("%.2f", price - dist)
                    takeProfitInput = String.format("%.2f", price + (dist * 3.2))
                } else {
                    stopLossInput = String.format("%.2f", price + dist)
                    takeProfitInput = String.format("%.2f", price - (dist * 3.2))
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Real Broker / API Configuration Card on the Chart Screen
        item {
            val telemetry by engine.telemetry.collectAsState()
            val bots by engine.bots.collectAsState()

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
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = "API",
                                    tint = if (isConfigured) AccentNeonGreen else AccentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isSwahili) "HALI YA MADALALI & EXCHANGE (SETTINGS)" else "BROKER & EXCHANGE CONNECTIVITY",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isConfigured) AccentNeonGreen else AccentGold
                                )
                                Text(
                                    text = if (isConfigured) 
                                        (if (isSwahili) "✅ Madalali Zimeunganishwa • Salio: $${String.format("%,.2f", telemetry.totalEquityUsd)} • Boti ${bots.count { it.isActive }} Active" else "✅ Connected • Equity: $${String.format("%,.2f", telemetry.totalEquityUsd)} • ${bots.count { it.isActive }} Active Bots")
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

        // --- Autonomous Agent & Intelligent Bot Fleet Launcher Panel ---
        item {
            var agentCapital by remember { mutableStateOf("10") }
            var targetProfit by remember { mutableStateOf("10.0") }
            val isAutonomousActive by engine.masterAgent.isAutonomousActive.collectAsState()
            val fleetSummary by engine.fleetDeploymentSummary.collectAsState()
            val currentBots by engine.bots.collectAsState()
            val currentMode by engine.masterAgent.executionMode.collectAsState()
            
            val activeFleetBots = currentBots.filter { it.isActive }
            val totalAllocated = activeFleetBots.sumOf { it.allocatedCapital }
            val totalPnl = activeFleetBots.sumOf { it.currentPnl }
            val capVal = agentCapital.toDoubleOrNull() ?: 10.0
            val pctVal = targetProfit.toDoubleOrNull() ?: 10.0
            val expProfitUsd = capVal * (pctVal / 100.0)
            val totalTargetUsd = if (activeFleetBots.isNotEmpty()) activeFleetBots.sumOf { it.targetProfitUsd }.let { if (it > 0.0) it else expProfitUsd } else expProfitUsd
            val progressFraction = if (totalTargetUsd > 0.0) (totalPnl / totalTargetUsd).toFloat().coerceIn(0f, 1f) else 0f

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isAutonomousActive && activeFleetBots.isNotEmpty()) AccentNeonGreen else AccentCyan
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header with Status Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isAutonomousActive && activeFleetBots.isNotEmpty()) AccentNeonGreen else AccentCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSwahili) "KIDHIBITI MAHIRI CHA KIKOSI CHA BOTI" else "INTELLIGENT FLEET & CAPITAL LAUNCHER",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isAutonomousActive && activeFleetBots.isNotEmpty()) AccentNeonGreen else AccentCyan
                            )
                        }

                        Surface(
                            color = (if (isAutonomousActive && activeFleetBots.isNotEmpty()) AccentNeonGreen else AccentCyan).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isAutonomousActive && activeFleetBots.isNotEmpty()) 
                                    (if (isSwahili) "${activeFleetBots.size} BOTI LIVE" else "${activeFleetBots.size} BOTS LIVE") 
                                else (if (isSwahili) "ONLINE (TAYARI)" else "ONLINE (READY)"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isAutonomousActive && activeFleetBots.isNotEmpty()) AccentNeonGreen else AccentCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isAutonomousActive && activeFleetBots.isNotEmpty()) {
                        // --- LIVE FLEET RUNNING CARD ---
                        Surface(
                            color = SurfaceCard,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = if (isSwahili) "Mtaji Uliogawiwa" else "Allocated Fleet Capital",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "$${String.format("%,.2f", totalAllocated)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = TextWhite
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (isSwahili) "Faida Sasa / Lengo (${String.format("%.1f", pctVal)}%)" else "Current PnL / Target (${String.format("%.1f", pctVal)}%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "${if (totalPnl >= 0) "+" else ""}$${String.format("%.2f", totalPnl)} / +$${String.format("%.2f", totalTargetUsd)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = if (totalPnl >= 0) AccentNeonGreen else DangerRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Profit Progress Bar
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = AccentNeonGreen,
                                    trackColor = SurfaceElevated
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Active Bots Chips Row
                                Text(
                                    text = if (isSwahili) "Boti Zilizofunguliwa Kwenye Masoko:" else "Active Deployed Bots:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(activeFleetBots) { bot ->
                                        Surface(
                                            color = SurfaceElevated,
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentNeonGreen)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = "${bot.name}: $${String.format("%.2f", bot.allocatedCapital)} (${bot.targetPair})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextWhite
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Fleet Control Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showAddBotDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.2f).height(40.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isSwahili) "+ Ongeza Boti" else "+ Add Bot",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            engine.pauseFleet()
                                            rlFeedbackMessage = if (isSwahili) "Kikosi kimesitishwa kwa muda." else "Fleet paused."
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isSwahili) "Sitisha" else "Pause",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            engine.stopFleet()
                                            rlFeedbackMessage = if (isSwahili) "Kikosi kimesimamishwa na kufungwa." else "Fleet stopped."
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.8f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isSwahili) "Funga" else "Stop",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // --- INPUT & CONFIGURATION SECTION ---
                        
                        // 1. Capital Quick Selector & Input
                        Text(
                            text = if (isSwahili) "1. Weka Mtaji Wako (Capital in USDT):" else "1. Enter Capital (USDT):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = agentCapital,
                                onValueChange = { agentCapital = it },
                                label = { Text("USDT") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            // Mode Dropdown
                            var showModeMenu by remember { mutableStateOf(false) }
                            Box {
                                Surface(
                                    onClick = { showModeMenu = true },
                                    color = AccentCyan.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                                    modifier = Modifier.height(52.dp).width(110.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    ) {
                                        Text("Hali (Mode)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextMuted)
                                        Text(
                                            text = currentMode.name,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentCyan,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                DropdownMenu(expanded = showModeMenu, onDismissRequest = { showModeMenu = false }) {
                                    com.example.data.model.ExecutionInstrumentMode.values().forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode.name) },
                                            onClick = { 
                                                engine.masterAgent.setExecutionMode(mode)
                                                showModeMenu = false 
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Capital Chips
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val capPresets = listOf("5", "10", "25", "50", "100", "500")
                            items(capPresets) { p ->
                                val isSel = agentCapital == p
                                Surface(
                                    onClick = { agentCapital = p },
                                    color = if (isSel) AccentCyan.copy(alpha = 0.3f) else SurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentCyan else BorderSubtle)
                                ) {
                                    Text(
                                        text = "$$p",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) AccentCyan else TextMuted,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Profit Target Percentage Selector
                        Text(
                            text = if (isSwahili) "2. Chagua Asilimia ya Faida (Profit Target %):" else "2. Select Target Profit Percentage (%):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = targetProfit,
                                onValueChange = { targetProfit = it },
                                label = { Text("Lengo %") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            // Calculation preview
                            Surface(
                                color = AccentGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f)),
                                modifier = Modifier.height(52.dp).weight(1.3f)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = if (isSwahili) "Faida Inayolengwa:" else "Expected Profit:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "+$${String.format("%.2f", expProfitUsd)} (➔ $${String.format("%.2f", capVal + expProfitUsd)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = AccentGold
                                    )
                                }
                            }
                        }

                        // Profit Percentage Quick Chips
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val pctPresets = listOf(
                                "5.0" to (if (isSwahili) "5% 🛡️ Usalama" else "5% 🛡️ Safe"),
                                "10.0" to (if (isSwahili) "10% ⚖️ Wastani" else "10% ⚖️ Balanced"),
                                "20.0" to (if (isSwahili) "20% 🚀 Ukuaji" else "20% 🚀 Growth"),
                                "50.0" to (if (isSwahili) "50% ⚡ Agresive" else "50% ⚡ High Alpha")
                            )
                            items(pctPresets) { (valStr, label) ->
                                val isSel = targetProfit == valStr
                                Surface(
                                    onClick = { targetProfit = valStr },
                                    color = if (isSel) AccentGold.copy(alpha = 0.3f) else SurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentGold else BorderSubtle)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) AccentGold else TextMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fleet Deployment Preview Explanation Card
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (capVal < 25.0) 
                                        (if (isSwahili) "Mtaji wa $${String.format("%.0f", capVal)} utawasha boti 4 za Micro: Deriv V75 Scalper, Crash/Boom, Crypto Satoshis na Forex Cent Lot." 
                                         else "Capital of $${String.format("%.0f", capVal)} auto-deploys 4 micro bots: Deriv V75, Crash/Boom, Crypto Satoshis & Forex Cent.")
                                    else if (capVal < 100.0)
                                        (if (isSwahili) "Mtaji wa $${String.format("%.0f", capVal)} utawasha boti 6 za masoko mbalimbali (Crypto, Deriv V75/Crash, Forex SMC & Gold Micro)."
                                         else "Capital of $${String.format("%.0f", capVal)} auto-deploys 6 balanced bots across Crypto, Deriv, Forex & Gold.")
                                    else
                                        (if (isSwahili) "Mtaji wa $${String.format("%.0f", capVal)} utawasha kikosi cha boti 10 cha kiwango cha taasisi (Multi-Asset Institutional Fleet)."
                                         else "Capital of $${String.format("%.0f", capVal)} deploys full 10-bot institutional multi-asset fleet."),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Launch Fleet Button
                        Button(
                            onClick = {
                                val amount = agentCapital.toDoubleOrNull() ?: 10.0
                                val profit = targetProfit.toDoubleOrNull() ?: 10.0
                                val summary = engine.deployIntelligentBotFleet(amount, profit, currentMode)
                                rlFeedbackMessage = if (isSwahili) 
                                    "🚀 Kikosi cha boti ${summary.deployedBotsCount} kimezinduliwa kwa $${String.format("%.2f", amount)}! Lengo: +$${String.format("%.2f", summary.targetProfitUsd)} (${String.format("%.1f", profit)}%)"
                                else 
                                    "🚀 Fleet of ${summary.deployedBotsCount} bots deployed with $${String.format("%.2f", amount)}! Target: +$${String.format("%.2f", summary.targetProfitUsd)} (${String.format("%.1f", profit)}%)"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSwahili) "🚀 ZINDUA KIKOSI CHA BOTI & ANZA BIASHARA" else "🚀 LAUNCH BOT FLEET & START TRADING",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Market Switcher Bar with Quick Search & Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button to open the Any Pair Selector & Creator
                Button(
                    onClick = { showPairSelectorDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSwahili) "+ Pair Yoyote" else "+ Any Pair",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Button to open Strategy Skills Library
                Button(
                    onClick = { showStrategySkillsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Skills", tint = AccentCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSwahili) "Stadi za Biashara" else "Trade Skills",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Button to open Binance 11 Indicators & Pine Clone Dialog
                Button(
                    onClick = { showBinanceIndicatorsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2329)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD535)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Binance Indicators", tint = Color(0xFFFCD535), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSwahili) "Viashiria 11 & Pine" else "11 Indicators & Pine",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFCD535),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Categorized Market Pairs in Boxes with Radius 20 at edge with color applet
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSwahili) "MAKUNDI YA MASOKO (RAD. 20 CORNERS):" else "MARKET SEGMENTS (RAD. 20 CORNERS):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = TextMuted
                    )
                    Text(
                        text = if (isSwahili) "Forex • Crypto • Commodities • Deriv • Stocks • Metals" else "Forex • Crypto • Commodities • Deriv • Stocks • Metals",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val targetCategories = listOf(
                    MarketType.FOREX,
                    MarketType.CRYPTO,
                    MarketType.COMMODITIES,
                    MarketType.DERIV,
                    MarketType.STOCKS,
                    MarketType.METALS
                )
                val groupedTicks = ticks.values.groupBy { it.marketType }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(targetCategories) { marketType ->
                        val marketTicks = groupedTicks[marketType] ?: emptyList()
                        if (marketTicks.isNotEmpty()) {
                            MarketCategoryBox(
                                marketType = marketType,
                                ticks = marketTicks,
                                selectedSymbol = selectedSymbol,
                                isSwahili = isSwahili,
                                onSelect = { selectedSymbol = it }
                            )
                        }
                    }
                }
            }
        }

        // Quick Toolbelt Row: Binance Pro, Market Analysis, Install APK
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Binance Pro Quick Launcher
                Button(
                    onClick = { ExternalPlatformManager.openPlatform(context, "binance") },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.ElectricBolt, contentDescription = "Binance Pro", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Binance Pro", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Black)
                }

                // AI Market Analysis & Prediction Trigger
                Button(
                    onClick = {
                        activePrediction = MarketAnalysisEngine.analyzeMarket(selectedSymbol, currentTick.price, candles)
                        showAnalysisDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentLavender),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.2f).height(38.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Analysis", tint = AccentLavender, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSwahili) "Chambua & Tabiri" else "AI Analysis",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentLavender
                    )
                }

                // Download / Install APK Guide
                Button(
                    onClick = { showInstallDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.InstallMobile, contentDescription = "Install", tint = AccentNeonGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSwahili) "Pakua App" else "Download APK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentNeonGreen
                    )
                }
            }
        }

        // Modern Master Agent + Agentic RAG & CAG + Indicator Setup Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Master Autonomous Agent Button
                Button(
                    onClick = { showMasterAgentDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.1f).height(38.dp)
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = "Master AI", tint = AccentNeonGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isSwahili) "Wakala Mkuu" else "Master AI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = AccentNeonGreen
                    )
                }

                // Agentic RAG & CAG Button
                Button(
                    onClick = {
                        activeAgenticDecision = AgenticRagCagEngine.evaluateTradeOpportunity(
                            symbol = selectedSymbol,
                            currentPrice = currentTick.price,
                            candles = candles
                        )
                        showAgenticRagCagDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = "Agentic AI", tint = AccentCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isSwahili) "RAG & CAG" else "RAG/CAG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = AccentCyan
                    )
                }

                // Indicator Setup (Pandas & Quant) Button
                Button(
                    onClick = { showIndicatorSetupDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(0.9f).height(38.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Setup za Indicator", tint = AccentGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isSwahili) "Viashiria" else "Indicators",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                }
            }
        }

        // Dynamic Pair AI Recommendation Strip (Mtaji, Faida, Kina & Liquidity)
        item {
            val topRecommendations by engine.masterAgent.pairRecommendations.collectAsState()
            val bestPair = topRecommendations.firstOrNull()
            if (bestPair != null) {
                Surface(
                    onClick = { showMasterAgentDialog = true },
                    color = AccentCyan.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
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
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isSwahili) "AI Pair Matcher: ${bestPair.symbol} (${bestPair.compositeScore}% Match)" else "AI Top Pair: ${bestPair.symbol} (${bestPair.compositeScore}% Match)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (isSwahili) "Kina Bora: ${bestPair.bestExchangeForLiquidity} • Lengo: ${engine.masterAgent.targetProfitPct.value}% Faida" else "Top Depth: ${bestPair.bestExchangeForLiquidity} • Target: ${engine.masterAgent.targetProfitPct.value}% Profit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isSwahili) "Chagua / Badili" else "Switch / Configure",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentNeonGreen,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = AccentNeonGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Live Candlestick Canvas Chart for Selected Pair
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, getMarketThemeColor(currentTick.marketType).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { showPairSelectorDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = selectedSymbol,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Pair Down Button",
                                    tint = getMarketThemeColor(currentTick.marketType),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(getMarketThemeColor(currentTick.marketType).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = currentTick.marketType.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = getMarketThemeColor(currentTick.marketType),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Live Price Tick Flash (Green on rise, Red on fall)
                                LivePriceTickView(
                                    price = currentTick.price,
                                    textStyle = MaterialTheme.typography.headlineMedium,
                                    showDirectionArrow = true,
                                    showBackgroundFlash = true
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { engine.tickEngine.forceRefreshNow() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Instant Refresh",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentCyan.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${pingLatency}ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = AccentCyan,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "24h High: $${String.format("%,.2f", currentTick.high24h)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "24h Low: $${String.format("%,.2f", currentTick.low24h)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "RSI (14): ${String.format("%.1f", currentTick.rsi)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Multi-colored Timeframe Bar ("ya rangi nyingine ya time")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(AllColoredTimeframes) { tf ->
                            val isSelected = tf.label == selectedTimeframe
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) tf.color else tf.color.copy(alpha = 0.12f))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else tf.color.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedTimeframe = tf.label }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tf.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.Black else tf.color
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Candlestick Graphic with Customizable Colors, Light Edge & JAZA UKURASA (Full Page Mode)
                    CandlestickChartView(
                        candles = candles,
                        isSwahili = isSwahili,
                        showControls = true,
                        symbol = selectedSymbol,
                        currentPrice = currentTick.price,
                        onQuickBuy = {
                            engine.placeTrade(
                                symbol = selectedSymbol,
                                marketType = currentTick.marketType,
                                side = OrderSide.BUY,
                                lotSize = lotSize,
                                price = currentTick.price,
                                connector = primaryConnector,
                                customTakeProfit = currentTick.price * 1.025,
                                customStopLoss = currentTick.price * 0.985
                            )
                        },
                        onQuickSell = {
                            engine.placeTrade(
                                symbol = selectedSymbol,
                                marketType = currentTick.marketType,
                                side = OrderSide.SELL,
                                lotSize = lotSize,
                                price = currentTick.price,
                                connector = primaryConnector,
                                customTakeProfit = currentTick.price * 0.975,
                                customStopLoss = currentTick.price * 1.015
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reinforcement Learning (RL) Control & Telemetry Panel
                    val brainState by engine.brainState.collectAsState()
                    val rlDecisionColor = when (brainState.agentDecision) {
                        "STRONG BUY" -> AccentNeonGreen
                        "STRONG SELL" -> DangerRed
                        else -> AccentCyan
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, rlDecisionColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = "RL Agent",
                                    tint = rlDecisionColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSwahili) "WAKALA WA RL (Q-LEARNING AGENT)" else "RL Q-LEARNING AGENT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(rlDecisionColor.copy(alpha = 0.2f))
                                    .border(1.dp, rlDecisionColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = brainState.agentDecision,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = rlDecisionColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // RL Telemetry Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isSwahili) "Hali ya Soko (Regime):" else "Market Regime:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = brainState.regime,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isSwahili) "Zawadi (RL Reward):" else "RL Reward:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = "${String.format("%+.2f", brainState.totalReward)} pts",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (brainState.totalReward >= 0) AccentNeonGreen else DangerRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Mastery Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSwahili) "Umahiri wa Kujifunza (Mastery): ${brainState.learningProgress}%" else "Q-Learning Mastery: ${brainState.learningProgress}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "Episodes: ${brainState.trainedEpisodes} | ε: ${brainState.explorationRatePct}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { brainState.learningProgress / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = rlDecisionColor,
                            trackColor = BorderSubtle
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Button to Execute / Apply RL Decision to Order Form
                        Button(
                            onClick = {
                                if (brainState.agentDecision == "STRONG BUY") {
                                    selectedSide = OrderSide.BUY
                                    val price = currentTick.price
                                    takeProfitInput = String.format("%.2f", price * 1.025)
                                    stopLossInput = String.format("%.2f", price * 0.985)
                                    rlFeedbackMessage = if (isSwahili) "Wakala wa RL ameweka oda ya KUNUNUA (BUY) na hesabu ya TP/SL!" else "RL Agent configured BUY order with mathematical TP/SL!"
                                } else if (brainState.agentDecision == "STRONG SELL") {
                                    selectedSide = OrderSide.SELL
                                    val price = currentTick.price
                                    takeProfitInput = String.format("%.2f", price * 0.975)
                                    stopLossInput = String.format("%.2f", price * 1.015)
                                    rlFeedbackMessage = if (isSwahili) "Wakala wa RL ameweka oda ya KUUZA (SELL) na hesabu ya TP/SL!" else "RL Agent configured SELL order with mathematical TP/SL!"
                                } else {
                                    rlFeedbackMessage = if (isSwahili) "RL Agent: Hali ya soko inasubiriwa (WAIT). Hakuna oda ya haraka." else "RL Agent: Market in consolidation (WAIT). Holding execution."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (brainState.agentDecision == "WAIT") SurfaceElevated else rlDecisionColor
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, rlDecisionColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(
                                Icons.Default.ElectricBolt,
                                contentDescription = "Execute RL",
                                tint = if (brainState.agentDecision == "WAIT") TextWhite else Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "TEKELEZA UAMUZI WA RL KWENYE ODA" else "APPLY RL DECISION TO ORDER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (brainState.agentDecision == "WAIT") TextWhite else Color.Black
                            )
                        }

                        // Feedback message if action applied
                        rlFeedbackMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(rlDecisionColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextWhite,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp).clickable { rlFeedbackMessage = null }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Order Execution Panel with Full SL & TP Logic
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Title and Quick Help Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSwahili) "Jopo la Biashara: $selectedSymbol" else "Order Terminal: $selectedSymbol",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { showHelpGuide = !showHelpGuide },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.HelpOutline,
                                contentDescription = "Help Guide",
                                tint = if (showHelpGuide) AccentCyan else TextMuted
                            )
                        }
                    }

                    // Educational Guide if requested or toggled
                    if (showHelpGuide) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isSwahili) "💡 Mwongozo wa Stop Loss & Take Profit:" else "💡 Guide to Stop Loss & Take Profit:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isSwahili)
                                        "• Oda ya KUNUNUA (BUY / LONG): Unafaidika bei ikipanda. Hivyo Take Profit (TP) lazima iwe JUU ya bei ya kuingilia, na Stop Loss (SL) lazima iwe CHINI kujilinda soko likishuka.\n" +
                                                "• Oda ya KUUZA (SELL / SHORT): Unafaidika bei ikishuka. Hivyo Take Profit (TP) lazima iwe CHINI ya bei ya sasa, na Stop Loss (SL) lazima iwe JUU kujilinda soko likipanda!"
                                    else
                                        "• BUY / LONG Order: You profit when price rises. Therefore Take Profit (TP) must be ABOVE market price, and Stop Loss (SL) must be BELOW market price.\n" +
                                                "• SELL / SHORT Order: You profit when price falls. Therefore Take Profit (TP) must be BELOW market price, and Stop Loss (SL) must be ABOVE market price!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mode Switcher: LIVE (Real Broker Funds) vs PAPER (Simulation Demo)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0A0F1D))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isLiveExecutionMode) AccentCyan.copy(alpha = 0.25f) else Color.Transparent)
                                .border(1.dp, if (!isLiveExecutionMode) AccentCyan else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { isLiveExecutionMode = false }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isSwahili) "PAPER (Majaribio)" else "PAPER (Demo Mode)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (!isLiveExecutionMode) FontWeight.Black else FontWeight.Normal,
                                color = if (!isLiveExecutionMode) AccentCyan else TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLiveExecutionMode) DangerRed.copy(alpha = 0.25f) else Color.Transparent)
                                .border(1.dp, if (isLiveExecutionMode) DangerRed else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable {
                                    if (activeRealAccount == null && !isConfigured) {
                                        onNavigateToSettings?.invoke()
                                    } else {
                                        isLiveExecutionMode = true
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Key, contentDescription = "Live", tint = if (isLiveExecutionMode) DangerRed else TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSwahili) "LIVE (Pesa Halisi)" else "LIVE (Real Money)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isLiveExecutionMode) FontWeight.Black else FontWeight.Normal,
                                    color = if (isLiveExecutionMode) DangerRed else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buy/Sell selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedSide = OrderSide.BUY },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBuy) AccentNeonGreen else SurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = "Buy", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSwahili) "KUNUNUA (BUY)" else "BUY / LONG",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = { selectedSide = OrderSide.SELL },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isBuy) DangerRed else SurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingDown, contentDescription = "Sell", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSwahili) "KUUZA (SELL)" else "SELL / SHORT",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lot size picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSwahili) "Saizi ya Oda (Lots):" else "Position Volume (Lots):",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.01, 0.1, 0.5, 1.0, 2.5, 5.0).forEach { size ->
                                val isChosen = lotSize == size
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isChosen) MaterialTheme.colorScheme.primary else SurfaceElevated)
                                        .clickable { lotSize = size }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$size",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChosen) Color.White else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Take Profit and Stop Loss Interactive Input Fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Take Profit Field
                        OutlinedTextField(
                            value = takeProfitInput,
                            onValueChange = { takeProfitInput = it },
                            label = {
                                Text(
                                    text = if (isBuy) "Take Profit (TP > Bei)" else "Take Profit (TP < Bei)",
                                    color = if (isTpValid) AccentNeonGreen else DangerRed,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            singleLine = true,
                            isError = !isTpValid && takeProfitInput.isNotBlank(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentNeonGreen,
                                unfocusedBorderColor = if (isTpValid) BorderSubtle else DangerRed,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        // Stop Loss Field
                        OutlinedTextField(
                            value = stopLossInput,
                            onValueChange = { stopLossInput = it },
                            label = {
                                Text(
                                    text = if (isBuy) "Stop Loss (SL < Bei)" else "Stop Loss (SL > Bei)",
                                    color = if (isSlValid) DangerRed else DangerRed,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            singleLine = true,
                            isError = !isSlValid && stopLossInput.isNotBlank(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DangerRed,
                                unfocusedBorderColor = if (isSlValid) BorderSubtle else DangerRed,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Preset Buttons for SL/TP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { applyPreset("RR_1_2") },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(30.dp)
                        ) {
                            Text("1:2 R:R", style = MaterialTheme.typography.labelSmall, color = TextWhite)
                        }

                        Button(
                            onClick = { applyPreset("RR_1_3") },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(30.dp)
                        ) {
                            Text("1:3 R:R", style = MaterialTheme.typography.labelSmall, color = TextWhite)
                        }

                        Button(
                            onClick = { applyPreset("SCALP") },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(30.dp)
                        ) {
                            Text("Scalp", style = MaterialTheme.typography.labelSmall, color = TextWhite)
                        }

                        Button(
                            onClick = { applyPreset("SWING") },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(30.dp)
                        ) {
                            Text("Swing", style = MaterialTheme.typography.labelSmall, color = TextWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Validation Warning Banner (If math is broken)
                    if (!isOrderValid) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = "Error", tint = DangerRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isBuy) {
                                            if (isSwahili)
                                                "⚠️ Tahadhari ya KUNUNUA (BUY): Stop Loss lazima iwe CHINI ya bei ($${String.format("%,.2f", currentTick.price)}) na Take Profit iwe JUU!"
                                            else
                                                "⚠️ Invalid BUY order: Stop Loss must be BELOW market price ($${String.format("%,.2f", currentTick.price)}) and Take Profit must be ABOVE!"
                                        } else {
                                            if (isSwahili)
                                                "⚠️ Tahadhari ya KUUZA (SELL): Stop Loss lazima iwe JUU ya bei ($${String.format("%,.2f", currentTick.price)}) na Take Profit iwe CHINI!"
                                            else
                                                "⚠️ Invalid SELL order: Stop Loss must be ABOVE market price ($${String.format("%,.2f", currentTick.price)}) and Take Profit must be BELOW!"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        // Profit / Loss and R:R Projection Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = if (isSwahili) "Lengo la Faida (TP)" else "Target Profit", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        text = "+$${String.format("%,.2f", profitDollar)} (+${String.format("%.1f", profitPct)}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentNeonGreen,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = if (isSwahili) "Kiwango cha Hasara (SL)" else "Risk / Loss", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        text = "-$${String.format("%,.2f", lossDollar)} (-${String.format("%.1f", lossPct)}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = if (isSwahili) "Uwiano (R:R)" else "R:R Ratio", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        text = rrRatioStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Execute Order Button
                    val isConnected = if (isLiveExecutionMode) {
                        activeRealAccount?.isConnected == true && activeRealAccount?.healthStatus != AccountHealthStatus.OFFLINE
                    } else {
                        primaryConnector.isConnected() || true
                    }

                    Button(
                        onClick = {
                            if (isOrderValid) {
                                if (isLiveExecutionMode && !isLiveAuthorized && !isConfigured) {
                                    onNavigateToSettings?.invoke()
                                } else {
                                    engine.placeTrade(
                                        symbol = selectedSymbol,
                                        marketType = currentTick.marketType,
                                        side = selectedSide,
                                        lotSize = lotSize,
                                        price = currentTick.price,
                                        connector = primaryConnector,
                                        customTakeProfit = parsedTp,
                                        customStopLoss = parsedSl,
                                        isLiveExecution = isLiveExecutionMode
                                    )
                                }
                            }
                        },
                        enabled = isOrderValid && isConnected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBuy) AccentNeonGreen else DangerRed,
                            disabledContainerColor = Color.DarkGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isLiveExecutionMode) Icons.Default.Key else (if (isBuy) Icons.Default.TrendingUp else Icons.Default.TrendingDown),
                                contentDescription = "Order",
                                tint = if (isBuy) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLiveExecutionMode) {
                                    if (isBuy)
                                        (if (isSwahili) "TEKELEZA BUY YA PESA HALISI (LIVE)" else "SUBMIT LIVE REAL BUY ORDER")
                                    else
                                        (if (isSwahili) "TEKELEZA SELL YA PESA HALISI (LIVE)" else "SUBMIT LIVE REAL SELL ORDER")
                                } else {
                                    if (isBuy)
                                        (if (isSwahili) "WEKA ODA YA KUNUNUA (PAPER)" else "PLACE PAPER BUY ORDER")
                                    else
                                        (if (isSwahili) "WEKA ODA YA KUUZA (PAPER)" else "PLACE PAPER SELL ORDER")
                                },
                                fontWeight = FontWeight.Black,
                                color = if (isBuy) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Active Trades Table with Live PnL & TP/SL Display
        item {
            Text(
                text = if (isSwahili) "Mikataba Inayoendelea (${activeTrades.size})" else "Active Live Positions (${activeTrades.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        if (activeTrades.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isSwahili) "Hakuna mikataba iliyofunguliwa kwa sasa. Weka oda hapo juu!" else "No open positions. Submit a trade above!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        items(activeTrades) { trade ->
            val isTradeBuy = trade.side == OrderSide.BUY
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(8.dp),
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
                            Text(
                                text = trade.symbol,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = trade.side.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isTradeBuy) AccentNeonGreen else DangerRed
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${trade.lotSize} Lots",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Entry: $${String.format("%,.2f", trade.entryPrice)} | Sasa: $${String.format("%,.2f", trade.currentPrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "TP: $${String.format("%,.2f", trade.takeProfit)} | SL: $${String.format("%,.2f", trade.stopLoss)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${if (trade.unrealizedPnl >= 0) "+" else ""}$${String.format("%,.2f", trade.unrealizedPnl)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (trade.unrealizedPnl >= 0) AccentNeonGreen else DangerRed
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { engine.closeTrade(trade.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Add Bot to Fleet Dialog
    if (showAddBotDialog) {
        com.example.ui.components.AddBotToFleetDialog(
            engine = engine,
            isSwahili = isSwahili,
            onDismiss = { showAddBotDialog = false }
        )
    }

    // Modal Any Pair Selector Dialog
    if (showPairSelectorDialog) {
        AnyPairSelectorDialog(
            ticks = ticks,
            currentSelectedSymbol = selectedSymbol,
            isSwahili = isSwahili,
            onSelectPair = { symbol ->
                selectedSymbol = symbol
                showPairSelectorDialog = false
            },
            onAddCustomPair = { rawSymbol, category, customPrice ->
                val added = engine.tickEngine.addCustomPair(rawSymbol, category, customPrice)
                selectedSymbol = added.symbol
                showPairSelectorDialog = false
            },
            onDismiss = { showPairSelectorDialog = false }
        )
    }

    // Modal Market Analysis & AI Prediction Dialog
    if (showAnalysisDialog && activePrediction != null) {
        MarketAnalysisDialog(
            prediction = activePrediction!!,
            isSwahili = isSwahili,
            onApplyToOrder = { side, tp, sl ->
                selectedSide = side
                takeProfitInput = String.format("%.2f", tp)
                stopLossInput = String.format("%.2f", sl)
            },
            onDismiss = { showAnalysisDialog = false }
        )
    }

    // Modal Install & Download APK Guide Dialog
    if (showInstallDialog) {
        InstallAndDownloadDialog(
            isSwahili = isSwahili,
            onDismiss = { showInstallDialog = false }
        )
    }

    // Modal Strategy & Trade Skills Dialog
    if (showStrategySkillsDialog) {
        StrategySkillsDialog(
            isSwahili = isSwahili,
            onSelectStrategy = { strat ->
                val pred = MarketAnalysisEngine.analyzeMarket(selectedSymbol, currentTick.price, candles)
                activePrediction = pred.copy(
                    signal = "SKILL: ${strat.name.uppercase()}",
                    confidencePct = strat.winRateHistorical.toInt(),
                    riskRewardRatio = "${strat.profitFactor}x Profit Factor"
                )
                showAnalysisDialog = true
            },
            onDismiss = { showStrategySkillsDialog = false }
        )
    }

    // Modal Agentic RAG & CAG System Dialog
    if (showAgenticRagCagDialog && activeAgenticDecision != null) {
        AgenticRagCagDialog(
            decision = activeAgenticDecision!!,
            isSwahili = isSwahili,
            onApplyToOrder = { side, tp, sl ->
                selectedSide = side
                takeProfitInput = String.format("%.2f", tp)
                stopLossInput = String.format("%.2f", sl)
            },
            onDismiss = { showAgenticRagCagDialog = false }
        )
    }

    // Modal Indicator Setup Dialog
    if (showIndicatorSetupDialog) {
        IndicatorSetupDialog(
            currentConfig = currentIndicatorConfig,
            isSwahili = isSwahili,
            onSaveConfig = { updated ->
                currentIndicatorConfig = updated
            },
            onDismiss = { showIndicatorSetupDialog = false }
        )
    }

    // Modal Master Autonomous Agent Console Dialog (Dynamic Pair AI & Autonomous Reasoning)
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
                selectedSymbol = sym
                showMasterAgentDialog = false
            },
            onExecuteTrade = { sym, side, lot, tp, sl ->
                engine.placeTrade(
                    symbol = sym,
                    marketType = currentTick.marketType,
                    side = side,
                    lotSize = lot,
                    price = currentTick.price,
                    connector = primaryConnector,
                    customTakeProfit = tp,
                    customStopLoss = sl
                )
                selectedSymbol = sym
                showMasterAgentDialog = false
            },
            onDismiss = { showMasterAgentDialog = false }
        )
    }

    // Modal Binance 11 Indicators & Pine Script v5 Dialog
    if (showBinanceIndicatorsDialog) {
        com.example.ui.components.BinanceIndicatorsAndPineDialog(
            currentSymbol = selectedSymbol,
            isSwahili = isSwahili,
            onDismiss = { showBinanceIndicatorsDialog = false }
        )
    }
}

@Composable
fun MarketCategoryBox(
    marketType: MarketType,
    ticks: List<MarketTick>,
    selectedSymbol: String,
    isSwahili: Boolean,
    onSelect: (String) -> Unit
) {
    val categoryColor = getMarketThemeColor(marketType)
    val categoryTitle = when (marketType) {
        MarketType.FOREX -> if (isSwahili) "FOREX / Fedha za Kigeni" else "FOREX / Currencies"
        MarketType.CRYPTO -> if (isSwahili) "CRYPTO / Sarafu za Kidijitali" else "CRYPTO / Digital Assets"
        MarketType.COMMODITIES -> if (isSwahili) "COMMODITIES / Bidhaa Ghafi" else "COMMODITIES / Energy & Agro"
        MarketType.DERIV -> if (isSwahili) "DERIV / Masoko ya Synthetics" else "DERIV / Synthetic Volatility"
        MarketType.STOCKS -> if (isSwahili) "STOCKS / Hisa za Kimataifa na DSE" else "STOCKS & EQUITIES"
        MarketType.METALS -> if (isSwahili) "METALS / Madini ya Dhahabu na Fedha" else "PRECIOUS METALS (Gold/Silver)"
        MarketType.INDICES -> if (isSwahili) "INDICES / Fahirisi za Masoko" else "GLOBAL INDICES"
        MarketType.ETFS -> if (isSwahili) "ETFs / Mifuko ya Uwekezaji ya ETF" else "EXCHANGE TRADED FUNDS (ETFs)"
    }

    Column(
        modifier = Modifier
            .widthIn(min = 280.dp, max = 380.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(categoryColor.copy(alpha = 0.08f))
            .border(
                width = 2.dp,
                color = categoryColor.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(12.dp)
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
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(categoryColor)
                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = categoryColor
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(categoryColor.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${ticks.size} Pairs",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal scroll of all pairs in this category
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) categoryColor.copy(alpha = 0.25f) else SurfaceElevated)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) categoryColor else BorderSubtle.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(t.symbol) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text(
                            text = cleanSym,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) categoryColor else TextWhite
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (t.price > 1000.0) String.format("%,.0f", t.price) else String.format("%,.2f", t.price),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
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
