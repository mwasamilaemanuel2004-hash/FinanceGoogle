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
import com.example.ui.components.OnlineStatusBar
import com.example.ui.components.EsmhGlobalHeader
import com.example.ui.components.EsmhBottomNav
import com.example.ui.components.UniversalUserAssistantOverlay
import com.example.ui.theme.*
import com.example.ui.navigation.EsmhDestination
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
    var isAppFullscreen by remember { mutableStateOf(true) }

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
                        }
                    }
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Destinations", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    
                    val destinations = listOf(
                        Triple(EsmhDestination.HOME, "Home Dashboard", Icons.Default.Home),
                        Triple(EsmhDestination.TRADE, "Trade Terminal", Icons.Default.TrendingUp),
                        Triple(EsmhDestination.MARKETS, "Market Streams", Icons.Default.List),
                        Triple(EsmhDestination.BOTS, "Autonomous Bots", Icons.Default.SmartToy),
                        Triple(EsmhDestination.MOTHER_BRAIN, "Mother-Brain Hub", Icons.Default.Psychology),
                        Triple(EsmhDestination.PORTFOLIO, "Asset Portfolio", Icons.Default.AccountBalanceWallet),
                        Triple(EsmhDestination.RISK_CENTER, "Hard-Risk Center", Icons.Default.Shield),
                        Triple(EsmhDestination.SYSTEM_HEALTH, "System Health", Icons.Default.QueryStats),
                        Triple(EsmhDestination.SETTINGS, "API Integrations", Icons.Default.Settings),
                        Triple(EsmhDestination.BACKTEST_LAB, "Backtest Lab", Icons.Default.Assessment),
                        Triple(EsmhDestination.QUANT_LAB, "Quant Lab", Icons.Default.Science)
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        destinations.forEach { triple ->
                            val dest = triple.first
                            val label = triple.second
                            val icon = triple.third
                            val isSelected = currentDestination == dest

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
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentDestination) {
                        EsmhDestination.HOME -> Text("Home")
                        EsmhDestination.TRADE -> Text("Trade")
                        else -> Text("Destination: $currentDestination")
                    }
                }
            }
    }
}
