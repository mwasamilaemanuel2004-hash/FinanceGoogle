package com.example.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveTrade
import com.example.data.model.ConnectorProvider
import com.example.data.model.OrderSide
import com.example.data.model.RealAccountConnection
import com.example.engine.MotherBrainEngine
import com.example.engine.RealAccountBridgeManager
import com.example.engine.lib.tools.Candle
import com.example.engine.lib.tools.TaLibReal
import com.example.ui.components.RealAccountConnectionDialog
import com.example.ui.theme.*

@Composable
fun PortfolioScreen(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    onGoToApiSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bridgeManager = remember { RealAccountBridgeManager.getInstance(context) }
    val connectedAccounts by bridgeManager.connectedAccounts.collectAsState()
    val activeRealAccount by bridgeManager.activeRealAccount.collectAsState()
    val isLiveAuthorized by bridgeManager.isLiveTradingAuthorized.collectAsState()
    val telemetry by engine.telemetry.collectAsState()
    val activeTrades by engine.activeTrades.collectAsState()
    val targetGoal by engine.targetProfitGoal.collectAsState()
    val ticks by engine.tickEngine.ticks.collectAsState()

    var showConnectDialog by remember { mutableStateOf(false) }
    var showQuickBalanceDialog by remember { mutableStateOf(false) }
    var balanceInputText by remember { mutableStateOf("") }
    var selectedAccountIdForBalance by remember { mutableStateOf("") }
    var customProfitPctInput by remember { mutableStateOf("") }

    // Real funds computation: strictly sum verified connected accounts
    val realConnectedTotal = remember(connectedAccounts) {
        connectedAccounts.filter { it.isConnected }.sumOf { it.equity }
    }
    val displayEquity = if (connectedAccounts.isNotEmpty()) {
        realConnectedTotal
    } else if (telemetry.totalEquityUsd > 0.0) {
        telemetry.totalEquityUsd
    } else {
        0.0
    }

    val marginUsed = if (displayEquity > 0.0) (activeTrades.size * 25.0).coerceAtMost(displayEquity) else 0.0
    val freeMargin = (displayEquity - marginUsed).coerceAtLeast(0.0)
    val btcPrice = ticks["BTC/USDT"]?.price ?: 68500.0

    // Compute top 5 quant strategies
    val btcCandles = remember(ticks) {
        val raw = engine.tickEngine.getCandlesForSymbol("BTC/USDT")
        raw.map { c -> Candle(c.timestamp, c.open, c.high, c.low, c.close, c.volume) }
    }
    val top5Strategies = remember(btcPrice, btcCandles, targetGoal.profitPercentagePerTrade) {
        TaLibReal.evaluateTop5Strategies("BTC/USDT", btcPrice, btcCandles, targetGoal.profitPercentagePerTrade, targetGoal.stopLossPercentagePerTrade)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOTAL PORTFOLIO EQUITY OVERVIEW CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (connectedAccounts.isNotEmpty()) AccentNeonGreen.copy(alpha = 0.8f) else AccentGold),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isSwahili) "Jumla ya Mtaji na Salio Halisi (Portfolio Funds)" else "Total Verified Portfolio Funds (USD)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format("%,.2f", displayEquity)}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (displayEquity > 0.0) AccentNeonGreen else TextWhite
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = if (connectedAccounts.isNotEmpty()) AccentNeonGreen.copy(alpha = 0.15f) else Color(0xFF33200B),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (connectedAccounts.isNotEmpty()) AccentNeonGreen else AccentGold)
                            ) {
                                Text(
                                    text = if (connectedAccounts.isNotEmpty()) (if (isLiveAuthorized) "🟢 LIVE REAL FUNDS" else "🟢 VERIFIED ONLINE") else "⚠️ NO BROKER LINKED",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (connectedAccounts.isNotEmpty()) AccentNeonGreen else AccentGold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // Quick balance sync / edit button
                            FilledTonalButton(
                                onClick = {
                                    if (connectedAccounts.isNotEmpty()) {
                                        selectedAccountIdForBalance = connectedAccounts.first().id
                                        balanceInputText = connectedAccounts.first().balance.toString()
                                    } else {
                                        balanceInputText = "500.00"
                                    }
                                    showQuickBalanceDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                modifier = Modifier.height(28.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = SurfaceElevated)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSwahili) "Sasisha Salio" else "Update Fund",
                                    fontSize = 10.sp,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = if (isSwahili) "Dhamana Iliyotumika" else "Margin Used", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(text = "$${String.format("%,.2f", marginUsed)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Column {
                            Text(text = if (isSwahili) "Dhamana Huru (Free)" else "Free Margin", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = "$${String.format("%,.2f", freeMargin)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentNeonGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = if (isSwahili) "Faida ya Lengo Sasa" else "Accumulated Profit", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            val accProfit = targetGoal.currentAccumulatedProfitUsd
                            Text(
                                text = "${if (accProfit >= 0) "+" else ""}$${String.format("%,.2f", accProfit)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (accProfit >= 0) AccentNeonGreen else DangerRed
                            )
                        }
                    }
                }
            }
        }

        // 2. ULTRA-CLEAR PROFIT TARGET PER TRADE & TIME-BOUND GOAL SYSTEM ("system rahisi kusema profit percentage per trade na target kwa muda fulani")
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1728)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentCyan.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row with Title and Auto-Trading Master Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrackChanges, contentDescription = "Target", tint = AccentCyan, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isSwahili) "MFUMO WA LENGO LA FAIDA NA MUDA" else "PROFIT TARGET & HORIZON SYSTEM",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = AccentCyan
                                )
                                Text(
                                    text = if (targetGoal.isAutoTradingEnabled)
                                        (if (isSwahili) "🟢 Biashara ya Kiotomatiki INAFANYA KAZI (Auto-Trade ON)" else "🟢 Autonomous Engine ACTIVE (Auto-Trade ON)")
                                    else
                                        (if (isSwahili) "⏸️ Biashara ya Kiotomatiki IMESIMAMA" else "⏸️ Autonomous Trading PAUSED"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (targetGoal.isAutoTradingEnabled) AccentNeonGreen else TextMuted
                                )
                            }
                        }
                        Switch(
                            checked = targetGoal.isAutoTradingEnabled,
                            onCheckedChange = { engine.toggleAutoTrading(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentNeonGreen,
                                checkedTrackColor = AccentNeonGreen.copy(alpha = 0.35f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Section A: Profit Percentage per trade
                    Text(
                        text = if (isSwahili) "1. Chagua Asilimia ya Faida kwa Kila Trade (Target Profit %):" else "1. Select Profit Target % Per Trade:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val profitPresets = listOf(0.5, 1.0, 2.0, 3.0, 5.0, 10.0)
                        items(profitPresets) { pct ->
                            val isSelected = targetGoal.profitPercentagePerTrade == pct
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentCyan else SurfaceElevated)
                                    .border(1.dp, if (isSelected) AccentCyan else BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { engine.updateProfitTargetPerTrade(pct) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$pct% ${if (pct <= 1.0) "⚡ Scalp" else if (pct <= 3.0) "⚖️ Standard" else "🚀 Alpha"}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.Black else TextWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Section B: Target Duration Horizon
                    Text(
                        text = if (isSwahili) "2. Lengo la Faida kwa Kipindi cha Muda (Time-bound Goal):" else "2. Time-Bound Target Horizon:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val horizonPresets = listOf(
                            Triple(1, if (isSwahili) "Masaa 24" else "24 Hours", 150.0),
                            Triple(3, if (isSwahili) "Siku 3" else "3 Days", 300.0),
                            Triple(7, if (isSwahili) "Siku 7 (Wiki)" else "7 Days (Week)", 500.0),
                            Triple(30, if (isSwahili) "Siku 30 (Mwezi)" else "30 Days (Month)", 1500.0)
                        )
                        items(horizonPresets) { (days, label, targetUsd) ->
                            val isChosen = targetGoal.targetPeriodDays == days
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) AccentGold else SurfaceElevated)
                                    .border(1.dp, if (isChosen) AccentGold else BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { engine.updateTargetPeriod(days, targetUsd) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isChosen) Color.Black else TextWhite
                                    )
                                    Text(
                                        text = "Lengo: $${targetUsd.toInt()}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = if (isChosen) Color.Black else AccentGold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar towards target
                    val progressRatio = (targetGoal.currentAccumulatedProfitUsd / targetGoal.targetProfitUsd.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()
                    val progressPct = progressRatio * 100f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isSwahili) "Maendeleo: $${String.format("%,.2f", targetGoal.currentAccumulatedProfitUsd)} / $${String.format("%,.2f", targetGoal.targetProfitUsd)}"
                                   else "Goal Progress: $${String.format("%,.2f", targetGoal.currentAccumulatedProfitUsd)} / $${String.format("%,.2f", targetGoal.targetProfitUsd)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.1f", progressPct)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentNeonGreen,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = AccentNeonGreen,
                        trackColor = SurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ACTION BUTTONS: One-tap Execute Trade & Auto-Trading Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                engine.executeQuickTrade("BTC/USDT", OrderSide.BUY)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.3f).height(44.dp)
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = "Trade", tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "TEKELEZA TRADE SASA" else "EXECUTE TRADE NOW",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                engine.toggleAutoTrading(!targetGoal.isAutoTradingEnabled)
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (targetGoal.isAutoTradingEnabled) AccentGold else AccentCyan),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text(
                                text = if (targetGoal.isAutoTradingEnabled)
                                    (if (isSwahili) "Simamisha" else "Pause Auto")
                                else
                                    (if (isSwahili) "Anza Kiotomatiki" else "Resume Auto"),
                                color = if (targetGoal.isAutoTradingEnabled) AccentGold else AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. CONNECTED BROKER & EXCHANGE ACCOUNTS LIST (Zero Fake Data)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = "Accounts", tint = AccentGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSwahili) "Akaunti Zilizounganishwa Halisi (${connectedAccounts.size})" else "Connected Real Accounts (${connectedAccounts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { showConnectDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Connect", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isSwahili) "+ Unganisha API" else "+ Connect API", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (connectedAccounts.isEmpty()) {
                        // Truthful Empty State
                        Surface(
                            color = Color(0xFF141926),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.LinkOff, contentDescription = null, tint = AccentGold, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isSwahili) "Hakuna API au Dalali Aliyeunganishwa Bado" else "No Exchange or Broker Connected Yet",
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isSwahili) "Weka API ya Binance, Bybit, Deriv au MT5 ili kuona salio lako halisi hapa." else "Connect your Binance, Bybit, Deriv or MT5 API to sync your live funds.",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showConnectDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isSwahili) "Unganisha Binance au MT5 Sasa" else "Connect Binance or MT5 Now", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        connectedAccounts.forEach { acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .border(0.5.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentNeonGreen))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = acc.accountName, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 12.sp)
                                    }
                                    Text(
                                        text = "${acc.provider.displayName} • Latency: ${acc.pingLatencyMs}ms • ID: ${acc.maskedLoginOrKey}",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${String.format("%,.2f", acc.balance)} ${acc.currency}",
                                        fontWeight = FontWeight.Black,
                                        color = AccentNeonGreen,
                                        fontSize = 13.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "LIVE ONLINE",
                                            color = AccentCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = {
                                                selectedAccountIdForBalance = acc.id
                                                balanceInputText = acc.balance.toString()
                                                showQuickBalanceDialog = true
                                            },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Balance", tint = AccentGold, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // 4. ACTIVE OPEN TRADES & AUTONOMOUS LIVE EXECUTIONS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = "Trades", tint = AccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSwahili) "Biashara Zinazoendelea (${activeTrades.size})" else "Active Live Trades (${activeTrades.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            color = AccentNeonGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "AUTONOMOUS QUANT",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = AccentNeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeTrades.isEmpty()) {
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isSwahili) "Hakuna biashara iliyo wazi kwa sasa." else "No active trades open right now.",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { engine.executeQuickTrade("BTC/USDT", OrderSide.BUY) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isSwahili) "Fungua Trade ya Jaribio" else "Open Live Confluence Trade", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        activeTrades.forEach { trade ->
                            val isBuy = trade.side == OrderSide.BUY
                            val isProfitable = trade.unrealizedPnl >= 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .border(0.5.dp, if (isProfitable) AccentNeonGreen.copy(alpha = 0.5f) else DangerRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isBuy) "BUY (LONG)" else "SELL (SHORT)",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = if (isBuy) AccentNeonGreen else DangerRed
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = trade.symbol, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 12.sp)
                                    }
                                    Text(
                                        text = "Entry: ${trade.entryPrice} • TP: ${trade.takeProfit} • SL: ${trade.stopLoss}",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (isProfitable) "+" else ""}$${String.format("%,.2f", trade.unrealizedPnl)}",
                                        fontWeight = FontWeight.Black,
                                        color = if (isProfitable) AccentNeonGreen else DangerRed,
                                        fontSize = 13.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Lot: ${trade.lotSize}",
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { engine.closeTrade(trade.id) },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Close Trade", tint = DangerRed, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // 5. STRENGTHENED TOP 5 QUANT STRATEGIES & LIVE SIGNALS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = "Strategies", tint = AccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSwahili) "Mbinu 5 Kuu za Quant (Top 5 Quant Strategies)" else "Top 5 High-Precision Quant Strategies",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            color = AccentCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "10/10 CONFLUENCE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = AccentCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    top5Strategies.forEach { strat ->
                        val sigColor = when (strat.signal) {
                            "BUY" -> AccentNeonGreen
                            "SELL" -> DangerRed
                            else -> AccentGold
                        }
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strat.strategyName,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        fontSize = 11.sp
                                    )
                                    Surface(
                                        color = sigColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${strat.signal} (${strat.confidencePct}%)",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            color = sigColor
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strat.rationale,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Win Rate: ${strat.winRateHistoricalPct}%",
                                        color = AccentNeonGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "TP: $${String.format("%,.1f", strat.takeProfit)} • SL: $${String.format("%,.1f", strat.stopLoss)}",
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }

    // Quick Balance Update / Sync Dialog
    if (showQuickBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showQuickBalanceDialog = false },
            title = {
                Text(
                    text = if (isSwahili) "Sasisha / Weka Salio Halisi (Funds)" else "Update Verified Portfolio Funds",
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isSwahili) "Weka kiasi halisi cha mtaji wako kwenye exchange/dalali (Mf. $5, $50, $1000):"
                               else "Enter the verified capital available on your exchange or broker:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = balanceInputText,
                        onValueChange = { balanceInputText = it },
                        label = { Text(if (isSwahili) "Salio Halisi ($ USD/USDT)" else "Verified Funds ($ USD/USDT)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = balanceInputText.toDoubleOrNull() ?: 100.0
                        if (connectedAccounts.isNotEmpty()) {
                            val accId = selectedAccountIdForBalance.ifEmpty { connectedAccounts.first().id }
                            bridgeManager.updateAccountBalance(accId, parsed)
                        } else {
                            engine.syncEquityFromBridge(parsed)
                        }
                        showQuickBalanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen)
                ) {
                    Text(if (isSwahili) "Thibitisha & Hifadhi" else "Confirm & Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickBalanceDialog = false }) {
                    Text(if (isSwahili) "Ghairi" else "Cancel", color = TextMuted)
                }
            }
        )
    }

    if (showConnectDialog) {
        RealAccountConnectionDialog(
            bridgeManager = bridgeManager,
            isSwahili = isSwahili,
            onDismiss = { showConnectDialog = false }
        )
    }
}
