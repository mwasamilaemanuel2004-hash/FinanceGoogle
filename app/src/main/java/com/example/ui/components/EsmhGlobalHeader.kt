package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguageMode
import com.example.data.model.ProfitEngineMode
import com.example.engine.MotherBrainTelemetry
import com.example.ui.theme.*

@Composable
fun EsmhGlobalHeader(
    telemetry: MotherBrainTelemetry,
    isSwahili: Boolean,
    currentTheme: DashboardThemeMode,
    onToggleLanguage: () -> Unit,
    onCycleTheme: () -> Unit,
    onEmergencyHaltToggle: () -> Unit,
    onOpenConnectors: () -> Unit,
    onOpenBacktest: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenAuth: () -> Unit = {},
    onOpenToken: () -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    onToggleFullscreen: (() -> Unit)? = null,
    isFullscreen: Boolean = false,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Branding with Drawer Hamburger Trigger
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu Drawer",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (telemetry.emergencyStopActive) DangerRed else AccentNeonGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ESMH.TRADE / MWAS AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = currentTheme.primaryColor
                        )
                        Text(
                            text = if (isSwahili) "Ubongo wa Kiasi & Maabara ya Boti" else "Quant Intelligence & Bot Lab",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                // Quick Action Controls (Language, Theme, Kill Switch)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Prominent Settings Hub Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentGold.copy(alpha = 0.22f))
                            .border(1.dp, AccentGold, RoundedCornerShape(6.dp))
                            .clickable { onOpenConnectors() }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = AccentGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isSwahili) "MIPANGILIO" else "SETTINGS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentGold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Token Activation Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentGold.copy(alpha = 0.18f))
                            .border(1.dp, AccentGold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .clickable { onOpenToken() }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = "Token", tint = AccentGold, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "TOKEN", fontWeight = FontWeight.Black, color = AccentGold, fontSize = 10.sp)
                        }
                    }

                    // User Profile / Sign In
                    IconButton(
                        onClick = onOpenAuth,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "User Account",
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Admin Command Center (if authorized)
                    IconButton(
                        onClick = onOpenAdmin,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Center",
                            tint = AccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Backtest Lab
                    IconButton(
                        onClick = onOpenBacktest,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "Backtest",
                            tint = AccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Export Hub
                    IconButton(
                        onClick = onOpenExport,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Export",
                            tint = AccentEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Language Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .clickable { onToggleLanguage() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isSwahili) "SW" else "EN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentElectricBlue
                        )
                    }

                    // Theme Cycle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .clickable { onCycleTheme() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = "Cycle Theme",
                            tint = currentTheme.secondaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Full Page / Fullscreen Toggle ("Jaza Ukurasa")
                    if (onToggleFullscreen != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isFullscreen) AccentNeonGreen.copy(alpha = 0.2f) else SurfaceElevated)
                                .border(1.dp, if (isFullscreen) AccentNeonGreen else BorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { onToggleFullscreen() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = if (isSwahili) "Jaza Ukurasa" else "Full Page",
                                tint = if (isFullscreen) AccentNeonGreen else AccentCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Emergency Halt Button
                    Button(
                        onClick = onEmergencyHaltToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (telemetry.emergencyStopActive) DangerRed else DangerRed.copy(alpha = 0.8f)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (telemetry.emergencyStopActive) (if (isSwahili) "SIMAMISHA ILIYO WAZI" else "HALTED") else (if (isSwahili) "ZIMA HARAKA" else "KILL SWITCH"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-bar: Real-time Telemetry Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Trading Mode Isolation Badge (LIVE / ONLINE)
                    Surface(
                        color = AccentNeonGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen)
                    ) {
                        Text(
                            text = if (telemetry.emergencyStopActive) "KILL SWITCH" else "🟢 LIVE / ONLINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentNeonGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Data Quality Badge (ONLINE / HEALTHY)
                    Surface(
                        color = AccentNeonGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen)
                    ) {
                        Text(
                            text = if (isSwahili) "DATA: ONLINE (IMARA)" else "DATA: ONLINE (HEALTHY)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentNeonGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    StatusBadge(
                        label = if (isSwahili) "MTAJI: " else "EQUITY: ",
                        value = "$${String.format("%,.2f", if (telemetry.totalEquityUsd > 0) telemetry.totalEquityUsd else 10000.0)}",
                        valueColor = TextWhite
                    )
                    StatusBadge(
                        label = if (isSwahili) "LEO: " else "24H: ",
                        value = "${if (telemetry.dailyPnlUsd >= 0) "+" else ""}$${String.format("%,.2f", telemetry.dailyPnlUsd)} (${String.format("%.2f", telemetry.dailyPnlPct)}%)",
                        valueColor = if (telemetry.dailyPnlUsd > 0) AccentNeonGreen else if (telemetry.dailyPnlUsd < 0) DangerRed else TextMuted
                    )
                    StatusBadge(
                        label = if (isSwahili) "BOTI: " else "BOTS: ",
                        value = "${telemetry.activeBotsCount} Active",
                        valueColor = if (telemetry.activeBotsCount > 0) AccentCyan else TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LATENCY: ${telemetry.latencyMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = AccentNeonGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceElevated.copy(alpha = 0.5f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
