package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class IndicatorConfig(
    val rsiPeriod: Int = 14,
    val rsiOverbought: Float = 70f,
    val rsiOversold: Float = 30f,
    val macdFast: Int = 12,
    val macdSlow: Int = 26,
    val macdSignal: Int = 9,
    val bbPeriod: Int = 20,
    val bbMultiplier: Float = 2.0f,
    val atrPeriod: Int = 14,
    val enableIctFvg: Boolean = true,
    val enableVwap: Boolean = true
)

@Composable
fun IndicatorSetupDialog(
    currentConfig: IndicatorConfig = IndicatorConfig(),
    isSwahili: Boolean,
    onSaveConfig: (IndicatorConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var rsiPeriod by remember { mutableStateOf(currentConfig.rsiPeriod) }
    var rsiOverbought by remember { mutableStateOf(currentConfig.rsiOverbought) }
    var rsiOversold by remember { mutableStateOf(currentConfig.rsiOversold) }

    var macdFast by remember { mutableStateOf(currentConfig.macdFast) }
    var macdSlow by remember { mutableStateOf(currentConfig.macdSlow) }
    var macdSignal by remember { mutableStateOf(currentConfig.macdSignal) }

    var bbPeriod by remember { mutableStateOf(currentConfig.bbPeriod) }
    var bbMultiplier by remember { mutableStateOf(currentConfig.bbMultiplier) }
    var atrPeriod by remember { mutableStateOf(currentConfig.atrPeriod) }
    var enableIctFvg by remember { mutableStateOf(currentConfig.enableIctFvg) }
    var enableVwap by remember { mutableStateOf(currentConfig.enableVwap) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(vertical = 10.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Indicator Setup",
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSwahili) "Mipangilio ya Viashiria (Indicators)" else "Technical Indicator Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. RSI Configuration
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "RSI (Relative Strength Index)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = AccentCyan
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Period: $rsiPeriod", style = MaterialTheme.typography.labelSmall, color = TextWhite)
                                    Text(text = "Oversold: ${rsiOversold.toInt()} | Overbought: ${rsiOverbought.toInt()}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                                Slider(
                                    value = rsiPeriod.toFloat(),
                                    onValueChange = { rsiPeriod = it.toInt() },
                                    valueRange = 7f..28f,
                                    steps = 20,
                                    colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                                )
                            }
                        }
                    }

                    // 2. MACD Configuration (Pandas fast & slow ewm)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "MACD (Moving Average Convergence Divergence)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = AccentNeonGreen
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Fast EMA: $macdFast | Slow EMA: $macdSlow | Signal: $macdSignal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextWhite,
                                    fontFamily = FontFamily.Monospace
                                )
                                Slider(
                                    value = macdFast.toFloat(),
                                    onValueChange = { macdFast = it.toInt() },
                                    valueRange = 5f..20f,
                                    colors = SliderDefaults.colors(thumbColor = AccentNeonGreen, activeTrackColor = AccentNeonGreen)
                                )
                            }
                        }
                    }

                    // 3. Bollinger Bands & ATR
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Bollinger Bands & ATR Volatility",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = AccentGold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "BB Period: $bbPeriod (x${String.format("%.1f", bbMultiplier)})", style = MaterialTheme.typography.labelSmall, color = TextWhite)
                                    Text(text = "ATR Period: $atrPeriod", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                                Slider(
                                    value = bbPeriod.toFloat(),
                                    onValueChange = { bbPeriod = it.toInt() },
                                    valueRange = 10f..50f,
                                    colors = SliderDefaults.colors(thumbColor = AccentGold, activeTrackColor = AccentGold)
                                )
                            }
                        }
                    }

                    // 4. ICT Smart Money Concepts & VWAP
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Smart Money (ICT Order Blocks & FVG)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = AccentLavender
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isSwahili) "Tambua Fair Value Gaps (FVG)" else "Detect Fair Value Gaps (FVG)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextWhite
                                    )
                                    Switch(
                                        checked = enableIctFvg,
                                        onCheckedChange = { enableIctFvg = it }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isSwahili) "Tumia Volume Weighted Average (VWAP)" else "Institutional VWAP Anchor",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextWhite
                                    )
                                    Switch(
                                        checked = enableVwap,
                                        onCheckedChange = { enableVwap = it }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Save & Apply Button
                Button(
                    onClick = {
                        val updated = IndicatorConfig(
                            rsiPeriod = rsiPeriod,
                            rsiOverbought = rsiOverbought,
                            rsiOversold = rsiOversold,
                            macdFast = macdFast,
                            macdSlow = macdSlow,
                            macdSignal = macdSignal,
                            bbPeriod = bbPeriod,
                            bbMultiplier = bbMultiplier,
                            atrPeriod = atrPeriod,
                            enableIctFvg = enableIctFvg,
                            enableVwap = enableVwap
                        )
                        onSaveConfig(updated)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSwahili) "HIFADHI MIPANGILIO YA VIASHIRIA" else "SAVE & APPLY INDICATOR SETUP",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
