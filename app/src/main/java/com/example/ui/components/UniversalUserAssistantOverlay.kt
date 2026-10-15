package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatSender
import com.example.engine.AuthAndAccessManager
import com.example.ui.theme.*

/**
 * Universal Floating User Assistant (Inapatikana kwenye kila Ukurasa)
 * Users can tap to expand, ask questions, get guidance on Bots, Capital Levels, Tokens, and Loss Prevention.
 */
@Composable
fun UniversalUserAssistantOverlay(
    isSwahili: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authManager = remember { AuthAndAccessManager.getInstance(context) }
    val chatMessages by authManager.userAssistantChat.collectAsState()
    val currentUser by authManager.currentUser.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }
    var userQueryText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty() && isExpanded) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Expanded Glassmorphism Chat Window
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandIn(expandFrom = Alignment.BottomEnd),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.BottomEnd)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassmorphismDefaults.GlassSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GlassmorphismDefaults.GlassBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .width(360.dp)
                    .height(480.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp))
                    .padding(bottom = 60.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Surface(
                        color = SurfaceCard.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(AccentCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = "AI Assistant",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isSwahili) "Msaidizi wa ESMH / MWASA" else "ESMH / MWASA Assistant",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = TextWhite,
                                        fontSize = 12.sp
                                    )
                                    val tierLabel = currentUser?.tokenTier?.code ?: "Hana Token"
                                    Text(
                                        text = if (isSwahili) "Msaada wa Moja kwa Moja • $tierLabel" else "Live Assistant • $tierLabel",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentGold,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isExpanded = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                            }
                        }
                    }

                    // Chat messages list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isUser = msg.sender == ChatSender.USER
                            val bubbleBg = if (isUser) AccentElectricBlue.copy(alpha = 0.25f) else SurfaceElevated.copy(alpha = 0.95f)
                            val bubbleBorder = if (isUser) AccentElectricBlue else BorderSubtle

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                            ) {
                                Text(
                                    text = msg.senderName,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) AccentElectricBlue else AccentCyan
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    color = bubbleBg,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, bubbleBorder),
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Input Row
                    Surface(
                        color = SurfaceCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = userQueryText,
                                onValueChange = { userQueryText = it },
                                placeholder = { Text(if (isSwahili) "Uliza swali au msaada..." else "Ask for assistance...", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                )
                            )

                            IconButton(
                                onClick = {
                                    if (userQueryText.isNotBlank()) {
                                        authManager.sendUserAssistantMessage(userQueryText)
                                        userQueryText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AccentCyan)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Bubble (Always Visible on all pages)
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = if (isExpanded) AccentGold else AccentCyan,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .size(54.dp)
                .shadow(12.dp, CircleShape)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ChatBubble else Icons.Default.SmartToy,
                contentDescription = "User Assistant",
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
