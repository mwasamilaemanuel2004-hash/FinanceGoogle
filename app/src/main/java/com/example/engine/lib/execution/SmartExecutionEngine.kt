package com.example.engine.lib.execution

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ExecutionOrderType {
    MARKET,
    TWAP_CHUNKS,
    VWAP_SCHEDULED,
    ICEBERG_HIDDEN
}

data class ExecutionPlan(
    val symbol: String,
    val shouldExecute: Boolean,
    val originalLot: Double,
    val adjustedLot: Double,
    val executionType: ExecutionOrderType,
    val expectedSlippagePct: Double,
    val spreadPct: Double,
    val latencyMs: Long,
    val message: String
)

data class ExecutionLogItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val symbol: String,
    val action: String,
    val originalLot: Double,
    val adjustedLot: Double,
    val spreadPct: Double,
    val slippagePct: Double,
    val latencyMs: Long,
    val executionType: ExecutionOrderType,
    val status: String = "CONFIRMED"
)

object SmartExecutionEngine {
    private val _logs = MutableStateFlow<List<ExecutionLogItem>>(emptyList())
    val logs: StateFlow<List<ExecutionLogItem>> = _logs.asStateFlow()

    fun evaluateExecution(
        symbol: String,
        action: String,
        requestedLot: Double,
        spreadPct: Double,
        kyleLambda: Double = 0.000015,
        measuredLatencyMs: Long = 42L
    ): ExecutionPlan {
        // Microstructure checks:
        if (spreadPct > 0.005) { // >0.5% spread
            return ExecutionPlan(
                symbol = symbol,
                shouldExecute = false,
                originalLot = requestedLot,
                adjustedLot = 0.0,
                executionType = ExecutionOrderType.MARKET,
                expectedSlippagePct = 0.0,
                spreadPct = spreadPct,
                latencyMs = measuredLatencyMs,
                message = "REJECTED: Spread ${String.format("%.3f", spreadPct * 100)}% exceeds 0.5% limit"
            )
        }

        if (measuredLatencyMs > 500) {
            return ExecutionPlan(
                symbol = symbol,
                shouldExecute = false,
                originalLot = requestedLot,
                adjustedLot = 0.0,
                executionType = ExecutionOrderType.MARKET,
                expectedSlippagePct = 0.0,
                spreadPct = spreadPct,
                latencyMs = measuredLatencyMs,
                message = "REJECTED: Latency ${measuredLatencyMs}ms exceeds 500ms safety threshold"
            )
        }

        val kyleResult = com.example.engine.lib.quant_core.KyleLambdaEngine.evaluateImpact(
            priceChange = spreadPct * 100.0,
            signedVolume = 1000.0,
            orderSizeLots = requestedLot,
            midPrice = 100.0
        )

        val expectedSlippage = kyleResult.expectedSlippagePct
        var adjustedLot = requestedLot * kyleResult.recommendedLotScale // If toxic (>70%), reduces lot 50%
        if (expectedSlippage > 0.003) {
            adjustedLot = (adjustedLot * 0.6).coerceAtLeast(0.01)
        }

        // Smart routing strategy selection
        val executionType = when {
            symbol.contains("VOLATILITY") || symbol.contains("BOOM") || symbol.contains("CRASH") ->
                ExecutionOrderType.ICEBERG_HIDDEN
            requestedLot > 1.0 ->
                ExecutionOrderType.TWAP_CHUNKS
            symbol.contains("AAPL") || symbol.contains("TSLA") || symbol.contains("SPX") ->
                ExecutionOrderType.VWAP_SCHEDULED
            else ->
                ExecutionOrderType.MARKET
        }

        val plan = ExecutionPlan(
            symbol = symbol,
            shouldExecute = true,
            originalLot = requestedLot,
            adjustedLot = adjustedLot,
            executionType = executionType,
            expectedSlippagePct = expectedSlippage,
            spreadPct = spreadPct,
            latencyMs = measuredLatencyMs,
            message = "APPROVED: Routed via $executionType with expected slippage ${String.format("%.3f", expectedSlippage * 100)}%"
        )

        // Append to logs
        val logItem = ExecutionLogItem(
            symbol = symbol,
            action = action,
            originalLot = requestedLot,
            adjustedLot = adjustedLot,
            spreadPct = spreadPct,
            slippagePct = expectedSlippage,
            latencyMs = measuredLatencyMs,
            executionType = executionType
        )
        _logs.value = listOf(logItem) + _logs.value.take(49)

        return plan
    }
}
