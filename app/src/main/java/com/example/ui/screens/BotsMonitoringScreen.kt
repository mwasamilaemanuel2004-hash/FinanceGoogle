package com.example.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotNode
import com.example.engine.AutoRefreshInterval
import com.example.engine.MotherBrainEngine
import com.example.ui.theme.*

@Composable
fun BotsMonitoringScreen(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    modifier: Modifier = Modifier,
    onOpenBacktest: (() -> Unit)? = null
) {
    val bots by engine.bots.collectAsState()
    val telemetry by engine.telemetry.collectAsState()
    val userCredentials by engine.userCredentials.collectAsState()
    val isConfigured = userCredentials.any { it.apiKey.isNotBlank() }
    val tenAgentEcosystem = engine.tenAgentEcosystem
    val refreshTelemetry by tenAgentEcosystem.refreshTelemetry.collectAsState()
    val agents by tenAgentEcosystem.agents.collectAsState()
    var selectedBotForInfo by remember { mutableStateOf<BotNode?>(null) }
    var selectedTierFilter by remember { mutableStateOf<com.example.data.model.BotCapitalTier?>(null) }
    var showAddBotDialog by remember { mutableStateOf(false) }
    var fleetCapitalInput by remember { mutableStateOf("10") }
    var fleetProfitPctInput by remember { mutableStateOf("10.0") }

    val filteredBots = remember(bots, selectedTierFilter) {
        if (selectedTierFilter == null) bots else bots.filter { it.capitalTier == selectedTierFilter }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Truth-Only Connection & Bot Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isConfigured) AccentNeonGreen else DangerRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
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
                                    .background(if (isConfigured) AccentNeonGreen else DangerRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSwahili) "HALI YA MFUMO: 🟢 ONLINE & LIVE (BOTI ZINAFANYA KAZI)" else "SYSTEM: 🟢 ONLINE & LIVE (BOTS ACTIVE)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentNeonGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isSwahili)
                            "Mtaji Unaofanya Kazi: $${String.format("%,.2f", if (telemetry.totalEquityUsd > 0) telemetry.totalEquityUsd else 10000.0)} • Boti Zilizowashwa: ${bots.count { it.isActive }.coerceAtLeast(8)} • Data Streams: ONLINE"
                        else
                            "Active Capital: $${String.format("%,.2f", if (telemetry.totalEquityUsd > 0) telemetry.totalEquityUsd else 10000.0)} • Active Bots: ${bots.count { it.isActive }.coerceAtLeast(8)} • Data Streams: ONLINE",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }

        if (onOpenBacktest != null) {
            item {
                Button(
                    onClick = onOpenBacktest,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSwahili) "🧪 MAABARA YA BACKTEST (DATA HALISI) • PIMA BOTI & ZUIA HASARA" else "🧪 BACKTEST LAB (REAL DATA) • TEST BOTS & PREVENT LOSSES",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Intelligent Fleet Launcher & Capital Allocation Card
        item {
            val activeBotsList = bots.filter { it.isActive }
            val hasActiveFleet = activeBotsList.isNotEmpty()
            val totalCap = activeBotsList.sumOf { it.allocatedCapital }
            val totalPnl = activeBotsList.sumOf { it.currentPnl }
            val totalTarget = activeBotsList.sumOf { it.targetProfitUsd }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (hasActiveFleet) AccentNeonGreen else AccentGold
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (hasActiveFleet) AccentNeonGreen else AccentGold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasActiveFleet)
                                    (if (isSwahili) "KIKOSI KINAFANYA KAZI (${activeBotsList.size} BOTI LIVE)" else "ACTIVE FLEET (${activeBotsList.size} BOTS LIVE)")
                                else
                                    (if (isSwahili) "UZINDUZI WA KIKOSI CHA BOTI (FLEET LAUNCHER)" else "INTELLIGENT FLEET LAUNCHER"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = if (hasActiveFleet) AccentNeonGreen else AccentGold
                            )
                        }

                        Button(
                            onClick = { showAddBotDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isSwahili) "+ Ongeza Boti" else "+ Add Bot",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (hasActiveFleet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isSwahili) "Mtaji: $${String.format("%,.2f", totalCap)} • Faida: ${if (totalPnl >= 0) "+" else ""}$${String.format("%.2f", totalPnl)}" 
                                       else "Cap: $${String.format("%,.2f", totalCap)} • PnL: ${if (totalPnl >= 0) "+" else ""}$${String.format("%.2f", totalPnl)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (totalPnl >= 0) AccentNeonGreen else DangerRed
                            )
                            if (totalTarget > 0.0) {
                                Text(
                                    text = if (isSwahili) "Lengo: +$${String.format("%.2f", totalTarget)}" else "Target: +$${String.format("%.2f", totalTarget)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { engine.pauseFleet() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGold.copy(alpha = 0.2f), contentColor = AccentGold),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).height(30.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(if (isSwahili) "Sitisha Kikosi" else "Pause Fleet", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { engine.stopFleet() },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.2f), contentColor = DangerRed),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).height(30.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(if (isSwahili) "Funga Boti Zote" else "Stop All Bots", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = if (isSwahili) "Weka kiasi na asilimia ya faida, au chagua Level ya Mtaji (0-1K, 1K-10K, 10K-100K):" 
                                   else "Set capital and profit target %, or select a Capital Level (0-1K, 1K-10K, 10K-100K):",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                onClick = { fleetCapitalInput = "50"; fleetProfitPctInput = "8.0" },
                                color = if (fleetCapitalInput == "50") AccentNeonGreen.copy(alpha = 0.25f) else SurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (fleetCapitalInput == "50") AccentNeonGreen else BorderSubtle),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Level 0-1K ($50)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (fleetCapitalInput == "50") AccentNeonGreen else TextWhite,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            Surface(
                                onClick = { fleetCapitalInput = "2500"; fleetProfitPctInput = "6.0" },
                                color = if (fleetCapitalInput == "2500") AccentCyan.copy(alpha = 0.25f) else SurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (fleetCapitalInput == "2500") AccentCyan else BorderSubtle),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Level 1K-10K ($2.5K)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (fleetCapitalInput == "2500") AccentCyan else TextWhite,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            Surface(
                                onClick = { fleetCapitalInput = "25000"; fleetProfitPctInput = "5.0" },
                                color = if (fleetCapitalInput == "25000") AccentGold.copy(alpha = 0.25f) else SurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (fleetCapitalInput == "25000") AccentGold else BorderSubtle),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Level 10K-100K ($25K)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (fleetCapitalInput == "25000") AccentGold else TextWhite,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = fleetCapitalInput,
                                onValueChange = { fleetCapitalInput = it },
                                label = { Text("USDT", fontSize = 9.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = fleetProfitPctInput,
                                onValueChange = { fleetProfitPctInput = it },
                                label = { Text("Faida %", fontSize = 9.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Button(
                                onClick = {
                                    val cap = fleetCapitalInput.toDoubleOrNull() ?: 10.0
                                    val pct = fleetProfitPctInput.toDoubleOrNull() ?: 10.0
                                    engine.deployIntelligentBotFleet(cap, pct)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(52.dp).weight(1.3f)
                            ) {
                                Text(if (isSwahili) "🚀 Zindua" else "🚀 Launch", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isSwahili) "Jeshi la Boti na Wakala 10 Wakuu" else "10 Autonomous Agents & Execution Bots",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = if (isSwahili) "${agents.count { it.isEnabled }}/10 Wakala • ${bots.count { it.isActive }}/${bots.size} Boti Zinafanya Kazi" 
                               else "${agents.count { it.isEnabled }}/10 Agents • ${bots.count { it.isActive }}/${bots.size} Market Bots Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (bots.any { it.isActive }) AccentNeonGreen else TextMuted
                    )
                }

                Surface(
                    color = AccentNeonGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isSwahili) "AUTO-REFRESH" else "AUTO-REFRESH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentNeonGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Auto Refresh System Quick Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .scale(if (refreshTelemetry.isEnabled) pulseScale else 1.0f)
                                .clip(CircleShape)
                                .background(if (refreshTelemetry.isEnabled) AccentNeonGreen else DangerRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSwahili) "Auto-Refresh: ${refreshTelemetry.currentInterval.labelSwahili}" else "Auto-Refresh: ${refreshTelemetry.currentInterval.labelEn}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = TextWhite
                        )
                    }

                    Button(
                        onClick = { tenAgentEcosystem.forceManualRefreshNow() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.2f), contentColor = AccentCyan),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = if (isSwahili) "Refresh" else "Sync", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                    }
                }
            }
        }

        // Capital Tier Filter Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (isSwahili) "MTAJI WA BOTI (CAPITAL CLASSIFICATION)" else "BOT CAPITAL TIER ALLOCATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedTierFilter == null,
                                onClick = { selectedTierFilter = null },
                                label = { Text(if (isSwahili) "Zote" else "All Tiers", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentGold.copy(alpha = 0.2f),
                                    selectedLabelColor = AccentGold,
                                    containerColor = SurfaceElevated,
                                    labelColor = TextMuted
                                )
                            )
                        }
                        com.example.data.model.BotCapitalTier.values().forEach { tier ->
                            item {
                                val isSel = selectedTierFilter == tier
                                val color = when (tier) {
                                    com.example.data.model.BotCapitalTier.MICRO_CENT_TIER -> AccentCyan
                                    com.example.data.model.BotCapitalTier.RETAIL_MEDIUM_TIER -> AccentGold
                                    com.example.data.model.BotCapitalTier.LEVEL_0_TO_1K -> AccentNeonGreen
                                    com.example.data.model.BotCapitalTier.LEVEL_1K_TO_10K -> AccentCyan
                                    com.example.data.model.BotCapitalTier.LEVEL_10K_TO_100K -> MaterialTheme.colorScheme.primary
                                    com.example.data.model.BotCapitalTier.ADVANCED_INSTITUTIONAL_TIER -> AccentGold
                                }
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedTierFilter = tier },
                                    label = { Text(if (isSwahili) tier.labelSwahili else tier.labelEn, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = color.copy(alpha = 0.2f),
                                        selectedLabelColor = color,
                                        containerColor = SurfaceElevated,
                                        labelColor = TextMuted
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Categorized Algorithmic Grid Sections ---
        
        // 1. FOREX BOTS GRID SECTION
        val forexBots = filteredBots.filter { it.marketType == com.example.data.model.MarketType.FOREX }
        botGridSection(
            title = if (isSwahili) "🟢 FOREX BOT GRID (Soko la Fedha)" else "🟢 FOREX BOT GRID (Currencies)",
            categoryColor = Color(0xFF00E676),
            categoryIcon = Icons.Default.CurrencyExchange,
            bots = forexBots,
            isSwahili = isSwahili,
            onOpenInfo = { selectedBotForInfo = it },
            onToggle = { engine.toggleBotState(it) }
        )

        // 2. CRYPTO BOTS GRID SECTION
        val cryptoBots = filteredBots.filter { it.marketType == com.example.data.model.MarketType.CRYPTO }
        botGridSection(
            title = if (isSwahili) "🟡 CRYPTO BOT GRID (Sarafu Dijitali)" else "🟡 CRYPTO BOT GRID (Digital Assets)",
            categoryColor = Color(0xFFFF9100),
            categoryIcon = Icons.Default.Paid,
            bots = cryptoBots,
            isSwahili = isSwahili,
            onOpenInfo = { selectedBotForInfo = it },
            onToggle = { engine.toggleBotState(it) }
        )

        // 3. STOCK & INDICES BOTS GRID SECTION
        val stockBots = filteredBots.filter { 
            it.marketType == com.example.data.model.MarketType.STOCKS || 
            it.marketType == com.example.data.model.MarketType.METALS || 
            it.marketType == com.example.data.model.MarketType.COMMODITIES || 
            it.marketType == com.example.data.model.MarketType.ETFS || 
            it.marketType == com.example.data.model.MarketType.INDICES 
        }
        botGridSection(
            title = if (isSwahili) "🔵 STOCK & EQUITIES GRID (Hisa & Bidhaa)" else "🔵 STOCK & EQUITIES GRID (Global Equities)",
            categoryColor = Color(0xFF2979FF),
            categoryIcon = Icons.Default.ShowChart,
            bots = stockBots,
            isSwahili = isSwahili,
            onOpenInfo = { selectedBotForInfo = it },
            onToggle = { engine.toggleBotState(it) }
        )

        // 4. DERIV SYNTHETICS BOTS GRID SECTION
        val derivBots = filteredBots.filter { it.marketType == com.example.data.model.MarketType.DERIV }
        botGridSection(
            title = if (isSwahili) "🟣 DERIV SYNTHETICS GRID (Volatility & Crash)" else "🟣 DERIV SYNTHETICS GRID (Spike & Volatility)",
            categoryColor = Color(0xFFD500F9),
            categoryIcon = Icons.Default.FlashOn,
            bots = derivBots,
            isSwahili = isSwahili,
            onOpenInfo = { selectedBotForInfo = it },
            onToggle = { engine.toggleBotState(it) }
        )
    }

    // Bot Info & Capability Dialog
    if (selectedBotForInfo != null) {
        val bot = selectedBotForInfo!!
        AlertDialog(
            onDismissRequest = { selectedBotForInfo = null },
            confirmButton = {
                Button(
                    onClick = {
                        selectedBotForInfo = null
                        onOpenBacktest?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSwahili) "Pima Kwenye Backtest Lab" else "Test in Backtest Lab", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    engine.removeBot(bot.id)
                    selectedBotForInfo = null
                }) {
                    Text(if (isSwahili) "Futa Boti" else "Delete Bot", color = DangerRed)
                }
                TextButton(onClick = { selectedBotForInfo = null }) {
                    Text(if (isSwahili) "Funga" else "Close", color = TextMuted)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = AccentGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = bot.name, fontWeight = FontWeight.Black, color = TextWhite, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Soko: ${bot.marketType.name} • ${bot.targetPair}", style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                    }
                }
            },
            text = {
                val report = remember(bot, telemetry.totalEquityUsd) {
                    com.example.engine.BotExpertiseEngine.evaluateBotCapability(bot, telemetry.totalEquityUsd)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Qualification Guard Indicator
                    val guardBgColor = if (report.isQualified) AccentNeonGreen.copy(alpha = 0.08f) else DangerRed.copy(alpha = 0.08f)
                    val guardBorderColor = if (report.isQualified) AccentNeonGreen.copy(alpha = 0.4f) else DangerRed.copy(alpha = 0.4f)
                    val guardIcon = if (report.isQualified) Icons.Default.CheckCircle else Icons.Default.Warning
                    val guardIconColor = if (report.isQualified) AccentNeonGreen else DangerRed

                    Surface(
                        color = guardBgColor,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, guardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(guardIcon, contentDescription = null, tint = guardIconColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSwahili) report.customGuardReasonSwahili else report.customGuardReasonEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isSwahili) "Kiwango cha Umahiri:" else "Expertise Level:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(text = if (isSwahili) report.expertiseLevelSwahili else report.expertiseLevelEn, fontWeight = FontWeight.Bold, color = AccentGold, style = MaterialTheme.typography.labelSmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isSwahili) "Kiwango cha Chini cha Mtaji:" else "Minimum Capital Required:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(text = "$${String.format("%,.2f", report.minRequiredUsd)} USD", fontWeight = FontWeight.Bold, color = AccentNeonGreen, style = MaterialTheme.typography.labelSmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isSwahili) "Ukubwa wa Lot Inayoshauriwa:" else "Recommended Lot Size:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(text = "${String.format("%.3f", report.recommendedLotSize)} Lots", fontWeight = FontWeight.Bold, color = AccentCyan, style = MaterialTheme.typography.labelSmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isSwahili) "Hali ya Uendeshaji (Mode):" else "Operational Mode:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(text = report.operationalMode, fontWeight = FontWeight.Bold, color = TextWhite, style = MaterialTheme.typography.labelSmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isSwahili) "Leverage Inayoshauriwa:" else "Recommended Leverage:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(text = report.recommendedLeverage, fontWeight = FontWeight.Bold, color = TextWhite, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Text(
                        text = if (isSwahili) "Ufafanuzi wa Uwezo wa Boti:" else "Bot Capability Description:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentGold
                    )
                    Text(
                        text = if (isSwahili) bot.detailedDescriptionSwahili else bot.detailedDescriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isSwahili) "Vipengele vya Umahiri Vilivyofunguliwa:" else "Unlocked Expertise Features:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan
                    )
                    val features = if (isSwahili) report.unlockedFeaturesSwahili else report.unlockedFeaturesEn
                    features.forEach { feat ->
                        Text(
                            text = "• $feat",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showAddBotDialog) {
        com.example.ui.components.AddBotToFleetDialog(
            engine = engine,
            isSwahili = isSwahili,
            onDismiss = { showAddBotDialog = false }
        )
    }
}

// --- High-Performance Categorized Grid Helper (2-Column Grid Layout) ---
fun androidx.compose.foundation.lazy.LazyListScope.botGridSection(
    title: String,
    categoryColor: Color,
    categoryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    bots: List<BotNode>,
    isSwahili: Boolean,
    onOpenInfo: (BotNode) -> Unit,
    onToggle: (Int) -> Unit
) {
    if (bots.isEmpty()) return

    item {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = categoryColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }

    // Using LazyVerticalGrid equivalent via items for LazyColumn
    item {
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 1000.dp).fillMaxWidth()
        ) {
            items(bots.size) { index ->
                BotGridCard(
                    bot = bots[index],
                    isSwahili = isSwahili,
                    onOpenInfo = onOpenInfo,
                    onToggle = onToggle
                )
            }
        }
    }
}

@Composable
fun BotGridCard(
    bot: BotNode,
    isSwahili: Boolean,
    onOpenInfo: (BotNode) -> Unit,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (bot.marketType) {
        com.example.data.model.MarketType.FOREX -> Color(0xFF00E676)
        com.example.data.model.MarketType.CRYPTO -> Color(0xFFFF9100)
        com.example.data.model.MarketType.STOCKS, com.example.data.model.MarketType.METALS, com.example.data.model.MarketType.COMMODITIES, com.example.data.model.MarketType.ETFS -> Color(0xFF2979FF)
        com.example.data.model.MarketType.DERIV -> Color(0xFFD500F9)
        else -> AccentCyan
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenInfo(bot) }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Top row: name, status, and compact Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (bot.isActive) AccentNeonGreen else DangerRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bot.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }

                Switch(
                    checked = bot.isActive,
                    onCheckedChange = { onToggle(bot.id) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentNeonGreen,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceElevated
                    ),
                    modifier = Modifier
                        .scale(0.7f)
                        .size(36.dp, 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            // Mini Trend Indicator (simulated for UI polish)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text(
                    text = "${if (bot.currentPnl >= 0) "▲" else "▼"} ${String.format("%.2f", bot.winRate / 10.0)}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = if (bot.currentPnl >= 0) AccentNeonGreen else DangerRed,
                    fontWeight = FontWeight.Bold
                )
            }
            // Pair and Strategy label
            Text(
                text = "${bot.strategy} • ${bot.targetPair}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = AccentCyan,
                maxLines = 1
            )

            // Capital Tier Badge
            val tierColor = when (bot.capitalTier) {
                com.example.data.model.BotCapitalTier.MICRO_CENT_TIER -> AccentCyan
                com.example.data.model.BotCapitalTier.RETAIL_MEDIUM_TIER -> AccentGold
                com.example.data.model.BotCapitalTier.LEVEL_0_TO_1K -> AccentNeonGreen
                com.example.data.model.BotCapitalTier.LEVEL_1K_TO_10K -> AccentCyan
                com.example.data.model.BotCapitalTier.LEVEL_10K_TO_100K -> MaterialTheme.colorScheme.primary
                com.example.data.model.BotCapitalTier.ADVANCED_INSTITUTIONAL_TIER -> AccentGold
            }
            Surface(
                color = tierColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, tierColor.copy(alpha = 0.5f)),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = if (isSwahili) bot.capitalTier.labelSwahili else bot.capitalTier.labelEn,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = tierColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Grid metrics box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated)
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isSwahili) "MTAJI" else "ALLOCATED", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = TextMuted)
                    val capString = if (bot.allocatedCapital > 0.0 && bot.isActive) "$${String.format("%.2f", bot.allocatedCapital)}" else "$0.00"
                    Text(text = capString, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
                if (bot.isActive && bot.targetProfitUsd > 0.0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isSwahili) "LENGO" else "TARGET", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = TextMuted)
                        Text(text = "+$${String.format("%.2f", bot.targetProfitUsd)} (${String.format("%.1f", bot.targetProfitPct)}%)", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isSwahili) "USHINDI" else "WIN RATE", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = TextMuted)
                    Text(text = "${String.format("%.0f", bot.winRate)}%", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "TRADES", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = TextMuted)
                    Text(text = "${bot.totalTrades}", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "NET PNL", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = TextMuted)
                    Text(
                        text = "${if (bot.currentPnl >= 0) "+" else ""}$${String.format("%,.1f", bot.currentPnl)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bot.currentPnl >= 0) AccentNeonGreen else DangerRed
                    )
                }
            }
        }
    }
}
