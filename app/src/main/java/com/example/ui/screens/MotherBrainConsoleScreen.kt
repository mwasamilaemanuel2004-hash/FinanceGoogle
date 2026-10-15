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
import com.example.data.model.OrderSide
import com.example.engine.*
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun MotherBrainConsoleScreen(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    modifier: Modifier = Modifier
) {
    val telemetry by engine.telemetry.collectAsState()
    val threeAgentSystem = engine.threeAgentSystem
    val tenAgentEcosystem = engine.tenAgentEcosystem
    val tacticalEngine = engine.tacticalEngine
    val mlPipeline = engine.mlTrainingPipeline

    val allAgents by tenAgentEcosystem.agents.collectAsState()
    val refreshTelemetry by tenAgentEcosystem.refreshTelemetry.collectAsState()
    val systemActivityLogs by tenAgentEcosystem.systemActivityFeed.collectAsState()

    val marketIntel by threeAgentSystem.marketAgent.state.collectAsState()
    val quantMl by threeAgentSystem.quantMlAgent.state.collectAsState()
    val supervisorEval by threeAgentSystem.supervisor.evaluation.collectAsState()

    val predictionsHistory by tacticalEngine.predictionsHistory.collectAsState()
    val scannedOpportunities by tacticalEngine.scannedOpportunities.collectAsState()
    val registeredModels by mlPipeline.modelsRegistry.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var agentFilter by remember { mutableStateOf<AgentGroupCategory?>(null) }
    var opportunityFilter by remember { mutableStateOf<OpportunityCategory?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Pulsating animation for the auto-refresh heartbeat
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

    val tabs = if (isSwahili) {
        listOf("Wakala 10 Wote", "3 Wakala Wakuu wa AI", "Utabiri wa Kisoko", "Rada ya Fursa", "Mafunzo ya ML", "Ujuzi Wote 52 & Barafu")
    } else {
        listOf("All 10 Agents", "3 Central AI Agents", "Tactical Predictions", "Opportunity Scanner", "ML Training & Registry", "All 52 Skills & Iceberg")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Top Mother Brain Status Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlow),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = "AI",
                        tint = AccentLavender,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ESMH MOTHER-BRAIN SOVEREIGN ENGINE",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentLavender
                        )
                        Text(
                            text = if (isSwahili) "Jeshi la Wakala 10 (3 Wapya wa AI + 7 wa Msingi) • Mfumo wa Auto-Refresh" 
                                   else "10-Agent Sovereign Army (3 New AI + 7 Foundation) • Auto-Refresh Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    color = if (telemetry.emergencyStopActive) DangerRed.copy(alpha = 0.2f) else AccentNeonGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (telemetry.emergencyStopActive) DangerRed else AccentNeonGreen)
                ) {
                    Text(
                        text = if (telemetry.emergencyStopActive) "HALTED / EMERGENCY" else "10 AGENTS ONLINE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (telemetry.emergencyStopActive) DangerRed else AccentNeonGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // -------------------------------------------------------------
        // AUTO-REFRESH SYSTEM CONTROL BAR
        // -------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pulsating Live Dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .scale(if (refreshTelemetry.isEnabled) pulseScale else 1.0f)
                                .clip(CircleShape)
                                .background(if (refreshTelemetry.isEnabled) AccentNeonGreen else DangerRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSwahili) "MFUMO WA AUTO-REFRESH:" else "AUTO-REFRESH SYSTEM:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (refreshTelemetry.isEnabled) 
                                (if (isSwahili) "IMEWASHWA (KASI YA MOJA KWA MOJA)" else "ACTIVE (REAL-TIME)") 
                                else (if (isSwahili) "IMESIMAMISHWA" else "PAUSED"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (refreshTelemetry.isEnabled) AccentNeonGreen else DangerRed
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${refreshTelemetry.totalCycles} • ${refreshTelemetry.averageCycleLatencyMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Manual Force Sync Button
                        Button(
                            onClick = {
                                tenAgentEcosystem.forceManualRefreshNow()
                                snackbarMessage = if (isSwahili) "Mifumo yote 10 na masoko yamesawazishwa sasa!" else "All 10 agents and data feeds synced now!"
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.2f), contentColor = AccentCyan),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync", modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = if (isSwahili) "Refresh Sasa" else "Sync Now", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Interval Selector Chips & Pause Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(AutoRefreshInterval.values()) { interval ->
                            val isSelected = refreshTelemetry.currentInterval == interval && refreshTelemetry.isEnabled
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSelected) AccentCyan.copy(alpha = 0.25f) else SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentCyan else BorderSubtle),
                                modifier = Modifier.clickable {
                                    tenAgentEcosystem.toggleAutoRefresh(true)
                                    tenAgentEcosystem.setAutoRefreshInterval(interval)
                                }
                            ) {
                                Text(
                                    text = if (isSwahili) interval.labelSwahili else interval.labelEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) AccentCyan else TextMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Pause / Resume Toggle
                    OutlinedButton(
                        onClick = { tenAgentEcosystem.toggleAutoRefresh(!refreshTelemetry.isEnabled) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = if (refreshTelemetry.isEnabled) 
                                (if (isSwahili) "Sitisha" else "Pause") 
                                else (if (isSwahili) "Washa" else "Resume"),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (refreshTelemetry.isEnabled) AccentGold else AccentNeonGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceCard,
            contentColor = AccentCyan,
            edgePadding = 0.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) AccentCyan else TextMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        when (selectedTab) {
            0 -> TenAgentsHubTabContent(
                isSwahili = isSwahili,
                agents = allAgents,
                activeFilter = agentFilter,
                onSelectFilter = { agentFilter = it },
                onToggleAgent = { agentId ->
                    tenAgentEcosystem.toggleAgentEnabled(agentId)
                    val ag = allAgents.firstOrNull { it.id == agentId }
                    snackbarMessage = if (isSwahili) "Hali ya ${ag?.nameSwahili ?: "Wakala"} imebadilishwa." else "Status of ${ag?.nameEn ?: "Agent"} toggled."
                },
                activityFeed = systemActivityLogs
            )
            1 -> ThreeAgentsTabContent(
                isSwahili = isSwahili,
                marketIntel = marketIntel,
                quantMl = quantMl,
                supervisorEval = supervisorEval
            )
            2 -> TacticalPredictionsTabContent(
                isSwahili = isSwahili,
                predictions = predictionsHistory,
                activeSymbol = "BTC/USDT"
            )
            3 -> OpportunityScannerTabContent(
                isSwahili = isSwahili,
                opportunities = scannedOpportunities,
                activeFilter = opportunityFilter,
                onSelectFilter = { opportunityFilter = it },
                onExecuteSetup = { symbol, side ->
                    snackbarMessage = if (isSwahili) "Biashara ya $symbol ($side) imeanzishwa kwa idhini ya Msimamizi Mkuu." else "Trade for $symbol ($side) initiated under Supervisor approval."
                }
            )
            4 -> MlTrainingRegistryTabContent(
                isSwahili = isSwahili,
                models = registeredModels,
                pipelineEngine = mlPipeline,
                onTriggerRetrain = { id ->
                    mlPipeline.performOnlineRetraining(id)
                    snackbarMessage = if (isSwahili) "Mifano $id imeboreshwa kwa Online Incremental Learning." else "Model $id retrained via Online Incremental Learning."
                }
            )
            5 -> AgenticSkillsAndIcebergDashboardView(
                targetSymbol = "BTC/USDT",
                isSwahili = isSwahili
            )
        }

        snackbarMessage?.let { msg ->
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = msg, style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                    IconButton(onClick = { snackbarMessage = null }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// TAB 0: ALL 10 AGENTS HUB (3 NEW DEEP AI + 7 FOUNDATION)
// ----------------------------------------------------------------------------

@Composable
private fun TenAgentsHubTabContent(
    isSwahili: Boolean,
    agents: List<UnifiedAgentNode>,
    activeFilter: AgentGroupCategory?,
    onSelectFilter: (AgentGroupCategory?) -> Unit,
    onToggleAgent: (Int) -> Unit,
    activityFeed: List<String>
) {
    val filteredAgents = if (activeFilter != null) {
        agents.filter { it.group == activeFilter }
    } else {
        agents
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Summary & Category Filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isSwahili) "Jeshi la Wakala 10 Wakuu (10 Autonomous Agents)" else "10 Autonomous Agents Matrix",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = if (isSwahili) "Wakala 3 Wapya wa Deep ML Matrix + Wakala 7 wa Uendeshaji & Broker" 
                               else "3 New Deep AI/ML Agents + 7 Foundation Operational Agents",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Surface(
                    color = AccentCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${agents.count { it.isEnabled }} / 10 ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = activeFilter == null,
                    onClick = { onSelectFilter(null) },
                    label = { Text(if (isSwahili) "Wote 10 (${agents.size})" else "All 10 (${agents.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = activeFilter == AgentGroupCategory.NEW_DEEP_AI,
                    onClick = { onSelectFilter(AgentGroupCategory.NEW_DEEP_AI) },
                    label = { 
                        Text(
                            if (isSwahili) "⚡ 3 Wapya (Deep AI/ML)" else "⚡ 3 New (Deep AI/ML)", 
                            fontSize = 11.sp,
                            color = Color(AgentGroupCategory.NEW_DEEP_AI.badgeColorHex)
                        ) 
                    }
                )
                FilterChip(
                    selected = activeFilter == AgentGroupCategory.FOUNDATION_OPS,
                    onClick = { onSelectFilter(AgentGroupCategory.FOUNDATION_OPS) },
                    label = { 
                        Text(
                            if (isSwahili) "🛡️ 7 wa Msingi (Ops)" else "🛡️ 7 Foundation (Ops)", 
                            fontSize = 11.sp,
                            color = Color(AgentGroupCategory.FOUNDATION_OPS.badgeColorHex)
                        ) 
                    }
                )
            }
        }

        // Live Activity Feed Accordion
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = "Logs", tint = AccentNeonGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSwahili) "Matukio ya Moja kwa Moja ya Wakala 10 (Live Stream)" else "Live Multi-Agent Stream",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentNeonGreen
                            )
                        }
                        Text(
                            text = "Auto-Synced",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    activityFeed.take(3).forEach { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Agent Cards
        items(filteredAgents) { agent ->
            AgentCardItem(
                agent = agent,
                isSwahili = isSwahili,
                onToggleEnabled = { onToggleAgent(agent.id) }
            )
        }
    }
}

@Composable
private fun AgentCardItem(
    agent: UnifiedAgentNode,
    isSwahili: Boolean,
    onToggleEnabled: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (agent.isNewThree) Color(AgentGroupCategory.NEW_DEEP_AI.badgeColorHex).copy(alpha = 0.6f) else BorderSubtle
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(agent.status.colorHex))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isSwahili) agent.nameSwahili else agent.nameEn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            if (agent.isNewThree) {
                                Surface(
                                    color = Color(AgentGroupCategory.NEW_DEEP_AI.badgeColorHex).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(
                                        text = "NEW AI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(AgentGroupCategory.NEW_DEEP_AI.badgeColorHex),
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isSwahili) agent.roleSwahili else agent.roleEn,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                // Enable/Pause Toggle Button
                OutlinedButton(
                    onClick = onToggleEnabled,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = if (agent.isEnabled) 
                            (if (isSwahili) "Washa" else "Active") 
                            else (if (isSwahili) "Simama" else "Paused"),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (agent.isEnabled) AccentNeonGreen else DangerRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Current task description
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = "Task",
                        tint = AccentGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = agent.currentTask,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                agent.primaryMetrics.entries.take(4).forEach { entry ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.key,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                        Text(
                            text = entry.value,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Workload Bar & Telemetry footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Workload: ${agent.workloadPct}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LinearProgressIndicator(
                        progress = { agent.workloadPct / 100f },
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (agent.workloadPct > 80) AccentGold else AccentCyan,
                        trackColor = SurfaceElevated
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Latency: ${agent.latencyMs}ms • RAM: ${agent.memoryUsageMb}MB",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(agent.status.colorHex).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text(
                            text = if (isSwahili) agent.status.labelSwahili else agent.status.labelEn,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(agent.status.colorHex),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}


// ----------------------------------------------------------------------------
// TAB 0: THREE AGENTS INTELLIGENCE ARCHITECTURE
// ----------------------------------------------------------------------------

@Composable
private fun ThreeAgentsTabContent(
    isSwahili: Boolean,
    marketIntel: MarketIntelligenceState,
    quantMl: QuantMlState,
    supervisorEval: SupervisorEvaluation?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // AGENT 1: MARKET INTELLIGENCE AGENT
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
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
                            Icon(Icons.Default.ShowChart, contentDescription = "Market", tint = AccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "AGENT 1: WAKALA WA MAARIFA YA SOKO" else "AGENT 1 — MARKET INTELLIGENCE AGENT",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                        }
                        Surface(
                            color = AccentCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = marketIntel.detectedRegime.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isSwahili) "Uchambuzi wa moja kwa moja wa Microstructure, Ukwasi, Order Book na Volatility:" else "Real-time Microstructure, Order-Book Depth, CVD, Liquidity & Volatility:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricPill("Price", "\$${marketIntel.price.roundToInt()}", AccentElectricBlue)
                        MetricPill("24h Vol", "\$${(marketIntel.volume24hUsdMillions / 1000.0).roundToInt()}B", AccentGold)
                        MetricPill("Spread", "${marketIntel.spreadBps} bps", if (marketIntel.spreadBps < 2.0) AccentNeonGreen else WarningOrange)
                        MetricPill("Liquidity", "${marketIntel.liquidityScore}/100", AccentNeonGreen)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricPill("Bid Depth", "\$${marketIntel.orderBookDepthBidMillions.roundToInt()}M", AccentCyan)
                        MetricPill("Ask Depth", "\$${marketIntel.orderBookDepthAskMillions.roundToInt()}M", AccentOrange)
                        MetricPill("Realized Vol", "${marketIntel.realizedVolAnnualizedPct.roundToInt()}%", AccentLavender)
                        MetricPill("Slippage", "${(marketIntel.estimatedSlippagePct * 100).roundToInt() / 100.0}%", AccentEmerald)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = Color(0xFF060B18),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "• CVD Trend: ${marketIntel.cvdTrend} | Skew: ${marketIntel.bidAskImbalancePct.roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = AccentCyan
                            )
                            Text(
                                text = "• Market Structure: ${marketIntel.marketStructureState} | VWAP: \$${marketIntel.vwapPrice.roundToInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = TextWhite
                            )
                            Text(
                                text = "• Adaptive Analysis Frequency: ${marketIntel.analysisFrequencyMs}ms | Sizing Mult: ${marketIntel.positionSizingMultiplier}x",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = AccentNeonGreen
                            )
                        }
                    }
                }
            }
        }

        // AGENT 2: QUANT + ML INTELLIGENCE AGENT
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentLavender.copy(alpha = 0.5f)),
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
                            Icon(Icons.Default.Memory, contentDescription = "ML", tint = AccentLavender, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "AGENT 2: WAKALA WA QUANT & ML" else "AGENT 2 — QUANT + ML INTELLIGENCE AGENT",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentLavender
                            )
                        }
                        Surface(
                            color = AccentLavender.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${quantMl.activeModelsCount} Models Ensemble",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentLavender,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricPill("Consensus Bias", quantMl.primaryDirectionalBias.name, if (quantMl.primaryDirectionalBias == OrderSide.BUY) AccentNeonGreen else DangerRed)
                        MetricPill("Ensemble Prob", "${quantMl.consensusProbabilityPct}%", AccentCyan)
                        MetricPill("Concept Drift", if (quantMl.diagnostics.conceptDriftDetected) "DETECTED" else "HEALTHY", if (quantMl.diagnostics.conceptDriftDetected) DangerRed else AccentNeonGreen)
                        MetricPill("Overfitting", "${(quantMl.diagnostics.overfittingPenalty * 100).roundToInt()}%", AccentGold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isSwahili) "Mifano ya Kisayansi & Machine Learning Inayofanya Kazi:" else "Active Mathematical & ML Architectures in Ensemble:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = Color(0xFF060B18),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            quantMl.modelScores.take(6).forEach { m ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = m.modelName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${m.predictedDirection?.name ?: "NEUTRAL"} (${m.probabilityPct}%) • W:${(m.reliabilityWeight * 100).roundToInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (m.predictedDirection == OrderSide.BUY) AccentNeonGreen else DangerRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // AGENT 3: SUPERVISOR / MOTHER-BRAIN AGENT
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.6f)),
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
                            Icon(Icons.Default.Security, contentDescription = "Supervisor", tint = AccentGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwahili) "AGENT 3: MSIMAMIZI MKUU (SUPERVISOR)" else "AGENT 3 — SUPERVISOR / MOTHER-BRAIN AGENT",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                        }

                        val decision = supervisorEval?.decision ?: SupervisorDecision.WAIT
                        Surface(
                            color = when (decision) {
                                SupervisorDecision.TRADE -> AccentNeonGreen.copy(alpha = 0.2f)
                                SupervisorDecision.REDUCE -> AccentGold.copy(alpha = 0.2f)
                                SupervisorDecision.WAIT -> AccentCyan.copy(alpha = 0.2f)
                                else -> DangerRed.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (decision) {
                                    SupervisorDecision.TRADE -> AccentNeonGreen
                                    SupervisorDecision.REDUCE -> AccentGold
                                    SupervisorDecision.WAIT -> AccentCyan
                                    else -> DangerRed
                                }
                            )
                        ) {
                            Text(
                                text = decision.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (decision) {
                                    SupervisorDecision.TRADE -> AccentNeonGreen
                                    SupervisorDecision.REDUCE -> AccentGold
                                    SupervisorDecision.WAIT -> AccentCyan
                                    else -> DangerRed
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    supervisorEval?.let { eval ->
                        Text(
                            text = if (isSwahili) eval.supervisorRationaleSwahili else eval.supervisorRationaleEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextWhite
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MetricPill("Consensus Score", "${eval.consensusScorePct}%", AccentGold)
                            MetricPill("Approved Lot", "${eval.approvedLotSize}", AccentCyan)
                            MetricPill("Stop Loss", "\$${eval.dynamicStopLoss.roundToInt()}", DangerRed)
                            MetricPill("Target TP1", "\$${eval.dynamicTakeProfit1.roundToInt()}", AccentNeonGreen)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            color = Color(0xFF130E05),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = "Law", tint = AccentGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSwahili) "SHERIA KUU YA MFUMO: Hakuna mfano mmoja au wakala mmoja anayeruhusiwa kuanzisha Live Trade peke yake bila idhini ya Msimamizi Mkuu." else "STRICT SAFETY LAW: Never allow one model or one agent to independently force a live trade without Supervisor Multi-Agent consensus.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// TAB 1: TACTICAL PREDICTIONS TAB (ALL 15 POINTS)
// ----------------------------------------------------------------------------

@Composable
private fun TacticalPredictionsTabContent(
    isSwahili: Boolean,
    predictions: List<TacticalPrediction>,
    activeSymbol: String
) {
    if (predictions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (isSwahili) "Inakokotoa utabiri wa kisoko wa pointi 15..." else "Generating 15-point probabilistic predictions...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
        return
    }

    val latest = predictions.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 ${latest.symbol} • PROBABILISTIC PREDICTION",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                        Surface(
                            color = if (latest.directionalBias == OrderSide.BUY) AccentNeonGreen.copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${latest.directionalBias.name} (${latest.probabilityConfidencePct}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (latest.directionalBias == OrderSide.BUY) AccentNeonGreen else DangerRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PredictionField("1. Directional Bias", latest.directionalBias.name, AccentNeonGreen)
                    PredictionField("2. Probability / Confidence", "${latest.probabilityConfidencePct}% Confidence", AccentCyan)
                    PredictionField("3. Time Horizon", latest.timeHorizon, AccentGold)
                    PredictionField("4. Market Regime", latest.marketRegime.name, AccentLavender)
                    PredictionField("5. Supporting Evidence", latest.supportingEvidence.joinToString(" • "), TextWhite)
                    PredictionField("6. Entry / Trigger Zone", "\$${latest.entryTriggerZoneLow.roundToInt()} - \$${latest.entryTriggerZoneHigh.roundToInt()}", AccentElectricBlue)
                    PredictionField("7. Confirmation Conditions", latest.confirmationConditions, TextWhite)
                    PredictionField("8. Invalidation Condition", latest.invalidationCondition, DangerRed)
                    PredictionField("9, 10, 11. Targets", "TP1: \$${latest.takeProfit1.roundToInt()} | TP2: \$${latest.takeProfit2.roundToInt()} | TP3: \$${latest.takeProfit3.roundToInt()}", AccentNeonGreen)
                    PredictionField("12. Risk Condition", latest.riskCondition, WarningOrange)
                    PredictionField("13. Selected Strategy", latest.selectedStrategyName, AccentCyan)
                    PredictionField("14. Why Strategy Selected", latest.whyStrategySelected, TextWhite)
                    PredictionField("15. Conditions Forcing WAIT/BLOCK", latest.conditionsForcingWaitOrBlock, DangerRed)
                }
            }
        }

        item {
            Text(
                text = if (isSwahili) "Historia ya Utabiri na Matokeo Halisi ya Kisoko" else "Prediction Historical Accuracy & Outcome Verification",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        items(predictions.take(10)) { p ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(8.dp),
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
                        Text(text = "${p.symbol} • ${p.directionalBias.name}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text(text = "Strategy: ${p.selectedStrategyName}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (p.outcomeEvaluated) {
                                if (p.wasPredictionAccurate == true) "HIT TP (+${(p.actualPnlPct * 10).roundToInt() / 10.0}%)" else "STOPPED"
                            } else "IN PLAY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (p.wasPredictionAccurate == true) AccentNeonGreen else if (p.outcomeEvaluated) DangerRed else AccentCyan
                        )
                        Text(text = "${p.probabilityConfidencePct}% Conf", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// TAB 2: OPPORTUNITY SCANNER TAB
// ----------------------------------------------------------------------------

@Composable
private fun OpportunityScannerTabContent(
    isSwahili: Boolean,
    opportunities: List<ScannedOpportunityItem>,
    activeFilter: OpportunityCategory?,
    onSelectFilter: (OpportunityCategory?) -> Unit,
    onExecuteSetup: (String, OrderSide) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = activeFilter == null,
                    onClick = { onSelectFilter(null) },
                    label = { Text("Zote (All)") }
                )
            }
            items(OpportunityCategory.values()) { cat ->
                FilterChip(
                    selected = activeFilter == cat,
                    onClick = { onSelectFilter(if (activeFilter == cat) null else cat) },
                    label = { Text(if (isSwahili) cat.labelSwahili else cat.labelEn) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val filtered = if (activeFilter == null) opportunities else opportunities.filter { it.category == activeFilter }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { opp ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (opp.category == OpportunityCategory.MARKET_BLOCKED) DangerRed else BorderSubtle
                    ),
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
                                Text(
                                    text = opp.symbol,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                opp.recommendedSide?.let { side ->
                                    Surface(
                                        color = if (side == OrderSide.BUY) AccentNeonGreen.copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = side.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (side == OrderSide.BUY) AccentNeonGreen else DangerRed,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Score: ${opp.compositeOpportunityScore}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (opp.compositeOpportunityScore > 80) AccentNeonGreen else AccentCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isSwahili) opp.swahiliSummary else opp.primarySetupSummary,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spread: ${opp.spreadQualityBps} bps • R:R 1:${opp.expectedRewardRiskRatio} • Kina: \$${opp.liquidityDepthMillions.roundToInt()}M",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )

                            if (opp.category != OpportunityCategory.MARKET_BLOCKED && opp.recommendedSide != null) {
                                Button(
                                    onClick = { onExecuteSetup(opp.symbol, opp.recommendedSide) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (isSwahili) "Fanya Biashara" else "Trade Setup",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// TAB 3: FULL ML TRAINING & MODEL REGISTRY TAB
// ----------------------------------------------------------------------------

@Composable
private fun MlTrainingRegistryTabContent(
    isSwahili: Boolean,
    models: List<ModelRegistryEntry>,
    pipelineEngine: FullMlTrainingPipelineEngine,
    onTriggerRetrain: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Pipeline Roadmap
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlow),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isSwahili) "MKONDO KAMILI WA MAFUNZO YA ML (TRAINING PIPELINE)" else "COMPLETE ML TRAINING & VALIDATION PIPELINE",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "DATA → CLEANING → FEATURE ENG → LABELING → TRAINING → VALIDATION → BACKTEST → WALK-FORWARD → OUT-OF-SAMPLE → PAPER/SHADOW → SUPERVISOR APPROVAL → LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = AccentGold
                    )
                }
            }
        }

        item {
            Text(
                text = if (isSwahili) "Daftari Rasmi la Mifano ya AI (Model Registry)" else "Official Model Registry & Verified Metrics",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        items(models) { m ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (m.isLiveActive) AccentNeonGreen else BorderSubtle
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = m.modelName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text(text = "Ver: ${m.version} • Arch: ${m.architecture}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }

                        Surface(
                            color = if (m.isLiveActive) AccentNeonGreen.copy(alpha = 0.2f) else AccentCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = m.lifecycleStatus.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (m.isLiveActive) AccentNeonGreen else AccentCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricPill("Sharpe", "${m.tradingMetrics.sharpeRatio}", AccentGold)
                        MetricPill("Sortino", "${m.tradingMetrics.sortinoRatio}", AccentLavender)
                        MetricPill("Profit Factor", "${m.tradingMetrics.profitFactor}x", AccentNeonGreen)
                        MetricPill("Max DD", "${m.tradingMetrics.maxDrawdownPct}%", DangerRed)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricPill("Win Rate", "${m.tradingMetrics.winRatePct}%", AccentCyan)
                        MetricPill("Walk-Forward", "${m.walkForwardEfficiencyPct}%", AccentEmerald)
                        MetricPill("Monte Carlo", "${m.monteCarloSurvivalPct}%", AccentNeonGreen)
                        MetricPill("ROC-AUC", "${m.classificationMetrics.rocAuc}", AccentGold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Notes: ${m.supervisorApprovalNotes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { onTriggerRetrain(m.modelId) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (isSwahili) "Mafunzo ya Moja kwa Moja (Online Train)" else "Online Retrain",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// HELPER COMPOSABLES
// ----------------------------------------------------------------------------

@Composable
private fun MetricPill(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun PredictionField(title: String, content: String, color: Color) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AccentCyan)
        Text(text = content, style = MaterialTheme.typography.bodySmall, color = color)
    }
}
