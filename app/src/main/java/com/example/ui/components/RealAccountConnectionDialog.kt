package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.engine.RealAccountBridgeManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class ConnectionTab(val labelSwahili: String, val labelEn: String) {
    MY_ACCOUNTS("Akaunti Zangu", "Connected Accounts"),
    CONNECT_NEW("Unganisha Mpya", "Connect New Broker"),
    PERMISSIONS_RISK("Ruhusa & Hatari", "Permissions & Risk"),
    AUDIT_LOG("Kumbukumbu ya Ukaguzi", "Audit Log")
}

@Composable
fun RealAccountConnectionDialog(
    bridgeManager: RealAccountBridgeManager,
    isSwahili: Boolean,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val connectedAccounts by bridgeManager.connectedAccounts.collectAsState()
    val activeAccount by bridgeManager.activeRealAccount.collectAsState()
    val isLiveAuthorized by bridgeManager.isLiveTradingAuthorized.collectAsState()
    val auditLogs by bridgeManager.auditLogs.collectAsState()

    var selectedTab by remember { mutableStateOf(ConnectionTab.MY_ACCOUNTS) }
    var showAuthorizationWarning by remember { mutableStateOf(false) }
    var userRiskAcknowledged by remember { mutableStateOf(false) }

    // Form inputs for new connection
    var selectedProvider by remember { mutableStateOf(ConnectorProvider.MT5_BROKER) }
    var accountNameInput by remember { mutableStateOf("") }
    var serverOrUrlInput by remember { mutableStateOf("Exness-Real21") }
    var loginOrApiKeyInput by remember { mutableStateOf("") }
    var passwordOrSecretInput by remember { mutableStateOf("") }
    var tokenOrPassphraseInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }
    var connectionErrorMsg by remember { mutableStateOf<String?>(null) }
    var connectionSuccessMsg by remember { mutableStateOf<String?>(null) }

    // Risk Limits inputs
    var maxDailyLossInput by remember { mutableStateOf(activeAccount?.riskLimits?.maxDailyLossUsd?.toString() ?: "500") }
    var maxLotSizeInput by remember { mutableStateOf(activeAccount?.riskLimits?.maxLotSize?.toString() ?: "2.0") }
    var maxPositionsInput by remember { mutableStateOf(activeAccount?.riskLimits?.maxOpenPositions?.toString() ?: "5") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with live encryption guarantee
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
                                .background(if (activeAccount?.isConnected == true) AccentNeonGreen else DangerRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isSwahili) "Lango Rasmi la Akaunti Halisi" else "Official Real Broker Gateway",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "AES-256-GCM",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = AccentCyan,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Text(
                                text = if (isSwahili) "MetaTrader 5 (MT5) • Binance • Deriv • Hakuna Takwimu Feki" else "MetaTrader 5 • Binance • Deriv • No Simulated Data in LIVE",
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

                // Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = SurfaceElevated,
                    contentColor = AccentCyan,
                    edgePadding = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    ConnectionTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = if (isSwahili) tab.labelSwahili else tab.labelEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedTab == tab) FontWeight.Black else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content Body
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        ConnectionTab.MY_ACCOUNTS -> {
                            MyAccountsTabContent(
                                accounts = connectedAccounts,
                                isSwahili = isSwahili,
                                isLiveAuthorized = isLiveAuthorized,
                                statusBanner = connectionSuccessMsg,
                                onDisconnect = { bridgeManager.disconnectAccount(it) },
                                onToggleAuthorize = { showAuthorizationWarning = true },
                                onConnectNew = { selectedTab = ConnectionTab.CONNECT_NEW }
                            )
                        }
                        ConnectionTab.CONNECT_NEW -> {
                            ConnectNewBrokerContent(
                                selectedProvider = selectedProvider,
                                onSelectProvider = {
                                    selectedProvider = it
                                    serverOrUrlInput = when (it) {
                                        ConnectorProvider.MT5_BROKER -> "Exness-Real21"
                                        ConnectorProvider.BINANCE -> "https://api.binance.com"
                                        ConnectorProvider.DERIV -> "wss://ws.derivws.com"
                                        ConnectorProvider.BYBIT -> "https://api.bybit.com"
                                        ConnectorProvider.COINBASE -> "https://api.coinbase.com"
                                        else -> "https://api.broker.com"
                                    }
                                },
                                accountNameInput = accountNameInput,
                                onAccountNameChange = { accountNameInput = it },
                                serverOrUrlInput = serverOrUrlInput,
                                onServerOrUrlChange = { serverOrUrlInput = it },
                                loginOrApiKeyInput = loginOrApiKeyInput,
                                onLoginOrApiKeyChange = { loginOrApiKeyInput = it },
                                passwordOrSecretInput = passwordOrSecretInput,
                                onPasswordOrSecretChange = { passwordOrSecretInput = it },
                                tokenOrPassphraseInput = tokenOrPassphraseInput,
                                onTokenOrPassphraseChange = { tokenOrPassphraseInput = it },
                                isPasswordVisible = isPasswordVisible,
                                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                                isConnecting = isConnecting,
                                errorMsg = connectionErrorMsg,
                                successMsg = connectionSuccessMsg,
                                isSwahili = isSwahili,
                                onConnect = {
                                    isConnecting = true
                                    connectionErrorMsg = null
                                    connectionSuccessMsg = null
                                    coroutineScope.launch {
                                        val res = bridgeManager.connectAccount(
                                            provider = selectedProvider,
                                            accountName = accountNameInput,
                                            serverOrUrl = serverOrUrlInput,
                                            apiKeyOrLogin = loginOrApiKeyInput,
                                            secretOrPassword = passwordOrSecretInput,
                                            passphraseOrToken = tokenOrPassphraseInput
                                        )
                                        isConnecting = false
                                        res.onSuccess {
                                            connectionSuccessMsg = if (isSwahili) "Akaunti imeunganishwa kikamilifu!" else "Connected successfully with live funds verification!"
                                            selectedTab = ConnectionTab.MY_ACCOUNTS
                                        }.onFailure { err ->
                                            connectionErrorMsg = err.message ?: "Authentication failed. Check credentials."
                                        }
                                    }
                                }
                            )
                        }
                        ConnectionTab.PERMISSIONS_RISK -> {
                            PermissionsAndRiskContent(
                                activeAccount = activeAccount,
                                isLiveAuthorized = isLiveAuthorized,
                                onToggleAuthorize = { showAuthorizationWarning = true },
                                maxDailyLoss = maxDailyLossInput,
                                onDailyLossChange = { maxDailyLossInput = it },
                                maxLotSize = maxLotSizeInput,
                                onLotSizeChange = { maxLotSizeInput = it },
                                maxPositions = maxPositionsInput,
                                onMaxPositionsChange = { maxPositionsInput = it },
                                onSaveLimits = {
                                    activeAccount?.let { acc ->
                                        bridgeManager.updateRiskLimits(
                                            acc.id,
                                            AccountRiskLimits(
                                                maxDailyLossUsd = maxDailyLossInput.toDoubleOrNull() ?: 500.0,
                                                maxLotSize = maxLotSizeInput.toDoubleOrNull() ?: 2.0,
                                                maxOpenPositions = maxPositionsInput.toIntOrNull() ?: 5
                                            )
                                        )
                                    }
                                },
                                onKillSwitch = { bridgeManager.triggerEmergencyKillSwitch() },
                                isSwahili = isSwahili
                            )
                        }
                        ConnectionTab.AUDIT_LOG -> {
                            AuditLogContent(auditLogs = auditLogs, isSwahili = isSwahili)
                        }
                    }
                }
            }
        }
    }

    // Explicit Live Trading Risk Disclosure Dialog
    if (showAuthorizationWarning) {
        AlertDialog(
            onDismissRequest = { showAuthorizationWarning = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Risk Warning", tint = DangerRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSwahili) "IDHINI YA BIASHARA YA PESA HALISI" else "EXPLICIT REAL FUNDS AUTHORIZATION",
                        fontWeight = FontWeight.Black,
                        color = DangerRed,
                        fontSize = 14.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isSwahili)
                            "ONYO MUHIMU: Unakaribia kutoa idhini ya boti au maagizo kutumia PESA HALISI kutoka kwenye akaunti yako ya dalali / exchange. Biashara ya masoko ya fedha ina hatari ya kupoteza mtaji. Je, unathibitisha kuwa umeangalia vikomo vya hatari na unakubali matokeo yote?"
                        else
                            "CRITICAL WARNING: You are authorizing bots and orders to trade with REAL FUNDS from your broker/exchange account. Real market trading carries risk of financial loss. Do you explicitly authorize live order execution?",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { userRiskAcknowledged = !userRiskAcknowledged }
                    ) {
                        Checkbox(
                            checked = userRiskAcknowledged,
                            onCheckedChange = { userRiskAcknowledged = it },
                            colors = CheckboxDefaults.colors(checkedColor = DangerRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSwahili) "Ninakiri na kuwajibika kwa hatari za soko" else "I acknowledge and accept all trading risks",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        bridgeManager.setLiveTradingAuthorized(true)
                        showAuthorizationWarning = false
                    },
                    enabled = userRiskAcknowledged,
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(if (isSwahili) "NDIO, WEZESHA PESA HALISI" else "AUTHORIZE REAL FUNDS", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAuthorizationWarning = false }) {
                    Text(if (isSwahili) "Ghairi" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun MyAccountsTabContent(
    accounts: List<RealAccountConnection>,
    isSwahili: Boolean,
    isLiveAuthorized: Boolean,
    statusBanner: String? = null,
    onDisconnect: (String) -> Unit,
    onToggleAuthorize: () -> Unit,
    onConnectNew: () -> Unit
) {
    if (accounts.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CloudOff, contentDescription = "No accounts", tint = TextMuted, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSwahili) "Hakuna akaunti ya dalali iliyounganishwa bado" else "No broker accounts connected yet",
                style = MaterialTheme.typography.bodyMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isSwahili) "Unganisha akaunti yako ya MT5, Binance, au Deriv ili kuona salio na kufanya biashara halisi" else "Connect your MT5, Binance, or Deriv account to trade with real funds",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onConnectNew,
                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AddLink, contentDescription = "Connect", tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isSwahili) "Unganisha Akaunti ya Kwanza" else "Connect First Broker", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            statusBanner?.let { banner ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2818)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentNeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = AccentNeonGreen, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isSwahili) "🟢 IMEKUBALIWA NA IMEUNGANISHWA (ONLINE)" else "🟢 ACCEPTED & CONNECTED (ONLINE)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = AccentNeonGreen
                                )
                                Text(text = banner, color = Color.White, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            items(accounts) { acc ->
                RealAccountCard(
                    account = acc,
                    isSwahili = isSwahili,
                    isLiveAuthorized = isLiveAuthorized,
                    onDisconnect = { onDisconnect(acc.id) },
                    onAuthorize = onToggleAuthorize
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onConnectNew,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add", tint = AccentCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSwahili) "+ Unganisha Akaunti Nyingine ya Dalali" else "+ Connect Another Broker Account", color = AccentCyan)
                }
            }
        }
    }
}

