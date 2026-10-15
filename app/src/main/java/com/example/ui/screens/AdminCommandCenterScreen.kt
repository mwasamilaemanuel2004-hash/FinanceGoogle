package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import com.example.data.model.ChatSender
import com.example.data.model.TokenTier
import com.example.data.model.UserRole
import com.example.engine.AuthAndAccessManager
import com.example.ui.theme.*

/**
 * Admin Command Center Screen
 * Dedicated portal for Administrator:
 * 1. Generate Tokens (L1 Best, L2 Normal, L3 Affordable)
 * 2. View Active Generated Tokens & Redemptions
 * 3. Chat with Jenerali (Mother Brain Core) & Master Assistant
 * 4. Email Notification Dispatch & Logs
 * 5. Role Switcher for previewing standard user interface
 */
@Composable
fun AdminCommandCenterScreen(
    isSwahili: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthAndAccessManager.getInstance(context) }
    val currentUser by authManager.currentUser.collectAsState()
    val systemTokens by authManager.systemTokens.collectAsState()
    val emailLogs by authManager.emailNotifications.collectAsState()
    val generalChat by authManager.generalAndAdminChat.collectAsState()

    var selectedTierToGenerate by remember { mutableStateOf(TokenTier.L1_BEST) }
    var newlyGeneratedToken by remember { mutableStateOf<String?>(null) }
    var adminChatInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Tokens, 1 = Chat Jenerali, 2 = Emails

    val isAdmin = currentUser?.role == UserRole.ADMIN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isSwahili) "KITUO KIKUU CHA ADMIN (COMMAND CENTER)" else "ADMIN COMMAND CENTER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = AccentGold
                    )
                    Text(
                        text = "Admin: ${currentUser?.fullName ?: "Super Admin"} (${currentUser?.email})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // Role Badge / Switcher
            Surface(
                color = AccentGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAdmin) "ADMIN ACTIVE" else "USER PREVIEW",
                        fontWeight = FontWeight.Black,
                        color = AccentGold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Shareable Admin Link Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSwahili) "Kiungo cha Kuingia Admin (Admin Portal Link):" else "Admin Portal Share Link:",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val adminLink = "https://esmh.trade/admin?auth=estradingmachine@gmail.com&token=ADMIN-MASTER-TOKEN"
                Text(
                    text = adminLink,
                    color = AccentCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                var copiedFeedback by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        copiedFeedback = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (copiedFeedback) (if (isSwahili) "✅ Imenakiliwa kwenye Kumbukumbu!" else "✅ Link Copied!") else (if (isSwahili) "Nakili Kiungo cha Admin" else "Copy Admin Link"),
                        color = DarkCharcoal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceElevated,
            contentColor = AccentGold,
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (isSwahili) "1. Tengeneza Token" else "1. Token Manager", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (isSwahili) "2. Chat Jenerali & Master" else "2. General & Master Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(if (isSwahili) "3. Taarifa za Email" else "3. Email Notifications", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Tab 0: Token Generation & Pool
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GlassmorphismDefaults.GlassSurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (isSwahili) "Tengeneza Token Mpya kwa Watumiaji" else "Generate User Access Token",
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = if (isSwahili) 
                                        "Chagua ngazi ya Token. L1 ndio bora zaidi (masoko 51 na boti zote 34), L2 ni ya kawaida (ukuaji), na L3 ni ya nafuu (micro cent)." 
                                    else 
                                        "Select Token level: L1 Best (all 51 markets & 34 bots), L2 Normal (growth), L3 Affordable (micro cent).",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TokenTier.values().forEach { tier ->
                                        val isSel = selectedTierToGenerate == tier
                                        val tierColor = when (tier) {
                                            TokenTier.L1_BEST -> AccentGold
                                            TokenTier.L2_NORMAL -> AccentCyan
                                            TokenTier.L3_AFFORDABLE -> AccentNeonGreen
                                        }
                                        Surface(
                                            onClick = { selectedTierToGenerate = tier },
                                            color = if (isSel) tierColor.copy(alpha = 0.25f) else SurfaceElevated,
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) tierColor else BorderSubtle),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(tier.code, fontWeight = FontWeight.Black, color = tierColor, fontSize = 14.sp)
                                                Text(
                                                    text = if (tier == TokenTier.L1_BEST) "Bora (Best)" else if (tier == TokenTier.L2_NORMAL) "Kawaida" else "Nafuu",
                                                    fontSize = 9.sp,
                                                    color = TextWhite
                                                )
                                                Text(
                                                    text = "Boti ${tier.maxActiveBots}",
                                                    fontSize = 8.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val tok = authManager.adminGenerateToken(selectedTierToGenerate)
                                        newlyGeneratedToken = tok.tokenString
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSwahili) "Tengeneza Token ya ${selectedTierToGenerate.code}" else "Generate ${selectedTierToGenerate.code} Token",
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                newlyGeneratedToken?.let { tokenStr ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        color = AccentNeonGreen.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentNeonGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = if (isSwahili) "✅ TOKEN MPYA IMETENGENEZWA:" else "✅ NEW TOKEN CREATED:",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentNeonGreen
                                            )
                                            Text(
                                                text = tokenStr,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black,
                                                color = TextWhite
                                            )
                                            Text(
                                                text = if (isSwahili) "Mtumiaji anaweza kuingiza token hii ili kuanza kutumia mara moja." else "Give this token to user for immediate activation.",
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = if (isSwahili) "ORODHA YA TOKEN ZILIZOPO KWENYE MFUMO" else "SYSTEM ACTIVE TOKENS REGISTRY",
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan,
                            fontSize = 11.sp
                        )
                    }

                    items(systemTokens) { token ->
                        val tierColor = when (token.tier) {
                            TokenTier.L1_BEST -> AccentGold
                            TokenTier.L2_NORMAL -> AccentCyan
                            TokenTier.L3_AFFORDABLE -> AccentNeonGreen
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (token.isRedeemed) BorderSubtle else tierColor.copy(alpha = 0.6f)),
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = tierColor.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, tierColor)
                                        ) {
                                            Text(
                                                text = token.tier.code,
                                                fontWeight = FontWeight.Bold,
                                                color = tierColor,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = token.tokenString, fontWeight = FontWeight.Black, color = TextWhite, fontSize = 13.sp)
                                    }
                                    Text(
                                        text = "${token.tier.titleSwahili} • Boti ${token.tier.maxActiveBots} • Max $${String.format("%,.0f", token.tier.maxCapitalUsd)}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    if (token.isRedeemed) {
                                        Text(
                                            text = "Imetumiwa na: ${token.redeemedByEmail ?: "User"}",
                                            fontSize = 9.sp,
                                            color = AccentCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Surface(
                                    color = if (token.isRedeemed) BorderSubtle else AccentNeonGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (token.isRedeemed) "USED" else "ACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (token.isRedeemed) TextMuted else AccentNeonGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: Chat Jenerali (Mother Brain) & Master Assistant
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        color = GlassmorphismDefaults.GlassSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassmorphismDefaults.GlassBorderGold),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSwahili) "CHANNEL YA ADMIN PEKEE (Jenerali & Master Assistant)" else "ADMIN-EXCLUSIVE STRATEGY CHANNEL",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = AccentGold
                                )
                            }
                            Text(
                                text = if (isSwahili) 
                                    "Watumiaji wa L1, L2, na L3 hawana ruhusa ya kuchat na Jenerali. Hii ni kwa Admin pekee. Master Assistant anatoa ushauri na kumpa changamoto Jenerali kuzuia hasara." 
                                else 
                                    "L1, L2, L3 users cannot chat with the General. Only Admin communicates here. Master Assistant audits and challenges the General.",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(generalChat) { msg ->
                            val isGen = msg.sender == ChatSender.GENERAL_MOTHER_BRAIN
                            val isMaster = msg.sender == ChatSender.MASTER_ASSISTANT
                            val isAdminMsg = msg.sender == ChatSender.ADMIN

                            val cardBg = when {
                                isGen -> SurfaceElevated
                                isMaster -> Color(0xFF1E1338) // Deep Purple / Master Assistant
                                else -> AccentElectricBlue.copy(alpha = 0.2f)
                            }
                            val borderC = when {
                                isGen -> AccentCyan
                                isMaster -> AccentLavender
                                else -> AccentGold
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderC.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = msg.senderName,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = borderC
                                        )
                                        Text(
                                            text = if (isGen) "ALGO CORE" else if (isMaster) "ADVISOR" else "DIRECTIVE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.text,
                                        fontSize = 12.sp,
                                        color = TextWhite,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = adminChatInput,
                            onValueChange = { adminChatInput = it },
                            placeholder = { Text(if (isSwahili) "Toa agizo au changamoto kwa Jenerali..." else "Dispatch directive or challenge...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                if (adminChatInput.isNotBlank()) {
                                    authManager.sendAdminMessageToGeneralOrAssistant(adminChatInput)
                                    adminChatInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Email Notifications Log
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = if (isSwahili) "REKODI ZA TAARIFA ZILIZOTUMWA KWA EMAIL" else "EMAIL NOTIFICATIONS DISPATCH AUDIT",
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan,
                            fontSize = 11.sp
                        )
                    }

                    items(emailLogs) { mail ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "To: ${mail.recipientEmail}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                    Surface(
                                        color = AccentNeonGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = mail.status,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentNeonGreen,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mail.subject,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mail.body,
                                    color = TextMuted,
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
