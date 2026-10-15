package com.example.ui.components

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MarketTick
import com.example.data.model.OrderSide
import com.example.data.model.PriceCandle
import com.example.engine.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun MasterAgentConsoleDialog(
    agent: MasterAutonomousTradingAgent,
    currentSymbol: String,
    isSwahili: Boolean,
    ticks: Map<String, MarketTick> = emptyMap(),
    candlesMap: Map<String, List<PriceCandle>> = emptyMap(),
    initialTab: Int = 0,
    onSelectPair: ((String) -> Unit)? = null,
    onExecuteTrade: ((symbol: String, side: OrderSide, lot: Double, tp: Double, sl: Double) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isAutonomous by agent.isAutonomousActive.collectAsState()
    val systemMode by agent.systemMode.collectAsState()
    val apiHealth by agent.apiHealth.collectAsState()
    val thoughts by agent.thoughts.collectAsState()
    val prediction by agent.latestPrediction.collectAsState()
    val killSwitch by agent.killSwitchEngaged.collectAsState()
    val currentCapital by agent.userCapitalUsd.collectAsState()
    val currentProfitTarget by agent.targetProfitPct.collectAsState()
    val pairRecommendations by agent.pairRecommendations.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) }
    var capitalInputText by remember { mutableStateOf(currentCapital.toInt().toString()) }
    var profitInputText by remember { mutableStateOf(currentProfitTarget.toString()) }
    var commandInput by remember { mutableStateOf("") }
    var commandOutput by remember { mutableStateOf<String?>(null) }
    var expandedPairDetails by remember { mutableStateOf<String?>(null) }

    // Ensure we trigger initial evaluation if recommendations empty
    LaunchedEffect(Unit) {
        if (pairRecommendations.isEmpty()) {
            agent.refreshDynamicPairSelection(ticks, candlesMap)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (killSwitch) DangerRed else AccentCyan),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.96f)
                .padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (killSwitch) DangerRed else if (isAutonomous) AccentNeonGreen else AccentGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isSwahili) "Wakala Mkuu wa Kujiendesha (Master AI)" else "Unified Master Autonomous Trading Agent",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Text(
                                text = "Mode: ${systemMode.name} • Status: ${apiHealth.name} • Mtaji: $${String.format("%,.0f", currentCapital)} • Lengo: ${currentProfitTarget}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (apiHealth == ApiHealthStatus.HEALTHY) AccentNeonGreen else DangerRed,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs (1. Dynamic Pair Selector, 2. Live Thoughts & Forecast, 3. Autonomous Bot & Commands)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceElevated,
                    contentColor = AccentCyan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(15.dp), tint = if (selectedTab == 0) AccentCyan else TextMuted)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSwahili) "Uchaguzi wa Pair & Mtaji" else "Dynamic Pair AI",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(15.dp), tint = if (selectedTab == 1) AccentGold else TextMuted)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSwahili) "Mawazo & Utabiri" else "Thoughts & Forecast",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(15.dp), tint = if (selectedTab == 2) AccentNeonGreen else TextMuted)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSwahili) "Amri & Uendeshaji" else "Bot & Commands",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 2) FontWeight.Black else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(15.dp), tint = if (selectedTab == 3) AccentLavender else TextMuted)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSwahili) "Wakala 3 & Quant" else "3 Agents & Quant",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 3) FontWeight.Black else FontWeight.Normal
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content Area
                when (selectedTab) {
                    0 -> {
                        // TAB 1: DYNAMIC PAIR SELECTION & CAPITAL ALLOCATION
                        DynamicPairSelectionTab(
                            agent = agent,
                            currentCapital = currentCapital,
                            currentProfitTarget = currentProfitTarget,
                            capitalInputText = capitalInputText,
                            onCapitalChange = { capitalInputText = it },
                            profitInputText = profitInputText,
                            onProfitChange = { profitInputText = it },
                            pairRecommendations = pairRecommendations,
                            expandedPairDetails = expandedPairDetails,
                            onTogglePairDetails = { sym ->
                                expandedPairDetails = if (expandedPairDetails == sym) null else sym
                            },
                            ticks = ticks,
                            candlesMap = candlesMap,
                            isSwahili = isSwahili,
                            onSelectPair = onSelectPair,
                            onExecuteTrade = onExecuteTrade
                        )
                    }
                    1 -> {
                        // TAB 2: LIVE THOUGHTS STREAM & MARKET PREDICTIONS
                        LiveThoughtsAndForecastTab(
                            prediction = prediction,
                            thoughts = thoughts,
                            isSwahili = isSwahili
                        )
                    }
                    2 -> {
                        // TAB 3: AUTONOMOUS BOT CONTROLS, MODES & COMMANDS
                        AutonomousBotAndCommandsTab(
                            agent = agent,
                            isAutonomous = isAutonomous,
                            systemMode = systemMode,
                            killSwitch = killSwitch,
                            commandInput = commandInput,
                            onCommandChange = { commandInput = it },
                            commandOutput = commandOutput,
                            onSendCommand = { cmd ->
                                commandOutput = agent.executeCommand(cmd, currentSymbol, ticks[currentSymbol])
                                commandInput = ""
                            },
                            isSwahili = isSwahili
                        )
                    }
                    3 -> {
                        // TAB 4: THREE AGENTS ARCHITECTURE & TACTICAL PREDICTIONS
                        ThreeAgentConsoleTab(
                            symbol = currentSymbol,
                            isSwahili = isSwahili,
                            onExecuteTrade = onExecuteTrade
                        )
                    }
                }
            }
        }
    }
}

