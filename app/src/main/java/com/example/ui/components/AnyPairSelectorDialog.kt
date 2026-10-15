package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.MarketType
import com.example.data.model.MarketTick
import com.example.ui.theme.*

@Composable
fun AnyPairSelectorDialog(
    ticks: Map<String, MarketTick>,
    currentSelectedSymbol: String,
    isSwahili: Boolean,
    onSelectPair: (String) -> Unit,
    onAddCustomPair: (String, MarketType, Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MarketType?>(null) }

    // Custom pair input fields
    var showAddSection by remember { mutableStateOf(false) }
    var customSymbolInput by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf(MarketType.CRYPTO) }
    var customPriceInput by remember { mutableStateOf("") }

    val filteredTicks = remember(ticks, searchQuery, selectedCategory) {
        ticks.values.filter { tick ->
            val matchesCategory = selectedCategory == null || tick.marketType == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    tick.symbol.contains(searchQuery, ignoreCase = true) ||
                    tick.marketType.displayName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isSwahili) "Chagua au Ongeza Pair Yoyote" else "Universal Pair Selector",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isSwahili) "Biashara ya soko lolote: Crypto, Forex, Deriv, Hisa" else "Trade any market: Crypto, Forex, Deriv, Equities",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (isSwahili) "Tafuta pair (mfano: BTC, EUR, VOL 75, CRDB)..." else "Search symbol (e.g., BTC, EUR, VOL 75, CRDB)...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle Add Custom Pair Section
                Button(
                    onClick = { showAddSection = !showAddSection },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAddSection) MaterialTheme.colorScheme.primary else SurfaceElevated
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        if (showAddSection) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add",
                        tint = if (showAddSection) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showAddSection)
                            (if (isSwahili) "Ficha Jopo la Kuongeza Pair" else "Hide Custom Pair Creator")
                        else
                            (if (isSwahili) "+ Ongeza Pair Yako Mpya (Any Pair)" else "+ Create & Add Any Custom Pair"),
                        fontWeight = FontWeight.Bold,
                        color = if (showAddSection) Color.White else MaterialTheme.colorScheme.primary
                    )
                }

                // Add Custom Pair Form (Collapsible)
                if (showAddSection) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isSwahili) "Ingiza Jina la Pair Mpya:" else "Enter New Pair Symbol:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customSymbolInput,
                                onValueChange = { customSymbolInput = it },
                                placeholder = { Text("mfano: XRP/USDT, AUD/NZD, PEPE, BOOM 500", style = MaterialTheme.typography.bodySmall) },
                                singleLine = true,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isSwahili) "Aina ya Soko:" else "Market Category:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(MarketType.values()) { type ->
                                    val isPicked = customCategory == type
                                    FilterChip(
                                        selected = isPicked,
                                        onClick = { customCategory = type },
                                        label = { Text(if (isSwahili) type.displayNameSwahili else type.displayName, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = customPriceInput,
                                onValueChange = { customPriceInput = it },
                                placeholder = { Text(if (isSwahili) "Bei ya kuanzia (si lazima, acha wazi)" else "Starting price (optional)", style = MaterialTheme.typography.bodySmall) },
                                singleLine = true,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (customSymbolInput.isNotBlank()) {
                                        val price = customPriceInput.toDoubleOrNull()
                                        onAddCustomPair(customSymbolInput, customCategory, price)
                                        showAddSection = false
                                        customSymbolInput = ""
                                        customPriceInput = ""
                                    }
                                },
                                enabled = customSymbolInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isSwahili) "Ongeza & Anza Biashara Sasa" else "Add & Trade This Pair Now",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text(if (isSwahili) "Zote" else "All") }
                        )
                    }
                    items(MarketType.values()) { type ->
                        FilterChip(
                            selected = selectedCategory == type,
                            onClick = { selectedCategory = type },
                            label = { Text(if (isSwahili) type.displayNameSwahili else type.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of filtered pairs
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredTicks) { tick ->
                        val isCurrent = tick.symbol == currentSelectedSymbol
                        val isPositive = tick.change24h >= 0
                        val marketColor = getMarketThemeColor(tick.marketType)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) SurfaceElevated else MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    if (isCurrent) MaterialTheme.colorScheme.primary else BorderSubtle,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSelectPair(tick.symbol)
                                    onDismiss()
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp, 28.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(marketColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tick.symbol,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) MaterialTheme.colorScheme.primary else TextWhite
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Active",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = tick.marketType.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${String.format("%,.2f", tick.price)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextWhite
                                )
                                Text(
                                    text = "${if (isPositive) "+" else ""}${String.format("%.2f", tick.change24h)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) AccentNeonGreen else DangerRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
