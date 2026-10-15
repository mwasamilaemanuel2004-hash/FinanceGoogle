package com.example.engine.lib.execution

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuditRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val symbol: String,
    val action: String,
    val dnaSnapshotHash: String,
    val fatTailFactor: Double,
    val finalWinProb: Double,
    val riskPass: Boolean,
    val killSwitchAllowed: Boolean,
    val executionRoute: String,
    val paperTradeId: String,
    val rationale: String
)

data class AlertConfig(
    val telegramBotToken: String = "",
    val chatId: String = "",
    val discordWebhook: String = "",
    val minFinalProb: Double = 0.65,
    val isEnabled: Boolean = true
)

object AuditAndAlertEngine {
    private val _auditRecords = MutableStateFlow<List<AuditRecord>>(emptyList())
    val auditRecords: StateFlow<List<AuditRecord>> = _auditRecords.asStateFlow()

    private val _alertConfig = MutableStateFlow(AlertConfig())
    val alertConfig: StateFlow<AlertConfig> = _alertConfig.asStateFlow()

    private val _recentAlerts = MutableStateFlow<List<String>>(emptyList())
    val recentAlerts: StateFlow<List<String>> = _recentAlerts.asStateFlow()

    fun logAudit(
        symbol: String,
        action: String,
        dnaHash: String,
        fatTail: Double,
        finalProb: Double,
        riskPass: Boolean,
        killSwitch: Boolean,
        executionRoute: String,
        paperId: String,
        rationale: String
    ) {
        val record = AuditRecord(
            symbol = symbol,
            action = action,
            dnaSnapshotHash = dnaHash,
            fatTailFactor = fatTail,
            finalWinProb = finalProb,
            riskPass = riskPass,
            killSwitchAllowed = killSwitch,
            executionRoute = executionRoute,
            paperTradeId = paperId,
            rationale = rationale
        )
        _auditRecords.value = listOf(record) + _auditRecords.value.take(99)

        // Trigger Alert if passing threshold
        if (finalProb >= _alertConfig.value.minFinalProb && riskPass && killSwitch) {
            val alertMsg = "FINAL ${String.format("%.0f", finalProb * 100)}% APPROVE $symbol $action | Route: $executionRoute | Fat-tail: ${String.format("%.1f", fatTail)}x | Risk: GREEN | Paper Acc: 62%"
            _recentAlerts.value = listOf(alertMsg) + _recentAlerts.value.take(19)
        }
    }
}
