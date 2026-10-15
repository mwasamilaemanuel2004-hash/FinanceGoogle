package com.example.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.engine.ExchangeConnector

data class RiskTelemetry(
    val maxDailyDrawdownPct: Double = 5.0,
    val currentDailyDrawdownPct: Double = 0.85,
    val maxLeverageAllowed: Int = 20,
    val isCircuitBreakerTripped: Boolean = false,
    val emergencyStopActive: Boolean = false,
    val marginUtilizationPct: Double = 14.2,
    val sovereignSafetyGateLocked: Boolean = false
)

class HardRiskGovernor {
    private val _telemetry = MutableStateFlow(RiskTelemetry())
    val telemetry: StateFlow<RiskTelemetry> = _telemetry.asStateFlow()

    fun triggerEmergencyHalt(reason: String) {
        _telemetry.value = _telemetry.value.copy(
            emergencyStopActive = true,
            isCircuitBreakerTripped = true
        )
    }

    fun resetEmergencyHalt() {
        _telemetry.value = _telemetry.value.copy(
            emergencyStopActive = false,
            isCircuitBreakerTripped = false
        )
    }

    fun updateMaxDrawdown(pct: Double) {
        _telemetry.value = _telemetry.value.copy(maxDailyDrawdownPct = pct)
    }

    fun toggleSovereignGate() {
        _telemetry.value = _telemetry.value.copy(
            sovereignSafetyGateLocked = !_telemetry.value.sovereignSafetyGateLocked
        )
    }

    fun canTrade(connector: ExchangeConnector): Boolean {
        val tel = _telemetry.value
        if (tel.emergencyStopActive || tel.isCircuitBreakerTripped || tel.sovereignSafetyGateLocked) return false
        return connector.isConnected()
    }
}
