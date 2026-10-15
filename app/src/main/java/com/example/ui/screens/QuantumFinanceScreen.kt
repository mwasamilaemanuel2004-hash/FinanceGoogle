package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.WeekendMaintenanceManager
import com.example.engine.WeekendMaintenanceState
import com.example.engine.lib.quant_core.QuantumFinanceEngine
import com.example.engine.vps.VpsLiveManager
import com.example.ui.theme.*

@Composable
fun QuantumFinanceScreen(
    isSwahili: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val weekendManager = remember { WeekendMaintenanceManager.getInstance(context) }
    val weekendState by weekendManager.currentState.collectAsState()
    val healingReport by weekendManager.latestHealingReport.collectAsState()
    val is24_6Active by weekendManager.is24_6Enforced.collectAsState()
    val logs by weekendManager.maintenanceLog.collectAsState()

    val vpsState by VpsLiveManager.vpsState.collectAsState()

    // Quantum calculations for live representative asset (e.g., EUR/USD or BTC)
    val quantumState = remember {
        QuantumFinanceEngine.evaluateQuantumState(
            currentPrice = 1.0845,
            supportLevel = 1.0810,
            resistanceLevel = 1.0880,
            volatilitySigma = 0.0075,
            orderFlowImbalance = 0.38
        )
    }

    val squareRootImpact = remember {
        QuantumFinanceEngine.calculateSquareRootImpact(
            orderSizeUsd = 50000.0,
            dailyVolumeUsd = 12500000.0,
            dailyVolatilityPct = 1.2,
            yConstant = 0.6
        )
    }

    val scalingLaw = remember {
        QuantumFinanceEngine.evaluateScalingLaws(
            priceReturns = listOf(0.002, -0.0015, 0.003, -0.0008, 0.004, -0.0025),
            defaultHurst = 0.62
        )
    }

    val sizeLine = remember {
        QuantumFinanceEngine.computeSizeLine(
            symbol = "EUR/USD",
            accountBalanceUsd = 2500.0,
            dailyVolumeUsd = 50000000.0,
            dailyVolatilityPct = 0.95,
            kellyLotSize = 0.25,
            minOrderLot = 0.01,
            maxLeverage = 30
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Live VPS 24/6 Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGlow, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isSwahili) "LIVE CLOUD VPS & 24/6 RATIBO" else "LIVE CLOUD VPS & 24/6 SCHEDULE",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = if (isSwahili) "Haina Localhost • Ulinzi wa Wikendi Ijumaa-Jumamosi" else "Zero Localhost • Friday-Saturday Weekend Protection",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentNeonGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen)
                        ) {
                            Text(
                                text = "LIVE VPS 24/6",
                                color = AccentNeonGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Primary Region", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(text = vpsState.primaryRegion, style = MaterialTheme.typography.bodySmall, color = TextWhite, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Uptime & Ping", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(text = "${vpsState.uptimeHours}h • ${vpsState.pingLatencyMs}ms", style = MaterialTheme.typography.bodySmall, color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Clean Live URL: ${vpsState.publicDnsEndpoint}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentGold
                    )
                }
            }
        }

        // 2. 24/6 Weekend Maintenance & Self-Healing Control
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (weekendState == WeekendMaintenanceState.ACTIVE_TRADING_WEEK) AccentNeonGreen.copy(alpha = 0.5f) else AccentGold, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isSwahili) "Hali ya 24/6: ${weekendState.labelSw}" else "24/6 Status: ${weekendState.labelEn}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (weekendState == WeekendMaintenanceState.ACTIVE_TRADING_WEEK) AccentNeonGreen else AccentGold
                            )
                            Text(
                                text = if (isSwahili) "Inafunga Ijumaa jioni, inatibu Jumamosi, inafungua Jumamosi jioni" else "Flushes Fri 21:00 GMT, Heals Sat, Reopens Sat 21:00 GMT",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        Switch(
                            checked = is24_6Active,
                            onCheckedChange = { weekendManager.toggle24_6Enforcement() }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { weekendManager.triggerManualSelfHeal() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isSwahili) "Jitibu Sasa (Self-Heal)" else "Run Self-Heal", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { weekendManager.evaluateCurrentTimeWindow() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = if (isSwahili) "Kagua Ratiba" else "Audit Schedule", fontSize = 12.sp, color = TextWhite)
                        }
                    }

                    // Diagnostic results
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.4f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isSwahili) "Ripoti ya Hasara & Faida (Self-Healing Audit):" else "Self-Healing P&L & Diagnostic Report:",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentGold,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Biashara zilizokaguliwa: ${healingReport.totalTradesReviewed} • Faida: +$${healingReport.totalProfitRealizedUsd} • Hasara: -$${healingReport.totalLossRealizedUsd} • Net PnL: +$${healingReport.netPnlUsd} (${healingReport.winRatePct}% Win Rate)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mifano 35 iliyosasishwa kiotomatiki (Auto-Trained): KNN, RF, XGBoost, GARCH, Ornstein-Uhlenbeck, Quantum Hamiltonian.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentNeonGreen
                            )
                        }
                    }
                }
            }
        }

        // 3. Quantum Finance Hamiltonian & Wavefunction
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGlow.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AllInclusive, contentDescription = null, tint = AccentCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSwahili) "QUANTUM FINANCE: SCHRODINGER WAVEFUNCTION" else "QUANTUM FINANCE: SCHRODINGER WAVEFUNCTION",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Hali: ${quantumState.stateLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Tunneling Prob", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(text = "${String.format("%.1f", quantumState.tunnelingProbability * 100)}%", style = MaterialTheme.typography.bodySmall, color = AccentNeonGreen, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "Potential Barrier V(x)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(text = String.format("%.4f", quantumState.quantumPotentialBarrier), style = MaterialTheme.typography.bodySmall, color = TextWhite)
                        }
                        Column {
                            Text(text = "Kinetic Energy E", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(text = String.format("%.4f", quantumState.quantumKineticEnergy), style = MaterialTheme.typography.bodySmall, color = TextWhite)
                        }
                        Column {
                            Text(text = "Phase Angle", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(text = "${String.format("%.0f", quantumState.quantumPhaseAngleDeg)}°", style = MaterialTheme.typography.bodySmall, color = AccentGold)
                        }
                    }
                }
            }
        }

        // 4. Square Root Law & Scaling Laws
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.SquareFoot, contentDescription = null, tint = AccentGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSwahili) "SHERIA YA SQUARE ROOT & SCALING LAWS" else "SQUARE ROOT LAW & SCALING LAWS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Kanuni ya Athari ya Bouchaud: I = Y * sigma * sqrt(Q / V)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Text(
                        text = squareRootImpact.recommendation,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (squareRootImpact.isExecutableWithinLimit) AccentNeonGreen else AccentRed,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Scaling Law (Hurst Diffusion & Pareto Tail Alpha):",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "${scalingLaw.regimeDescription} • Alpha: ${String.format("%.2f", scalingLaw.alphaPareto)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Volatility Scaling: 1m -> 15m (${String.format("%.2f", scalingLaw.scalingFactor1mTo15m)}x), 1m -> 1h (${String.format("%.2f", scalingLaw.scalingFactor1mTo1h)}x)",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan
                    )
                }
            }
        }

        // 5. Size Line & Liquidity Budget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AccentNeonGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LineWeight, contentDescription = null, tint = AccentNeonGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSwahili) "SIZE LINE: KIPIMO CHA UKUBWA WA LOTI" else "SIZE LINE: POSITION SIZING GOVERNOR",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentNeonGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = sizeLine.sizeLineRationale,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Loti Inayopendekezwa:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(text = "${sizeLine.recommendedSizeLots} Lot", style = MaterialTheme.typography.bodySmall, color = AccentNeonGreen, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Kikomo cha Juu (Ceiling):", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(text = "$${String.format("%,.0f", sizeLine.maxLiquidityCeilingUsd)} USD", style = MaterialTheme.typography.bodySmall, color = AccentGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 6. Live Maintenance & Healing Logs
        item {
            Text(
                text = if (isSwahili) "Kumbukumbu ya Kujitibu (Self-Healing Logs):" else "Self-Healing & Maintenance Stream:",
                style = MaterialTheme.typography.titleSmall,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        }

        items(logs) { log ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceCard,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Text(
                    text = log,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (log.contains("✅") || log.contains("🟢")) AccentNeonGreen else if (log.contains("🛑")) AccentRed else TextWhite
                )
            }
        }
    }
}
