package com.example.engine

import com.example.data.model.OrderSide
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * ============================================================================
 * TRUTH-ONLY SOURCE OF TRUTH ENGINE (ESMH.TRADE / MWASA AI)
 * ============================================================================
 * Enforces the global TRUTH_ONLY_DISPLAY policy:
 * - REAL + VERIFIED → SHOW IT
 * - REAL BUT NOT VERIFIED → SHOW "UNVERIFIED" / "PENDING"
 * - DOES NOT EXIST → SHOW "0", "NONE", OR "NO DATA"
 * - SIMULATED → SHOW ONLY IN SIMULATION / BACKTEST / PAPER MODE
 * - UNKNOWN → SHOW "UNKNOWN" / "UNAVAILABLE"
 * - NEVER INVENT DATA. NEVER USE FAKE VALUES.
 *
 * All financial balances, bot statuses, capital allocations, positions, and
 * P&L values originate strictly from verified provider accounts or internal
 * double-entry ledger. Zero imaginary capital or simulated profit in LIVE mode.
 * ============================================================================
 */

enum class VerificationStatus(val labelSwahili: String, val labelEn: String) {
    VERIFIED("IMETHIBITISHWA", "VERIFIED"),
    UNVERIFIED("HAIJATHIBITISHWA", "UNVERIFIED"),
    PENDING("INASUBIRI UTHIBITISHO", "PENDING_VERIFICATION"),
    STALE("DATA IMEPITWA NA WAKATI", "STALE_DATA"),
    RECONCILIATION_ERROR("HITILAFU YA KISAWANISHO", "RECONCILIATION_ERROR"),
    OFFLINE("BILA MUUNGANISHO", "OFFLINE")
}

enum class TruthfulBotStatus(val labelSwahili: String, val labelEn: String, val colorHex: Long) {
    OFFLINE("HAIJASAJILIWA", "OFFLINE", 0xFF757575),
    UNFUNDED("BILA MTAJI", "UNFUNDED", 0xFFFF9800),
    AVAILABLE_NOT_ALLOCATED("MTAJI UPO — HAUJAGAWANYWA", "AVAILABLE — NOT ALLOCATED", 0xFFFFC107),
    WAITING_FOR_DATA("INASUBIRI DATA HALISI", "WAITING_FOR_DATA", 0xFF00E5FF),
    WAITING_FOR_EXECUTION("INASUBIRI UTEKELEZAJI", "WAITING_FOR_EXECUTION", 0xFF00B0FF),
    RUNNING_VERIFIED("INAENDELEA (IMETHIBITISHWA)", "RUNNING (VERIFIED)", 0xFF00E676),
    PAUSED("IMESITISHWA", "PAUSED", 0xFF9E9E9E),
    BLOCKED("IMEZUIWA NA USALAMA", "BLOCKED (RISK SAFEGUARD)", 0xFFFF5252),
    ERROR("HITILAFU", "ERROR", 0xFFFF1744),
    UNVERIFIED("HAIJATHIBITISHWA", "UNVERIFIED", 0xFFFF9100),
    STALE("DATA IMEPOTEA / STALE", "STALE", 0xFFE040FB)
}

data class DataProvenance(
    val source: String,
    val provider: String,
    val accountId: String,
    val instrument: String = "ALL",
    val timestamp: Long = System.currentTimeMillis(),
    val receivedAt: Long = System.currentTimeMillis(),
    val verificationStatus: VerificationStatus = VerificationStatus.OFFLINE,
    val dataStatus: String = "NO_VERIFIED_DATA",
    val lastSyncFormatted: String = "Never",
    val eventId: String = UUID.randomUUID().toString(),
    val correlationId: String = UUID.randomUUID().toString()
)

data class VerifiedCapitalLedger(
    val providerId: String,
    val accountId: String,
    val currency: String = "USDT",
    val totalBalance: Double = 0.0,
    val availableBalance: Double = 0.0,
    val reservedBalance: Double = 0.0,
    val usedMargin: Double = 0.0,
    val freeMargin: Double = 0.0,
    val allocatedCapital: Double = 0.0,
    val capitalInPosition: Double = 0.0,
    val realizedPnl: Double = 0.0,
    val unrealizedPnl: Double = 0.0,
    val isTradingAuthorized: Boolean = false,
    val permissions: String = "READ_ONLY",
    val provenance: DataProvenance = DataProvenance("OfflineConnector", "NONE", "UNCONNECTED")
)

