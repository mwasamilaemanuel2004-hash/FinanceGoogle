package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.MarketType
import com.example.engine.MotherBrainEngine
import com.example.ui.components.AnyPairSelectorDialog
import com.example.ui.components.MasterAgentConsoleDialog
import com.example.ui.theme.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.unit.sp

@Composable
fun MarketsScreen(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    onSelectMarketSymbol: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ticks by engine.tickEngine.ticks.collectAsState()
    var selectedFilter by remember { mutableStateOf<MarketType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddPairDialog by remember { mutableStateOf(false) }
    var showMasterAgentDialog by remember { mutableStateOf(false) }

    val filteredTicks = remember(ticks, selectedFilter, searchQuery) {
        ticks.values.filter { tick ->
            val matchesFilter = selectedFilter == null || tick.marketType == selectedFilter
            val matchesQuery = searchQuery.isBlank() ||
                    tick.symbol.contains(searchQuery, ignoreCase = true) ||
                    tick.marketType.displayName.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    val topGainer = remember(ticks) {
        ticks.values.filter { it.marketType == MarketType.CRYPTO || it.marketType == MarketType.DERIV }.maxByOrNull { it.change24h }
    }
    val topLoser = remember(ticks) {
        ticks.values.filter { it.marketType == MarketType.CRYPTO || it.marketType == MarketType.DERIV }.minByOrNull { it.change24h }
    }
    val topRsiExtreme = remember(ticks) {
        ticks.values.filter { it.marketType == MarketType.CRYPTO || it.marketType == MarketType.DERIV }.maxByOrNull { kotlin.math.abs(it.rsi - 50.0) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // CoinMarketCap / Binance Style Global Market Stats Ribbon
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = if (isSwahili) "MTAJI WA DUNIA" else "GLOBAL CAP",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "$2.94T",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Divider(modifier = Modifier.width(1.dp).height(20.dp), color = BorderSubtle)
                        Column {
                            Text(
                                text = if (isSwahili) "KIASI CHOTE (24H)" else "24H VOLUME",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "$112.5B",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Divider(modifier = Modifier.width(1.dp).height(20.dp), color = BorderSubtle)
                        Column {
                            Text(
                                text = "DOMINANCE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "BTC 57.2%",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = AccentGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Divider(modifier = Modifier.width(1.dp).height(20.dp), color = BorderSubtle)
                        Column {
                            Text(
                                text = if (isSwahili) "WOGA & TAMAA" else "FEAR & GREED",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "72 (Greed)",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = AccentNeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Search & Add Pair Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (isSwahili) "Tafuta soko au pair yoyote..." else "Search market or symbol...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { showAddPairDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Pair", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSwahili) "Ongeza" else "Add",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Market Sector Filter Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text(if (isSwahili) "Yote (All)" else "All Markets") }
                    )
                }
                items(MarketType.values()) { type ->
                    FilterChip(
                        selected = selectedFilter == type,
                        onClick = { selectedFilter = type },
                        label = { Text(if (isSwahili) type.displayNameSwahili else type.displayName) }
                    )
                }
            }
        }

        // CoinMarketCap / Binance Highlights Panel (Top Gainer, Top Dip Discount, RSI Anomalies)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                topGainer?.let { gainer ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(160.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isSwahili) "🔥 Kupanda Juu" else "🔥 Top Gainer",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(AccentNeonGreen)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = gainer.symbol,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$${String.format("%,.2f", gainer.price)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "+${String.format("%.2f", gainer.change24h)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = AccentNeonGreen,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                topLoser?.let { loser ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(160.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isSwahili) "📉 Punguzo (Dip)" else "📉 Top Discount",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(DangerRed)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = loser.symbol,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$${String.format("%,.2f", loser.price)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${String.format("%.2f", loser.change24h)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = DangerRed,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                topRsiExtreme?.let { rsiExt ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(160.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isSwahili) "⚡ Kasi ya RSI" else "⚡ RSI Extreme",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(AccentCyan)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = rsiExt.symbol,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$${String.format("%,.2f", rsiExt.price)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "RSI ${String.format("%.0f", rsiExt.rsi)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = AccentCyan,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Pair AI Matching Banner (Volume, Depth, Volatility & Capital)
        item {
            val topRecs by engine.masterAgent.pairRecommendations.collectAsState()
            val topPick = topRecs.firstOrNull()
            Surface(
                onClick = { showMasterAgentDialog = true },
                color = AccentCyan.copy(alpha = 0.09f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isSwahili) "🎯 Uchaguzi Mahiri wa Pair (AI Matrix)" else "🎯 AI Dynamic Pair Selector",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = AccentCyan
                                )
                                if (topPick != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AccentNeonGreen)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "#1 ${topPick.symbol} (${topPick.compositeScore}%)",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isSwahili)
                                    "Wakala anapima Volume (CoinGecko), Order-Book Depth (Binance/Bybit/Coinbase/MT5), Spread na Mtaji wako."
                                else
                                    "Selects assets dynamically based on CoinGecko volume, multi-exchange order-book depth & capital.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Header Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSwahili) "Rada ya Masoko ya Kimataifa" else "Universal Multi-Market Radar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "${filteredTicks.size} Instruments",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // Binance & CMC Grid Header Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSwahili) "ASSET / SOKO" else "ASSET / MARKET",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                )
                Text(
                    text = if (isSwahili) "BEI / MABADILIKO / RSI" else "PRICE / 24H CHANGE / RSI",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                )
            }
        }

        // Market Instruments List
        items(filteredTicks) { tick ->
            val isPositive = tick.change24h >= 0
            val marketColor = getMarketThemeColor(tick.marketType)
            val candles = remember(tick.symbol, ticks) {
                engine.tickEngine.getCandlesForSymbol(tick.symbol)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectMarketSymbol(tick.symbol) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1.2f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 36.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(marketColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tick.symbol,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = tick.marketType.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    // SPARKLINE MINI TREND CHART (Binance & CoinMarketCap Style)
                    Sparkline(
                        candles = candles,
                        isPositive = isPositive,
                        modifier = Modifier
                            .weight(0.8f)
                            .height(28.dp)
                            .padding(horizontal = 6.dp)
                    )

                    Row(
                        modifier = Modifier.weight(1.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$${String.format("%,.2f", tick.price)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextWhite
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${if (isPositive) "+" else ""}${String.format("%.2f", tick.change24h)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) AccentNeonGreen else DangerRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RSI ${String.format("%.0f", tick.rsi)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Quick Trade Button
                        Button(
                            onClick = { onSelectMarketSymbol(tick.symbol) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (isSwahili) "Biashara" else "Trade",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddPairDialog) {
        AnyPairSelectorDialog(
            ticks = ticks,
            currentSelectedSymbol = "",
            isSwahili = isSwahili,
            onSelectPair = { symbol ->
                showAddPairDialog = false
                onSelectMarketSymbol(symbol)
            },
            onAddCustomPair = { rawSymbol, category, customPrice ->
                val added = engine.tickEngine.addCustomPair(rawSymbol, category, customPrice)
                showAddPairDialog = false
                onSelectMarketSymbol(added.symbol)
            },
            onDismiss = { showAddPairDialog = false }
        )
    }

    if (showMasterAgentDialog) {
        val candidateCandles = remember(ticks) {
            listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "XRP/USDT", "DOGE/USDT", "XAU/USD", "EUR/USD", "VOLATILITY 75")
                .associateWith { sym -> engine.tickEngine.getCandlesForSymbol(sym) }
        }
        MasterAgentConsoleDialog(
            agent = engine.masterAgent,
            currentSymbol = filteredTicks.firstOrNull()?.symbol ?: "BTC/USDT",
            isSwahili = isSwahili,
            ticks = ticks,
            candlesMap = candidateCandles,
            initialTab = 0,
            onSelectPair = { sym ->
                showMasterAgentDialog = false
                onSelectMarketSymbol(sym)
            },
            onExecuteTrade = { sym, side, lot, tp, sl ->
                showMasterAgentDialog = false
                onSelectMarketSymbol(sym)
            },
            onDismiss = { showMasterAgentDialog = false }
        )
    }
}

@Composable
fun Sparkline(
    candles: List<com.example.data.model.PriceCandle>,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        if (candles.size < 2) return@Canvas
        val closes = candles.map { it.close }
        val min = closes.minOrNull() ?: 0.0
        val max = closes.maxOrNull() ?: 0.0
        val range = max - min
        val path = androidx.compose.ui.graphics.Path()
        val width = size.width
        val height = size.height

        val points = closes.mapIndexed { idx, price ->
            val x = (idx.toFloat() / (closes.size - 1).toFloat()) * width
            val y = if (range > 0) {
                height - (((price - min) / range).toFloat() * height)
            } else {
                height / 2f
            }
            androidx.compose.ui.geometry.Offset(x, y)
        }

        points.forEachIndexed { index, point ->
            if (index == 0) {
                path.moveTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }

        drawPath(
            path = path,
            color = if (isPositive) AccentNeonGreen else DangerRed,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1.5.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

