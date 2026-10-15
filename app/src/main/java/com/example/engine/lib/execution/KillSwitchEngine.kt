package com.example.engine.lib.execution

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RiskKillEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val symbol: String,
    val reason: String,
    val fatTailFactor: Double = 0.0,
    val anomalyScore: Double = 0.0,
    val severity: String = "CRITICAL"
)

data class KillSwitchState(
    val isTradingAllowed: Boolean = true,
    val reason: String = "ALL RISK CHECKS NOMINAL",
    val pausedUntil: Long = 0L,
    val maxDailyLossPct: Double = 5.0,
    val maxConsecutiveLosses: Int = 3,
    val maxDrawdownPct: Double = 10.0,
    val consecutiveLossesCount: Int = 0,
    val currentDailyLossPct: Double = 0.85
)

object KillSwitchEngine {
    private val _state = MutableStateFlow(KillSwitchState())
    val state: StateFlow<KillSwitchState> = _state.asStateFlow()

    private val _riskEvents = MutableStateFlow<List<RiskKillEvent>>(emptyList())
    val riskEvents: StateFlow<List<RiskKillEvent>> = _riskEvents.asStateFlow()

    fun triggerKill(reason: String, symbol: String = "GLOBAL", fatTail: Double = 0.0, anomaly: Double = 0.0) {
        val event = RiskKillEvent(
            symbol = symbol,
            reason = reason,
            fatTailFactor = fatTail,
            anomalyScore = anomaly
        )
        _riskEvents.value = listOf(event) + _riskEvents.value.take(49)
        _state.value = _state.value.copy(
            isTradingAllowed = false,
            reason = reason,
            pausedUntil = System.currentTimeMillis() + (60 * 60 * 1000L) // 1h pause
        )
    }

    fun manualEmergencyStop() {
        triggerKill(reason = "MANUAL EMERGENCY STOP TRIGGERED BY USER")
    }

    fun resetKillSwitch() {
        _state.value = _state.value.copy(
            isTradingAllowed = true,
            reason = "ALL RISK CHECKS NOMINAL",
            pausedUntil = 0L,
            consecutiveLossesCount = 0
        )
    }

    fun checkIsTradingAllowed(symbol: String, fatTail: Double = 0.0, anomaly: Double = 0.0): Boolean {
        val cur = _state.value
        if (!cur.isTradingAllowed) {
            if (cur.pausedUntil > 0 && System.currentTimeMillis() > cur.pausedUntil) {
                // Auto recover pause
                _state.value = cur.copy(isTradingAllowed = true, reason = "AUTO-RECOVERED AFTER PAUSE COOLDOWN")
                return true
            }
            return false
        }
        if (fatTail > 5.0) {
            triggerKill("EXTREME FAT TAIL HAZARD (Factor: ${String.format("%.2f", fatTail)}x > 5.0x)", symbol, fatTail, anomaly)
            return false
        }
        if (anomaly > 95.0) {
            triggerKill("CRITICAL MULTIVARIATE ANOMALY (${String.format("%.1f", anomaly)} > 95.0)", symbol, fatTail, anomaly)
            return false
        }
        return true
    }
}