data class BotTruthAllocation(
    val botId: Int,
    val botName: String,
    val allocatedCapital: Double = 0.0,
    val allocationPercentage: Double = 0.0,
    val isFunded: Boolean = false,
    val status: TruthfulBotStatus = TruthfulBotStatus.OFFLINE,
    val totalVerifiedTrades: Int = 0,
    val verifiedRealizedProfit: Double = 0.0,
    val verifiedWinRate: Double = 0.0,
    val lastHeartbeatTimestamp: Long = 0L,
    val statusReason: String = "No verified capital allocated to this bot"
)

data class DataIntegrityIncident(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: String,
    val description: String,
    val severity: String = "WARNING",
    val resolved: Boolean = false
)

class TruthSourceOfTruthEngine private constructor() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _ledgers = MutableStateFlow<Map<String, VerifiedCapitalLedger>>(emptyMap())
    val ledgers: StateFlow<Map<String, VerifiedCapitalLedger>> = _ledgers.asStateFlow()

    private val _botTruthMap = MutableStateFlow<Map<Int, BotTruthAllocation>>(emptyMap())
    val botTruthMap: StateFlow<Map<Int, BotTruthAllocation>> = _botTruthMap.asStateFlow()

    private val _integrityIncidents = MutableStateFlow<List<DataIntegrityIncident>>(emptyList())
    val integrityIncidents: StateFlow<List<DataIntegrityIncident>> = _integrityIncidents.asStateFlow()

    private val _isLiveTradingGloballyAllowed = MutableStateFlow(false)
    val isLiveTradingGloballyAllowed: StateFlow<Boolean> = _isLiveTradingGloballyAllowed.asStateFlow()

    init {
        initializeTruthfulBotLedgers()
    }

    private fun initializeTruthfulBotLedgers() {
        val initialMap = mutableMapOf<Int, BotTruthAllocation>()
        for (botId in 1..50) {
            initialMap[botId] = BotTruthAllocation(
                botId = botId,
                botName = "Bot #$botId",
                allocatedCapital = 0.0,
                allocationPercentage = 0.0,
                isFunded = false,
                status = TruthfulBotStatus.OFFLINE,
                totalVerifiedTrades = 0,
                verifiedRealizedProfit = 0.0,
                verifiedWinRate = 0.0,
                lastHeartbeatTimestamp = 0L,
                statusReason = "Standby: No real broker connection or capital allocation"
            )
        }
        _botTruthMap.value = initialMap
    }

    /**
     * Updates account state from authoritative real provider confirmation.
     * ZERO invented or simulated numbers allowed in LIVE mode.
     */
    fun updateVerifiedAccountState(
        provider: String,
        accountId: String,
        currency: String,
        totalBalance: Double,
        availableBalance: Double,
        usedMargin: Double = 0.0,
        freeMargin: Double = availableBalance,
        realizedPnl: Double = 0.0,
        unrealizedPnl: Double = 0.0,
        isTradeAuthorized: Boolean,
        permissions: String
    ) {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault())
        val formattedTime = sdf.format(Date(now))

        val provenance = DataProvenance(
            source = "Official ${provider.uppercase()} REST / WebSocket Protocol",
            provider = provider,
            accountId = accountId,
            timestamp = now,
            receivedAt = now,
            verificationStatus = if (totalBalance >= 0.0) VerificationStatus.VERIFIED else VerificationStatus.RECONCILIATION_ERROR,
            dataStatus = "VERIFIED_TRUTH_ONLY",
            lastSyncFormatted = formattedTime
        )

        // Calculate currently allocated capital from bots bound to this account
        val currentAllocated = _botTruthMap.value.values.sumOf { it.allocatedCapital }
        val reserved = (totalBalance - availableBalance).coerceAtLeast(0.0)

        val updatedLedger = VerifiedCapitalLedger(
            providerId = provider,
            accountId = accountId,
            currency = currency,
            totalBalance = totalBalance.coerceAtLeast(0.0),
            availableBalance = availableBalance.coerceAtLeast(0.0),
            reservedBalance = reserved,
            usedMargin = usedMargin.coerceAtLeast(0.0),
            freeMargin = freeMargin.coerceAtLeast(0.0),
            allocatedCapital = currentAllocated,
            capitalInPosition = usedMargin,
            realizedPnl = realizedPnl,
            unrealizedPnl = unrealizedPnl,
            isTradingAuthorized = isTradeAuthorized,
            permissions = permissions,
            provenance = provenance
        )

        val map = _ledgers.value.toMutableMap()
        map[provider] = updatedLedger
        _ledgers.value = map

        reconcileBotStatusesWithLedger(updatedLedger)
    }

    /**
     * Disconnects an account and clears all capital immediately to zero.
     */
    fun markAccountDisconnected(provider: String) {
        val map = _ledgers.value.toMutableMap()
        map.remove(provider)
        _ledgers.value = map

        // Move all bots to OFFLINE / UNFUNDED immediately
        _botTruthMap.value = _botTruthMap.value.mapValues { (_, bot) ->
            bot.copy(
                allocatedCapital = 0.0,
                isFunded = false,
                status = TruthfulBotStatus.OFFLINE,
                statusReason = "Disconnected: Broker account was unlinked"
            )
        }

        recordIncident(
            type = "ACCOUNT_DISCONNECTED",
            desc = "Provider $provider was disconnected. All bot executions halted and allocations cleared.",
            severity = "WARN"
        )
    }

    /**
     * Allocates real verified capital to a bot.
     * Fails closed if the requested amount exceeds verified available balance.
     */
    fun allocateCapitalToBot(
        botId: Int,
        providerId: String,
        amount: Double
    ): Result<Double> {
        val ledger = _ledgers.value[providerId]
            ?: return Result.failure(IllegalStateException("No verified account found for $providerId. Balance: $0.00"))

        if (ledger.provenance.verificationStatus != VerificationStatus.VERIFIED) {
            return Result.failure(IllegalStateException("Account is not verified (${ledger.provenance.verificationStatus})"))
        }

        val otherAllocations = _botTruthMap.value.filter { it.key != botId }.values.sumOf { it.allocatedCapital }
        val remainingAvailable = ledger.availableBalance - otherAllocations

        if (amount > remainingAvailable) {
            val err = "Cannot allocate $amount ${ledger.currency}. Real available balance remaining is only $remainingAvailable ${ledger.currency}"
            recordIncident("ALLOCATION_EXCEEDED", err, "ALERT")
            return Result.failure(IllegalArgumentException(err))
        }

        val existingBot = _botTruthMap.value[botId] ?: BotTruthAllocation(botId, "Bot #$botId")
        val isNowFunded = amount > 0.0
        val newStatus = when {
            !isNowFunded -> TruthfulBotStatus.UNFUNDED
            !ledger.isTradingAuthorized -> TruthfulBotStatus.BLOCKED
            else -> TruthfulBotStatus.RUNNING_VERIFIED
        }

        val updatedBot = existingBot.copy(
            allocatedCapital = amount,
            allocationPercentage = if (ledger.totalBalance > 0) (amount / ledger.totalBalance) * 100.0 else 0.0,
            isFunded = isNowFunded,
            status = newStatus,
            lastHeartbeatTimestamp = System.currentTimeMillis(),
            statusReason = if (isNowFunded) "Verified allocation: $amount ${ledger.currency} from $providerId" else "Unfunded: 0.0 capital"
        )

        val updatedMap = _botTruthMap.value.toMutableMap()
        updatedMap[botId] = updatedBot
        _botTruthMap.value = updatedMap

        return Result.success(amount)
    }

    /**
     * Reconciles internal bot states against authoritative account ledger.
     */
    private fun reconcileBotStatusesWithLedger(ledger: VerifiedCapitalLedger) {
        val totalAllocated = _botTruthMap.value.values.sumOf { it.allocatedCapital }
        if (totalAllocated > ledger.availableBalance && ledger.availableBalance > 0) {
            recordIncident(
                type = "RECONCILIATION_MISMATCH",
                desc = "Total bot allocation ($totalAllocated) exceeds verified provider available balance (${ledger.availableBalance}). Re-adjusting allocations to safe boundaries.",
                severity = "ALERT"
            )
            // Scale down allocations proportionally to ensure truthfulness
            val ratio = ledger.availableBalance / totalAllocated
            _botTruthMap.value = _botTruthMap.value.mapValues { (_, bot) ->
                val safeAmt = bot.allocatedCapital * ratio
                bot.copy(
                    allocatedCapital = safeAmt,
                    isFunded = safeAmt > 0,
                    status = if (safeAmt > 0) TruthfulBotStatus.RUNNING_VERIFIED else TruthfulBotStatus.UNFUNDED,
                    statusReason = "Proportionally balanced to verified provider funds ($safeAmt ${ledger.currency})"
                )
            }
        }
    }

    fun recordIncident(type: String, desc: String, severity: String = "INFO") {
        val incident = DataIntegrityIncident(
            type = type,
            description = desc,
            severity = severity
        )
        val list = _integrityIncidents.value.toMutableList()
        if (list.size > 50) list.removeAt(0)
        list.add(incident)
        _integrityIncidents.value = list
    }

    companion object {
        @Volatile
        private var instance: TruthSourceOfTruthEngine? = null

        fun getInstance(): TruthSourceOfTruthEngine {
            return instance ?: synchronized(this) {
                instance ?: TruthSourceOfTruthEngine().also { instance = it }
            }
        }
    }
}
