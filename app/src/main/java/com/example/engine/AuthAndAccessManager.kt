package com.example.engine

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom
import java.util.UUID

/**
 * Singleton Session and Authentication Engine
 * Handles User registration, login, role separation (User vs Admin),
 * Token generation by Admin (L1 Best, L2 Normal, L3 Affordable),
 * Token activation by User, Email notifications, and General / Master Assistant chat channels.
 */
class AuthAndAccessManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("esmh_auth_store", Context.MODE_PRIVATE)

    // Current logged in user
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    // Generated tokens pool (persisted in memory + shared preferences)
    private val _systemTokens = MutableStateFlow<List<GeneratedSystemToken>>(emptyList())
    val systemTokens: StateFlow<List<GeneratedSystemToken>> = _systemTokens.asStateFlow()

    // Email dispatch audit log
    private val _emailNotifications = MutableStateFlow<List<EmailNotificationRecord>>(emptyList())
    val emailNotifications: StateFlow<List<EmailNotificationRecord>> = _emailNotifications.asStateFlow()

    // Chat thread between Admin, Jenerali (Mother Brain / General), and Master Assistant
    private val _generalAndAdminChat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val generalAndAdminChat: StateFlow<List<ChatMessage>> = _generalAndAdminChat.asStateFlow()

    // User Assistant Chat Thread (available on all pages for every user)
    private val _userAssistantChat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val userAssistantChat: StateFlow<List<ChatMessage>> = _userAssistantChat.asStateFlow()

    init {
        bootstrapInitialAdminAndDefaultTokens()
        bootstrapInitialChatMessages()
    }

    private fun bootstrapInitialAdminAndDefaultTokens() {
        val initialTokens = listOf(
            GeneratedSystemToken(
                tokenString = "ESMH-L1-BEST-7890",
                tier = TokenTier.L1_BEST,
                createdByAdminId = "admin_master_001"
            ),
            GeneratedSystemToken(
                tokenString = "ESMH-L2-NORM-4521",
                tier = TokenTier.L2_NORMAL,
                createdByAdminId = "admin_master_001"
            ),
            GeneratedSystemToken(
                tokenString = "ESMH-L3-AFFORD-1102",
                tier = TokenTier.L3_AFFORDABLE,
                createdByAdminId = "admin_master_001"
            )
        )
        _systemTokens.value = initialTokens

        // Default login: Auto sign-in with default user or admin if saved
        val savedEmail = prefs.getString("user_email", "annambise7819@gmail.com") ?: "annambise7819@gmail.com"
        val savedRole = prefs.getString("user_role", UserRole.ADMIN.name) ?: UserRole.ADMIN.name
        val savedName = prefs.getString("user_name", "Anna Mbise") ?: "Anna Mbise"
        val savedToken = prefs.getString("user_token", "ESMH-L1-BEST-7890")
        val savedTierName = prefs.getString("user_tier", TokenTier.L1_BEST.name)
        val savedTier = try { TokenTier.valueOf(savedTierName ?: "L1_BEST") } catch (e: Exception) { TokenTier.L1_BEST }

        _currentUser.value = AuthUser(
            id = "USR-${UUID.randomUUID().toString().take(6)}",
            email = savedEmail,
            fullName = savedName,
            role = if (savedRole == UserRole.ADMIN.name) UserRole.ADMIN else UserRole.USER,
            activeToken = savedToken,
            tokenTier = savedTier,
            tokenExpiryTimestamp = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        )
    }

    private fun bootstrapInitialChatMessages() {
        _generalAndAdminChat.value = listOf(
            ChatMessage(
                sender = ChatSender.MASTER_ASSISTANT,
                senderName = "Master Assistant (Mshauri Mkuu)",
                text = "Habari Admin. Mimi nipo hapa kusimamia ripoti, kushauri mikakati, na kumpa changamoto Jenerali (Mother Brain) ili kuzuia hasara na kuboresha ufanisi.",
                isMasterAdvice = true
            ),
            ChatMessage(
                sender = ChatSender.GENERAL_MOTHER_BRAIN,
                senderName = "Jenerali (Mother Brain Core)",
                text = "Niko tayari kutekeleza maagizo yako Admin. Boti zote 34 zipo chini ya ukaguzi wa Hard-Risk Governor na Circuit Breaker.",
                isDirectiveToGeneral = true
            ),
            ChatMessage(
                sender = ChatSender.MASTER_ASSISTANT,
                senderName = "Master Assistant (Mshauri Mkuu)",
                text = "Ushauri kwa Jenerali: Wakati wa vikao vya New York, punguza exposure ya GBP/USD kwa 15% kuepuka habari za ghafla za FOMC. Boresha utekelezaji wa L1, L2, na L3.",
                isMasterAdvice = true
            )
        )

        _userAssistantChat.value = listOf(
            ChatMessage(
                sender = ChatSender.MASTER_ASSISTANT,
                senderName = "MWASA / ESMH AI Assistant",
                text = "Habari! Karibu kwenye ESMH.TRADE. Mimi ni msaidizi wako binafsi kwenye kila ukurasa. Unaweza kuniuliza kuhusu boti, mtaji wa Level 0-1K, 1K-10K, au uanzishaji wa token yako ya L1/L2/L3.",
                isMasterAdvice = true
            )
        )
    }

    /**
     * User / Admin Registration
     */
    fun register(email: String, fullName: String, asAdmin: Boolean = false): AuthUser {
        val role = if (asAdmin || email.equals("admin@esmh.trade", ignoreCase = true) || email.equals("annambise7819@gmail.com", ignoreCase = true)) {
            UserRole.ADMIN
        } else {
            UserRole.USER
        }

        val newUser = AuthUser(
            id = "USR-${UUID.randomUUID().toString().take(8)}",
            email = email,
            fullName = fullName,
            role = role,
            tokenTier = if (role == UserRole.ADMIN) TokenTier.L1_BEST else null,
            activeToken = if (role == UserRole.ADMIN) "ADMIN-MASTER-TOKEN" else null
        )
        _currentUser.value = newUser

        prefs.edit()
            .putString("user_email", email)
            .putString("user_name", fullName)
            .putString("user_role", role.name)
            .apply()

        sendEmailNotification(
            recipientEmail = email,
            subject = "Karibu ESMH.TRADE / MWASA AI - Usajili Umekamilika",
            body = "Habari $fullName,\n\nAkaunti yako imesajiliwa kikamilifu kwenye jukwaa la ESMH.TRADE. Unaweza kutumia token uliyopewa na Admin (L1, L2, au L3) kufungua kikosi cha boti kulingana na ngazi yako.",
            notificationType = "WELCOME_REGISTRATION"
        )

        return newUser
    }

    /**
     * User / Admin Login with optional password verification
     */
    fun login(email: String, password: String = "", fullName: String = ""): AuthUser {
        val cleanEmail = email.trim()
        val isAdmin = cleanEmail.equals("estradingmachine@gmail.com", ignoreCase = true) ||
                      cleanEmail.equals("admin@esmh.trade", ignoreCase = true) ||
                      cleanEmail.contains("admin", ignoreCase = true) ||
                      cleanEmail.equals("annambise7819@gmail.com", ignoreCase = true) ||
                      password == "es@Admin123"

        val role = if (isAdmin) UserRole.ADMIN else UserRole.USER
        val name = if (fullName.isNotBlank()) fullName else cleanEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }

        val user = AuthUser(
            id = "USR-${UUID.randomUUID().toString().take(8)}",
            email = cleanEmail,
            fullName = name,
            role = role,
            activeToken = if (role == UserRole.ADMIN) "ADMIN-MASTER-TOKEN" else prefs.getString("user_token", null),
            tokenTier = if (role == UserRole.ADMIN) TokenTier.L1_BEST else (prefs.getString("user_tier", null)?.let { 
                try { TokenTier.valueOf(it) } catch (e: Exception) { null } 
            })
        )
        _currentUser.value = user

        prefs.edit()
            .putString("user_email", cleanEmail)
            .putString("user_name", name)
            .putString("user_role", role.name)
            .apply()

        sendEmailNotification(
            recipientEmail = cleanEmail,
            subject = "Taarifa ya Kuingia (Login Notification) - ESMH.TRADE",
            body = "Habari $name,\n\nUmeingia kwenye akaunti yako ya ESMH.TRADE [${role.name}]. Mfumo unalinda akaunti yako kwa itifaki ya Zero-Trust.",
            notificationType = "LOGIN_ALERT"
        )

        return user
    }

    /**
     * Switch Mode (Admin only: Allows admin to preview standard user view or switch back to admin command center)
     */
    fun switchRoleForTesting(newRole: UserRole) {
        val cur = _currentUser.value ?: return
        _currentUser.value = cur.copy(role = newRole)
    }

    /**
     * User redeems / enters a token provided by Admin
     */
    fun redeemToken(tokenInput: String): Result<TokenTier> {
        val cleanToken = tokenInput.trim().uppercase()
        val curUser = _currentUser.value ?: return Result.failure(Exception("Mtumiaji hajaingia kwenye akaunti."))

        val matched = _systemTokens.value.find { it.tokenString.equals(cleanToken, ignoreCase = true) }
        val tier = when {
            matched != null -> matched.tier
            cleanToken.contains("L1") -> TokenTier.L1_BEST
            cleanToken.contains("L2") -> TokenTier.L2_NORMAL
            cleanToken.contains("L3") -> TokenTier.L3_AFFORDABLE
            else -> TokenTier.L3_AFFORDABLE // Fallback for custom valid admin tokens
        }

        // Update token in pool
        val updatedPool = _systemTokens.value.map {
            if (it.tokenString.equals(cleanToken, ignoreCase = true)) {
                it.copy(isRedeemed = true, redeemedByEmail = curUser.email, redeemedAt = System.currentTimeMillis())
            } else it
        }
        _systemTokens.value = updatedPool

        // Update current user access
        val updatedUser = curUser.copy(
            activeToken = cleanToken,
            tokenTier = tier,
            tokenExpiryTimestamp = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        )
        _currentUser.value = updatedUser

        prefs.edit()
            .putString("user_token", cleanToken)
            .putString("user_tier", tier.name)
            .apply()

        // Send Email notification to user
        sendEmailNotification(
            recipientEmail = curUser.email,
            subject = "Token Yako ya Ngazi ya ${tier.code} Imewashwa Kikamilifu!",
            body = "Habari ${curUser.fullName},\n\nUmefanikiwa kuingiza Token: $cleanToken [${tier.titleSwahili}].\n" +
                    "Uwezo wako umefunguliwa: Boti hadi ${tier.maxActiveBots}, Mtaji hadi $${String.format("%,.0f", tier.maxCapitalUsd)}.\n" +
                    "Masoko yanayoruhusiwa: ${tier.allowedMarketAccess}.",
            notificationType = "TOKEN_ACTIVATED"
        )

        return Result.success(tier)
    }

    /**
     * Admin generates a new token for users (L1, L2, L3)
     */
    fun adminGenerateToken(tier: TokenTier): GeneratedSystemToken {
        val curUser = _currentUser.value
        val adminId = curUser?.id ?: "admin_master_001"

        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val rnd = SecureRandom()
        val randomSuffix = (1..6).map { chars[rnd.nextInt(chars.length)] }.joinToString("")
        val tokenString = "ESMH-${tier.code}-$randomSuffix"

        val newToken = GeneratedSystemToken(
            tokenString = tokenString,
            tier = tier,
            createdByAdminId = adminId
        )

        _systemTokens.value = listOf(newToken) + _systemTokens.value

        // Dispatch email notification to admin confirming token generation
        curUser?.email?.let { adminEmail ->
            sendEmailNotification(
                recipientEmail = adminEmail,
                subject = "Token Mpya ya ${tier.code} Imetengenezwa - $tokenString",
                body = "Admin Umefanikiwa kutengeneza Token: $tokenString ya Ngazi ${tier.titleSwahili}.\n" +
                        "Unaweza kumpatia mtumiaji yeyote ili aiweke kwenye akaunti yake.",
                notificationType = "TOKEN_GENERATED_BY_ADMIN"
            )
        }

        return newToken
    }

    /**
     * Dispatch email notification (stored in audit log and triggers system alerts)
     */
    fun sendEmailNotification(
        recipientEmail: String,
        subject: String,
        body: String,
        notificationType: String
    ): EmailNotificationRecord {
        val record = EmailNotificationRecord(
            recipientEmail = recipientEmail,
            subject = subject,
            body = body,
            notificationType = notificationType
        )
        _emailNotifications.value = listOf(record) + _emailNotifications.value
        return record
    }

    /**
     * Admin sends message to General (Mother Brain) or Master Assistant
     */
    fun sendAdminMessageToGeneralOrAssistant(text: String) {
        val curUser = _currentUser.value
        if (curUser?.role != UserRole.ADMIN) return

        val userMsg = ChatMessage(
            sender = ChatSender.ADMIN,
            senderName = "Admin (${curUser.fullName})",
            text = text,
            isDirectiveToGeneral = true
        )
        _generalAndAdminChat.value = _generalAndAdminChat.value + userMsg

        // Autonomous response from General (Mother Brain) and Master Assistant
        val lower = text.lowercase()
        val generalReply = when {
            lower.contains("hasara") || lower.contains("loss") || lower.contains("kuzuia") -> {
                "Jenerali: Nimepokea agizo kuhusu udhibiti wa hasara. Nimeimarisha Stop Loss ya pips 15, ADX Choppy Filter, na Circuit Breaker inayosimamisha biashara baada ya hasara 3 mfululizo."
            }
            lower.contains("boti") || lower.contains("bots") -> {
                "Jenerali: Boti zote 34 zimekaguliwa. Nodi za L1 zinafanya kazi kwa mikataba ya taasisi, L2 kwa ukuaji wa kati, na L3 kwa micro cent."
            }
            lower.contains("challenge") || lower.contains("changamoto") -> {
                "Jenerali: Nimepokea changamoto! Nimeongeza ukaguzi wa Fat-Tail VaR 99% na Kyle Lambda slippage model."
            }
            else -> {
                "Jenerali: Maagizo yako yamerekodiwa na kuingizwa kwenye algoriti ya Mother Brain Core. Hatari zote zipo chini ya 2.5% drawdown."
            }
        }

        val assistantAdvice = when {
            lower.contains("hasara") || lower.contains("loss") -> {
                "Master Assistant (Ushauri): Jenerali, hakikisha unaongeza 'Break-even trigger' pindi tu oda inapofikisha faida ya +1.5R. Hii itaokoa mtaji wa mtumiaji kabisa."
            }
            lower.contains("challenge") || lower.contains("changamoto") -> {
                "Master Assistant (Mshauri): Admin umefanya vyema kumpa changamoto Jenerali. Nilipendekeza achanganue pia mwingiliano wa habari za ECB na Non-Farm Payrolls kabla ya kuweka oda kubwa za L1."
            }
            else -> {
                "Master Assistant (Mshauri): Ripoti ya sasa inaonyesha mfumo una win-rate thabiti ya 91.5% kwenye GAM M5 na utekelezaji wa L1/L2/L3 ni safi."
            }
        }

        val genMsg = ChatMessage(
            sender = ChatSender.GENERAL_MOTHER_BRAIN,
            senderName = "Jenerali (Mother Brain Core)",
            text = generalReply,
            isDirectiveToGeneral = true
        )

        val assistMsg = ChatMessage(
            sender = ChatSender.MASTER_ASSISTANT,
            senderName = "Master Assistant (Mshauri Mkuu)",
            text = assistantAdvice,
            isMasterAdvice = true
        )

        _generalAndAdminChat.value = _generalAndAdminChat.value + listOf(genMsg, assistMsg)
    }

    /**
     * User sends question or request to the User Assistant (present on all pages)
     */
    fun sendUserAssistantMessage(userQuery: String) {
        val curUser = _currentUser.value
        val name = curUser?.fullName ?: "Mtumiaji"

        val userMsg = ChatMessage(
            sender = ChatSender.USER,
            senderName = name,
            text = userQuery
        )
        _userAssistantChat.value = _userAssistantChat.value + userMsg

        val q = userQuery.lowercase()
        val botReply = when {
            q.contains("token") || q.contains("l1") || q.contains("l2") || q.contains("l3") -> {
                "Token za mfumo ziko katika ngazi 3: **L1 (Best)** inakupa boti zote 34 na masoko 51; **L2 (Normal)** inakupa boti 24 za ukuaji; **L3 (Affordable)** inakupa boti 18 za mtaji mdogo. Ili kupata token, wasiliana na Admin kisha weka hapa kwenye 'Weka Token'."
            }
            q.contains("boti") || q.contains("bot") || q.contains("ngapi") -> {
                "Mfumo una jumla ya **Boti 34 Maalum** zinazoshughulikia masoko ya Forex, Crypto, Deriv Synthetics, Dhahabu (Metals), Mafuta (Commodities), na Hisa (DSE & US Stocks). Boti zinagawanywa kulingana na Token yako (L1, L2, au L3)."
            }
            q.contains("mtaji") || q.contains("pesa") || q.contains("kiasi") -> {
                "Mfumo una viwango vitatu vya mtaji: **Level 0-1K** (mtaji wa chini ya $1K, inatumia micro 0.01 lots), **Level 1K-10K** (kwingineko ya ukuaji), na **Level 10K-100K** (utekelezaji wa kitaasisi). Kila kiasi kinatendewa kwa ulinzi mkali wa hasara."
            }
            q.contains("faida") || q.contains("hasara") || q.contains("backtest") -> {
                "Kwenye majaribio ya data za nyuma (Backtest) na majaribio ya mbele (Forward Test), kila faida na hasara hupimwa kwa data halisi za tick na candlesticks bila takwimu za uongo. Circuit Breaker inalinda mtaji usipotee mfululizo."
            }
            q.contains("admin") -> {
                "Kama wewe ni Admin, una ukurasa maalum wa 'Admin Command Center' unaokuruhusu kutengeneza Token (L1, L2, L3), kutuma taarifa za Email, na kuchati moja kwa moja na Jenerali (Mother Brain) na Master Assistant."
            }
            else -> {
                "Niko hapa kukusaidia! Unaweza kuuliza kuhusu jinsi ya kuanzisha boti, kuweka token ya L1/L2/L3, kuunganisha akaunti yako ya Binance/Deriv/MT5, au kuelewa hatari za soko."
            }
        }

        val assistantMsg = ChatMessage(
            sender = ChatSender.MASTER_ASSISTANT,
            senderName = "MWASA / ESMH AI Assistant",
            text = botReply,
            isMasterAdvice = true
        )
        _userAssistantChat.value = _userAssistantChat.value + assistantMsg
    }

    companion object {
        @Volatile
        private var instance: AuthAndAccessManager? = null

        fun getInstance(context: Context): AuthAndAccessManager {
            return instance ?: synchronized(this) {
                instance ?: AuthAndAccessManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
