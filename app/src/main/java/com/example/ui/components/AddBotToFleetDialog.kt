package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MarketType
import com.example.engine.MotherBrainEngine
import com.example.ui.theme.*

@Composable
fun AddBotToFleetDialog(
    engine: MotherBrainEngine,
    isSwahili: Boolean,
    onDismiss: () -> Unit
) {
    val allBots by engine.bots.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MarketType?>(null) }
    var showCreateCustomTab by remember { mutableStateOf(false) }

    // Selected bot expansion inputs
    var selectedBotIdForAllocation by remember { mutableStateOf<Int?>(null) }
    var allocationCapitalInput by remember { mutableStateOf("10.0") }
    var allocationProfitPctInput by remember { mutableStateOf("10.0") }

    // Custom bot creation inputs
    var customName by remember { mutableStateOf("") }
    var customPair by remember { mutableStateOf("VOLATILITY 75") }
    var customMarketType by remember { mutableStateOf(MarketType.DERIV) }
    var customStrategy by remember { mutableStateOf("Micro Tick Delta Momentum") }
    var customCapital by remember { mutableStateOf("10.0") }
    var customProfitPct by remember { mutableStateOf("10.0") }

    val filteredBots = remember(allBots, searchQuery, selectedCategory) {
        allBots.filter { bot ->
            val matchesCat = selectedCategory == null || bot.marketType == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    bot.name.contains(searchQuery, ignoreCase = true) ||
                    bot.targetPair.contains(searchQuery, ignoreCase = true) ||
                    bot.strategy.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesSearch
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentCyan),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSwahili) "ONGEZA BOTI KWENYE KIKOSI" else "EXPAND BOT FLEET",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Tabs (Select Existing Bots vs Create Custom Bot)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { showCreateCustomTab = false },
                        shape = RoundedCornerShape(8.dp),
                        color = if (!showCreateCustomTab) AccentCyan.copy(alpha = 0.25f) else SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (!showCreateCustomTab) AccentCyan else BorderSubtle),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isSwahili) "Chagua Boti Iliyopo" else "Existing Bots (${allBots.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (!showCreateCustomTab) AccentCyan else TextMuted
                            )
                        }
                    }

                    Surface(
                        onClick = { showCreateCustomTab = true },
                        shape = RoundedCornerShape(8.dp),
                        color = if (showCreateCustomTab) AccentNeonGreen.copy(alpha = 0.25f) else SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showCreateCustomTab) AccentNeonGreen else BorderSubtle),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isSwahili) "+ Unda Boti Mpya" else "+ Create Custom Bot",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (showCreateCustomTab) AccentNeonGreen else TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (showCreateCustomTab) {
                    // --- CREATE BRAND NEW CUSTOM BOT SECTION ---
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = if (isSwahili) "Unda boti mpya maalum na uiongeze kwenye kikosi cha biashara:" else "Configure a custom automated bot and attach it to the active fleet:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                label = { Text(if (isSwahili) "Jina la Boti (mfano: Deriv V75 Scalper)" else "Bot Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = customPair,
                                onValueChange = { customPair = it },
                                label = { Text(if (isSwahili) "Jozi ya Soko (mfano: BTC/USDT, EUR/USD, VOLATILITY 75)" else "Target Market Pair") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = customStrategy,
                                onValueChange = { customStrategy = it },
                                label = { Text(if (isSwahili) "Mbinu / Mkakati (Strategy)" else "Strategy Description") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customCapital,
                                    onValueChange = { customCapital = it },
                                    label = { Text("Mtaji ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = customProfitPct,
                                    onValueChange = { customProfitPct = it },
                                    label = { Text("Lengo la Faida (%)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            // Market Category Chips
                            Text("Aina ya Soko (Market Category):", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(MarketType.values()) { cat ->
                                    val isSel = customMarketType == cat
                                    Surface(
                                        onClick = { customMarketType = cat },
                                        color = if (isSel) AccentCyan.copy(alpha = 0.3f) else SurfaceElevated,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentCyan else BorderSubtle)
                                    ) {
                                        Text(
                                            text = cat.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) AccentCyan else TextMuted,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val name = customName.ifBlank { "Custom Bot ${customPair}" }
                                    val cap = customCapital.toDoubleOrNull() ?: 10.0
                                    val pct = customProfitPct.toDoubleOrNull() ?: 10.0
                                    engine.addNewCustomBot(
                                        name = name,
                                        targetPair = customPair,
                                        marketType = customMarketType,
                                        strategy = customStrategy.ifBlank { "Autonomous Alpha Engine" },
                                        capital = cap,
                                        profitPct = pct
                                    )
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSwahili) "UNDA & WASHA BOTI KWENYE KIKOSI" else "CREATE & LAUNCH BOT IN FLEET",
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                } else {
                    // --- SELECT EXISTING BOT FROM THE 34 BOTS SECTION ---
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isSwahili) "Tafuta boti, soko au mkakati..." else "Search bots, markets or strategies...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Market Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Surface(
                                onClick = { selectedCategory = null },
                                color = if (selectedCategory == null) AccentCyan.copy(alpha = 0.3f) else SurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCategory == null) AccentCyan else BorderSubtle)
                            ) {
                                Text(
                                    text = if (isSwahili) "Zote (${allBots.size})" else "All (${allBots.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCategory == null) AccentCyan else TextMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        items(MarketType.values()) { cat ->
                            val isSel = selectedCategory == cat
                            val count = allBots.count { it.marketType == cat }
                            Surface(
                                onClick = { selectedCategory = cat },
                                color = if (isSel) AccentCyan.copy(alpha = 0.3f) else SurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentCyan else BorderSubtle)
                            ) {
                                Text(
                                    text = "${cat.displayName} ($count)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) AccentCyan else TextMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // List of Bots
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredBots) { bot ->
                            val isExpanded = selectedBotIdForAllocation == bot.id
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (bot.isActive) AccentNeonGreen.copy(alpha = 0.8f) else (if (isExpanded) AccentCyan else BorderSubtle)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier.size(8.dp).clip(CircleShape).background(if (bot.isActive) AccentNeonGreen else TextMuted)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = bot.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextWhite
                                                )
                                            }
                                            Text(
                                                text = "${bot.targetPair} • ${bot.marketType.displayName} • ${bot.strategy}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }

                                        if (bot.isActive) {
                                            Surface(
                                                color = AccentNeonGreen.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "$${String.format("%.2f", bot.allocatedCapital)} ACTIVE",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = AccentNeonGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    selectedBotIdForAllocation = if (isExpanded) null else bot.id
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.2f), contentColor = AccentCyan),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(if (isExpanded) "Funga" else "+ Washa Boti", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                                            }
                                        }
                                    }

                                    // Expandable allocation inputs if clicked
                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = BorderSubtle, thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = allocationCapitalInput,
                                                onValueChange = { allocationCapitalInput = it },
                                                label = { Text("Mtaji ($)", fontSize = 10.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = allocationProfitPctInput,
                                                onValueChange = { allocationProfitPctInput = it },
                                                label = { Text("Lengo (%)", fontSize = 10.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = {
                                                val cap = allocationCapitalInput.toDoubleOrNull() ?: 10.0
                                                val pct = allocationProfitPctInput.toDoubleOrNull() ?: 10.0
                                                engine.addOrExpandBot(bot.id, cap, pct)
                                                selectedBotIdForAllocation = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen, contentColor = Color.Black),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth().height(36.dp)
                                        ) {
                                            Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isSwahili) "Thibitisha na Washa Boti Hii" else "Confirm & Activate Bot",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
