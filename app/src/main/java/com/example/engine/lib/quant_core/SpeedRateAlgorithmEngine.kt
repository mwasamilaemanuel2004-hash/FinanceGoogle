package com.example.engine.lib.quant_core

import com.example.data.model.PriceCandle
import kotlin.math.*

data class SpeedRateResult(
    val tickVelocityTicksPerSec: Double,
    val tickDeltaBuySellRatio: Double,
    val speedAcceleration: Double,
    val speedRateState: String, // "SUPERSONIC_EXPANSION", "HIGH_VELOCITY", "NORMAL", "EXHAUSTION_SLOWDOWN"
    val lsfScore: Double, // Liquidity Speed Flow score 0 to 100
    val lseExecutionPriority: String // "PRIORITY_TWAP_ACCELERATED", "STANDARD_VWAP", "HOLD_EXECUTION"
)

object SpeedRateAlgorithmEngine {

    /**
     * LSF/LSE Speed Rate Algorithm:
     * Measures tick velocity (price displacement / time delta) and volume acceleration.
     */
    fun calculate(candles: List<PriceCandle>): SpeedRateResult {
        if (candles.size < 5) {
            return SpeedRateResult(
                tickVelocityTicksPerSec = 1.0,
                tickDeltaBuySellRatio = 1.0,
                speedAcceleration = 0.0,
                speedRateState = "NORMAL",
                lsfScore = 50.0,
                lseExecutionPriority = "STANDARD_VWAP"
            )
        }

        val recent = candles.takeLast(10)
        var totalDisplacement = 0.0
        var totalVolume = 0.0
        var buyVol = 0.0
        var sellVol = 0.0

        for (c in recent) {
            val range = abs(c.high - c.low)
            totalDisplacement += range
            totalVolume += c.volume
            if (c.close >= c.open) {
                buyVol += c.volume
            } else {
                sellVol += c.volume
            }
        }

        // Time window assumed 60 seconds per candle
        val totalSecs = (recent.size * 60).toDouble()
        val tickVelocity = (totalDisplacement / totalSecs) * 1000.0 // Scaled ticks/sec

        val lastCandle = recent.last()
        val prevCandle = recent[recent.size - 2]
        val lastRange = abs(lastCandle.high - lastCandle.low)
        val prevRange = abs(prevCandle.high - prevCandle.low).coerceAtLeast(1e-6)
        val speedAcceleration = (lastRange - prevRange) / prevRange

        val buySellRatio = if (sellVol > 0) buyVol / sellVol else 2.0

        val speedState = when {
            tickVelocity > 5.0 && speedAcceleration > 0.8 -> "SUPERSONIC_EXPANSION"
            tickVelocity > 2.5 -> "HIGH_VELOCITY"
            speedAcceleration < -0.5 -> "EXHAUSTION_SLOWDOWN"
            else -> "NORMAL"
        }

        val lsf = ((buySellRatio / (1.0 + buySellRatio)) * 50.0 + (tickVelocity.coerceIn(0.0, 10.0) * 5.0)).coerceIn(0.0, 100.0)

        val priority = when {
            speedState == "SUPERSONIC_EXPANSION" -> "PRIORITY_TWAP_ACCELERATED"
            speedState == "EXHAUSTION_SLOWDOWN" -> "HOLD_EXECUTION"
            else -> "STANDARD_VWAP"
        }

        return SpeedRateResult(
            tickVelocityTicksPerSec = tickVelocity,
            tickDeltaBuySellRatio = buySellRatio,
            speedAcceleration = speedAcceleration,
            speedRateState = speedState,
            lsfScore = lsf,
            lseExecutionPriority = priority
        )
    }
}
