package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.InfinitePairsAndClusteringEngine
import com.example.engine.MotherBrainEngine
import com.example.ui.theme.*

@Composable
fun InfinitePairsClusteringScreen(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pairsEngine = remember { InfinitePairsAndClusteringEngine.getInstance(context) }
    val clusters by pairsEngine.clusters.collectAsState()
    val samplePairs by pairsEngine.samplePairs.collectAsState()
    val indicators by pairsEngine.indicators.collectAsState()
    val selectedStrategy by pairsEngine.selectedAgentStrategy.collectAsState()

    var selectedClusterId by remember { mutableStateOf("cluster_1") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val filteredPairs = samplePairs.filter {
        (selectedCategoryFilter == "ALL" || it.category == selectedCategoryFilter) &&
        (it.symbol.contains(searchQuery, ignoreCase = true) || it.baseAsset.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSwahili) "50K+ Infinite Pairs & Behavior Clustering" else "50K+ Infinite Pairs & Behavior Clustering",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                Text(
                    text = if (isSwahili) "Udhibiti wa Tabia za Masoko 50,000+ na Uchaguzi wa Mkakati wa Wakala" else "50,000+ Scaled Assets Clustering & Autonomous Agent Selector",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Surface(
                color = AccentGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AccentGold)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentGold))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSwahili) "50,450 Zimesawazishwa" else "50,450 Active Indexed",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Behavior Clusters Row
        Text(
            text = if (isSwahili) "Makundi 5 ya Tabia (Behavior Clusters - Hybrid Combine 1, 2, 3)" else "5 Behavior Clusters (Hybrid Hierarchy & ML Clustering)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AccentCyan
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            clusters.forEach { cluster ->
                val isSelected = selectedClusterId == cluster.id
                Surface(
                    onClick = { selectedClusterId = cluster.id },
                    color = if (isSelected) AccentCyan.copy(alpha = 0.2f) else SurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) AccentCyan else BorderSubtle),
                    modifier = Modifier.width(240.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = cluster.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextWhite, maxLines = 1)
                            Badge(containerColor = AccentGold.copy(alpha = 0.2f)) {
                                Text(text = "${cluster.instrumentCount}", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSwahili) cluster.descriptionSw else cluster.descriptionEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            maxLines = 2,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = cluster.primaryMarket, style = MaterialTheme.typography.labelSmall, color = AccentCyan, fontSize = 10.sp)
                            Text(text = "Risk: ${cluster.riskMultiplier}x", style = MaterialTheme.typography.labelSmall, color = TextWhite, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Agent Strategy & Indicator Selector Box
        Surface(
            color = SurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = ParameterRowArrangement(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isSwahili) "Uchaguzi wa Mkakati na Viashiria 11" else "Dynamic Strategy & 11 Indicators Selector",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                        Text(
                            text = if (isSwahili) "Wakala anachagua mkakati bora kwa kila soko moja kwa moja" else "Agent auto-selects optimal strategy per cluster with <5ms WASM indicators",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    Button(
                        onClick = { pairsEngine.setAgentStrategy("Autonomous Hybrid Multi-Strategy AI (Dynamic Cluster Selection)") },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Auto Agent Select", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Indicators grid / chips
                Text(
                    text = if (isSwahili) "Viashiria 11 Vilivyounganishwa (Bofya kuwasha/kuzima):" else "Active 11 Indicators (Click to Toggle):",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    indicators.forEach { ind ->
                        FilterChip(
                            selected = ind.isActive,
                            onClick = { pairsEngine.toggleIndicator(ind.id) },
                            label = { Text(text = ind.name, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (ind.isActive) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGold.copy(alpha = 0.25f),
                                selectedLabelColor = AccentGold,
                                selectedLeadingIconColor = AccentGold,
                                containerColor = SurfaceElevated,
                                labelColor = TextMuted
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Infinite Pairs Search & List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isSwahili) "Tafuta pair (mf. BTC, V75, EUR)..." else "Search pairs (e.g. BTC, V75, EUR)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(10.dp))

            val categories = listOf("ALL", "CRYPTO", "FOREX", "DERIV", "METALS", "INDICES", "COMMODITIES")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    val isCatSelected = selectedCategoryFilter == cat
                    Surface(
                        onClick = { selectedCategoryFilter = cat },
                        color = if (isCatSelected) AccentCyan else SurfaceElevated,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCatSelected) Color.Black else TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pairs List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredPairs) { pair ->
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pair.baseAsset.take(3),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = pair.symbol, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text(text = "Cluster: ${pair.clusterId.uppercase()} • Liquidity: ${pair.liquidityScore}%", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "\$${String.format("%,.2f", pair.currentPrice)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (pair.volatility24h >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (pair.volatility24h >= 0) Color.Green else Color.Red,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${pair.volatility24h}% Vol",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (pair.volatility24h >= 0) Color.Green else Color.Red,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ParameterRowArrangement() = Arrangement.SpaceBetween
