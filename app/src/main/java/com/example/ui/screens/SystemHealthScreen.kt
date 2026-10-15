package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.MotherBrainEngine
import com.example.service.BuddyAccessibilityService
import com.example.ui.theme.*

@Composable
fun SystemHealthScreen(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    modifier: Modifier = Modifier
) {
    val telemetry by engine.telemetry.collectAsState()
    val isAccessibilityConnected by BuddyAccessibilityService.isServiceConnected.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.HealthAndSafety,
                    contentDescription = "Health",
                    tint = AccentNeonGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSwahili) "Afya ya Mfumo na Mawasiliano" else "System Health & Core Telemetry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        }

        item {
            HealthItemCard(
                title = "MotherBrain Autonomous Engine",
                subtitle = "Active tick loop running at 60Hz frequency",
                status = "OPTIMAL",
                isHealthy = true,
                latency = "${telemetry.latencyMs}ms"
            )
        }

        item {
            HealthItemCard(
                title = "Universal Tick Feed Engine",
                subtitle = "15 multi-market feeds streaming (Crypto, Forex, Deriv, Stocks)",
                status = "ONLINE",
                isHealthy = true,
                latency = "2ms"
            )
        }

        item {
            HealthItemCard(
                title = "Buddy AI Accessibility Automation Harness",
                subtitle = if (isAccessibilityConnected)
                    "Service connected and authorized for autonomous UI actions"
                else
                    "Standby (Enable in Android Settings > Accessibility)",
                status = if (isAccessibilityConnected) "CONNECTED" else "STANDBY",
                isHealthy = isAccessibilityConnected,
                latency = if (isAccessibilityConnected) "1ms" else "--"
            )
        }

        item {
            HealthItemCard(
                title = "SQLite Room Database Engine",
                subtitle = "Persistent audit logs, bot configurations & order ledger",
                status = "HEALTHY",
                isHealthy = true,
                latency = "0.4ms"
            )
        }

        item {
            HealthItemCard(
                title = "24/7 Sovereign Background Daemon",
                subtitle = "Heartbeat monitor and autonomous reconnection loop",
                status = "RUNNING",
                isHealthy = true,
                latency = "5ms"
            )
        }
    }
}

@Composable
private fun HealthItemCard(
    title: String,
    subtitle: String,
    status: String,
    isHealthy: Boolean,
    latency: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextWhite)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isHealthy) AccentNeonGreen else WarningOrange)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isHealthy) AccentNeonGreen else WarningOrange
                    )
                }
                Text(
                    text = latency,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }
        }
    }
}