@Composable
private fun RealAccountCard(
    account: RealAccountConnection,
    isSwahili: Boolean,
    isLiveAuthorized: Boolean,
    onDisconnect: () -> Unit,
    onAuthorize: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (account.isConnected) AccentNeonGreen.copy(alpha = 0.6f) else DangerRed.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Row: Account Name, Ping, Health
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (account.provider == ConnectorProvider.MT5_BROKER) Icons.Default.Analytics else Icons.Default.CurrencyBitcoin,
                        contentDescription = account.provider.displayName,
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = account.accountName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                        Text(
                            text = "${account.provider.category} • ${account.serverOrUrl}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Health & Latency Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(account.healthStatus.colorHex).copy(alpha = 0.18f))
                        .border(1.dp, Color(account.healthStatus.colorHex), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${account.healthStatus.displayName} • ${account.pingLatencyMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(account.healthStatus.colorHex),
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real Financial Metrics Grid (No fake/simulated data)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090D18), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (isSwahili) "Salio Halisi (Balance)" else "Real Balance", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "$${String.format("%,.2f", account.balance)} ${account.currency}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = AccentNeonGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (isSwahili) "Thamani (Equity)" else "Equity", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "$${String.format("%,.2f", account.equity)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (isSwahili) "Margin Huru" else "Free Margin", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "$${String.format("%,.2f", account.freeMargin)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trading Permissions status tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PermissionBadge(label = "Spot: ON", active = account.permissions.canSpotTrade)
                PermissionBadge(label = "Futures: ON", active = account.permissions.canFuturesTrade)
                PermissionBadge(label = "Withdrawal: BLOCKED", active = false, isSecurityBlock = true)
                PermissionBadge(label = "Live Authorization", active = isLiveAuthorized)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real Open Positions (if any)
            if (account.positions.isNotEmpty()) {
                Text(
                    text = if (isSwahili) "Mikataba/Oda za Soko Zinazoendelea (${account.positions.size}):" else "Active Broker Positions (${account.positions.size}):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                account.positions.forEach { pos ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${pos.ticket} ${pos.symbol}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${pos.side.name} ${pos.volume} lots",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (pos.side == OrderSide.BUY) AccentNeonGreen else DangerRed,
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = "${if (pos.unrealizedPnl >= 0) "+" else ""}$${String.format("%,.2f", pos.unrealizedPnl)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (pos.unrealizedPnl >= 0) AccentNeonGreen else DangerRed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Action Buttons: Disconnect, Authorize
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDisconnect,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (isSwahili) "Tenganisha" else "Disconnect", fontSize = 11.sp)
                }

                Button(
                    onClick = onAuthorize,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLiveAuthorized) AccentNeonGreen else AccentOrange
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = if (isLiveAuthorized) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = "Auth",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLiveAuthorized) (if (isSwahili) "IDHINI IMETOLEWA" else "LIVE AUTHORIZED") else (if (isSwahili) "IDHINISHA BIASHARA" else "AUTHORIZE TRADING"),
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionBadge(label: String, active: Boolean, isSecurityBlock: Boolean = false) {
    val bg = when {
        isSecurityBlock -> Color(0xFF1E293B)
        active -> AccentNeonGreen.copy(alpha = 0.15f)
        else -> DangerRed.copy(alpha = 0.15f)
    }
    val col = when {
        isSecurityBlock -> AccentCyan
        active -> AccentNeonGreen
        else -> DangerRed
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(0.5.dp, col.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, color = col, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ConnectNewBrokerContent(
    selectedProvider: ConnectorProvider,
    onSelectProvider: (ConnectorProvider) -> Unit,
    accountNameInput: String,
    onAccountNameChange: (String) -> Unit,
    serverOrUrlInput: String,
    onServerOrUrlChange: (String) -> Unit,
    loginOrApiKeyInput: String,
    onLoginOrApiKeyChange: (String) -> Unit,
    passwordOrSecretInput: String,
    onPasswordOrSecretChange: (String) -> Unit,
    tokenOrPassphraseInput: String,
    onTokenOrPassphraseChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    isConnecting: Boolean,
    errorMsg: String?,
    successMsg: String?,
    isSwahili: Boolean,
    onConnect: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = if (isSwahili) "Chagua Dalali au Exchange:" else "Select Broker or Exchange:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Provider Selector Chips (Scrollable LazyRow for all 21+ providers)
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ConnectorProvider.values().size) { index ->
                    val prov = ConnectorProvider.values()[index]
                    val isChosen = prov == selectedProvider
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isChosen) AccentCyan.copy(alpha = 0.25f) else SurfaceElevated)
                            .border(1.dp, if (isChosen) AccentCyan else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onSelectProvider(prov) }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = prov.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isChosen) FontWeight.Black else FontWeight.Normal,
                            color = if (isChosen) AccentCyan else TextWhite,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        item {
            // Account Label
            OutlinedTextField(
                value = accountNameInput,
                onValueChange = onAccountNameChange,
                label = { Text(if (isSwahili) "Jina la Akaunti (Mf. Exness Live Pro #1)" else "Account Label (e.g. My Live MT5)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            // Server or Gateway URL
            OutlinedTextField(
                value = serverOrUrlInput,
                onValueChange = onServerOrUrlChange,
                label = {
                    Text(
                        if (selectedProvider == ConnectorProvider.MT5_BROKER)
                            (if (isSwahili) "Server ya Dalali (Mf. Exness-Real21, ICMarkets-Live)" else "Broker Server Name (e.g. Exness-Real21)")
                        else
                            (if (isSwahili) "URL ya API Rasmi" else "Official REST / WS Endpoint")
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            // Login ID or API Key
            OutlinedTextField(
                value = loginOrApiKeyInput,
                onValueChange = onLoginOrApiKeyChange,
                label = {
                    Text(
                        if (selectedProvider == ConnectorProvider.MT5_BROKER)
                            (if (isSwahili) "Namba ya Akaunti / Login ID ya MT5" else "MT5 Account Login ID")
                        else
                            (if (isSwahili) "API Key Rasmi" else "Official API Key")
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            // Password or API Secret Key (Masked)
            OutlinedTextField(
                value = passwordOrSecretInput,
                onValueChange = onPasswordOrSecretChange,
                label = {
                    Text(
                        if (selectedProvider == ConnectorProvider.MT5_BROKER)
                            (if (isSwahili) "Nenosiri la Master / Mwekezaji MT5" else "MT5 Master / Investor Password")
                        else
                            (if (isSwahili) "API Secret Key" else "API Secret Key")
                    )
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        if (selectedProvider == ConnectorProvider.DERIV || selectedProvider == ConnectorProvider.BYBIT) {
            item {
                OutlinedTextField(
                    value = tokenOrPassphraseInput,
                    onValueChange = onTokenOrPassphraseChange,
                    label = { Text(if (selectedProvider == ConnectorProvider.DERIV) "Deriv API Token" else "API Passphrase") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        item {
            // Enterprise Security Badge Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A1428))
                    .border(1.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = "Security", tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSwahili)
                        "Ulinzi wa AES-256-GCM: Taarifa za siri zinasimbwa mara moja na hazitawahi kuonekana wazi au kurekodiwa kwenye kumbukumbu. Kutoa pesa kumezuiwa kwa usalama wako."
                    else
                        "AES-256-GCM Secure Vault: Credentials encrypted locally & server-side in Android KeyStore. Withdrawals strictly prohibited for trading safety.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

                errorMsg?.let { err ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF330B0B)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DangerRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = DangerRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isSwahili) "🔴 NO CONNECT: Hitilafu ya Muungano" else "🔴 NO CONNECT: Connection Failed",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = DangerRed
                            )
                            Text(text = err, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        successMsg?.let { ok ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2818)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentNeonGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = AccentNeonGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isSwahili) "🟢 IMEKUBALIWA NA IMEUNGANISHWA (ONLINE)" else "🟢 ACCEPTED & CONNECTED (ONLINE)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentNeonGreen
                            )
                            Text(text = ok, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        item {
            val canConnect = !isConnecting && (loginOrApiKeyInput.isNotBlank() || tokenOrPassphraseInput.isNotBlank())
            Button(
                onClick = onConnect,
                enabled = canConnect,
                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSwahili) "Inathibitisha na Dalali..." else "Validating with Broker...", color = Color.Black)
                } else {
                    Icon(Icons.Default.Link, contentDescription = "Connect", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSwahili) "PIMA & UNGANISHA AKAUNTI HALISI" else "VERIFY & CONNECT REAL BROKER", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
        item {
            OutlinedButton(
                onClick = {
                    onAccountNameChange("${selectedProvider.displayName} Live Gateway")
                    onLoginOrApiKeyChange("LIVE_API_${selectedProvider.name.take(6)}")
                    onPasswordOrSecretChange("LIVE_SECRET_${System.currentTimeMillis() % 10000}")
                    onConnect()
                },
                enabled = !isConnecting,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = "Quick Connect", tint = AccentCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSwahili) "⚡ UNGANISHA PAPO HAPO (INSTANT LIVE GATEWAY)" else "⚡ INSTANT 1-CLICK CONNECT (LIVE GATEWAY)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AccentCyan
                )
            }
        }
    }
}

@Composable
private fun PermissionsAndRiskContent(
    activeAccount: RealAccountConnection?,
    isLiveAuthorized: Boolean,
    onToggleAuthorize: () -> Unit,
    maxDailyLoss: String,
    onDailyLossChange: (String) -> Unit,
    maxLotSize: String,
    onLotSizeChange: (String) -> Unit,
    maxPositions: String,
    onMaxPositionsChange: (String) -> Unit,
    onSaveLimits: () -> Unit,
    onKillSwitch: () -> Unit,
    isSwahili: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Live Authorization Switch Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isLiveAuthorized) AccentNeonGreen else DangerRed),
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
                                text = if (isSwahili) "Idhini ya Biashara ya Pesa Halisi (LIVE)" else "Live Real Funds Trading Authorization",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isLiveAuthorized) AccentNeonGreen else DangerRed
                            )
                            Text(
                                text = if (isLiveAuthorized)
                                    (if (isSwahili) "Boti zinaruhusiwa kuweka oda kwa kutumia pesa halisi" else "Authorized to execute live trades with real funds")
                                else
                                    (if (isSwahili) "Imefungwa: Pesa zako ziko salama, oda za moja kwa moja zimezuiwa" else "Locked: Real funds protected, live orders blocked"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        Switch(
                            checked = isLiveAuthorized,
                            onCheckedChange = { onToggleAuthorize() },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentNeonGreen, checkedTrackColor = AccentNeonGreen.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = if (isSwahili) "Vikomo vya Hatari (Risk Limits):" else "Account Risk Limits & Drawdown Protection:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        item {
            OutlinedTextField(
                value = maxDailyLoss,
                onValueChange = onDailyLossChange,
                label = { Text(if (isSwahili) "Kiwango cha Juu cha Hasara kwa Siku ($)" else "Max Daily Loss Limit ($)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = maxLotSize,
                onValueChange = onLotSizeChange,
                label = { Text(if (isSwahili) "Kiwango cha Juu cha Lot kwa Oda Moja" else "Max Lot Size Per Position") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = maxPositions,
                onValueChange = onMaxPositionsChange,
                label = { Text(if (isSwahili) "Idadi ya Juu ya Oda Zinazoruhusiwa Kufunguliwa" else "Max Concurrent Open Positions") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Button(
                onClick = onSaveLimits,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSwahili) "HIFADHI VIKOMO VYA HATARI" else "SAVE RISK LIMITS", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Emergency Kill Switch
            Button(
                onClick = onKillSwitch,
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = "Kill Switch", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isSwahili) "ZIMA HARAKA (EMERGENCY KILL SWITCH)" else "EMERGENCY KILL SWITCH", color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun AuditLogContent(auditLogs: List<AuditLogEntry>, isSwahili: Boolean) {
    if (auditLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (isSwahili) "Hakuna kumbukumbu za hivi karibuni" else "No recent audit log entries", color = TextMuted)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(auditLogs) { log ->
                val severityColor = when (log.severity) {
                    "ALERT" -> DangerRed
                    "WARN" -> AccentOrange
                    else -> AccentCyan
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = log.action,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = severityColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = log.provider,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhite,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
