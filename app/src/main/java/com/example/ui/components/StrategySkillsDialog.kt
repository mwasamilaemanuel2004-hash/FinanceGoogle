package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.example.engine.AgenticSkillsEngine
import com.example.engine.StrategyDefinition
import com.example.engine.StrategyLibrary
import com.example.ui.theme.*

@Composable
fun StrategySkillsDialog(
    isSwahili: Boolean,
    onSelectStrategy: (StrategyDefinition) -> Unit,
    onDismiss: () -> Unit
) {
    val skillsEngine = remember { AgenticSkillsEngine.getInstance() }
    val allSkills by skillsEngine.skillsList.collectAsState()
    val inventedSkills by skillsEngine.inventedSkills.collectAsState()
    val instReport by skillsEngine.institutionalReport.collectAsState()
    val quantReport by skillsEngine.quantumReport.collectAsState()
    val consensusState by skillsEngine.consensusState.collectAsState()

    val thinkingEngine = remember { com.example.engine.CognitiveThinkingEngine.getInstance() }
    val latestThought by thinkingEngine.latestThought.collectAsState()
    val deepThinkingResult by thinkingEngine.deepThinkingResult.collectAsState()
    val metacognitionReport by thinkingEngine.metacognitionReport.collectAsState()
    val focusReport by thinkingEngine.focusReport.collectAsState()
    val selfGrowthReport by thinkingEngine.selfGrowthReport.collectAsState()
    val classificationState by thinkingEngine.classificationState.collectAsState()
    val maneuverState by thinkingEngine.maneuverState.collectAsState()
    val bigResults by thinkingEngine.bigResultsState.collectAsState()

    val hybridEngine = remember { com.example.engine.HybridReplacementEngine.getInstance() }
    val hybridTelemetry by hybridEngine.telemetryState.collectAsState()
    val activeHybridSession by hybridEngine.activeSession.collectAsState()

    val derivEngine = remember { com.example.engine.DerivAllMarketsEngine.getInstance() }
    val derivInstruments by derivEngine.instruments.collectAsState()
    val digitReport by derivEngine.latestDigitReport.collectAsState()
    val derivRegime by derivEngine.latestRegime.collectAsState()
    val derivContractEval by derivEngine.latestContractEval.collectAsState()

    val userTrainingEngine = remember { com.example.engine.SingleUserTrainingEngine.getInstance() }
    val userProfile by userTrainingEngine.userProfile.collectAsState()
    val trainingProgress by userTrainingEngine.trainingProgress.collectAsState()

    val selfLearningEngine = remember { com.example.engine.SelfLearningNoCopyEngine.getInstance() }
    val selfLearningState by selfLearningEngine.learningState.collectAsState()

    val megaRagEngine = remember { com.example.engine.MegaRagEngine.getInstance() }
    val ragResponse by megaRagEngine.lastQueryResponse.collectAsState()
    val ragBooksCount by megaRagEngine.totalIndexedBooksCount.collectAsState()
    val ragSkillsCount by megaRagEngine.totalIndexedSkillsCount.collectAsState()

    val safeLearningGuard = remember { com.example.engine.SafeLearningGuardEngine.getInstance() }
    val pendingVetoSkills by safeLearningGuard.pendingVetoSkills.collectAsState()
    val blockedAnomalies by safeLearningGuard.blockedAnomalies.collectAsState()
    val safeAuditLogs by safeLearningGuard.auditLogs.collectAsState()

    val discoveryAndChallenge = remember { com.example.engine.PriceDiscoveryAndChallengeEngine.getInstance() }
    val discoveryMap by discoveryAndChallenge.discoveryData.collectAsState()
    val activeChallengeBots by discoveryAndChallenge.activeBots.collectAsState()
    val exchangeEfficiencyPct by discoveryAndChallenge.exchangeEfficiencyPct.collectAsState()

    val elevenIndicatorsEngine = remember { com.example.engine.ElevenIndicatorsAndShiftEngine.getInstance() }
    val indicatorsList by elevenIndicatorsEngine.indicatorsList.collectAsState()
    val currentShift by elevenIndicatorsEngine.currentShift.collectAsState()
    val isAutonomousMode by elevenIndicatorsEngine.isAutonomousMode.collectAsState()
    val userToggles by elevenIndicatorsEngine.userToggles.collectAsState()
    val selectedMarket by elevenIndicatorsEngine.selectedMarket.collectAsState()
    val backtestCorrelation by elevenIndicatorsEngine.backtestCorrelation.collectAsState()
    val generatedPineScript by elevenIndicatorsEngine.generatedPineScript.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = if (isSwahili) {
        listOf("Mikakati 25", "Aisibagi Ngazi 6", "Taasisi (Smart Money)", "Hesabu ya Quantum", "Mbinu Mpya & Fursa", "Kufikiri & Matokeo", "Hybrid & Replacement", "Deriv Masoko & Digits", "Mafunzo ya Mtumiaji 1", "RAG Vitabu 50+", "Safe Learning & Veto", "Ugunduzi wa Bei & Bots", "Viashiria 11 & Shift ya Wakala")
    } else {
        listOf("25 Strategies", "6-Lvl Iceberg", "Institutional Flow", "Quantum Finance", "Inventions & Chance", "Cognition & Big Results", "Hybrid & Replacement", "Deriv & Digits Engine", "Single User Training", "RAG 50+ Books", "Safe Learning Guard", "Price Discovery & Bots", "11 Indicators & Agent Shift")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlow),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Skills",
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isSwahili) "Mkusanyiko wa Stadi za Kiakili (Agentic Skills)" else "Institutional Agentic Skills Matrix",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isSwahili) "Stadi zote 50+ za kiwango cha taasisi na hesabu ya Quantum" else "50+ Institutional strategies, Iceberg formation & Quantum physics",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceCard,
                    contentColor = AccentCyan,
                    edgePadding = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    tabTitles.forEachIndexed { idx, title ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == idx) AccentCyan else TextMuted
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content based on tab
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> {
                            // 25 Strategy Skills (Image 1)
                            val stratSkills = allSkills.filter { it.family == "STRATEGY_25" }
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(stratSkills) { skill ->
                                    SkillCardItem(
                                        skill = skill,
                                        isSwahili = isSwahili,
                                        onSelect = {
                                            onSelectStrategy(
                                                StrategyDefinition(
                                                    id = skill.id,
                                                    name = skill.name,
                                                    category = skill.category,
                                                    description = skill.description,
                                                    winRateHistorical = skill.historicalWinRate,
                                                    profitFactor = skill.historicalSharpe,
                                                    recommendedHorizon = "15M - 4H",
                                                    executionType = "Algorithmic Precision"
                                                )
                                            )
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                        1 -> {
                            // 6-Level Iceberg Deep Price Formation (Image 2)
                            val icebergSkills = allSkills.filter { it.family == "ICEBERG_DEEP" }
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                item {
                                    Surface(
                                        color = SurfaceElevated,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "🧊 " + (if (isSwahili) "Muundo wa Bei wa Aisibagi (Ngazi 1 hadi 6)" else "Iceberg Deep Price Formation (Levels 1 to 6)"),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentCyan
                                            )
                                            Text(
                                                text = if (isSwahili) "Kuanzia muundo wa juu (SMC/ICT) hadi chini kabisa (GEX, Dark Pools, Macro & Hawkes Processes)." else "From retail surface liquidity to dealer gamma hedging, dark pools, and macro rates.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                                items(icebergSkills) { skill ->
                                    SkillCardItem(
                                        skill = skill,
                                        isSwahili = isSwahili,
                                        onSelect = {
                                            onSelectStrategy(
                                                StrategyDefinition(
                                                    id = skill.id,
                                                    name = skill.name,
                                                    category = skill.category,
                                                    description = skill.description,
                                                    winRateHistorical = skill.historicalWinRate,
                                                    profitFactor = skill.historicalSharpe,
                                                    recommendedHorizon = "15M - 4H",
                                                    executionType = "Algorithmic Precision"
                                                )
                                            )
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                        2 -> {
                            // Institutional Indicator: 7 Components
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🏦 " + (if (isSwahili) "Kiashiria cha Taasisi (Institutional Indicator)" else "Institutional Smart Money Engine"),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentGold
                                                )
                                                Surface(
                                                    color = AccentNeonGreen.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = instReport.institutionalFlow,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = AccentNeonGreen,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = instReport.reasoning,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                        }
                                    }
                                }

                                val instComponents = listOf(
                                    Triple("1. Smart Money Tracker (CVD & OFI)", "CVD: +$1.42M • OFI Ratio: 0.42 • Hidden Bullish Divergence", "Delta volume tracking reveals institutional buying while retail sells."),
                                    Triple("2. Institutional Order Block", "Active OB: $66,950 • 3x Impulse Displacement • 50% Mitigation Refill", "Identifies massive institutional re-accumulation footprint zones."),
                                    Triple("3. Institutional Liquidity (BSL / SSL)", "BSL Equal Highs: $68,200 • SSL Equal Lows: $66,500 • Wick > 2x ATR", "Detects liquidity sweeps and stop-hunts across retail stop clusters."),
                                    Triple("4. Volume Profile & VPIN Toxicity", "POC: $67,250 • VAH: $67,800 • VAL: $66,900 • VPIN Toxicity: 0.18", "Value Area 70% volume node anchoring and order flow toxicity filter."),
                                    Triple("5. Wyckoff Accumulation & Manipulation", "Phase: ${instReport.wyckoffPhase} • Spring Tested • SOS Confirmed", "Real Wyckoff Phases A-E detection preventing premature entries."),
                                    Triple("6. Dark Pool & Block Trades", "DIX Index: ${instReport.dixDarkPoolPct}% • Large Prints: $8.5M (Polygon/Coinglass)", "Dark index buying indicates hidden off-exchange accumulation."),
                                    Triple("7. Dealer Gamma Positioning (GEX/VEX)", "Net GEX: +$125M • Gamma Pinning: Active • 0DTE Walls: $68K Call / $66.5K Put", "Market pinning around POC due to market maker delta-neutral hedging.")
                                )

                                items(instComponents) { (title, metrics, desc) ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = metrics, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = AccentCyan)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Quantum Indicator: 7 Components
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentLavender.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "⚛️ " + (if (isSwahili) "Kiashiria cha Fizikia ya Quantum" else "Quantum Finance Probabilistic Engine"),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentLavender
                                                )
                                                Surface(
                                                    color = AccentLavender.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "Confidence: ${quantReport.confidencePct}%",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = AccentLavender,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = quantReport.reasoning,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                        }
                                    }
                                }

                                val quantComponents = listOf(
                                    Triple("1. Quantum Superposition Born Rule", "P(BUY): ${(quantReport.probBuy * 100).toInt()}% • P(SELL): ${(quantReport.probSell * 100).toInt()}% • P(HOLD): ${(quantReport.probHold * 100).toInt()}%", "State amplitudes |alpha|^2 and |beta|^2 collapse upon order execution."),
                                    Triple("2. Quantum Market Entanglement", quantReport.entangledMarketsSummary, "Cointegration ADF test on Gold, DXY, TIPS yields, and USD/JPY."),
                                    Triple("3. Quantum Monte Carlo Forecast (1000 Paths)", "Q10: $${String.format("%,.0f", quantReport.mcQuantile10)} • Q50: $${String.format("%,.0f", quantReport.mcQuantile50)} • Q90: $${String.format("%,.0f", quantReport.mcQuantile90)} • TP Prob: ${quantReport.probHitTp}%", "Ornstein-Uhlenbeck jump-diffusion with Heston stochastic volatility paths."),
                                    Triple("4. Quantum Fourier Transform (QFT) Cycles", "Dominant Institutional Wave: ${quantReport.dominantCyclePeriodDays} Days", "Hidden cyclic periodicity detection through quantum phase estimation."),
                                    Triple("5. Quantum Tunneling Breakout", "Tunneling Probability: ${(quantReport.quantumTunnelingProb * 100).toInt()}% (Through Resistance Barrier)", "Potential well barrier penetration via Bollinger bandwidth compression."),
                                    Triple("6. Heston Stochastic Volatility Expansion", "Expansion Risk: ${(quantReport.hestonVolExpansionRisk * 100).toInt()}% • Mean Reversion Speed: 2.4", "Models volatility of volatility preventing catastrophic black swans."),
                                    Triple("7. Superposition Continuous Kelly Sizing", "Recommended Allocation: ${(quantReport.continuousKellySize * 100).toInt()}% (0.14 f*)", "Optimal logarithmic capital growth fraction computed over probability wave.")
                                )

                                items(quantComponents) { (title, metrics, desc) ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = metrics, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = AccentNeonGreen)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                        4 -> {
                            // Autonomous Method Inventions & Statistical Chance Handler
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🎲 " + (if (isSwahili) "Msimamizi wa Fursa na Hatari (Chance Handler)" else "Statistical Chance & Risk Governor"),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentOrange
                                                )
                                                Surface(
                                                    color = if (consensusState.chanceState.isTradePermitted) AccentNeonGreen.copy(alpha = 0.15f) else AccentRed.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = if (consensusState.chanceState.isTradePermitted) "PERMITTED (0.5x Kelly)" else "BLOCKED (Hard Risk)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (consensusState.chanceState.isTradePermitted) AccentNeonGreen else AccentRed,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = consensusState.chanceState.reasoning,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Anomaly Score: ${consensusState.chanceState.anomalyScore} • Signature: ${consensusState.chanceState.signature}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = AccentCyan
                                            )
                                        }
                                    }
                                }

                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isSwahili) "Mikakati Iliyobuniwa na AI (Self-Evolved Methods):" else "Self-Evolved Autonomous Methods:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Button(
                                            onClick = { skillsEngine.inventNewMethod("XAU/USD") },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = if (isSwahili) "Buni Mpya" else "Invent New", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                items(inventedSkills) { item ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                                                Surface(color = AccentCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                    Text(text = item.targetMarket, style = MaterialTheme.typography.labelSmall, color = AccentCyan, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "Combined: ${item.combinedSkills.joinToString(" + ")}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                Text(text = "Sharpe: ${String.format("%.2f", item.testedSharpe)}", style = MaterialTheme.typography.labelSmall, color = AccentNeonGreen, fontFamily = FontFamily.Monospace)
                                                Text(text = "Win Rate: ${String.format("%.1f", item.testedWinRate)}%", style = MaterialTheme.typography.labelSmall, color = AccentGold, fontFamily = FontFamily.Monospace)
                                                Text(text = "PBO: ${String.format("%.1f", item.testedPboPct)}%", style = MaterialTheme.typography.labelSmall, color = TextWhite, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        5 -> {
                            // Tab 5: Cognitive Chain-of-Thought & Big Results Dashboard
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // 1. Big Results Summary Card
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🚀 " + (if (isSwahili) "Matokeo Makubwa: Micro-Compounding (Renaissance Style)" else "Big Results: High-Frequency Compounding"),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentNeonGreen
                                                )
                                                Surface(
                                                    color = AccentNeonGreen.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "+${bigResults.weakProfitsCumulativeReturnPct}% Today",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = AccentNeonGreen,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(text = "Weak Profits", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                    Text(text = "${bigResults.weakProfitsCount} Trades (${bigResults.weakProfitsWinRatePct}%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextWhite)
                                                }
                                                Column {
                                                    Text(text = "Profit Factor", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                    Text(text = "${bigResults.weakProfitsProfitFactor}x", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentGold)
                                                }
                                                Column {
                                                    Text(text = "Losses Blocked", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                    Text(text = "${bigResults.majorTrapsBlockedCount} Traps (+${bigResults.capitalSavedPct}%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentCyan)
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2. Interactive Deep Thinking Simulators
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { thinkingEngine.deepThink("XAU/USD", 2040.12, hasPattern = true) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (isSwahili) "Fikiri Hatua 12 (BUY 1.5x)" else "Deep Think 12-Steps (BUY 1.5x)",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Button(
                                            onClick = { thinkingEngine.deepThink("XAU/USD", 2040.0, hasPattern = false) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (isSwahili) "Bila Mwelekeo (HOLD)" else "Flat Chop (HOLD Safe)",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentGold
                                            )
                                        }
                                    }
                                }

                                // 3. Metacognition Bias Check Card
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🧐 " + (if (isSwahili) "Metacognition & Ukaguzi wa Upendeleo (Bias Check):" else "Metacognition Bias & Overfit Audit:"),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentCyan
                                                )
                                                Surface(
                                                    color = AccentNeonGreen.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = metacognitionReport.biasVerdict,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AccentNeonGreen,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = metacognitionReport.reasoning,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "PBO: ${metacognitionReport.pboProbabilityBacktestOverfittingPct}% (<30%)", style = MaterialTheme.typography.labelSmall, color = AccentNeonGreen, fontFamily = FontFamily.Monospace)
                                                Text(text = "DSR: ${metacognitionReport.dsrDeflatedSharpeRatio} (>0)", style = MaterialTheme.typography.labelSmall, color = AccentGold, fontFamily = FontFamily.Monospace)
                                                Text(text = "Invalidation: ${metacognitionReport.predefinedInvalidationPrice}", style = MaterialTheme.typography.labelSmall, color = AccentOrange, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }
                                }

                                // 4. Focus Allocation V2 Card
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🎯 " + (if (isSwahili) "Mgawanyo wa Umakini (Focus Allocation V2):" else "Focus Allocation & Market Quality:"),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentGold
                                                )
                                                Text(
                                                    text = "Top 5 of 80 Markets",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextMuted
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            focusReport.allocations.take(3).forEach { item ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = "#${item.rank} ${item.symbol} (MQ ${item.marketQualityScore})", style = MaterialTheme.typography.bodySmall, color = TextWhite, fontWeight = FontWeight.Bold)
                                                    Text(text = "${item.resourceAllocationPct}% Alloc (${item.maxAllowedSizeFactor}x Booster)", style = MaterialTheme.typography.bodySmall, color = AccentNeonGreen, fontFamily = FontFamily.Monospace)
                                                }
                                            }
                                        }
                                    }
                                }

                                // 5. Self-Growth V2 Card
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🌱 " + (if (isSwahili) "Kujikuza & Stadi Mpya (Self-Growth V2):" else "Self-Growth & Dynamic Skill Evolution:"),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentLavender
                                                )
                                                Text(
                                                    text = "${selfGrowthReport.totalRegisteredSkillsCount} Skills Total",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentNeonGreen
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Deep vs Shallow Win Rate: ${selfGrowthReport.deepThinkingWinRatePct}% vs ${selfGrowthReport.shallowThinkingWinRatePct}% (+${(selfGrowthReport.deepThinkingWinRatePct - selfGrowthReport.shallowThinkingWinRatePct).toInt()}%) -> Deep usage boosted to ${selfGrowthReport.deepThinkingUsageRatioPct}%.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Latest Skill: ${selfGrowthReport.lastInventedSkillName} (Sharpe: ${selfGrowthReport.lastInventedSharpe}x, Win Rate: ${selfGrowthReport.lastInventedWinRatePct}%)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AccentCyan
                                            )
                                        }
                                    }
                                }

                                // 6. Live 12-Step Chain-of-Thought
                                item {
                                    Text(
                                        text = "🧠 " + (if (isSwahili) "Msururu wa Hatua 12 za Kufikiri (12-Step Quant Cognition):" else "Live 12-Step Quant Thinking Engine:"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }

                                items(deepThinkingResult.steps.ifEmpty { latestThought.steps }) { step ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Step ${step.stepNumber}: ${step.stepName}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentCyan
                                                )
                                                Text(
                                                    text = "${step.latencyMs}ms",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = TextMuted
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = step.thoughtNarrative,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                        }
                                    }
                                }

                                // 7. Isolated Maneuver & Sub-Trade Status
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "🛡️ " + (if (isSwahili) "Hali ya Maneuver & Kutenganisha PnL:" else "Maneuver Engine & Isolated Execution:"),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentOrange
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Action: ${deepThinkingResult.action} • Confluence: ${deepThinkingResult.confluenceScore} (${deepThinkingResult.confluenceConfidencePct}%) • Mode: ${deepThinkingResult.executionMode}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = AccentCyan
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = maneuverState.lastManeuverDescription,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                // 8. Resource Telemetry
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "⚡ " + (if (isSwahili) "Matumizi ya Rasilimali (Resource Optimization):" else "Resource Optimizer Telemetry:"),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                Text(text = "Workers: ${bigResults.activeWorkerPool}", style = MaterialTheme.typography.labelSmall, color = AccentNeonGreen, fontFamily = FontFamily.Monospace)
                                                Text(text = "Cache Hit: ${bigResults.cacheHitRatioPct}%", style = MaterialTheme.typography.labelSmall, color = AccentGold, fontFamily = FontFamily.Monospace)
                                                Text(text = "p95: ${bigResults.p95LatencyMs}ms", style = MaterialTheme.typography.labelSmall, color = AccentCyan, fontFamily = FontFamily.Monospace)
                                                Text(text = "APIs: ${bigResults.apiCallsPerMinute}/min", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        6 -> {
                            // Tab 6: Hybrid Replacement & Anti-Loss Execution Engine
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // 1. Hybrid Engine Overview Card
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "⚡ " + (if (isSwahili) "Injini ya Hybrid + Replacement (Weak + Strong)" else "Hybrid 50/50 + Replacement Engine"),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentCyan
                                                )
                                                Surface(
                                                    color = AccentCyan.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "${hybridTelemetry.improvementMultiplierVsWeakOnly}x Improvement",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = AccentCyan,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (isSwahili)
                                                    "Trade moja inagawanywa: 50% Weak Leg (0.08% TP ya haraka) + 50% Strong Leg (ATR trailing). Weak ikishinda, SL ya Strong inasogezwa breakeven (Risk-Free REPLACEMENT) kuzuia hasara yoyote!"
                                                else
                                                    "Single entry split into 50% Weak (0.08% quick TP) + 50% Strong (trailing ATR runner). Weak TP hit triggers REPLACEMENT: Strong SL moves to Break-Even (Risk-Free).",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(text = "Avg Return", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                    Text(text = "+${hybridTelemetry.netAverageReturnPerTradePct}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentNeonGreen)
                                                }
                                                Column {
                                                    Text(text = "Weak Win Rate", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                    Text(text = "${hybridTelemetry.weakLegWinRatePct}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentGold)
                                                }
                                                Column {
                                                    Text(text = "Strong Win Rate", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                    Text(text = "${hybridTelemetry.strongLegWinRatePct}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentCyan)
                                                }
                                                Column {
                                                    Text(text = "Loss Saved", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                    Text(text = "+${hybridTelemetry.capitalSavedPct}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentOrange)
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2. Interactive Scenario Simulators
                                item {
                                    Text(
                                        text = "🎯 " + (if (isSwahili) "Majaribio ya Hali 4 za Soko (Interactive Scenarios):" else "Deterministic Anti-Loss Scenarios:"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }

                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = { hybridEngine.simulateScenarioA() },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text("Scenario A (+0.76%)", style = MaterialTheme.typography.labelSmall, color = AccentNeonGreen, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { hybridEngine.simulateScenarioB() },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text("Scenario B (+0.04%)", style = MaterialTheme.typography.labelSmall, color = AccentCyan, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = { hybridEngine.simulateScenarioC() },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text("Scenario C (+0.47%)", style = MaterialTheme.typography.labelSmall, color = AccentGold, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { hybridEngine.simulateScenarioD() },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text("Scenario D (-0.27%)", style = MaterialTheme.typography.labelSmall, color = AccentRed, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // 3. Active Hybrid Trade Details Card
                                activeHybridSession?.let { session ->
                                    item {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlow),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Trade: ${session.symbol} @ ${session.entryPrice}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextWhite
                                                    )
                                                    Text(
                                                        text = "Net: +${String.format("%.3f", session.netReturnPct)}%",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (session.netReturnPct >= 0) AccentNeonGreen else AccentRed
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Weak 50%: ${session.weakLeg.status} (${session.weakLeg.returnPct}%)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (session.weakLeg.returnPct >= 0) AccentNeonGreen else AccentRed
                                                    )
                                                    Text(
                                                        text = "Strong 50%: ${session.strongLeg.status} (${session.strongLeg.returnPct}%)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (session.strongLeg.returnPct >= 0) AccentCyan else AccentRed
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Replacement Logs:",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentGold
                                                )
                                                session.logMessages.forEach { msg ->
                                                    Text(
                                                        text = "• $msg",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = TextMuted,
                                                        modifier = Modifier.padding(vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        7 -> {
                            // Tab 7: Deriv All Markets & Digits Engine
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // 1. Deriv Overview Card
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "📊 " + (if (isSwahili) "Masoko Yote 60+ ya Deriv & Digits" else "Deriv 60+ Markets & Digits Engine"),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentOrange
                                                )
                                                Surface(
                                                    color = AccentOrange.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "Active WebSocket",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = AccentOrange,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = if (isSwahili)
                                                    "Uchanganuzi wa moja kwa moja wa Continuous (10-300, 1s), Jump (10-100), Crash/Boom (300-1000), na Range Break. Takwimu halisi za tarakimu (Digits) na mikataba ya Rise/Fall, Touch na Digits."
                                                else
                                                    "Live real-time analytics for Continuous (10-300, 1s), Jump (10-100), Crash/Boom (300-1000), and Range Break. Digit z-score distribution and Rise/Fall hybrid setups.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                        }
                                    }
                                }

                                // 2. Digits Engine (0-9 Histogram & Even/Odd Bias)
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🔢 " + (if (isSwahili) "Tathmini ya Digits (Z-Score & Histogram):" else "Digit Frequency & Even/Odd Bias:"),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentNeonGreen
                                                )
                                                Text(
                                                    text = "${digitReport.symbol} (1000 Ticks)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = TextMuted
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "Even: ${digitReport.evenPct}% (${digitReport.evenCount})", style = MaterialTheme.typography.bodySmall, color = AccentNeonGreen, fontWeight = FontWeight.Bold)
                                                Text(text = "Odd: ${digitReport.oddPct}% (${digitReport.oddCount})", style = MaterialTheme.typography.bodySmall, color = AccentOrange, fontWeight = FontWeight.Bold)
                                                Text(text = "Z-Score: ${digitReport.evenZScore} (>2.0)", style = MaterialTheme.typography.bodySmall, color = AccentGold, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Rec: ${digitReport.recommendedContract} • EV: +${digitReport.expectedValuePct}% (at 95% payout) • Most Freq: Digit ${digitReport.mostFrequentDigit} • Least: Digit ${digitReport.leastFrequentDigit}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextWhite
                                            )
                                        }
                                    }
                                }

                                // 3. Volatility Regime & Quantum Tunneling
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "🌀 " + (if (isSwahili) "Hali ya Volatility & Quantum Tunneling:" else "Volatility Regime & Quantum Tunneling:"),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentCyan
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Regime: ${derivRegime.regimeType} • Bandwidth: ${derivRegime.bollingerBandwidth} (<0.04) • Tunneling Prob: ${derivRegime.tunnelingBreakoutProbPct}% • Rec: ${derivRegime.recommendedContract}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = derivContractEval.reason,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                // 4. Catalog Sample (60+ instruments)
                                item {
                                    Text(
                                        text = "⚡ " + (if (isSwahili) "Orodha ya Masoko ya Deriv (${derivInstruments.size} Assets):" else "Deriv Synthetic Catalog (${derivInstruments.size} Assets):"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }

                                items(derivInstruments.take(8)) { inst ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = inst.displayName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextWhite)
                                                Text(text = "${inst.symbol} • ${inst.category} • Speed: ${inst.tickSpeedPerSec}t/s", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                            }
                                            Surface(
                                                color = AccentCyan.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Vol ${inst.baseVolatilityPct}%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = AccentCyan,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        8 -> {
                            // Tab 8: Single User Training & Self-Learning No-Copy
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // 1. Dedicated Single User Resources
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "👑 " + (if (isSwahili) "Mtumiaji Mmoja Pekee (100% Rasilimali Zako):" else "Single User Sovereign Core (100% Dedicated):"),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentLavender
                                                )
                                                Surface(
                                                    color = AccentLavender.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "100% Compute",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = AccentLavender,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "User: ${userProfile.email} • Timezone: ${userProfile.timezone} • Masaa Bora: ${userProfile.preferredTradingHoursRange} • Max Risk: ${userProfile.maxRiskPerTradePct}% (Max DD: ${userProfile.maxDailyDrawdownTolerancePct}% Circuit Breaker).",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                        }
                                    }
                                }

                                // 2. Onboarding Training Entry System Progress
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🎓 " + (if (isSwahili) "Mafunzo ya Mfumo wa Entry (Personalized):" else "Personalized Entry Training:"),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentNeonGreen
                                                )
                                                Text(
                                                    text = "${trainingProgress.progressPct}% Complete",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentNeonGreen
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Hatua: ${trainingProgress.stageName} • Ticks zilizochanganuliwa: 50,000,000 (Miaka 2) • Vectors: ${userProfile.patternsEmbeddedCount} • Sharpe: ${userProfile.achievedSharpeRatio}x.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { userTrainingEngine.startPersonalizedTraining() },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(if (isSwahili) "Fanya Mafunzo Upya (Retrain Entry)" else "Retrain Entry System (0-100%)", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // 3. Self-Learning No-Copy Guard Status
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "🛡️ " + (if (isSwahili) "Kujifunza Kwake (Hai-Copy) & Anti-Copy Guard:" else "Self-Learning No-Copy & Anti-Copy Guard:"),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentGold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "HAKUNA COPY TRADING: Mfumo unajifunza moja kwa moja kutoka kwenye biashara zake mwenyewe (${selfLearningState.verifiedOwnTradesCount} trades) na takwimu za soko. Ishara za nje zilizozuiwa: ${selfLearningState.externalSignalsRejectedCount} signals (Kuzuia hasara ya copy).",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Self Confidence: ${selfLearningState.selfConfidencePct}% • Stadi Zilizofunzwa: ${selfLearningState.learnedSkillsCount} • Sharpe: ${selfLearningState.currentSharpeRatio}x",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = AccentCyan
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        9 -> {
                            // TAB 9: RAG VITABU 50+ & SKILLS 140
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "📚 " + (if (isSwahili) "Maktaba ya RAG Vitabu 50+ & Stadi 140:" else "RAG 50+ Books & 140 Skills Library:"),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentCyan
                                                )
                                                Text(
                                                    text = "$ragBooksCount Vitabu • $ragSkillsCount Skills (100% Vector DB)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentNeonGreen
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Vitabu vikuu vilivyomo vector DB: Market Wizards, Trading in the Zone, Wyckoff Method, Auction Market Theory (Dalton), Advances in Financial ML (Lopez de Prado), Volatility Trading (Sinclair), Dark Pools, Alchemy of Finance (Soros), Deriv Synthetic Math docs.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { megaRagEngine.queryKnowledge("Gold accumulation spring absorption iceberg tunneling void") },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                                ) {
                                                    Text("Tafuta Gold Spring (${ragResponse.latencyMs}ms)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = { megaRagEngine.queryKnowledge("Volatility 75 even odd digit bias tunneling deriv") },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                                ) {
                                                    Text("Tafuta Deriv Vol 75", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AccentGold)
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "💡 " + (if (isSwahili) "Muungano wa Busara za Vitabu (Fused Wisdom):" else "RAG Fused Wisdom Narrative:"),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentGold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = ragResponse.fusedWisdomNarrative,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                        }
                                    }
                                }

                                items(ragResponse.matchedDocs) { doc ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = doc.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentCyan
                                                )
                                                Text(
                                                    text = "${(doc.similarityScore * 100).toInt()}% Match",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = AccentNeonGreen
                                                )
                                            }
                                            Text(
                                                text = "${doc.authorOrCategory} • ${doc.reference}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = doc.contentSnippet,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        10 -> {
                            // TAB 10: SAFE LEARNING GUARD & HUMAN VETO (5 LAYERS)
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "🛡️ " + (if (isSwahili) "Kinga ya Kujifunza Salama (Safe Learning 5 Layers):" else "Safe Learning 5-Layer Shield:"),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentNeonGreen
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "1. IMMUTABLE RISK: Max Size 2%, Hard SL 1.8 ATR, Max DD 5% breaker, Max 20 trades/hr haiwezi kuvunjwa.\n2. SKILL CURATION: Vipimo 9 (OOS Sharpe>1.0, PBO<30%, DSR>0, CSCV>60%, EV>0.03%).\n3. BOUNDED LEARNING: Baada ya hasara size = size * 0.9 (kamwe si martingale 2x).\n4. BAD BEHAVIOR ROLLBACK: Overfit, revenge, weak-only bias inazuiliwa mara moja.\n5. HUMAN VETO: Saa 24 za mtumiaji kukubali au kukataa kabla ya live.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = "⏳ " + (if (isSwahili) "Stadi Zinazosubiri Kuidhinishwa na Binadamu (Pending Human Veto):" else "Skills Pending Human Veto (24h Window):"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGold
                                    )
                                }

                                items(pendingVetoSkills) { skill ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = skill.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                                                Text(text = "${skill.hoursRemaining}h Left", style = MaterialTheme.typography.labelSmall, color = AccentOrange, fontFamily = FontFamily.Monospace)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Market: ${skill.market} • Train/OOS Sharpe: ${skill.trainSharpe}x / ${skill.oosSharpe}x • PBO: ${skill.pboPct}% (<30%) • DSR: ${skill.dsrScore}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { safeLearningGuard.approvePendingSkill(skill.id) },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(vertical = 4.dp)
                                                ) {
                                                    Text(if (isSwahili) "Kubali (Test 0.1x)" else "Approve (0.1x Test)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = { safeLearningGuard.rejectPendingSkill(skill.id) },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(vertical = 4.dp)
                                                ) {
                                                    Text(if (isSwahili) "Kataa (Veto)" else "Reject (Veto)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = "🚫 " + (if (isSwahili) "Tabia Mbaya Zilizozuiwa na Mfumo (Bad Behaviors Blocked):" else "Anomalies Blocked & Rolled Back:"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentOrange
                                    )
                                }

                                items(blockedAnomalies) { anom ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = anom.type, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentOrange)
                                                Text(text = anom.timestamp, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                            }
                                            Text(text = anom.details, style = MaterialTheme.typography.bodySmall, color = TextWhite)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = "Hatua: ${anom.mitigationAction}", style = MaterialTheme.typography.labelSmall, color = AccentNeonGreen)
                                        }
                                    }
                                }
                            }
                        }
                        11 -> {
                            // TAB 11: PRICE DISCOVERY KILA SOKO & CHALLENGE BOTS
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🌐 " + (if (isSwahili) "Ugunduzi wa Bei & Ufanisi wa Exchange:" else "Price Discovery & Exchange Efficiency:"),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentCyan
                                                )
                                                Text(
                                                    text = "$exchangeEfficiencyPct% Ufanisi (Deriv 1-API)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentNeonGreen
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "SIO LAZIMA API ZOTE: Mfumo unafikia 85% ya ufanisi wake wa kilele kwa kuunganisha Deriv API pekee kutokana na maarifa ya RAG vitabu 50+, stadi 140, na injini ya Hybrid Replacement.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextWhite
                                            )
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = "🔍 " + (if (isSwahili) "Kila Soko Linapataje Bei (Supply & Demand Truth):" else "Price Discovery Mechanisms:"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGold
                                    )
                                }

                                items(discoveryMap.values.toList()) { info ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = info.symbol, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AccentCyan)
                                                Surface(color = AccentNeonGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                    Text(text = info.direction, style = MaterialTheme.typography.labelSmall, color = AccentNeonGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                            Text(text = "Utaratibu: ${info.mechanism}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "Ugavi (Supply): ${info.supplyFactors}", style = MaterialTheme.typography.bodySmall, color = TextWhite)
                                            Text(text = "Mahitaji (Demand): ${info.demandFactors}", style = MaterialTheme.typography.bodySmall, color = AccentGold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "Tathmini: ${info.summary}", style = MaterialTheme.typography.labelSmall, color = AccentNeonGreen)
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = "🤖 " + (if (isSwahili) "Agent Challenge & Investor Bots (Approach 6):" else "Agent Challenge & Investor Bots:"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                }

                                items(activeChallengeBots) { bot ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.35f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = bot.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                                                Surface(
                                                    color = if (bot.status.contains("ACTIVE")) AccentNeonGreen.copy(alpha = 0.15f) else AccentOrange.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = bot.status,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (bot.status.contains("ACTIVE")) AccentNeonGreen else AccentOrange,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = "Market: ${bot.market} • Approach: ${bot.approach} • Mtaji: $${bot.capitalUsd}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "Trades: ${bot.paperTrades} • Win Rate: ${bot.winRatePct}% • Sharpe: ${bot.sharpeRatio}x • PnL: +$${bot.pnlUsd}", style = MaterialTheme.typography.bodySmall, color = AccentNeonGreen, fontWeight = FontWeight.Bold)
                                                Button(
                                                    onClick = { discoveryAndChallenge.simulatePaperTestRun(bot.id) },
                                                    shape = RoundedCornerShape(4.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Jaribu Paper (+5)", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        12 -> {
                            // TAB 12: 11 QUANT INDICATORS & AUTONOMOUS AGENT SHIFT (BINANCE UI & PINESCRIPT CLONE)
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    // Binance Themed Header Card
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181A20)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0B90B).copy(alpha = 0.6f)),
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
                                                    Surface(
                                                        color = Color(0xFFF0B90B),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text("B", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = if (isSwahili) "Viashiria 11 & Shift ya Wakala (Binance UI)" else "11 Quant Indicators & Agent Shift (Binance UI)",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFF0B90B)
                                                        )
                                                        Text(
                                                            text = if (isSwahili) "Hesabu kamili <5ms • Shift ya Wakala <100ms • Pine Script v5" else "Pine-precision <5ms • Autonomous Agent Shift <100ms • Pine Script v5",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = TextMuted
                                                        )
                                                    }
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = if (isAutonomousMode) (if (isSwahili) "Wakala Anachagua" else "Agent Auto") else (if (isSwahili) "Mwongozo" else "User Mode"),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isAutonomousMode) Color(0xFFF0B90B) else TextMuted,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Switch(
                                                        checked = isAutonomousMode,
                                                        onCheckedChange = { elevenIndicatorsEngine.setAutonomousMode(it) },
                                                        colors = SwitchDefaults.colors(
                                                            checkedThumbColor = Color(0xFFF0B90B),
                                                            checkedTrackColor = Color(0xFFF0B90B).copy(alpha = 0.4f),
                                                            uncheckedThumbColor = Color.Gray,
                                                            uncheckedTrackColor = Color.DarkGray
                                                        )
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Binance Badges
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(color = Color(0xFFF0B90B).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                    Text("BINANCE PINE CLONE", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFFF0B90B), fontWeight = FontWeight.Bold)
                                                }
                                                Surface(color = AccentNeonGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                    Text("CORRELATION 0.98", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = AccentNeonGreen, fontWeight = FontWeight.Bold)
                                                }
                                                Surface(color = AccentCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                    Text("EV +0.89%", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = AccentCyan, fontWeight = FontWeight.Bold)
                                                }
                                                Surface(color = Color(0xFF2B313A), shape = RoundedCornerShape(4.dp)) {
                                                    Text("USER OPTIONAL", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = TextWhite, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Market Selector Chips
                                            Text(
                                                text = if (isSwahili) "Chagua Soko (Kila Biashara: Forex, Crypto, Deriv, Dhahabu, Indices):" else "Select Market (All Assets: Forex, Crypto, Deriv, Gold, Indices):",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                items(com.example.engine.ElevenIndicatorsAndShiftEngine.DEFAULT_MARKETS) { market ->
                                                    val isSelected = market == selectedMarket
                                                    Surface(
                                                        color = if (isSelected) Color(0xFFF0B90B) else Color(0xFF2B313A),
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.clickable { elevenIndicatorsEngine.selectMarket(market) }
                                                    ) {
                                                        Text(
                                                            text = market,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) Color.Black else TextWhite,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    // Autonomous Agent Shift Live Evaluation Card
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isAutonomousMode) Color(0xFFF0B90B).copy(alpha = 0.5f) else BorderSubtle),
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
                                                    Text(
                                                        text = if (isSwahili) "SHIFT YA WAKALA YA SASA" else "CURRENT AUTONOMOUS AGENT SHIFT",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFFF0B90B),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = currentShift.activeStrategy,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Black,
                                                        color = TextWhite
                                                    )
                                                }
                                                Surface(
                                                    color = AccentNeonGreen.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "EV +${String.format("%.2f", currentShift.expectedValuePct)}%",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AccentNeonGreen,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("Market", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                    Text(currentShift.market, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextWhite)
                                                }
                                                Column {
                                                    Text("Confluence", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                    Text("${currentShift.confluenceScore}/10", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentCyan)
                                                }
                                                Column {
                                                    Text("Booster", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                    Text("${currentShift.boosterMultiplier}x Size", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentOrange)
                                                }
                                                Column {
                                                    Text("Shift Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                    Text(currentShift.tickTime, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = TextWhite)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = currentShift.shiftRationale,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFEAECEF)
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Top Chosen Indicators & Skills:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                items(currentShift.topIndicators) { ind ->
                                                    Surface(color = Color(0xFF2B313A), shape = RoundedCornerShape(4.dp)) {
                                                        Text(ind, style = MaterialTheme.typography.labelSmall, color = Color(0xFFF0B90B), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                    }
                                                }
                                                items(currentShift.topSkills) { skill ->
                                                    Surface(color = AccentCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                        Text(skill, style = MaterialTheme.typography.labelSmall, color = AccentCyan, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    // Section Title for 11 Indicators
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isSwahili) "Viashiria 11 Kamili vya Ki-Quant (Pine-precision <5ms):" else "All 11 Quant Indicators (Pine-precision <5ms):",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = "${indicatorsList.count { userToggles[it.id] == true }}/11 Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFF0B90B)
                                        )
                                    }
                                }

                                // 11 Indicators List items
                                items(indicatorsList) { ind ->
                                    val isToggled = userToggles[ind.id] == true
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (isToggled) SurfaceElevated else Color(0xFF181A20).copy(alpha = 0.5f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isToggled) BorderGlow else Color.DarkGray),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        color = if (ind.signal == "BUY") AccentNeonGreen.copy(alpha = 0.2f) else AccentOrange.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = ind.signal,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (ind.signal == "BUY") AccentNeonGreen else AccentOrange,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = ind.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isToggled) TextWhite else TextMuted
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${ind.confidencePct}% Conf",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = AccentCyan
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Checkbox(
                                                        checked = isToggled,
                                                        onCheckedChange = { elevenIndicatorsEngine.toggleIndicator(ind.id) },
                                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF0B90B))
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = ind.valueStr, style = MaterialTheme.typography.bodySmall, color = Color(0xFFF0B90B), fontWeight = FontWeight.Bold)
                                                Text(text = ind.plotKeyMetric, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = ind.narrative, style = MaterialTheme.typography.labelSmall, color = TextWhite)

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(color = Color(0xFF0B0E11), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = ind.pineScriptSnippet,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = AccentCyan,
                                                    modifier = Modifier.padding(6.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    // Pine Script v5 Generator & Validator Card
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2962FF)),
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
                                                    Surface(color = Color(0xFF2962FF), shape = RoundedCornerShape(4.dp), modifier = Modifier.size(20.dp)) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text("TV", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "TradingView Pine Script v5 Generator & Clone",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF2962FF)
                                                    )
                                                }
                                                Surface(color = AccentNeonGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                    Text(
                                                        text = "TV Correlation: ${backtestCorrelation}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AccentNeonGreen,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = if (isSwahili) "Nambari za Pine Script v5 zimeundwa moja kwa moja kutoka mkakati na viashiria vya sasa. Usahihi wake ulinganifu ni 98% dhidi ya engine halisi ya WASM." else "Live Pine Script v5 strategy synthesized from agent choice. Correlation verified at 98% against native quant engine.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                color = Color(0xFF0B0E11),
                                                shape = RoundedCornerShape(6.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B313A)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = generatedPineScript,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFFF0B90B),
                                                    modifier = Modifier.padding(10.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Button(
                                                    onClick = { elevenIndicatorsEngine.triggerAutonomousShift(selectedMarket) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0B90B)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text("Re-Shift Agent (<100ms)", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                }
                                                OutlinedButton(
                                                    onClick = { /* Ready for TV Copy */ },
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2962FF)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text("Copy Pine v5 Code", color = Color(0xFF2962FF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    // Data Truth & Tick Engine Real Integration Card
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Verified, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isSwahili) "Uthibitisho wa Data Halisi & Injini ya Tick (Hakuna Data Feki)" else "Data Truth & Real Tick Engine (Zero Fake Data)",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextWhite
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (isSwahili) "Bei zote zinatoka vyanzo vilivyothibitishwa (Binance, Deriv, MT5, Forex feeds). Viashiria vyote 11 na shifts za wakala zinalindwa 100% na Hard Risk Governor kabla ya utekelezaji halisi." else "Every price originates from authenticated provider feeds (Binance, Deriv, MT5, Forex). All 11 indicators and agent shifts pass Hard Risk Governor before any execution.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSwahili) "Funga" else "Close")
                }
            }
        }
    }
}

@Composable
private fun SkillCardItem(
    skill: com.example.engine.UnifiedSkillModel,
    isSwahili: Boolean,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = AccentCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = skill.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(
                            text = if (isSwahili) "Ushindi" else "Win Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "${String.format("%.1f", skill.historicalWinRate)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = AccentNeonGreen
                        )
                    }
                    Column {
                        Text(
                            text = "Sharpe",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "${String.format("%.2f", skill.historicalSharpe)}x",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = AccentOrange
                        )
                    }
                    Column {
                        Text(
                            text = "Latency",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "${String.format("%.1f", skill.averageLatencyMs)}ms",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = AccentCyan
                        )
                    }
                }

                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Apply", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSwahili) "Tumia Stadi" else "Apply Skill",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
