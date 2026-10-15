package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.ConnectorCard
import com.example.engine.StrategyLibrary
import com.example.ui.theme.*
import com.example.util.ExternalPlatformManager

@Composable
fun ConnectorsHubDialog(
    connectors: List<ConnectorCard>,
    isSwahili: Boolean,
    onDismiss: () -> Unit,
    onOpenRealAccountConnection: () -> Unit = {}
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isSwahili) "Lango la Madalali & Binance Pro" else "Broker Gateways & Binance Pro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isSwahili) "Fungua au unganisha akaunti zako" else "Launch or connect live trading accounts",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real Broker & MT5 Account Connection Button
                Button(
                    onClick = {
                        onDismiss()
                        onOpenRealAccountConnection()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Key, contentDescription = "Connect", tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSwahili) "UNGANISHA MT5 / BINANCE HALISI (REAL API)" else "CONNECT REAL MT5 / BINANCE ACCOUNT",
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Binance Pro Featured Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange.copy(alpha = 0.6f)),
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
                                text = "Binance Pro Trading Terminal",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange
                            )
                            Text(
                                text = "OFFICIAL GATEWAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentNeonGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isSwahili)
                                "Bofya hapa chini kufungua App au Web ya Binance Pro moja kwa moja kwa biashara ya Futures & Spot:"
                            else
                                "Click below to launch the Binance Pro App or Web terminal directly for Spot & Futures:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { ExternalPlatformManager.openPlatform(context, "binance") },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isSwahili) "FUNGUA BINANCE PRO SASA" else "LAUNCH BINANCE PRO NOW",
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(connectors) { conn ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = conn.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "${conn.marketCoverage} • ${conn.pingMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        val platformId = when {
                                            conn.name.contains("Binance", ignoreCase = true) -> "binance"
                                            conn.name.contains("Deriv", ignoreCase = true) -> "deriv"
                                            conn.name.contains("MT5", ignoreCase = true) -> "mt5"
                                            else -> "tradingview"
                                        }
                                        ExternalPlatformManager.openPlatform(context, platformId)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (isSwahili) "Fungua" else "Launch",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isSwahili) "Funga" else "Close")
                }
            }
        }
    }
}

@Composable
fun BacktestLabDialog(
    isSwahili: Boolean,
    onDismiss: () -> Unit,
    onOpenFullLab: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val realEngine = remember { com.example.engine.backtest.RealBacktestEngine() }
    var isRunning by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<com.example.engine.backtest.BacktestResult?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSwahili) "Maabara ya Backtest (Data Halisi)" else "Quantitative Backtest Laboratory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isSwahili)
                        "Inatumia data halisi za kihistoria za Binance REST na masoko ya Fedha/Bidhaa/Deriv. Kila biashara, ada na ulinzi wa hasara hupimwa kihalisia."
                    else
                        "Uses real historical tick/candle data from Binance and multi-asset streams. Every trade, fee, and loss guard is verified deterministically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (lastResult != null) {
                    val res = lastResult!!
                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "REAL BACKTEST RUN COMPLETED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentNeonGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = """
                                    Asset: ${res.symbol} (${res.candleCount} real candles)
                                    Total Trades: ${res.totalTrades} (${res.winningTrades}W / ${res.losingTrades}L)
                                    Win Rate: ${res.winRatePct}%
                                    Profit Factor: ${res.profitFactor}
                                    Sharpe Ratio: ${res.sharpeRatio}
                                    Max Drawdown: -${res.maxDrawdownPct}%
                                    Net Return: ${if (res.netProfitUsd >= 0) "+" else ""}${res.netProfitPct}%
                                    Bad Decisions Blocked: ${res.badDecisionsPrevented.size} setups
                                    Capital Preserved: +$${res.capitalSavedUsd}
                                """.trimIndent(),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = TextWhite
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isRunning = true
                            val res = realEngine.runStrategyBacktest(
                                strategy = com.example.engine.backtest.StrategyType.SMC_ORDER_BLOCK,
                                symbol = "BTC/USDT",
                                timeframe = "1h",
                                initialCapital = 1000.0
                            )
                            lastResult = res
                            isRunning = false
                        }
                    },
                    enabled = !isRunning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSwahili) "Inapima Data Halisi..." else "Executing Real Simulation...")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSwahili) "Endesha Jaribio la Data Halisi" else "Run Real-Data Simulation")
                    }
                }

                if (onOpenFullLab != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenFullLab.invoke()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSwahili) "Fungua Maabara Kamili (Mikakati, Boti & Mfumo)" else "Open Full Lab (Strategies, Bots & Training)",
                            color = AccentGold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportAndDownloadCenterDialog(
    isSwahili: Boolean,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSwahili) "Kituo cha Usafirishaji na Uhifadhi" else "Export & Packaging Center",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isSwahili)
                        "Hapa unaweza kupakua takwimu za mikakati, ripoti ya faida, na maelekezo ya kupakua mradi (ZIP au GitHub)."
                    else
                        "Export strategy telemetry, transaction logs, or review repository export instructions (ZIP / GitHub Push).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isSwahili) "Jinsi ya Kupakua APK & Kuweka Kwenye Simu:" else "How to Download APK & Install on Phone:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentNeonGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isSwahili)
                                "1. PAKUA APK: Kwenye menyu ya juu ya AI Studio, bonyeza 'Settings / Export' kisha chagua 'Generate APK' au 'Export as ZIP'.\n" +
                                "2. KUWEKA KWENYE SIMU PAPO HAPO: Fungua kiungo cha programu kwenye Google Chrome ya simu yako, bofya nukta tatu (⋮), kisha chagua 'Add to Home screen' (Weka kwenye skrini ya simu). App itakaa kwenye simu kama programu zote za kawaida!"
                            else
                                "1. DOWNLOAD APK: From the top menu in AI Studio, click 'Settings / Export' and select 'Generate APK' or 'Export as ZIP'.\n" +
                                "2. DIRECT PHONE INSTALL: Open the live URL in Google Chrome on Android, tap the three-dots menu (⋮), and choose 'Add to Home screen' or 'Install App'. It runs as a full native app on your phone!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val liveUrl = "https://ais-pre-4wenbbar7odvuk7myk5j3d-10485178411.europe-west2.run.app"
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(liveUrl))
                        copiedNotice = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (copiedNotice) AccentNeonGreen else MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (copiedNotice)
                            (if (isSwahili) "Kiungo cha Simu Kimerakiriwa!" else "Live URL Copied!")
                        else
                            (if (isSwahili) "Nakili Kiungo cha Programu (Live URL)" else "Copy Live App URL"),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
