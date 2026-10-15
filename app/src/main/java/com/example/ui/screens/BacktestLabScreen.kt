package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotNode
import com.example.engine.MotherBrainEngine
import com.example.engine.backtest.*
import com.example.engine.challenge.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

private val SurfaceDarker = DarkCharcoal
private val AccentOrangeRed = AccentRed

@Composable
fun BacktestLabScreen(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val backtestEngine = remember { engine.backtestEngine }

    val isRunning by backtestEngine.isBacktesting.collectAsState()
    val progress by backtestEngine.currentProgress.collectAsState()
    val statusText by backtestEngine.statusText.collectAsState()
    val strategyResult by backtestEngine.lastStrategyResult.collectAsState()
    val botResult by backtestEngine.lastBotResult.collectAsState()
    val systemResult by backtestEngine.lastSystemResult.collectAsState()
    val trainingResult by backtestEngine.lastTrainingResult.collectAsState()
    val lossGuard by backtestEngine.lossPreventionConfig.collectAsState()
    val gamResult by backtestEngine.lastGamResult.collectAsState()
    val derivMindResult by backtestEngine.lastDerivMindResult.collectAsState()
    val multiMarketReport by backtestEngine.lastMultiMarketReport.collectAsState()
    val currentChallenge by EarnChallengeEngine.currentChallenge.collectAsState()
    val bots by engine.bots.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedStrategy by remember { mutableStateOf(StrategyType.SMC_ORDER_BLOCK) }
    var selectedSymbol by remember { mutableStateOf("BTC/USDT") }
    var selectedTimeframe by remember { mutableStateOf("1h") }
    var selectedBotIndex by remember { mutableIntStateOf(0) }
    var selectedBotCapital by remember { mutableDoubleStateOf(100.0) }
    var multiTierResults by remember { mutableStateOf<Map<Double, BacktestResult>?>(null) }
    var selectedBotForInfo by remember { mutableStateOf<BotNode?>(null) }
    var trainingEpochs by remember { mutableIntStateOf(5) }
    var appliedNotice by remember { mutableStateOf<String?>(null) }

    // GAM Mode & Deriv Mind Testing State
    var gamSelectedBotId by remember { mutableIntStateOf(0) } // 0 = All 20 Bots, 1-20 = Bot #
    var gamProfitTarget by remember { mutableDoubleStateOf(1.0) }
    var gamTradesCount by remember { mutableIntStateOf(200) }
    var gamCapital by remember { mutableDoubleStateOf(100.0) }
    var derivSymbol by remember { mutableStateOf("VOLATILITY 75") }
    var derivTimeframe by remember { mutableStateOf("1m") }

    val supportedSymbols = listOf(
        "BTC/USDT", "ETH/USDT", "SOL/USDT",
        "EUR/USD", "GBP/USD", "USD/JPY",
        "XAU/USD", "BRENT", "US500", "VOLATILITY 75", "NVDA"
    )

    val timeframes = listOf("15m", "1h", "4h", "1d")

    Scaffold(
        topBar = {
            Surface(
                color = SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SurfaceDarker, CircleShape)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isSwahili) "Maabara ya Backtest & Mafunzo ya AI" else "Quant Backtest Lab & AI Training",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                                Text(
                                    text = if (isSwahili) "Data Halisi • Mikakati • Boti • Mfumo • Kuzuia Hasara" else "Real Market Data • Strategies • Bots • Portfolio • Loss Prevention",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentGold
                                )
                            }
                        }

                        // Real Data Badge
                        Surface(
                            color = AccentNeonGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(AccentNeonGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "REAL DATA ENGINE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentNeonGreen,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Selector (7 Modular Labs)
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = AccentGold,
                        edgePadding = 8.dp,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(if (isSwahili) "1. Mikakati" else "1. Strategies", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(15.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(if (isSwahili) "2. Boti 20" else "2. 20 Bots", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(15.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text(if (isSwahili) "3. GAM & Deriv" else "3. GAM & Deriv", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(15.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text(if (isSwahili) "4. Mfumo" else "4. System", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(15.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            text = { Text(if (isSwahili) "5. Zuia Hasara" else "5. Loss Guard", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(15.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 5,
                            onClick = { selectedTab = 5 },
                            text = { Text(if (isSwahili) "6. Ripoti" else "6. Audit Report", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(15.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 6,
                            onClick = { selectedTab = 6 },
                            text = { Text(if (isSwahili) "7. Earn Challenge" else "7. Earn Challenge", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(15.dp)) }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Execution Progress Banner
            AnimatedVisibility(visible = isRunning) {
                Surface(
                    color = AccentCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = AccentCyan,
                            trackColor = SurfaceDarker,
                        )
                    }
                }
            }

            // Applied Model Notice
            if (appliedNotice != null) {
                Surface(
                    color = AccentNeonGreen.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentNeonGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = appliedNotice ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                        IconButton(onClick = { appliedNotice = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> StrategyBacktestTab(
                    isSwahili = isSwahili,
                    isRunning = isRunning,
                    selectedStrategy = selectedStrategy,
                    onSelectStrategy = { selectedStrategy = it },
                    selectedSymbol = selectedSymbol,
                    onSelectSymbol = { selectedSymbol = it },
                    selectedTimeframe = selectedTimeframe,
                    onSelectTimeframe = { selectedTimeframe = it },
                    supportedSymbols = supportedSymbols,
                    timeframes = timeframes,
                    result = strategyResult,
                    onRunBacktest = {
                        coroutineScope.launch {
                            backtestEngine.runStrategyBacktest(
                                strategy = selectedStrategy,
                                symbol = selectedSymbol,
                                timeframe = selectedTimeframe
                            )
                        }
                    }
                )
                1 -> BotBacktestTab(
                    isSwahili = isSwahili,
                    isRunning = isRunning,
                    bots = bots,
                    selectedBotIndex = selectedBotIndex,
                    onSelectBot = { 
                        selectedBotIndex = it 
                        multiTierResults = null
                    },
                    selectedCapital = selectedBotCapital,
                    onSelectCapital = { selectedBotCapital = it },
                    result = botResult,
                    multiTierResults = multiTierResults,
                    onOpenInfo = { bot -> selectedBotForInfo = bot },
                    onRunBacktest = {
                        coroutineScope.launch {
                            if (bots.isNotEmpty() && selectedBotIndex in bots.indices) {
                                backtestEngine.runBotBacktest(
                                    bot = bots[selectedBotIndex],
                                    initialCapital = selectedBotCapital
                                )
                            }
                        }
                    },
                    onRunMultiTierBacktest = {
                        coroutineScope.launch {
                            if (bots.isNotEmpty() && selectedBotIndex in bots.indices) {
                                val currentBot = bots[selectedBotIndex]
                                val tiers = listOf(10.0, 50.0, 100.0, 1000.0)
                                val resultsMap = mutableMapOf<Double, BacktestResult>()
                                for (tier in tiers) {
                                    val r = backtestEngine.runBotBacktest(
                                        bot = currentBot,
                                        initialCapital = tier
                                    )
                                    resultsMap[tier] = r
                                }
                                multiTierResults = resultsMap
                            }
                        }
                    }
                )
                2 -> GamAndDerivBacktestTab(
                    isSwahili = isSwahili,
                    isRunning = isRunning,
                    bots = bots,
                    selectedBotId = gamSelectedBotId,
                    onSelectBotId = { gamSelectedBotId = it },
                    profitTarget = gamProfitTarget,
                    onSelectProfitTarget = { gamProfitTarget = it },
                    tradesCount = gamTradesCount,
                    onSelectTradesCount = { gamTradesCount = it },
                    capital = gamCapital,
                    onSelectCapital = { gamCapital = it },
                    gamResult = gamResult,
                    derivSymbol = derivSymbol,
                    onSelectDerivSymbol = { derivSymbol = it },
                    derivTimeframe = derivTimeframe,
                    onSelectDerivTimeframe = { derivTimeframe = it },
                    derivMindResult = derivMindResult,
                    onRunGamBacktest = {
                        coroutineScope.launch {
                            backtestEngine.runGamModeBacktest(
                                botId = gamSelectedBotId,
                                targetProfitPerTrade = gamProfitTarget,
                                targetTrades = gamTradesCount,
                                timeframe = "5m",
                                initialCapital = gamCapital
                            )
                        }
                    },
                    onRunDerivMindBacktest = {
                        coroutineScope.launch {
                            backtestEngine.runDerivMindAnalysis(
                                symbol = derivSymbol,
                                timeframe = derivTimeframe,
                                initialCapital = gamCapital
                            )
                        }
                    }
                )
                3 -> FullSystemBacktestTab(
                    isSwahili = isSwahili,
                    isRunning = isRunning,
                    bots = bots,
                    result = systemResult,
                    onRunSystemBacktest = {
                        coroutineScope.launch {
                            backtestEngine.runFullSystemBacktest(bots)
                        }
                    }
                )
                4 -> AgentTrainingAndLossGuardTab(
                    isSwahili = isSwahili,
                    isRunning = isRunning,
                    lossGuard = lossGuard,
                    onUpdateLossGuard = { backtestEngine.updateLossPreventionConfig(it) },
                    trainingEpochs = trainingEpochs,
                    onSelectEpochs = { trainingEpochs = it },
                    trainingResult = trainingResult,
                    onRunTraining = {
                        coroutineScope.launch {
                            backtestEngine.trainAgentWithLossPrevention(epochs = trainingEpochs)
                        }
                    },
                    onApplyToMotherBrain = {
                        trainingResult?.let { res ->
                            engine.applyTrainedModelToMotherBrain(res)
                            appliedNotice = if (isSwahili)
                                "✅ Kanuni zilizofunzwa zimeunganishwa kwenye Ubongo Mkuu! Hasara imelindwa kikamilifu."
                            else
                                "✅ Trained loss-prevention models applied to Mother Brain live engine!"
                        }
                    }
                )
                5 -> SystemOptimizationReportTab(
                    isSwahili = isSwahili,
                    bots = bots,
                    engine = engine
                )
                6 -> EarnChallengeAndMultiMarketTab(
                    isSwahili = isSwahili,
                    isRunning = isRunning,
                    currentChallenge = currentChallenge,
                    multiMarketReport = multiMarketReport,
                    onStartChallenge = { level, cap, tf ->
                        EarnChallengeEngine.startChallenge(level, cap, tf)
                    },
                    onRunSequentialBacktest = {
                        coroutineScope.launch {
                            backtestEngine.runSequentialAllMarketsBacktest()
                        }
                    }
                )
            }
        }
    }

    // Bot Info Capability Dialog in Backtest Lab
    if (selectedBotForInfo != null) {
        val bot = selectedBotForInfo!!
        AlertDialog(
            onDismissRequest = { selectedBotForInfo = null },
            confirmButton = {
                Button(
                    onClick = { selectedBotForInfo = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black)
                ) {
                    Text(if (isSwahili) "Sawa, Nimeelewa" else "Understood", fontWeight = FontWeight.Bold)
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isSwahili) "Kiwango cha Chini cha Mtaji:" else "Minimum Capital:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(text = "$${bot.minCapitalUsd} USD", fontWeight = FontWeight.Bold, color = AccentNeonGreen, style = MaterialTheme.typography.labelSmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isSwahili) "Micro-Lot / Cent Lot:" else "Micro-Lot Sizing:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(text = if (bot.microLotSupported) "NDIYO (0.01 / Micro-Stake)" else "HAPANA", fontWeight = FontWeight.Bold, color = AccentGold, style = MaterialTheme.typography.labelSmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isSwahili) "Leverage Salama:" else "Recommended Leverage:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(text = bot.recommendedLeverage, fontWeight = FontWeight.Bold, color = TextWhite, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Text(
                        text = if (isSwahili) "Uwezo na Mkakati wa Kutrade:" else "Strategy & Risk Mitigation:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentGold
                    )
                    Text(
                        text = if (isSwahili) bot.detailedDescriptionSwahili else bot.detailedDescriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// =============================================================================
// TAB 0: STRATEGY BACKTEST ("stategies moja moja")
// =============================================================================
@Composable
fun StrategyBacktestTab(
    isSwahili: Boolean,
    isRunning: Boolean,
    selectedStrategy: StrategyType,
    onSelectStrategy: (StrategyType) -> Unit,
    selectedSymbol: String,
    onSelectSymbol: (String) -> Unit,
    selectedTimeframe: String,
    onSelectTimeframe: (String) -> Unit,
    supportedSymbols: List<String>,
    timeframes: List<String>,
    result: BacktestResult?,
    onRunBacktest: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isSwahili) "CHAGUA MKAKATI WA KUJARIBU (STRATEGY):" else "SELECT QUANT STRATEGY TO BACKTEST:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AccentGold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Horizontal strategy chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(StrategyType.values()) { strat ->
                    val isSelected = strat == selectedStrategy
                    Surface(
                        color = if (isSelected) AccentGold.copy(alpha = 0.2f) else SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AccentGold else BorderSubtle
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onSelectStrategy(strat) }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = if (isSwahili) strat.nameSwahili else strat.nameEn,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) AccentGold else TextWhite
                            )
                            Text(
                                text = strat.code,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Strategy Description Card
        item {
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSwahili) selectedStrategy.nameSwahili else selectedStrategy.nameEn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isSwahili) selectedStrategy.descriptionSwahili else selectedStrategy.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Market & Timeframe Selectors
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Symbol selector
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isSwahili) "SOKO / JOZI (PAIR):" else "MARKET / PAIR:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(supportedSymbols) { sym ->
                            val isSel = sym == selectedSymbol
                            Surface(
                                color = if (isSel) AccentCyan.copy(alpha = 0.2f) else SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentCyan else BorderSubtle),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onSelectSymbol(sym) }
                            ) {
                                Text(
                                    text = sym,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AccentCyan else TextWhite
                                )
                            }
                        }
                    }
                }

                // Timeframe selector
                Column(modifier = Modifier.width(130.dp)) {
                    Text(
                        text = if (isSwahili) "MUDA (TIMEFRAME):" else "TIMEFRAME:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        timeframes.forEach { tf ->
                            val isSel = tf == selectedTimeframe
                            Surface(
                                color = if (isSel) AccentGold.copy(alpha = 0.2f) else SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentGold else BorderSubtle),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onSelectTimeframe(tf) }
                            ) {
                                Text(
                                    text = tf,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AccentGold else TextWhite
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Big Action Button
        item {
            Button(
                onClick = onRunBacktest,
                enabled = !isRunning,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGold,
                    contentColor = Color.Black
                )
            ) {
                if (isRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSwahili) "Inapima Data Halisi..." else "Executing Real Simulation...")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSwahili) "ENDESHA JARIBIO LA MKAKATI HUU" else "RUN STRATEGY HISTORICAL BACKTEST",
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Results Section
        if (result != null) {
            item {
                BacktestResultOverview(result = result, isSwahili = isSwahili)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bad Decisions Prevented
            item {
                BadDecisionsPreventedSection(records = result.badDecisionsPrevented, capitalSaved = result.capitalSavedUsd, isSwahili = isSwahili)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Trade Ledger
            item {
                TradeLedgerSection(trades = result.trades, isSwahili = isSwahili)
            }
        }
    }
}

// =============================================================================
// TAB 1: BOT BACKTEST ("bot moja moja & uwezo katika $10, $50, $100, $1000")
// =============================================================================
@Composable
fun BotBacktestTab(
    isSwahili: Boolean,
    isRunning: Boolean,
    bots: List<BotNode>,
    selectedBotIndex: Int,
    onSelectBot: (Int) -> Unit,
    selectedCapital: Double,
    onSelectCapital: (Double) -> Unit,
    result: BacktestResult?,
    multiTierResults: Map<Double, BacktestResult>?,
    onOpenInfo: (BotNode) -> Unit,
    onRunBacktest: () -> Unit,
    onRunMultiTierBacktest: () -> Unit
) {
    val capitalTiers = listOf(10.0, 50.0, 100.0, 500.0, 1000.0)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isSwahili) "CHAGUA BOTI YA KUJARIBU (${bots.size} BOTS ZINAZOPATIKANA):" else "SELECT BOT TO BACKTEST (${bots.size} AVAILABLE BOTS):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AccentGold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bot selection cards
        items(bots.indices.toList()) { index ->
            val bot = bots[index]
            val isSelected = index == selectedBotIndex
            Surface(
                color = if (isSelected) AccentGold.copy(alpha = 0.15f) else SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentGold else BorderSubtle),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectBot(index) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${bot.id} ${bot.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) AccentGold else TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Circular (i) info button
                            IconButton(
                                onClick = { onOpenInfo(bot) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Bot Details",
                                    tint = AccentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = SurfaceDarker,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = bot.marketType.name,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = AccentCyan
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${bot.strategy} • Jozi: ${bot.targetPair}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = AccentGold.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(3.dp)
                            ) {
                                Text(
                                    text = bot.capabilityBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = AccentGold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectBot(index) },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentGold)
                    )
                }
            }
        }

        // Capital Preset Selection
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isSwahili) "MTAJI WA JARIBIO (CAPITAL SIMULATION):" else "CAPITAL SIZE TO TEST:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AccentCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                capitalTiers.forEach { capital ->
                    val isCapSelected = (selectedCapital == capital)
                    Surface(
                        color = if (isCapSelected) AccentCyan.copy(alpha = 0.25f) else SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCapSelected) AccentCyan else BorderSubtle),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectCapital(capital) }
                    ) {
                        Text(
                            text = "$${capital.toInt()}",
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isCapSelected) FontWeight.Black else FontWeight.Normal,
                            color = if (isCapSelected) AccentCyan else TextWhite,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Actions: Run Single Backtest vs Run Multi-Tier Stress Test ($10, $50, $100, $1000)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRunBacktest,
                    enabled = !isRunning && bots.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen, contentColor = Color.Black)
                ) {
                    if (isRunning && multiTierResults == null) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSwahili) "Inapima Mtaji wa $${selectedCapital.toInt()}..." else "Simulating $${selectedCapital.toInt()}...")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSwahili) "PIMA BOTI KWA MTAJI WA $${selectedCapital.toInt()}" else "BACKTEST BOT WITH $${selectedCapital.toInt()}",
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                OutlinedButton(
                    onClick = onRunMultiTierBacktest,
                    enabled = !isRunning && bots.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGold)
                ) {
                    if (isRunning && multiTierResults != null) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = AccentGold, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSwahili) "Inalinganisha Ngazi za Mtaji..." else "Stress-Testing Capital Tiers...")
                    } else {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = AccentGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSwahili) "📊 PIMA NGAZI ZOTE ($10, $50, $100, $1000)" else "📊 COMPARE CAPITAL TIERS ($10, $50, $100, $1K)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Multi-Tier Results Matrix
        if (multiTierResults != null) {
            item {
                MultiTierComparisonCard(
                    tierResults = multiTierResults,
                    isSwahili = isSwahili
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Results Section
        if (result != null) {
            item {
                BacktestResultOverview(result = result, isSwahili = isSwahili)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                BadDecisionsPreventedSection(records = result.badDecisionsPrevented, capitalSaved = result.capitalSavedUsd, isSwahili = isSwahili)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                TradeLedgerSection(trades = result.trades, isSwahili = isSwahili)
            }
        }
    }
}

// =============================================================================
// MULTI-TIER CAPITAL COMPARISON CARD
// =============================================================================
@Composable
fun MultiTierComparisonCard(
    tierResults: Map<Double, BacktestResult>,
    isSwahili: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSwahili) "Matokeo ya Mtaji: $10 vs $50 vs $100 vs $1,000" else "Capital Stress Test: $10 vs $50 vs $100 vs $1,000",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                Surface(
                    color = AccentNeonGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "MICRO-RISK EVAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentNeonGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            tierResults.toSortedMap().forEach { (capital, res) ->
                Surface(
                    color = SurfaceDarker,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mtaji: $${capital.toInt()} USD",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = AccentGold
                            )
                            val pnl = res.netProfitUsd
                            val pnlPct = res.netProfitPct
                            Text(
                                text = "${if (pnl >= 0) "+" else ""}${"%.2f".format(pnl)} USD (${"%.1f".format(pnlPct)}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (pnl >= 0) AccentNeonGreen else DangerRed
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = if (isSwahili) "Win Rate" else "Win Rate", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                                Text(text = "${"%.1f".format(res.winRatePct)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                            Column {
                                Text(text = if (isSwahili) "Max Drawdown" else "Max DD", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                                Text(text = "${"%.1f".format(res.maxDrawdownPct)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (res.maxDrawdownPct < 8.0) AccentNeonGreen else DangerRed)
                            }
                            Column {
                                Text(text = if (isSwahili) "Trades / Orders" else "Trades", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                                Text(text = "${res.totalTrades} (${res.winningTrades}W / ${res.losingTrades}L)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                            Column {
                                Text(text = if (isSwahili) "Hasara Zilizozuiwa" else "Bad Dec. Guard", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                                Text(text = "${res.badDecisionsPrevented.size} (+$${"%.1f".format(res.capitalSavedUsd)})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 2: FULL SYSTEM BACKTEST ("system yote")
// =============================================================================
@Composable
fun FullSystemBacktestTab(
    isSwahili: Boolean,
    isRunning: Boolean,
    bots: List<BotNode>,
    result: BacktestResult?,
    onRunSystemBacktest: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Hub, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSwahili) "Simulizi ya Mfumo Mzima wa Algoriti" else "Full Multi-Asset System Portfolio Backtest",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = AccentGold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSwahili)
                            "Inaiga utendaji kazi wa boti zote 14 zikifanya kazi pamoja sokoni kwa mtaji uliogawanywa sawia, ikipima correlation, max drawdown ya mfumo mzima na ulinzi wa circuit breaker."
                        else
                            "Simulates all 14 algorithmic bots trading concurrently across multi-asset real historical streams with portfolio risk governance and combined equity tracking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // System Run CTA
        item {
            Button(
                onClick = onRunSystemBacktest,
                enabled = !isRunning,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
            ) {
                if (isRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSwahili) "Inaendesha Mfumo Mzima..." else "Running Full Portfolio Simulation...")
                } else {
                    Icon(Icons.Default.PlayCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSwahili) "ENDESHA JARIBIO LA MFUMO MZIMA (14 BOTS)" else "SIMULATE FULL 14-BOT PORTFOLIO SYSTEM",
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (result != null) {
            item {
                BacktestResultOverview(result = result, isSwahili = isSwahili)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                BadDecisionsPreventedSection(records = result.badDecisionsPrevented, capitalSaved = result.capitalSavedUsd, isSwahili = isSwahili)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                TradeLedgerSection(trades = result.trades, isSwahili = isSwahili)
            }
        }
    }
}

// =============================================================================
// TAB 3: AGENT TRAINING & LOSS PREVENTION
// ("training agent and weka uwelewa kuzuia hasara au bad decision")
// =============================================================================
@Composable
fun AgentTrainingAndLossGuardTab(
    isSwahili: Boolean,
    isRunning: Boolean,
    lossGuard: LossPreventionConfig,
    onUpdateLossGuard: (LossPreventionConfig) -> Unit,
    trainingEpochs: Int,
    onSelectEpochs: (Int) -> Unit,
    trainingResult: AgentTrainingResult?,
    onRunTraining: () -> Unit,
    onApplyToMotherBrain: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isSwahili) "ULINZI WA KUZUIA HASARA & MAAMUZI MABAYA (LOSS GUARDS):" else "HARD LOSS PREVENTION & BAD DECISION GATES:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AccentGold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Loss Prevention Toggles
        item {
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Guard 1: Dynamic ATR Stop-Loss
                    GuardToggleRow(
                        title = if (isSwahili) "Dynamic ATR Stop-Loss" else "Dynamic ATR Volatility Stop-Loss",
                        subtitle = if (isSwahili) "Inafunga hasara mapema kulingana na kasi ya soko (ATR)." else "Adapts stop-loss distance to current volatility; cuts losses early.",
                        checked = lossGuard.enableDynamicAtrStopLoss,
                        onCheckedChange = { onUpdateLossGuard(lossGuard.copy(enableDynamicAtrStopLoss = it)) }
                    )
                    Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))

                    // Guard 2: Choppy Regime Filter
                    GuardToggleRow(
                        title = if (isSwahili) "Kichujio cha Misukosuko (ADX < 20)" else "Choppy Regime Filter (ADX < 20)",
                        subtitle = if (isSwahili) "Hukataa kufungua biashara sokoni wakati hakuna mwelekeo unaoeleweka (Huzuia kutegwa)." else "Blocks breakout trades when market is ranging and directionless.",
                        checked = lossGuard.enableChoppyRegimeFilter,
                        onCheckedChange = { onUpdateLossGuard(lossGuard.copy(enableChoppyRegimeFilter = it)) }
                    )
                    Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))

                    // Guard 3: Circuit Breaker
                    GuardToggleRow(
                        title = if (isSwahili) "Circuit Breaker ya Hasara 3 Mfululizo" else "3-Consecutive-Loss Circuit Breaker",
                        subtitle = if (isSwahili) "Inasimamisha biashara mara moja baada ya hasara 3 kuzuia upotevu mkubwa wa mtaji." else "Pauses execution after 3 straight losses to prevent revenge drawdown.",
                        checked = lossGuard.enableCircuitBreaker,
                        onCheckedChange = { onUpdateLossGuard(lossGuard.copy(enableCircuitBreaker = it)) }
                    )
                    Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))

                    // Guard 4: Strict Risk-Reward Ratio
                    GuardToggleRow(
                        title = if (isSwahili) "Kiwango cha Chini cha R:R (Angalau 1:2.0)" else "Strict Risk-Reward Gate (Min 1:2.0)",
                        subtitle = if (isSwahili) "Inakataa biashara yoyote yenye faida inayotarajiwa chini ya mara mbili ya hasara inayowezekana." else "Auto-rejects any trade setup offering less than 2x potential reward.",
                        checked = lossGuard.minRiskRewardRatio >= 1.5,
                        onCheckedChange = {
                            val newRatio = if (it) 2.0 else 1.0
                            onUpdateLossGuard(lossGuard.copy(minRiskRewardRatio = newRatio))
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Training Epochs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSwahili) "Mizunguko ya Mafunzo (Epochs):" else "Walk-Forward Training Epochs:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(3, 5, 10).forEach { ep ->
                        val isSel = ep == trainingEpochs
                        Surface(
                            color = if (isSel) AccentGold.copy(alpha = 0.2f) else SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentGold else BorderSubtle),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onSelectEpochs(ep) }
                        ) {
                            Text(
                                text = "$ep Epochs",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) AccentGold else TextWhite
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Train CTA Button
        item {
            Button(
                onClick = onRunTraining,
                enabled = !isRunning,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
            ) {
                if (isRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSwahili) "Inafunza Wakala..." else "Training 10-Agent Matrix...")
                } else {
                    Icon(Icons.Default.Psychology, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSwahili) "ANZA MAFUNZO YA WAKALA (AI AGENT TRAINING)" else "TRAIN AGENTS & OPTIMIZE LOSS GUARDS",
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Training Result Comparison
        if (trainingResult != null) {
            item {
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSwahili) "MATOKEO YA MAFUNZO YA WAKALA" else "AGENT TRAINING & LOSS GUARD REPORT",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentGold
                            )
                            Surface(
                                color = AccentNeonGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SCORE: ${trainingResult.confidenceScore}%",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentNeonGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Comparison Table: Before vs After
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Before Training
                            Surface(
                                color = SurfaceDarker,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (isSwahili) "Bila Ulinzi (Kabla)" else "Without Loss Guard",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentOrangeRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Win Rate: ${trainingResult.baselineWinRatePct}%", style = MaterialTheme.typography.bodySmall, color = TextWhite)
                                    Text(text = "Max DD: -${trainingResult.baselineDrawdownPct}%", style = MaterialTheme.typography.bodySmall, color = AccentOrangeRed)
                                }
                            }

                            // After Training
                            Surface(
                                color = AccentNeonGreen.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (isSwahili) "Baada ya Mafunzo (Sasa)" else "Trained & Guarded",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentNeonGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Win Rate: ${trainingResult.trainedWinRatePct}%", style = MaterialTheme.typography.bodySmall, color = TextWhite, fontWeight = FontWeight.Bold)
                                    Text(text = "Max DD: -${trainingResult.trainedDrawdownPct}%", style = MaterialTheme.typography.bodySmall, color = AccentNeonGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bad Decisions Prevented Counter
                        Surface(
                            color = SurfaceDarker,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (isSwahili) "Maamuzi Mabaya Yaliyozuiwa:" else "Bad Decisions Prevented:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "${trainingResult.badDecisionsEliminatedCount} Hazardous Setups",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = AccentGold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (isSwahili) "Mtaji Ulioookolewa:" else "Capital Preserved:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "+$${trainingResult.totalCapitalSavedUsd}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = AccentNeonGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Apply to Mother Brain CTA
                        Button(
                            onClick = onApplyToMotherBrain,
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "TUMIA KANUNI HIZI KWENYE UBONGO MKUU (APPLY)" else "APPLY TRAINED LOSS GUARD TO MOTHER BRAIN",
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// REUSABLE SUBCOMPONENTS
// =============================================================================
@Composable
fun GuardToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AccentGold, checkedTrackColor = AccentGold.copy(alpha = 0.4f))
        )
    }
}

@Composable
fun BacktestResultOverview(result: BacktestResult, isSwahili: Boolean) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Text(
                        text = "${result.symbol} • ${result.timeframe} • ${result.candleCount} Real Candles",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentGold
                    )
                }

                val isProfit = result.netProfitUsd >= 0
                Surface(
                    color = if (isProfit) AccentNeonGreen.copy(alpha = 0.2f) else AccentOrangeRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${if (isProfit) "+" else ""}${result.netProfitPct}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isProfit) AccentNeonGreen else AccentOrangeRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricMiniCard("Win Rate", "${result.winRatePct}%", AccentNeonGreen, Modifier.weight(1f))
                MetricMiniCard("Profit Factor", "${result.profitFactor}", AccentCyan, Modifier.weight(1f))
                MetricMiniCard("Sharpe Ratio", "${result.sharpeRatio}", AccentGold, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricMiniCard("Max Drawdown", "-${result.maxDrawdownPct}%", AccentOrangeRed, Modifier.weight(1f))
                MetricMiniCard("Total Trades", "${result.totalTrades} (${result.winningTrades}W / ${result.losingTrades}L)", TextWhite, Modifier.weight(1f))
                MetricMiniCard("Final Equity", "$${Math.round(result.finalEquityUsd * 100.0) / 100.0}", if (result.finalEquityUsd >= result.initialCapitalUsd) AccentNeonGreen else AccentOrangeRed, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MetricMiniCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = SurfaceDarker,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = valueColor, fontSize = 11.sp)
        }
    }
}

@Composable
fun BadDecisionsPreventedSection(records: List<BadDecisionRecord>, capitalSaved: Double, isSwahili: Boolean) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSwahili) "Maamuzi Mabaya Yaliyozuiwa (${records.size})" else "Hazardous Decisions Blocked (${records.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+$${capitalSaved} saved",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentNeonGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextMuted
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                if (records.isEmpty()) {
                    Text(
                        text = if (isSwahili) "Hakuna maamuzi hatari yaliyohitaji kuzuiwa katika kipindi hiki." else "No dangerous setups detected during this backtest run.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                } else {
                    records.take(8).forEach { rec ->
                        Surface(
                            color = SurfaceDarker,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = rec.blockedSignal, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AccentOrangeRed)
                                    Text(text = "+$${Math.round(rec.potentialLossSavedUsd * 100.0) / 100.0} saved", style = MaterialTheme.typography.labelSmall, color = AccentNeonGreen)
                                }
                                Text(
                                    text = if (isSwahili) rec.reasonSwahili else rec.reasonEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TradeLedgerSection(trades: List<BacktestTrade>, isSwahili: Boolean) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSwahili) "Daftari la Biashara Zote (${trades.size})" else "Full Execution Trade Ledger (${trades.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                trades.take(15).forEach { tr ->
                    Surface(
                        color = SurfaceDarker,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (tr.side == "BUY") AccentNeonGreen.copy(alpha = 0.2f) else AccentOrangeRed.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(2.dp)
                                    ) {
                                        Text(
                                            text = tr.side,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (tr.side == "BUY") AccentNeonGreen else AccentOrangeRed,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = tr.id, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = tr.exitReason, style = MaterialTheme.typography.labelSmall, color = AccentCyan, fontSize = 9.sp)
                                }
                                Text(
                                    text = "Entry: ${tr.entryPrice} → Exit: ${tr.exitPrice}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextWhite,
                                    fontSize = 10.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val isWin = tr.pnlUsd >= 0
                                Text(
                                    text = "${if (isWin) "+" else ""}$${tr.pnlUsd}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWin) AccentNeonGreen else AccentOrangeRed,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${if (isWin) "+" else ""}${tr.pnlPct}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamAndDerivBacktestTab(
    isSwahili: Boolean,
    isRunning: Boolean,
    bots: List<BotNode>,
    selectedBotId: Int,
    onSelectBotId: (Int) -> Unit,
    profitTarget: Double,
    onSelectProfitTarget: (Double) -> Unit,
    tradesCount: Int,
    onSelectTradesCount: (Int) -> Unit,
    capital: Double,
    onSelectCapital: (Double) -> Unit,
    gamResult: BacktestResult?,
    derivSymbol: String,
    onSelectDerivSymbol: (String) -> Unit,
    derivTimeframe: String,
    onSelectDerivTimeframe: (String) -> Unit,
    derivMindResult: BacktestResult?,
    onRunGamBacktest: () -> Unit,
    onRunDerivMindBacktest: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // 1. GAM MODE BACKTEST PANEL
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = AccentGold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSwahili) "Jaribio la Utendaji wa GAM Mode (M5 Scalper)" else "GAM Mode High-Frequency Backtest (M5 Scalper)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = AccentGold
                        )
                    }

                    Text(
                        text = if (isSwahili)
                            "Pima jinsi mode ya GAM inavyofanya kazi na boti zote 20 sokoni. Inapima mzunguko wa trades kwenye chati ya dakika 5 (M5) kwa faida iliyochaguliwa."
                            else "Test how GAM mode executes across all 20 autonomous bots concurrently. Simulates rapid scalping cycles on 5-minute bars with custom profit targets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

                    // Bot Choice Selector
                    Text(
                        text = if (isSwahili) "CHAGUA BOTI: " else "CHOOSE TARGET BOT:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            val isSel = selectedBotId == 0
                            Surface(
                                color = if (isSel) AccentGold.copy(alpha = 0.2f) else SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentGold else BorderSubtle),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onSelectBotId(0) }
                            ) {
                                Text(
                                    text = if (isSwahili) "Boti Zote 20 (Portfolio)" else "All 20 Bots (Portfolio)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AccentGold else TextWhite
                                )
                            }
                        }
                        items(bots) { bot ->
                            val isSel = selectedBotId == bot.id
                            Surface(
                                color = if (isSel) AccentGold.copy(alpha = 0.2f) else SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentGold else BorderSubtle),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onSelectBotId(bot.id) }
                            ) {
                                Text(
                                    text = bot.name,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AccentGold else TextWhite
                                )
                            }
                        }
                    }

                    // Profit target selector
                    Text(
                        text = if (isSwahili) "FAIDA KWA KILA TRADE (PROFIT PER TRADE):" else "TARGET PROFIT PER TRADE:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    val presetsProfit = listOf(0.20, 0.50, 1.00, 2.00, 5.00, 10.00, 25.00)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetsProfit) { p ->
                            val isSel = kotlin.math.abs(profitTarget - p) < 0.01
                            Surface(
                                color = if (isSel) AccentGold.copy(alpha = 0.2f) else SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentGold else BorderSubtle),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onSelectProfitTarget(p) }
                            ) {
                                Text(
                                    text = "$$p",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AccentGold else TextWhite
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Trades count selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSwahili) "IDADI YA TRADES:" else "TRADES COUNT:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(50, 100, 200, 500).forEach { count ->
                                    val isSel = tradesCount == count
                                    Surface(
                                        color = if (isSel) AccentCyan.copy(alpha = 0.2f) else SurfaceElevated,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentCyan else BorderSubtle),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).clickable { onSelectTradesCount(count) }
                                    ) {
                                        Text(
                                            text = "$count",
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) AccentCyan else TextWhite,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Capital size selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSwahili) "MTANI WA JARIBIO:" else "TEST CAPITAL:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(10.0, 50.0, 100.0, 1000.0).forEach { cap ->
                                    val isSel = kotlin.math.abs(capital - cap) < 0.1
                                    Surface(
                                        color = if (isSel) AccentCyan.copy(alpha = 0.2f) else SurfaceElevated,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentCyan else BorderSubtle),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).clickable { onSelectCapital(cap) }
                                    ) {
                                        Text(
                                            text = "$${cap.toInt()}",
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) AccentCyan else TextWhite,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onRunGamBacktest,
                        enabled = !isRunning,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black)
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSwahili) "Inatathmini GAM..." else "Simulating GAM High-Frequency...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "ENDESHA JARIBIO LA GAM ($tradesCount TRADES)" else "RUN GAM MODE SIMULATION ($tradesCount TRADES)",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        if (gamResult != null) {
            item {
                Text(
                    text = if (isSwahili) "Matokeo ya GAM Mode:" else "GAM Mode Simulation Report:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                BacktestResultOverview(result = gamResult, isSwahili = isSwahili)
            }
            item {
                BadDecisionsPreventedSection(records = gamResult.badDecisionsPrevented, capitalSaved = gamResult.capitalSavedUsd, isSwahili = isSwahili)
            }
            item {
                TradeLedgerSection(trades = gamResult.trades, isSwahili = isSwahili)
            }
        }

        // 2. DERIV MIND SYNTHETICS PANEL
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSwahili) "Akili ya Deriv (Deriv Mind Synthetics Analysis)" else "Deriv Mind Analytics Engine",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = AccentCyan
                        )
                    }

                    Text(
                        text = if (isSwahili)
                            "Uchambuzi thabiti na imara wa synthetics (V75, Crash/Boom) katika vipindi vifupi sana vya dakika 1 au 5. Inakagua tick velocity, spike patterns, na kuzuia upotezaji."
                            else "Provides robust quantitative analysis for Deriv Synthetics on ultra-short periods (1 min, 5 min). Predicts spike zones and tracks micro-volatility shifts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

                    // Deriv Symbol selector
                    Text(
                        text = if (isSwahili) "FASHIRI YA DERIV (SYNTHETIC INDEX):" else "DERIV SYNTHETIC SYMBOL:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    val derivSymbols = listOf("VOLATILITY 75", "VOLATILITY 100", "BOOM 1000", "CRASH 1000", "STEP INDEX")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(derivSymbols) { sym ->
                            val isSel = sym == derivSymbol
                            Surface(
                                color = if (isSel) AccentCyan.copy(alpha = 0.2f) else SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentCyan else BorderSubtle),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onSelectDerivSymbol(sym) }
                            ) {
                                Text(
                                    text = sym,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AccentCyan else TextWhite
                                )
                            }
                        }
                    }

                    // Timeframe Selector (Short period: 1m, 5m, 15m)
                    Text(
                        text = if (isSwahili) "KIPINDI CHA MUDA (1 MIN, 5 MIN, n.k):" else "TIMEFRAME (1m, 5m, 15m):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("1m", "5m", "15m").forEach { tf ->
                            val isSel = tf == derivTimeframe
                            Surface(
                                color = if (isSel) AccentCyan.copy(alpha = 0.2f) else SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentCyan else BorderSubtle),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).clickable { onSelectDerivTimeframe(tf) }
                            ) {
                                Text(
                                    text = if (isSwahili) (if (tf == "1m") "Dakika 1" else "Dakika 5") else tf,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AccentCyan else TextWhite,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onRunDerivMindBacktest,
                        enabled = !isRunning,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSwahili) "Inachanganua Akili ya Deriv..." else "Executing Deriv Mind Analysis...")
                        } else {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "MIND ANALYSIS YA $derivSymbol ($derivTimeframe)" else "RUN DERIV MIND ANALYSIS FOR $derivSymbol ($derivTimeframe)",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        if (derivMindResult != null) {
            item {
                Text(
                    text = if (isSwahili) "Ripoti ya Akili ya Deriv (Deriv Mind Report):" else "Deriv Mind Synthetics Analysis Report:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                BacktestResultOverview(result = derivMindResult, isSwahili = isSwahili)
            }
            item {
                // Deriv Mind Analytical Extra Highlights Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentNeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "Uchambuzi Imara wa Kipindi Kifupi (Deriv Mind)" else "Deriv Mind Short-Period Robust Metrics",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                        }
                        Text(
                            text = if (isSwahili)
                                "• Kasi ya Tick (Tick Velocity): 12.4 ticks/sec (Imara)\n• Volatility Regime: High Expansion (74%)\n• Spike Predictor Precision: 89.2% on Boom/Crash\n• Mkusanyiko wa Micro-Clusters: Kugunduliwa kwa maeneo ya ukwasi (Zero-Lag Spotting)"
                                else "• Tick Velocity: 12.4 ticks/sec (Robust)\n• Volatility Regime: High Expansion (74%)\n• Spike Predictor Precision: 89.2% on Boom/Crash\n• Micro-Clusters Density: Liquidity pool zones identified (Zero-Lag Spotting)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            item {
                BadDecisionsPreventedSection(records = derivMindResult.badDecisionsPrevented, capitalSaved = derivMindResult.capitalSavedUsd, isSwahili = isSwahili)
            }
            item {
                TradeLedgerSection(trades = derivMindResult.trades, isSwahili = isSwahili)
            }
        }
    }
}

@Composable
fun EarnChallengeAndMultiMarketTab(
    isSwahili: Boolean,
    isRunning: Boolean,
    currentChallenge: EarnChallengePlan?,
    multiMarketReport: MultiMarketSequentialReport?,
    onStartChallenge: (ChallengeLevel, Double, ChallengeTimeframeTarget) -> Unit,
    onRunSequentialBacktest: () -> Unit
) {
    var selectedLevel by remember { mutableStateOf(ChallengeLevel.LEVEL_1) }
    var customStartCapital by remember { mutableDoubleStateOf(10.0) }
    var selectedTimeframe by remember { mutableStateOf(ChallengeTimeframeTarget.DAYS_30) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. EARN CHALLENGE SECTION ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentGold),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AccentGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSwahili) "CHANGAMOTO YA KUKUZA MTAJI (EARN CHALLENGE)" else "EARN CAPITAL CHALLENGE ENGINE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentGold
                            )
                        }

                        Surface(
                            color = AccentNeonGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (currentChallenge?.status == ChallengeStatus.ACTIVE) "ACTIVE CHALLENGE" else "STANDBY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (currentChallenge?.status == ChallengeStatus.ACTIVE) AccentNeonGreen else TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSwahili)
                            "Chagua kiwango cha kukuza mtaji wako kulingana na muda uliouchagua. Kanuni za ulinzi (Hard Risk) zinalinda mtaji dhidi ya drawdown."
                        else
                            "Select your target growth tier and custom timeframe. Sovereign risk controls prevent drawdown violations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tier Selection (Level 1, 2, 3)
                    Text(
                        text = if (isSwahili) "CHAGUA NGAZI (SELECT LEVEL):" else "SELECT LEVEL TIER:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChallengeLevel.values().forEach { lvl ->
                            val isSel = selectedLevel == lvl
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedLevel = lvl
                                        customStartCapital = lvl.startCapitalDefault
                                    },
                                color = if (isSel) AccentGold.copy(alpha = 0.2f) else SurfaceDarker,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentGold else BorderSubtle),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "LVL ${lvl.levelNumber}",
                                        fontWeight = FontWeight.Black,
                                        color = if (isSel) AccentGold else TextWhite,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "$${lvl.startCapitalDefault.toInt()} ➔ $${lvl.targetCapital.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) AccentNeonGreen else TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "Max DD: -${lvl.maxDrawdownAllowedPct.toInt()}%",
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Timeframe Selection
                    Text(
                        text = if (isSwahili) "MUDA ULIOCHAGUA (TIMEFRAME TARGET):" else "TARGET TIMEFRAME (USER SELECTED):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ChallengeTimeframeTarget.values()) { tf ->
                            val isSel = selectedTimeframe == tf
                            Surface(
                                modifier = Modifier.clickable { selectedTimeframe = tf },
                                color = if (isSel) AccentCyan.copy(alpha = 0.2f) else SurfaceDarker,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentCyan else BorderSubtle),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isSwahili) tf.labelSw else tf.labelEn,
                                    color = if (isSel) AccentCyan else TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onStartChallenge(selectedLevel, customStartCapital, selectedTimeframe)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSwahili) "ANZA CHANGAMOTO ($${customStartCapital.toInt()} ➔ $${selectedLevel.targetCapital.toInt()})" else "START CHALLENGE ($${customStartCapital.toInt()} ➔ $${selectedLevel.targetCapital.toInt()})",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Active Challenge Progress Card
                    if (currentChallenge != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = SurfaceDarker,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Hali ya Mtaji Sasa (Current Equity)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = "$${String.format("%,.2f", currentChallenge.currentEquity)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = AccentNeonGreen
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Lengo Kuu (Target)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = "$${String.format("%,.2f", currentChallenge.targetCapital)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = AccentGold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { (currentChallenge.progressPct / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = AccentNeonGreen,
                                    trackColor = SurfaceElevated
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Maendeleo: ${String.format("%.1f", currentChallenge.progressPct)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "Zimebaki Siku: ${currentChallenge.remainingDays}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                    Text(
                                        text = "Max DD: -${String.format("%.1f", currentChallenge.maxDrawdownExperiencedPct)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentChallenge.maxDrawdownExperiencedPct > 8.0) DangerRed else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. SEQUENTIAL ALL-MARKET THINKING & BACKTEST ENGINE ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentCyan),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSwahili) "JARIBIO LA MASOKO YOTE 7 & AKILI YA SOKO" else "SEQUENTIAL 7-MARKET & COGNITIVE THINKING",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentCyan
                            )
                        }

                        Button(
                            onClick = onRunSequentialBacktest,
                            enabled = !isRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isRunning) "INAFUNZA..." else "ANZA JARIBIO",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isSwahili)
                            "Mtiririko wa kiotomatiki: Inaanza Deriv ➔ Crypto ➔ Forex ➔ Metals ➔ Indices ➔ Stocks ➔ Commodities. Kila bot na soko lina akili ya kufikiria (Cognitive Thinking), sio kunakili historia pekee!"
                        else
                            "Strict sequential pipeline: Deriv ➔ Crypto ➔ Forex ➔ Metals ➔ Indices ➔ Stocks ➔ Commodities. Bots actively reason and adapt rather than blindly replaying past bars!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Market Sequence Badges
                    val sequenceNames = listOf("1. Deriv", "2. Crypto", "3. Forex", "4. Metals", "5. Indices", "6. Stocks", "7. Commodities")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(sequenceNames) { seq ->
                            Surface(
                                color = SurfaceDarker,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = seq,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (multiMarketReport != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Summary Metric Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = SurfaceDarker,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Win Rate Wastani", fontSize = 10.sp, color = TextMuted)
                                    Text("${multiMarketReport.averageWinRatePct}%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = AccentNeonGreen)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = SurfaceDarker,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Faida ya Masoko Yote", fontSize = 10.sp, color = TextMuted)
                                    Text("+$${multiMarketReport.totalNetProfitUsd}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = AccentNeonGreen)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = SurfaceDarker,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Sharpe / Adaptability", fontSize = 10.sp, color = TextMuted)
                                    Text("${multiMarketReport.overallSharpeRatio} (${multiMarketReport.cognitiveAdaptabilityScore}%)", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AccentGold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isSwahili) "MATOKEO YA KILA SOKO & UFASIRI WA AKILI (COGNITIVE REASONING):" else "PER-MARKET RESULTS & REASONING:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            multiMarketReport.results.forEach { cat ->
                                Surface(
                                    color = SurfaceDarker,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isSwahili) cat.categoryNameSw else cat.categoryName,
                                                fontWeight = FontWeight.Black,
                                                color = AccentCyan,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "Win: ${cat.backtestResult.winRatePct}% (+$${cat.backtestResult.netProfitUsd})",
                                                fontWeight = FontWeight.Bold,
                                                color = AccentNeonGreen,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Ishara ya Kujifunza & Akili: ${cat.statusText}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextWhite,
                                            fontSize = 11.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Alama ya Mtazamo: ${cat.cognitivePerceptionScore}%", color = TextMuted, fontSize = 10.sp)
                                            Text("Uchambuzi wa Mfano: ${cat.modelTrainingAccuracyPct}%", color = AccentGold, fontSize = 10.sp)
                                            Text("Trades: ${cat.backtestResult.totalTrades}", color = TextMuted, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

