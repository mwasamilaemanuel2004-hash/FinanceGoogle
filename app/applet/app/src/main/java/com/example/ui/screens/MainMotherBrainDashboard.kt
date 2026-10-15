package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguageMode
import com.example.engine.MotherBrainEngine
import com.example.ui.components.*
import com.example.ui.components.OnlineStatusBar
import com.example.ui.navigation.EsmhDestination
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMotherBrainDashboard(
    currentTheme: DashboardThemeMode = DashboardThemeMode.NAVY_ELECTRIC_BLUE,
    onCycleTheme: () -> Unit = {},
    onSelectTheme: ((DashboardThemeMode) -> Unit)? = null
) {
    val context = LocalContext.current
    val engine = remember { MotherBrainEngine.getInstance(context) }
    val telemetry by engine.telemetry.collectAsState()
    val isSwahili = telemetry.languageMode == AppLanguageMode.SWAHILI_IMARA

    var currentDestination by remember { mutableStateOf(EsmhDestination.HOME) }
    var activeTradeSymbol by remember { mutableStateOf("BTC/USDT") }
    // Fullscreen enabled by default as requested by user
    var isAppFullscreen by remember { mutableStateOf(true) }

    // Dialog state
    var showBacktestDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showTokenDialog by remember { mutableStateOf(false) }

    val authManager = remember { com.example.engine.AuthAndAccessManager.getInstance(context) }
    val currentUser by authManager.currentUser.collectAsState()
    val connectors by engine.connectors.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = TextWhite,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header inside drawer
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ESMH.TRADE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = AccentCyan
                            )
                            Text(
                                text = if (isSwahili) "Ubongo wa Kiasi" else "Quant Strategy Suite",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Destinations
                    val destinations = listOf(
                        Triple(EsmhDestination.HOME, if (isSwahili) "Mwanzo (Home)" else "Home Dashboard", Icons.Default.Home),
                        Triple(EsmhDestination.TRADE, if (isSwahili) "Terminal ya Trade" else "Trade Terminal", Icons.Default.TrendingUp),
                        Triple(EsmhDestination.MARKETS, if (isSwahili) "Orodha ya Masoko" else "Market Streams", Icons.Default.List),
                        Triple(EsmhDestination.BOTS, if (isSwahili) "Boti na Wakala" else "Autonomous Bots", Icons.Default.SmartToy),
                        Triple(EsmhDestination.MOTHER_BRAIN, if (isSwahili) "Ubongo Mkuu" else "Mother-Brain Hub", Icons.Default.Psychology),
                        Triple(EsmhDestination.PORTFOLIO, if (isSwahili) "Mali na Salio" else "Asset Portfolio", Icons.Default.AccountBalanceWallet),
                        Triple(EsmhDestination.RISK_CENTER, if (isSwahili) "Kituo cha Riski" else "Hard-Risk Center", Icons.Default.Shield),
                        Triple(EsmhDestination.SYSTEM_HEALTH, if (isSwahili) "Afya ya Mfumo" else "System Health", Icons.Default.QueryStats),
                        Triple(EsmhDestination.SETTINGS, if (isSwahili) "Mipangilio API" else "API Integrations", Icons.Default.Settings),
                        Triple(EsmhDestination.BACKTEST_LAB, if (isSwahili) "🧪 Maabara ya Pima" else "🧪 Backtest Lab", Icons.Default.Assessment),
                        Triple(EsmhDestination.QUANT_LAB, if (isSwahili) "🧬 Maabara ya Quant" else "🧬 Quant Lab", Icons.Default.Science),
                        Triple(EsmhDestination.QUANTUM_FINANCE, if (isSwahili) "⚛️ Quantum Finance & 24/6" else "⚛️ Quantum Finance & 24/6", Icons.Default.AllInclusive),
                        Triple(EsmhDestination.INFINITE_CLUSTERS, if (isSwahili) "🌐 50K+ Pairs & Clusters" else "🌐 50K+ Pairs & Clusters", Icons.Default.Hub)
                    ).let { baseList ->
                        if (currentUser?.role == com.example.data.model.UserRole.ADMIN) {
                            baseList + Triple(EsmhDestination.ADMIN_COMMAND, if (isSwahili) "👑 Kituo cha Admin" else "👑 Admin Command", Icons.Default.AdminPanelSettings)
                        } else baseList
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        destinations.forEach { triple ->
                            val dest = triple.first
                            val label = triple.second
                            val icon = triple.third
                            val isSelected = currentDestination == dest || 
                                (dest == EsmhDestination.BOTS && currentDestination == EsmhDestination.COMMANDERS) ||
                                (dest == EsmhDestination.MOTHER_BRAIN && currentDestination == EsmhDestination.OPPORTUNITIES)

                            NavigationDrawerItem(
                                icon = { Icon(icon, contentDescription = null, tint = if (isSelected) AccentGold else TextMuted) },
                                label = { Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
                                selected = isSelected,
                                onClick = {
                                    currentDestination = dest
                                    coroutineScope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = AccentCyan.copy(alpha = 0.12f),
                                    selectedIconColor = AccentGold,
                                    selectedTextColor = AccentCyan,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextWhite
                                ),
                                modifier = Modifier.height(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Contact/Footer inside drawer
                    Text(
                        text = "WhatsApp Support:\n+255 625 989 646",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentGold.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                if (!isAppFullscreen) {
                Column {
                    OnlineStatusBar(
                        isConnected = telemetry.systemStatus == "LIVE / ONLINE",
                        isApiReady = connectors.any { it.status == "CONNECTED / ONLINE" },
                        balance = telemetry.totalEquityUsd
                    )
                    EsmhGlobalHeader(
                        telemetry = telemetry,
                        isSwahili = isSwahili,
                        currentTheme = currentTheme,
                        onToggleLanguage = { engine.toggleLanguage() },
                        onCycleTheme = onCycleTheme,
                        onEmergencyHaltToggle = {
                            if (telemetry.emergencyStopActive) {
                                engine.resetEmergencyHalt()
                            } else {
                                engine.triggerEmergencyHalt("Manual Kill Switch Triggered")
                            }
                        },
                        onOpenConnectors = { currentDestination = EsmhDestination.SETTINGS },
                        onOpenBacktest = { currentDestination = EsmhDestination.BACKTEST_LAB },
                        onOpenExport = { showExportDialog = true },
                        onOpenAuth = { showAuthDialog = true },
                        onOpenToken = { showTokenDialog = true },
                        onOpenAdmin = {
                            if (currentUser?.role == com.example.data.model.UserRole.ADMIN) {
                                currentDestination = EsmhDestination.ADMIN_COMMAND
                            } else {
                                showAuthDialog = true
                            }
                        },
                        onToggleFullscreen = { isAppFullscreen = !isAppFullscreen },
                        isFullscreen = isAppFullscreen,
                        onOpenDrawer = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (!isAppFullscreen) {
                    EsmhBottomNav(
                        currentDestination = currentDestination,
                        isSwahili = isSwahili,
                        onNavigate = { currentDestination = it }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isAppFullscreen) PaddingValues(0.dp) else innerPadding)
            ) {
                when (currentDestination) {
                    EsmhDestination.HOME -> {
                        HomeScreen(
                            engine = engine,
                            isSwahili = isSwahili,
                            selectedSymbol = activeTradeSymbol,
                            onSelectSymbol = { sym ->
                                activeTradeSymbol = sym
                            },
                            onNavigateToTrade = { sym ->
                                activeTradeSymbol = sym
                                currentDestination = EsmhDestination.TRADE
                            },
                            onNavigateToSettings = { currentDestination = EsmhDestination.SETTINGS }
                        )
                    }
                    EsmhDestination.TRADE -> {
                        TradeTerminalScreen(
                            engine = engine,
                            isSwahili = isSwahili,
                            initialSymbol = activeTradeSymbol,
                            onNavigateToSettings = { currentDestination = EsmhDestination.SETTINGS }
                        )
                    }
                    EsmhDestination.MARKETS -> {
                        MarketsScreen(
                            engine = engine,
                            isSwahili = isSwahili,
                            onSelectMarketSymbol = { sym ->
                                activeTradeSymbol = sym
                                currentDestination = EsmhDestination.TRADE
                            }
                        )
                    }
                    EsmhDestination.BOTS, EsmhDestination.COMMANDERS -> {
                        BotsMonitoringScreen(
                            engine = engine,
                            isSwahili = isSwahili,
                            onOpenBacktest = { currentDestination = EsmhDestination.BACKTEST_LAB }
                        )
                    }
                    EsmhDestination.MOTHER_BRAIN, EsmhDestination.OPPORTUNITIES -> {
                        MotherBrainConsoleScreen(
                            engine = engine,
                            isSwahili = isSwahili
                        )
                    }
                    EsmhDestination.PORTFOLIO -> {
                        PortfolioScreen(
                            engine = engine,
                            isSwahili = isSwahili
                        )
                    }
                    EsmhDestination.RISK_CENTER -> {
                        RiskCenterScreen(
                            engine = engine,
                            isSwahili = isSwahili
                        )
                    }
                    EsmhDestination.SYSTEM_HEALTH -> {
                        SystemHealthScreen(
                            engine = engine,
                            isSwahili = isSwahili
                        )
                    }
                    EsmhDestination.SETTINGS -> {
                        SettingsScreen(
                            engine = engine,
                            currentTheme = currentTheme,
                            isSwahili = isSwahili,
                            onSelectTheme = { onSelectTheme?.invoke(it) }
                        )
                    }
                    EsmhDestination.BACKTEST_LAB -> {
                        BacktestLabScreen(
                            engine = engine,
                            isSwahili = isSwahili,
                            onNavigateBack = { currentDestination = EsmhDestination.HOME }
                        )
                    }
                    EsmhDestination.QUANT_LAB -> {
                        QuantLabScreen(
                            engine = engine,
                            isSwahili = isSwahili
                        )
                    }
                    EsmhDestination.QUANTUM_FINANCE -> {
                        QuantumFinanceScreen(
                            isSwahili = isSwahili,
                            onNavigateBack = { currentDestination = EsmhDestination.HOME }
                        )
                    }
                    EsmhDestination.INFINITE_CLUSTERS -> {
                        InfinitePairsClusteringScreen(
                            engine = engine,
                            isSwahili = isSwahili
                        )
                    }
                    EsmhDestination.ADMIN_COMMAND -> {
                        AdminCommandCenterScreen(
                            isSwahili = isSwahili,
                            onNavigateBack = { currentDestination = EsmhDestination.HOME }
                        )
                    }
                }

                // Universal User Assistant Overlay
                UniversalUserAssistantOverlay(
                    isSwahili = isSwahili,
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Fullscreen Toolbar (shown in Fullscreen mode for convenient control)
                if (isAppFullscreen) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Menu / Drawer Trigger
                        Surface(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            },
                            color = Color.Black.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AccentGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = AccentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Menu",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Fullscreen Status & Toggle Pill
                        Surface(
                            onClick = { isAppFullscreen = false },
                            color = Color.Black.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF00D4FF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FullscreenExit,
                                    contentDescription = "Exit Fullscreen",
                                    tint = Color(0xFF00D4FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSwahili) "Full Screen (Imara)" else "Full Screen (Active)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00D4FF),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showBacktestDialog) {
        BacktestLabDialog(
            isSwahili = isSwahili,
            onDismiss = { showBacktestDialog = false },
            onOpenFullLab = { currentDestination = EsmhDestination.BACKTEST_LAB }
        )
    }
    if (showExportDialog) {
        ExportAndDownloadCenterDialog(
            isSwahili = isSwahili,
            onDismiss = { showExportDialog = false }
        )
    }
    if (showAuthDialog) {
        AuthSignInSignUpDialog(
            isSwahili = isSwahili,
            onDismiss = { showAuthDialog = false },
            onAuthSuccess = {
                showAuthDialog = false
                if (currentUser?.role == com.example.data.model.UserRole.ADMIN) {
                    currentDestination = EsmhDestination.ADMIN_COMMAND
                }
            }
        )
    }
    if (showTokenDialog) {
        UserTokenActivationDialog(
            isSwahili = isSwahili,
            onDismiss = { showTokenDialog = false }
        )
    }
}