/**
 * TAB 1: DYNAMIC PAIR SELECTION BASED ON CAPITAL, TARGET PROFIT, VOLUME, SPREAD,
 * ORDER-BOOK DEPTH, VOLATILITY, FUNDING RATE AND MARKET REGIME.
 */
@Composable
private fun DynamicPairSelectionTab(
    agent: MasterAutonomousTradingAgent,
    currentCapital: Double,
    currentProfitTarget: Double,
    capitalInputText: String,
    onCapitalChange: (String) -> Unit,
    profitInputText: String,
    onProfitChange: (String) -> Unit,
    pairRecommendations: List<PairRecommendation>,
    expandedPairDetails: String?,
    onTogglePairDetails: (String) -> Unit,
    ticks: Map<String, MarketTick>,
    candlesMap: Map<String, List<PriceCandle>>,
    isSwahili: Boolean,
    onSelectPair: ((String) -> Unit)?,
    onExecuteTrade: ((symbol: String, side: OrderSide, lot: Double, tp: Double, sl: Double) -> Unit)?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Capital and Profit Target Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isSwahili) "📊 Uteuzi wa Pair Kulingana na Mtaji na Lengo Lako" else "📊 Autonomous Dynamic Pair Matcher",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = AccentCyan
                    )
                    Text(
                        text = if (isSwahili)
                            "Wakala anapima Volume (CoinGecko), Order-Book Depth (Binance/Bybit/Coinbase/MT5), Spread, Volatility ATR na Funding Rate ili kuchagua pair bora inayokulinda na kukupa faida."
                        else
                            "Evaluates CoinGecko 24h volume, multi-exchange order-book depth, spread, ATR volatility, funding rate and regime to pick the mathematically superior pair.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Capital Input ($)
                        OutlinedTextField(
                            value = capitalInputText,
                            onValueChange = onCapitalChange,
                            label = { Text(if (isSwahili) "Mtaji Wako ($)" else "Capital ($)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        // Target Profit Input (%)
                        OutlinedTextField(
                            value = profitInputText,
                            onValueChange = onProfitChange,
                            label = { Text(if (isSwahili) "Faida Inayotakiwa (%)" else "Target Profit (%)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentNeonGreen,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        // Evaluate Button
                        Button(
                            onClick = {
                                val cap = capitalInputText.toDoubleOrNull() ?: 1000.0
                                val target = profitInputText.toDoubleOrNull() ?: 5.0
                                agent.configureCapitalAndProfitTarget(cap, target, ticks, candlesMap)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .height(52.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isSwahili) "Tathmini" else "Evaluate", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Capital Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isSwahili) "Mtaji Haraka:" else "Quick Cap:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        listOf(5.0, 10.0, 25.0, 50.0, 100.0, 500.0, 1000.0).forEach { cap ->
                            SuggestionChip(
                                onClick = {
                                    onCapitalChange(cap.toInt().toString())
                                    val target = profitInputText.toDoubleOrNull() ?: 5.0
                                    agent.configureCapitalAndProfitTarget(cap, target, ticks, candlesMap)
                                },
                                label = { Text("$${cap.toInt()}", fontSize = 10.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (currentCapital == cap) AccentCyan.copy(alpha = 0.25f) else Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (currentCapital == cap) AccentCyan else BorderSubtle
                                )
                            )
                        }
                    }

                    // Quick Target Profit Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isSwahili) "Lengo Haraka:" else "Quick Profit:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        listOf(2.0, 3.0, 5.0, 8.0, 10.0).forEach { pct ->
                            SuggestionChip(
                                onClick = {
                                    onProfitChange(pct.toString())
                                    val cap = capitalInputText.toDoubleOrNull() ?: 1000.0
                                    agent.configureCapitalAndProfitTarget(cap, pct, ticks, candlesMap)
                                },
                                label = { Text("${pct}%", fontSize = 10.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (currentProfitTarget == pct) AccentNeonGreen.copy(alpha = 0.25f) else Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (currentProfitTarget == pct) AccentNeonGreen else BorderSubtle
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. Ranking Summary Banner
        item {
            val topRecommendation = pairRecommendations.firstOrNull()
            if (topRecommendation != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AccentCyan.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentNeonGreen)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isSwahili) "#1 BORA ZAIDI KISOKO" else "#1 TOP PAIR SELECTION",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = topRecommendation.symbol,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isSwahili) topRecommendation.rationaleSwahili else topRecommendation.rationaleEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhite,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSelectPair?.invoke(topRecommendation.symbol)
                                onExecuteTrade?.invoke(
                                    topRecommendation.symbol,
                                    topRecommendation.recommendedSide,
                                    topRecommendation.recommendedLotSize,
                                    topRecommendation.targetProfitPrice,
                                    topRecommendation.stopLossPrice
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isSwahili) "Tumia Sasa" else "Apply Now",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. List of Evaluated Candidate Pairs with Depth & Metrics
        item {
            Text(
                text = if (isSwahili) "Orodha ya Pair Zilizopimwa (Rankings kulingana na Sababu za Kisoko):" else "Asset Matrix (Ranked by Market Depth, Volume & Capital Fit):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = AccentGold
            )
        }

        items(pairRecommendations) { rec ->
            PairRecommendationCard(
                recommendation = rec,
                isExpanded = expandedPairDetails == rec.symbol,
                onToggleExpand = { onTogglePairDetails(rec.symbol) },
                isSwahili = isSwahili,
                onSelectPair = onSelectPair,
                onExecuteTrade = onExecuteTrade
            )
        }
    }
}

/**
 * Individual Card for a Candidate Pair with complete depth breakdown and reasoning.
 */
@Composable
private fun PairRecommendationCard(
    recommendation: PairRecommendation,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    isSwahili: Boolean,
    onSelectPair: ((String) -> Unit)?,
    onExecuteTrade: ((symbol: String, side: OrderSide, lot: Double, tp: Double, sl: Double) -> Unit)?
) {
    val rankBadgeColor = when (recommendation.rank) {
        1 -> AccentNeonGreen
        2 -> AccentCyan
        3 -> AccentGold
        else -> TextMuted
    }

    val sideColor = if (recommendation.recommendedSide == OrderSide.BUY) AccentNeonGreen else DangerRed

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (recommendation.rank == 1) 1.5.dp else 1.dp,
            color = if (recommendation.rank == 1) AccentCyan else BorderSubtle
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Rank, Symbol, Side, Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(rankBadgeColor.copy(alpha = 0.2f))
                            .border(1.dp, rankBadgeColor, CircleShape)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${recommendation.rank}",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = rankBadgeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = recommendation.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(sideColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = recommendation.recommendedSide.name,
                                    color = sideColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Text(
                            text = "${recommendation.marketRegime.displayNameSwahili} • Kina Bora: ${recommendation.bestExchangeForLiquidity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Composite Score
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${recommendation.compositeScore}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = AccentNeonGreen,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isSwahili) "Alama ya Soko" else "Match Score",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Score Pills Strip (Capital Fit, Profit Feasibility, Liquidity)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricScorePill(
                    label = if (isSwahili) "Uwiano wa Mtaji" else "Capital Fit",
                    score = recommendation.capitalFitScore,
                    modifier = Modifier.weight(1f)
                )
                MetricScorePill(
                    label = if (isSwahili) "Urahisi wa Faida" else "Profit Fit",
                    score = recommendation.profitFeasibilityScore,
                    modifier = Modifier.weight(1f)
                )
                MetricScorePill(
                    label = if (isSwahili) "Ukwasi & Kina" else "Liquidity",
                    score = recommendation.liquidityScore,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recommended Parameters Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = if (isSwahili) "Saizi ya Oda (Lot)" else "Lot Size", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                    Text(text = "${recommendation.recommendedLotSize} Lots", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text(text = if (isSwahili) "Faida Inayotarajiwa" else "Exp. Profit", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                    Text(text = "+$${String.format("%,.2f", recommendation.expectedProfitUsd)}", fontWeight = FontWeight.Bold, color = AccentNeonGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text(text = "Take Profit (TP)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                    Text(text = "$${String.format("%,.2f", recommendation.targetProfitPrice)}", fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text(text = "Stop Loss (SL)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                    Text(text = "$${String.format("%,.2f", recommendation.stopLossPrice)}", fontWeight = FontWeight.Bold, color = DangerRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Rationale summary
            Text(
                text = if (isSwahili) recommendation.rationaleSwahili else recommendation.rationaleEn,
                style = MaterialTheme.typography.bodySmall,
                color = TextWhite,
                fontSize = 11.sp
            )

            // Expandable details (Multi-Exchange Order-Book Depth comparison: Binance vs Bybit vs Coinbase vs MT5)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (isSwahili) "📊 Ulinganisho wa Order-Book Depth (±2% Kina cha Soko kwa Exchange):" else "📊 Multi-Exchange Order-Book Depth Breakdown (±2% Liquidity):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val exchangeDepths = when (recommendation.baseAsset) {
                        "BTC" -> listOf(
                            ExchangeDepthInfo("Binance", 86.4, 84.1, 0.8, 0.01),
                            ExchangeDepthInfo("Bybit", 54.2, 52.8, 1.2, 0.02),
                            ExchangeDepthInfo("Coinbase", 46.8, 48.0, 1.5, 0.02),
                            ExchangeDepthInfo("MT5 Bridge", 22.0, 24.5, 2.0, 0.03)
                        )
                        "ETH" -> listOf(
                            ExchangeDepthInfo("Binance", 44.5, 42.0, 1.1, 0.02),
                            ExchangeDepthInfo("Bybit", 29.4, 28.1, 1.4, 0.02),
                            ExchangeDepthInfo("Coinbase", 34.0, 32.5, 1.8, 0.03),
                            ExchangeDepthInfo("MT5 Bridge", 14.2, 15.0, 2.5, 0.04)
                        )
                        "SOL" -> listOf(
                            ExchangeDepthInfo("Binance", 16.2, 14.8, 2.2, 0.03),
                            ExchangeDepthInfo("Bybit", 10.5, 9.8, 2.8, 0.04),
                            ExchangeDepthInfo("Coinbase", 8.4, 7.9, 3.5, 0.05),
                            ExchangeDepthInfo("MT5 Bridge", 4.1, 4.0, 5.0, 0.08)
                        )
                        "XRP" -> listOf(
                            ExchangeDepthInfo("Binance", 12.8, 11.4, 2.0, 0.03),
                            ExchangeDepthInfo("Bybit", 7.2, 6.8, 2.5, 0.04),
                            ExchangeDepthInfo("Coinbase", 5.1, 4.8, 3.2, 0.06),
                            ExchangeDepthInfo("MT5 Bridge", 3.0, 2.9, 4.5, 0.07)
                        )
                        "DOGE" -> listOf(
                            ExchangeDepthInfo("Binance", 8.5, 7.2, 2.8, 0.04),
                            ExchangeDepthInfo("Bybit", 5.2, 4.6, 3.4, 0.05),
                            ExchangeDepthInfo("Coinbase", 3.4, 3.1, 4.2, 0.07),
                            ExchangeDepthInfo("MT5 Bridge", 1.8, 1.5, 6.0, 0.12)
                        )
                        "Gold" -> listOf(
                            ExchangeDepthInfo("MT5 Bridge", 92.0, 88.5, 1.2, 0.01),
                            ExchangeDepthInfo("Exness Real", 78.0, 75.0, 1.0, 0.01),
                            ExchangeDepthInfo("Binance Gold", 15.0, 14.2, 2.5, 0.03)
                        )
                        "EUR" -> listOf(
                            ExchangeDepthInfo("MT5 Bridge", 240.0, 235.0, 0.4, 0.005),
                            ExchangeDepthInfo("Exness Real", 180.0, 175.0, 0.3, 0.005)
                        )
                        else -> listOf(ExchangeDepthInfo("Deriv Server", 25.0, 25.0, 1.5, 0.01))
                    }

                    exchangeDepths.forEach { depth ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "• ${depth.exchangeName}:", style = MaterialTheme.typography.labelSmall, color = TextWhite, fontSize = 10.sp)
                            Text(
                                text = "Bid: $${depth.bidDepth2PctUsdMillions}M | Ask: $${depth.askDepth2PctUsdMillions}M (Spread: ${depth.spreadBps} bps, Slippage: ${depth.estimatedSlippagePct}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onToggleExpand,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isExpanded) (if (isSwahili) "Funga Kina" else "Hide Depth") else (if (isSwahili) "Tazama Kina" else "View Depth"),
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                OutlinedButton(
                    onClick = { onSelectPair?.invoke(recommendation.symbol) },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan)
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = if (isSwahili) "Chati" else "Chart", fontSize = 10.sp, color = AccentCyan)
                }

                Button(
                    onClick = {
                        onSelectPair?.invoke(recommendation.symbol)
                        onExecuteTrade?.invoke(
                            recommendation.symbol,
                            recommendation.recommendedSide,
                            recommendation.recommendedLotSize,
                            recommendation.targetProfitPrice,
                            recommendation.stopLossPrice
                        )
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1.4f).height(34.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = sideColor)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSwahili) "1-Click Trade" else "Execute",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricScorePill(
    label: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        score >= 90 -> AccentNeonGreen
        score >= 75 -> AccentCyan
        score >= 60 -> AccentGold
        else -> DangerRed
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
            Text(text = "$score%", fontWeight = FontWeight.Bold, color = color, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

/**
 * TAB 2: LIVE THOUGHT STREAM & MARKET PREDICTIONS
 */
@Composable
private fun LiveThoughtsAndForecastTab(
    prediction: AutonomousPrediction?,
    thoughts: List<AgentThought>,
    isSwahili: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Latest Market Forecast / Prediction
        item {
            prediction?.let { pred ->
                val isBuy = pred.predictedDirection == OrderSide.BUY
                val predColor = if (isBuy) AccentNeonGreen else DangerRed
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, predColor.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSwahili) "UTABIRI WA WAKALA (PREDICTION):" else "AGENT MARKET FORECAST:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentCyan
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(predColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${pred.predictedDirection.name} (${pred.confidencePct}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = predColor,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Target Price: $${String.format("%,.2f", pred.targetPrice)} (${pred.timeframe} horizon)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextWhite
                        )
                        Text(
                            text = "Confluence: ${pred.indicatorConfluence} • Catalyst: ${pred.primaryCatalyst}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Live Agent Thought Stream ("anafikiri")
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSwahili) "Msururu wa Mawazo ya Wakala (Live Thoughts):" else "Agent Live Thought Stream:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = AccentGold
                )
                Text(
                    text = "${thoughts.size} logs",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        if (thoughts.isEmpty()) {
            item {
                Text(
                    text = if (isSwahili) "Wakala anasubiri tick ya kwanza ili kuanza uchambuzi..." else "Agent awaiting first tick to initialize thoughts...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        items(thoughts.reversed()) { thought ->
            val typeColor = when (thought.thoughtType) {
                "EXECUTION" -> AccentNeonGreen
                "STRATEGY" -> AccentLavender
                "INDICATOR" -> AccentCyan
                "COMMAND" -> AccentOrange
                "PREDICTION" -> AccentGold
                "DYNAMIC_PAIR_SELECTION" -> AccentCyan
                else -> TextWhite
            }
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(thought.timestamp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "[${thought.thoughtType}] • ${thought.targetSymbol}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = typeColor,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "$timeStr (${thought.confidencePct}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = thought.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * TAB 3: AUTONOMOUS BOT CONTROLS, MODES & COMMANDS
 */
@Composable
private fun AutonomousBotAndCommandsTab(
    agent: MasterAutonomousTradingAgent,
    isAutonomous: Boolean,
    systemMode: SystemTradingMode,
    killSwitch: Boolean,
    commandInput: String,
    onCommandChange: (String) -> Unit,
    commandOutput: String?,
    onSendCommand: (String) -> Unit,
    isSwahili: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mode Selector Strip & Autonomous Switch
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isSwahili) "Kujiendesha (Autonomous Bot):" else "Autonomous Execution:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = if (isAutonomous) "Agent executes trades dynamically" else "Manual/Observation only",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = isAutonomous && !killSwitch,
                        onCheckedChange = { agent.toggleAutonomousExecution(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentNeonGreen,
                            checkedTrackColor = AccentNeonGreen.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Modes Buttons (LIVE vs PAPER vs BACKTEST)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SystemTradingMode.values().forEach { mode ->
                        val isSelected = mode == systemMode
                        OutlinedButton(
                            onClick = { agent.setTradingMode(mode) },
                            modifier = Modifier.weight(1f).height(34.dp),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AccentCyan else BorderSubtle
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) AccentCyan.copy(alpha = 0.15f) else Color.Transparent
                            )
                        ) {
                            Text(
                                text = mode.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) AccentCyan else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Emergency Kill Switch Card
        Card(
            colors = CardDefaults.cardColors(containerColor = if (killSwitch) DangerRed.copy(alpha = 0.2f) else SurfaceElevated),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (killSwitch) DangerRed else BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isSwahili) "Kill Switch ya Dharura" else "Emergency Kill Switch",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (killSwitch) DangerRed else TextWhite
                    )
                    Text(
                        text = if (killSwitch) "All trading halted immediately" else "Active protection ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Button(
                    onClick = {
                        if (killSwitch) agent.disengageKillSwitch() else agent.engageKillSwitch("Manual operator toggle")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (killSwitch) AccentNeonGreen else DangerRed),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (killSwitch) (if (isSwahili) "Washa Upya" else "Resume") else (if (isSwahili) "Simamisha Yote" else "KILL SWITCH"),
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Command Console Prompt
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (isSwahili) "Amri za Haraka: /scan, /capital <usd>, /profit <pct>, /kill, /resume" else "Quick Commands: /scan, /capital <usd>, /profit <pct>, /kill, /resume",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            commandOutput?.let { out ->
                Text(
                    text = out,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = AccentCyan,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commandInput,
                    onValueChange = onCommandChange,
                    placeholder = { Text("/scan, /capital 500, /profit 5, /kill", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = {
                        if (commandInput.isNotBlank()) {
                            onSendCommand(commandInput)
                        }
                    },
                    modifier = Modifier.height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text("Send", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun ThreeAgentConsoleTab(
    symbol: String,
    isSwahili: Boolean,
    onExecuteTrade: ((symbol: String, side: OrderSide, lot: Double, tp: Double, sl: Double) -> Unit)? = null
) {
    val threeAgentSystem = remember { CentralThreeAgentSystem.getInstance() }
    val tacticalEngine = remember { TacticalMarketAndPredictionEngine }

    val marketIntel by threeAgentSystem.marketAgent.state.collectAsState()
    val quantMl by threeAgentSystem.quantMlAgent.state.collectAsState()
    val supervisorEval by threeAgentSystem.supervisor.evaluation.collectAsState()
    val predictions by tacticalEngine.predictionsHistory.collectAsState()

    val currentPrediction = predictions.firstOrNull { it.symbol == symbol } ?: predictions.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Title Bar
        item {
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentLavender),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isSwahili) "MUUNGANO WA WAKALA 3 WA KISOKO" else "THREE-AGENT INTELLIGENCE ARCHITECTURE",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentLavender
                        )
                        Text(
                            text = if (isSwahili) "Wakala 1 (Soko) • Wakala 2 (Quant/ML) • Wakala 3 (Msimamizi Mkuu)" else "Agent 1 (Market) • Agent 2 (Quant/ML) • Agent 3 (Supervisor Mother-Brain)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Surface(
                        color = AccentLavender.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = symbol,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentLavender,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // AGENT 1: MARKET INTEL
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSwahili) "AGENT 1: WAKALA WA MAARIFA YA SOKO" else "AGENT 1 — MARKET INTELLIGENCE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                        Text(
                            text = marketIntel.detectedRegime.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MiniMetric("24h Vol", "\$${(marketIntel.volume24hUsdMillions / 1000.0).roundToInt()}B", AccentGold)
                        MiniMetric("Spread", "${marketIntel.spreadBps} bps", if (marketIntel.spreadBps < 2.0) AccentNeonGreen else WarningOrange)
                        MiniMetric("Liquidity", "${marketIntel.liquidityScore}/100", AccentNeonGreen)
                        MiniMetric("Realized Vol", "${marketIntel.realizedVolAnnualizedPct.roundToInt()}%", AccentLavender)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• CVD: ${marketIntel.cvdTrend} | Imbalance: ${marketIntel.bidAskImbalancePct.roundToInt()}% | Depth Bid: \$${marketIntel.orderBookDepthBidMillions.roundToInt()}M",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
            }
        }

        // AGENT 2: QUANT ML
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentLavender.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSwahili) "AGENT 2: WAKALA WA QUANT & MACHINE LEARNING" else "AGENT 2 — QUANT + ML INTELLIGENCE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentLavender
                        )
                        Text(
                            text = "${quantMl.consensusProbabilityPct}% ${quantMl.primaryDirectionalBias.name}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (quantMl.primaryDirectionalBias == OrderSide.BUY) AccentNeonGreen else DangerRed
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MiniMetric("Active Models", "${quantMl.activeModelsCount}", AccentCyan)
                        MiniMetric("Concept Drift", if (quantMl.diagnostics.conceptDriftDetected) "DETECTED" else "CLEAR", if (quantMl.diagnostics.conceptDriftDetected) DangerRed else AccentNeonGreen)
                        MiniMetric("Ensemble Conf", "${quantMl.diagnostics.ensembleConfidencePct}%", AccentGold)
                        MiniMetric("Overfit Score", "${(quantMl.diagnostics.overfittingPenalty * 100).roundToInt()}%", AccentEmerald)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Architectures: Transformer Attention, XGBoost, LSTM, 1D-CNN, PPO Actor-Critic, HMM, SVM",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
            }
        }

        // AGENT 3: SUPERVISOR MOTHER BRAIN
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSwahili) "AGENT 3: MSIMAMIZI MKUU (SUPERVISOR)" else "AGENT 3 — SUPERVISOR MOTHER-BRAIN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )

                        val dec = supervisorEval?.decision ?: SupervisorDecision.WAIT
                        Surface(
                            color = when (dec) {
                                SupervisorDecision.TRADE -> AccentNeonGreen.copy(alpha = 0.2f)
                                SupervisorDecision.REDUCE -> AccentGold.copy(alpha = 0.2f)
                                SupervisorDecision.WAIT -> AccentCyan.copy(alpha = 0.2f)
                                else -> DangerRed.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = dec.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (dec) {
                                    SupervisorDecision.TRADE -> AccentNeonGreen
                                    SupervisorDecision.REDUCE -> AccentGold
                                    SupervisorDecision.WAIT -> AccentCyan
                                    else -> DangerRed
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    supervisorEval?.let { eval ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSwahili) eval.supervisorRationaleSwahili else eval.supervisorRationaleEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhite
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MiniMetric("Consensus", "${eval.consensusScorePct}%", AccentGold)
                            MiniMetric("Approved Lot", "${eval.approvedLotSize}", AccentCyan)
                            MiniMetric("Stop Loss", "\$${eval.dynamicStopLoss.roundToInt()}", DangerRed)
                            MiniMetric("Target TP1", "\$${eval.dynamicTakeProfit1.roundToInt()}", AccentNeonGreen)
                        }

                        if ((eval.decision == SupervisorDecision.TRADE || eval.decision == SupervisorDecision.REDUCE) && eval.approvedSide != null && onExecuteTrade != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    onExecuteTrade(symbol, eval.approvedSide, eval.approvedLotSize, eval.dynamicTakeProfit1, eval.dynamicStopLoss)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text(
                                    text = if (isSwahili) "Fanya Biashara (1-Click Supervisor Trade: ${eval.approvedSide.name})" else "Execute Approved Trade (${eval.approvedSide.name})",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // TACTICAL 15-POINT PREDICTION SUMMARY
        currentPrediction?.let { p ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🎯 UTABIRI WA POINTI 15 (${p.symbol})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "1. Mwelekeo: ${p.directionalBias.name} (${p.probabilityConfidencePct}% Confidence)", style = MaterialTheme.typography.labelSmall, color = AccentNeonGreen)
                        Text(text = "2. Muda wa Mkakati: ${p.timeHorizon} | Regime: ${p.marketRegime.name}", style = MaterialTheme.typography.labelSmall, color = TextWhite)
                        Text(text = "3. Eneo la Kuingia: \$${p.entryTriggerZoneLow.roundToInt()} - \$${p.entryTriggerZoneHigh.roundToInt()}", style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                        Text(text = "4. TP1: \$${p.takeProfit1.roundToInt()} | TP2: \$${p.takeProfit2.roundToInt()} | TP3: \$${p.takeProfit3.roundToInt()}", style = MaterialTheme.typography.labelSmall, color = AccentGold)
                        Text(text = "5. Invalidation: ${p.invalidationCondition}", style = MaterialTheme.typography.labelSmall, color = DangerRed)
                        Text(text = "6. Mkakati: ${p.selectedStrategyName} (${p.whyStrategySelected})", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
    }
}

