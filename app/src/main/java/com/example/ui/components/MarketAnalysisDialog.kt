package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.OrderSide
import com.example.engine.AiMarketPrediction
import com.example.ui.theme.*

@Composable
fun MarketAnalysisDialog(
    prediction: AiMarketPrediction,
    isSwahili: Boolean,
    onApplyToOrder: (OrderSide, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val isBuy = prediction.recommendedSide == OrderSide.BUY
    val signalColor = if (isBuy) AccentNeonGreen else DangerRed

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(vertical = 14.dp)
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
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI Analysis",
                            tint = AccentLavender,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isSwahili) "Uchambuzi & Utabiri wa Soko" else "Market Analysis & AI Prediction",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "${prediction.symbol} • ${prediction.timeHorizon}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Signal Banner Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, signalColor.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isSwahili) "ISHARA YA AKILI BANDIA (AI SIGNAL)" else "CONSENSUS AI SIGNAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isBuy) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                            contentDescription = "Signal",
                                            tint = signalColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = prediction.signal,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = signalColor
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (isSwahili) "UWEZEKANO WA USHINDI" else "WIN CONFIDENCE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "${prediction.confidencePct}%",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = AccentCyan
                                    )
                                    Text(
                                        text = "R:R ${prediction.riskRewardRatio}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    // Strategic Price Targets (Mathematically Sound)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isSwahili) "Viwango Vinavyopendekezwa vya Oda" else "Recommended Order Parameters",
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
                                        Text(
                                            text = if (isSwahili) "Take Profit 1 (TP1)" else "Take Profit 1",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "$${String.format("%,.2f", prediction.target1Price)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentNeonGreen,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = if (isSwahili) "Take Profit 2 (TP2)" else "Take Profit 2",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "$${String.format("%,.2f", prediction.target2Price)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentNeonGreen,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (isSwahili) "Stop Loss (SL)" else "Stop Loss",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "$${String.format("%,.2f", prediction.invalidationPrice)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DangerRed,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Button to apply these exact parameters to user's order
                                Button(
                                    onClick = {
                                        onApplyToOrder(
                                            prediction.recommendedSide,
                                            prediction.target1Price,
                                            prediction.invalidationPrice
                                        )
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = if (isSwahili)
                                            "Weka Viwango Hivi Kwenye Oda Yako (Apply Parameters)"
                                        else
                                            "Apply These Parameters to Order Form",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Analytical Rationale
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isSwahili) "Sababu za Kiasi & Akili Bandia (Rationale):" else "Quantitative Rationale:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isSwahili) prediction.reasoningSw else prediction.reasoningEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                                )
                            }
                        }
                    }

                    // Smart Money Concepts (SMC) Section
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Smart Money Concepts (SMC) & Liquidity Radar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentOrange
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Market Structure:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(text = prediction.smc.marketStructure, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Order Block Type:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(text = prediction.smc.orderBlockType, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = signalColor)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Order Block Zone:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(text = prediction.smc.orderBlockZone, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = TextWhite)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Fair Value Gap (FVG):", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(text = "$${String.format("%,.2f", prediction.smc.fairValueGapLevel)} (${prediction.smc.fvgStatus})", style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                                }
                            }
                        }
                    }

                    // Technical Indicators Details
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isSwahili) "Viashiria vya Kiufundi (Technical Indicators)" else "Technical Indicators",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "RSI (14):", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(text = "${String.format("%.1f", prediction.indicators.rsi)} (${prediction.indicators.rsiCondition})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AccentCyan)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Trend (EMA 20/50):", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(text = prediction.indicators.trendDirection, style = MaterialTheme.typography.labelSmall, color = TextWhite)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "MACD Momentum:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(text = prediction.indicators.macdStatus, style = MaterialTheme.typography.labelSmall, color = if (prediction.indicators.macdHistogram >= 0) AccentNeonGreen else DangerRed)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Support 1 / Resistance 1:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(text = "$${String.format("%,.2f", prediction.indicators.support1)} / $${String.format("%,.2f", prediction.indicators.resistance1)}", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = TextWhite)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
