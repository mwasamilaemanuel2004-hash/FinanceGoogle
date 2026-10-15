package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.MotherBrainEngine
import com.example.ui.theme.*

@Composable
fun RiskCenterScreen(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    modifier: Modifier = Modifier
) {
    val riskTelemetry by engine.riskGovernor.telemetry.collectAsState()
    var drawdownLimit by remember { mutableStateOf(riskTelemetry.maxDailyDrawdownPct.toFloat()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Governor Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = "Risk Governor",
                                tint = if (riskTelemetry.emergencyStopActive) DangerRed else AccentNeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSwahili) "Mlinzi wa Hatari na Mtaji" else "Hard Risk Governor & Sovereign Gate",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Text(
                            text = if (riskTelemetry.emergencyStopActive) "HALTED" else "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (riskTelemetry.emergencyStopActive) DangerRed else AccentNeonGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isSwahili)
                            "Kikomo cha Hasara ya Siku: ${String.format("%.1f", drawdownLimit)}%"
                        else
                            "Max Allowed Daily Drawdown: ${String.format("%.1f", drawdownLimit)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite
                    )

                    Slider(
                        value = drawdownLimit,
                        onValueChange = {
                            drawdownLimit = it
                            engine.riskGovernor.updateMaxDrawdown(it.toDouble())
                        },
                        valueRange = 1f..15f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "1.0% (Ultra-Safe)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(text = "15.0% (Aggressive)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }

        // Sovereign Safety Gate Lock / Kill Switch Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isSwahili) "Udhibiti wa Dharura" else "Emergency Circuit Breakers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (riskTelemetry.emergencyStopActive) {
                                engine.resetEmergencyHalt()
                            } else {
                                engine.triggerEmergencyHalt("Manual Kill-Switch Activated by User")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (riskTelemetry.emergencyStopActive) AccentNeonGreen else DangerRed
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            if (riskTelemetry.emergencyStopActive) Icons.Default.Security else Icons.Default.Warning,
                            contentDescription = "Halt"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (riskTelemetry.emergencyStopActive)
                                (if (isSwahili) "REJESHA MFUMO KATIKA HALI YA KAWAIDA" else "RESUME ALL TRADING OPERATIONS")
                            else
                                (if (isSwahili) "SIMAMISHA SHUGHULI ZOTE ZA BIASHARA (KILL SWITCH)" else "EMERGENCY KILL SWITCH (HALT ALL)")
                        )
                    }
                }
            }
        }
    }
}
