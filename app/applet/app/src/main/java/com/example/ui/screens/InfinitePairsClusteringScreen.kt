package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.*
import com.example.ui.theme.*

@Composable
fun InfinitePairsClusteringScreen(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pairsEngine = remember { InfinitePairsAndClusteringEngine.getInstance(context) }
    val clusters by pairsEngine.clusters.collectAsState()
    val samplePairs by pairsEngine.samplePairs.collectAsState()
    val indicators by pairsEngine.indicators.collectAsState()
    val turtleInputs by pairsEngine.turtleInputs.collectAsState()
    val visionEvents by pairsEngine.visionEvents.collectAsState()
    val pocIdeas by pairsEngine.pocIdeas.collectAsState()
    val blueGreenState by pairsEngine.blueGreenState.collectAsState()
    val testResults by pairsEngine.testResults.collectAsState()
    val selectedStrategy by pairsEngine.selectedAgentStrategy.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Pairs, 1: Clusters, 2: Turtle, 3: Vision/POC/ML, 4: 44 Indicators, 5: BlueGreen & Tests
    var selectedClusterId by remember { mutableStateOf("cluster_1") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showAddPairDialog by remember { mutableStateOf(false) }

    // Dialog state for Pair Factory
    var newPairSymbol by remember { mutableStateOf("") }
    var newPairCategory by remember { mutableStateOf("CRYPTO") }
    var newPairBase by remember { mutableStateOf("") }
    var newPairQuote by remember { mutableStateOf("USDT") }

    val filteredPairs = samplePairs.filter {
        (selectedCategoryFilter == "ALL" || it.category == selectedCategoryFilter) &&
        (it.symbol.contains(searchQuery, ignoreCase = true) || it.baseAsset.contains(searchQuery, ignoreCase = true))
    }

    val tabTitles = if (isSwahili) {
        listOf("Pairs & Factory (50K+)", "Makundi 12 (Clusters)", "Turtle 20 (Dennis)", "Vision, POC & ML", "Viashiria 44 (Indicators)", "Blue/Green & Tests")
    } else {
        listOf("50K+ Pairs & Factory", "12 Clusters", "Turtle 20 (Dennis)", "Vision, POC & ML", "44 Indicators", "Blue/Green & Tests")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // TOP HEADER: Project Identity & Blue-Green State
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSwahili) "50K+ Infinite Pairs & Behavior Clustering" else "50K+ Infinite Pairs & Behavior Clustering",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                Text(
                    text = if (isSwahili) "Udhibiti wa Tabia za Masoko 50K+, Boti Zenye Kujisimamia & Viashiria 44" else "50K+ Multi-Asset Engine, Self-Managing Agent & 44 Indicators",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Blue-Green Mode Pill
                Surface(
                    onClick = { pairsEngine.toggleBlueGreenEnvironment() },
                    color = if (blueGreenState.activeEnvironment == "BLUE") Color(0xFF00D4FF).copy(alpha = 0.2f) else Color(0xFF00FF88).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (blueGreenState.activeEnvironment == "BLUE") Color(0xFF00D4FF) else Color(0xFF00FF88))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (blueGreenState.activeEnvironment == "BLUE") Color(0xFF00D4FF) else Color(0xFF00FF88))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${blueGreenState.activeEnvironment} (Port ${if (blueGreenState.activeEnvironment == "BLUE") "3000" else "3001"})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (blueGreenState.activeEnvironment == "BLUE") Color(0xFF00D4FF) else Color(0xFF00FF88),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Active Indexed Pill
                Surface(
                    color = AccentGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AccentGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "50,450+ Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB BAR
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = SurfaceElevated,
            contentColor = AccentCyan,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CONTENT BY TAB
        when (activeTab) {
            0 -> PairsAndFactoryTab(
                samplePairs = filteredPairs,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedCategory = selectedCategoryFilter,
                onCategoryChange = { selectedCategoryFilter = it },
                onAddPairClick = { showAddPairDialog = true },
                isSwahili = isSwahili
            )
            1 -> BehaviorClustersTab(clusters = clusters, isSwahili = isSwahili)
            2 -> TurtleStrategyTab(
                inputs = turtleInputs,
                onUpdate = { pairsEngine.updateTurtleInputs(it) },
                isSwahili = isSwahili
            )
            3 -> VisionPocMlTab(
                visionEvents = visionEvents,
                pocIdeas = pocIdeas,
                isSwahili = isSwahili
            )
            4 -> Indicators44Tab(
                indicators = indicators,
                onToggle = { pairsEngine.toggleIndicator(it) },
                isSwahili = isSwahili
            )
            5 -> BlueGreenAndTestsTab(
                blueGreenState = blueGreenState,
                testResults = testResults,
                onToggleBlueGreen = { pairsEngine.toggleBlueGreenEnvironment() },
                onRunTests = { pairsEngine.runAgentIntelligenceTestSuite() },
                isSwahili = isSwahili
            )
        }
    }

    // PAIR FACTORY DIALOG
    if (showAddPairDialog) {
        AlertDialog(
            onDismissRequest = { showAddPairDialog = false },
            title = {
                Text(
                    text = if (isSwahili) "Pair Factory (Ongeza Pair Mpya)" else "Infinite Pair Factory (Add Any Pair)",
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isSwahili) "Weka pair yoyote (mf. PEPEUSDT, BONKUSDT, WIFUSDT, n.k.) Mfumo utabuni mikakati 20 mipya na kuziunganisha na makundi."
                        else "Enter any pair (e.g. PEPEUSDT, BONKUSDT, WIFUSDT, etc.) The system will invent 20 tailored strategies and attach shared cluster bases.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    OutlinedTextField(
                        value = newPairSymbol,
                        onValueChange = { newPairSymbol = it },
                        label = { Text("Symbol (e.g. PEPE/USDT)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPairBase,
                        onValueChange = { newPairBase = it },
                        label = { Text("Base Asset (e.g. PEPE)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPairQuote,
                        onValueChange = { newPairQuote = it },
                        label = { Text("Quote Asset (e.g. USDT, USD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("CRYPTO", "FOREX", "DERIV", "METALS").forEach { cat ->
                            FilterChip(
                                selected = newPairCategory == cat,
                                onClick = { newPairCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPairSymbol.isNotBlank()) {
                            pairsEngine.addCustomPair(
                                symbol = newPairSymbol,
                                category = newPairCategory,
                                baseAsset = if (newPairBase.isBlank()) newPairSymbol.take(4) else newPairBase,
                                quoteAsset = newPairQuote
                            )
                            showAddPairDialog = false
                            newPairSymbol = ""
                            newPairBase = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text("Auto-Generate 84 Strategies", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPairDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

// -------------------------------------------------------------------------
// TAB 0: PAIRS & PAIR FACTORY
// -------------------------------------------------------------------------
@Composable
private fun PairsAndFactoryTab(
    samplePairs: List<InfinitePairItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onAddPairClick: () -> Unit,
    isSwahili: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(if (isSwahili) "Tafuta pair (mf. BTC, PEPE, V75, EUR)..." else "Search 50K+ pairs (e.g. BTC, PEPE, V75)...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAddPairClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isSwahili) "Buni Pair" else "Pair Factory", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category chips
        val categories = listOf("ALL", "CRYPTO", "FOREX", "DERIV", "METALS", "INDICES", "COMMODITIES")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    onClick = { onCategoryChange(cat) },
                    color = if (isSelected) AccentCyan else SurfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.Black else TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pairs List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(samplePairs) { pair ->
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pair.baseAsset.take(3),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = pair.symbol, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text(
                                    text = "Cluster: ${pair.clusterId.replace("cluster_", "C")} • ${pair.totalStrategiesCount} Strategies • EV: +${pair.evPct}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (pair.currentPrice < 0.01) "\$${String.format("%.8f", pair.currentPrice)}" else "\$${String.format("%,.2f", pair.currentPrice)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (pair.volatility24h >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (pair.volatility24h >= 0) Color.Green else Color.Red,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${pair.volatility24h}% Vol",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (pair.volatility24h >= 0) Color.Green else Color.Red,
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

// -------------------------------------------------------------------------
// TAB 1: 12 BEHAVIOR CLUSTERS
// -------------------------------------------------------------------------
@Composable
private fun BehaviorClustersTab(
    clusters: List<BehaviorCluster>,
    isSwahili: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(clusters) { cluster ->
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = cluster.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentCyan)
                            Text(text = "Market: ${cluster.primaryMarket}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Badge(containerColor = AccentGold.copy(alpha = 0.2f)) {
                            Text(text = "${cluster.instrumentCount} pairs", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSwahili) cluster.descriptionSw else cluster.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Shared Base Strategies: ${cluster.sharedBaseStrategies.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentGold,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Default: ${cluster.defaultStrategy}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(text = "Risk Multiplier: ${cluster.riskMultiplier}x", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00FF88))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 2: RICHARD DENNIS TURTLE 20 DYNAMIC INPUTS
// -------------------------------------------------------------------------
@Composable
private fun TurtleStrategyTab(
    inputs: TurtleStrategyInputs20,
    onUpdate: (TurtleStrategyInputs20) -> Unit,
    isSwahili: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Surface(
            color = SurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isSwahili) "Richard Dennis Turtle Strategy (Vigezo 20 vya Dynamic Default)" else "Richard Dennis Turtle Strategy (20 Dynamic Default Inputs)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
                Text(
                    text = if (isSwahili) "Biashara ya kufuata mwenendo (Trend Following), kata hasara mapema (Cut Loss Quick) na acha faida iende (Let Profit Run)."
                    else "Trend Following, Cut Loss Quick (0.5%), Let Profit Run (1.0% + trailing), and Max 2% immutable risk limit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Input 1 & 2: Top / Bottom Brand Lookback
                Text(text = "1 & 2. Top & Bottom Brand Lookback: ${inputs.topBrandLookback} candles", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = inputs.topBrandLookback.toFloat(),
                    onValueChange = { onUpdate(inputs.copy(topBrandLookback = it.toInt(), bottomBrandLookback = it.toInt())) },
                    valueRange = 10f..55f,
                    steps = 8
                )

                // Input 3: MA Period
                Text(text = "3. MA Period: ${inputs.maPeriod} SMA", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = inputs.maPeriod.toFloat(),
                    onValueChange = { onUpdate(inputs.copy(maPeriod = it.toInt())) },
                    valueRange = 10f..50f,
                    steps = 7
                )

                // Input 7: ATR Length
                Text(text = "7. ATR Length: ${inputs.atrLength}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = inputs.atrLength.toFloat(),
                    onValueChange = { onUpdate(inputs.copy(atrLength = it.toInt())) },
                    valueRange = 10f..30f,
                    steps = 3
                )

                // Input 19: Risk Fixed %
                Text(text = "19. Risk Fixed % Per Trade: ${inputs.riskFixedPctPerTrade}% (Max Exposure: ${inputs.maxExposurePct}%)", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = inputs.riskFixedPctPerTrade,
                    onValueChange = { onUpdate(inputs.copy(riskFixedPctPerTrade = it)) },
                    valueRange = 0.5f..2.0f,
                    steps = 3
                )

                // Input 20: Confluence Threshold
                Text(text = "20. Confluence Threshold: ${inputs.confluenceThreshold}/10", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = inputs.confluenceThreshold.toFloat(),
                    onValueChange = { onUpdate(inputs.copy(confluenceThreshold = it.toInt())) },
                    valueRange = 1f..5f,
                    steps = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Checkbox controls
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "6. Style Remove MA combine", color = TextWhite, fontSize = 12.sp)
                    Switch(checked = inputs.styleRemoveMaCombine, onCheckedChange = { onUpdate(inputs.copy(styleRemoveMaCombine = it)) })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "8. Smoothing SMA in ATR (true)", color = TextWhite, fontSize = 12.sp)
                    Switch(checked = inputs.smoothingSmaInAtr, onCheckedChange = { onUpdate(inputs.copy(smoothingSmaInAtr = it)) })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "11. Add SMA20 overlay (yellow)", color = TextWhite, fontSize = 12.sp)
                    Switch(checked = inputs.addSma20, onCheckedChange = { onUpdate(inputs.copy(addSma20 = it)) })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "15 & 16. Breakout Up & Down triggers", color = TextWhite, fontSize = 12.sp)
                    Switch(checked = inputs.breakoutUp, onCheckedChange = { onUpdate(inputs.copy(breakoutUp = it, breakoutDown = it)) })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "17 & 18. Trend Up & Down filter", color = TextWhite, fontSize = 12.sp)
                    Switch(checked = inputs.trendUp, onCheckedChange = { onUpdate(inputs.copy(trendUp = it, trendDown = it)) })
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 3: VISION, POC & ML ENGINE
// -------------------------------------------------------------------------
@Composable
private fun VisionPocMlTab(
    visionEvents: List<VisionScanEvent>,
    pocIdeas: List<PocIdeaItem>,
    isSwahili: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // VISION ENGINE
        Surface(
            color = SurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Vision Engine (Scans 50K+ Pairs Every 1min)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Badge(containerColor = AccentCyan.copy(alpha = 0.2f)) {
                        Text(text = "LIVE SCANNER", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                visionEvents.forEach { ev ->
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = ev.symbol, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "• ${ev.eventType}", color = AccentGold, fontSize = 10.sp)
                                }
                                Text(text = ev.details, color = TextMuted, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = ev.timestamp, color = TextMuted, fontSize = 10.sp)
                                Text(text = "Conf: ${ev.confluenceScore}/10", color = Color(0xFF00FF88), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // POC IDEAS
        Surface(
            color = SurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "POC Ideas Engine (Automated Research Hypotheses)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentGold)
                Spacer(modifier = Modifier.height(10.dp))
                pocIdeas.forEach { poc ->
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = poc.title, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 12.sp)
                                Text(text = "Exp. Win: ${poc.expectedWinRate}%", color = Color(0xFF00FF88), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(text = poc.hypothesis, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // ML QUANT ML & LLM CoT 12 STEPS
        Surface(
            color = SurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "LLM 12-Step Chain-of-Thought & ML Quant Suite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Perceive -> Recognize -> Merge Confluence -> Differentiate -> Research FRED/BLS -> Focus MQ -> Decide -> Metacognition PBO/DSR -> Act -> Learn\n" +
                           "• ML Models: HMM Regime Classifier, LSTM 65% Tick Predictor, XGBoost 200 Features, PPO Strategy Switcher, DQN Q-Learning, Monte Carlo 10k Paths.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 4: 44 QUANT & TECHNICAL INDICATORS
// -------------------------------------------------------------------------
@Composable
private fun Indicators44Tab(
    indicators: List<IndicatorToggleState>,
    onToggle: (String) -> Unit,
    isSwahili: Boolean
) {
    var categoryFilter by remember { mutableStateOf("ALL") }
    val categories = listOf("ALL", "CORE_11", "TURTLE_TIMEFRAME_11", "ADVANCED_QUANT_ML_15", "TECHNICAL_7")

    val filtered = if (categoryFilter == "ALL") indicators else indicators.filter { it.category == categoryFilter }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = categoryFilter == cat
                Surface(
                    onClick = { categoryFilter = cat },
                    color = if (isSelected) AccentGold else SurfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.Black else TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { ind ->
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (ind.isActive) AccentCyan.copy(alpha = 0.2f) else BorderSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "${ind.number}", color = if (ind.isActive) AccentCyan else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = ind.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text(text = ind.description, style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = ind.isActive,
                            onCheckedChange = { onToggle(ind.id) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentCyan,
                                checkedTrackColor = AccentCyan.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 5: BLUE/GREEN & TEST SUITE
// -------------------------------------------------------------------------
@Composable
private fun BlueGreenAndTestsTab(
    blueGreenState: BlueGreenDeploymentState,
    testResults: List<AgentIntelligenceTestResult>,
    onToggleBlueGreen: () -> Unit,
    onRunTests: () -> Unit,
    isSwahili: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // BLUE/GREEN DEPLOYMENT PANEL
        Surface(
            color = SurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Blue-Green Zero Downtime Deployment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00D4FF))
                    Button(
                        onClick = onToggleBlueGreen,
                        colors = ButtonDefaults.buttonColors(containerColor = if (blueGreenState.activeEnvironment == "BLUE") Color(0xFF00FF88) else Color(0xFF00D4FF))
                    ) {
                        Text(
                            text = "Switch to ${if (blueGreenState.activeEnvironment == "BLUE") "GREEN (3001)" else "BLUE (3000)"}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Active Port: ${if (blueGreenState.activeEnvironment == "BLUE") "3000 (Production)" else "3001 (Staging Canary)"}", color = TextWhite, fontSize = 12.sp)
                    Text(text = "Canary Traffic: ${blueGreenState.canaryTrafficPct}%", color = AccentGold, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Switch Duration: ${blueGreenState.switchDurationSec}s Instant Failover", color = TextMuted, fontSize = 12.sp)
                    Text(text = "Uptime: ${blueGreenState.uptimePct}%", color = Color(0xFF00FF88), fontSize = 12.sp)
                }
            }
        }

        // AGENT INTELLIGENCE TEST SUITE
        Surface(
            color = SurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Agent Intelligence Test Suite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentGold)
                        Text(text = "Tests 1-4: Cognitive Switch, CoT 12 Steps, Governance & Final Audit", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Button(
                        onClick = onRunTests,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run Audit Tests", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (testResults.isEmpty()) {
                    Text(
                        text = if (isSwahili) "Bofya 'Run Audit Tests' ili kujaribu uwezo wa akili wa Agent katika kubadili mikakati, kufikiria na kutoa ripoti."
                        else "Click 'Run Audit Tests' to evaluate agent cognitive switching (<100ms), 12-step CoT reasoning, and generate the final audit report.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                } else {
                    testResults.forEach { res ->
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (res.isPassed) Color(0xFF00FF88).copy(alpha = 0.5f) else Color.Red),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = res.title, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 12.sp)
                                    Text(
                                        text = if (res.isPassed) "PASS (Score: ${res.scoreOutOfTen}/10)" else "FAIL",
                                        color = if (res.isPassed) Color(0xFF00FF88) else Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(text = res.description, color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = res.details, color = AccentCyan, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
