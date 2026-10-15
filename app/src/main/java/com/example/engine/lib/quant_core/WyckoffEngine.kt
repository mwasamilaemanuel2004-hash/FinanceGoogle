package com.example.engine.lib.quant_core

import com.example.data.model.PriceCandle
import kotlin.math.*

enum class WyckoffPhase {
    ACCUMULATION_PHASE_A, // PS, SC, AR, ST
    ACCUMULATION_PHASE_B, // Secondary tests in range
    ACCUMULATION_PHASE_C, // Spring / Shakeout
    ACCUMULATION_PHASE_D, // SOS, LPS
    ACCUMULATION_PHASE_E, // Mark-up / JOC (Jump Across Creek)
    DISTRIBUTION_PHASE_A, // PSY, BC, AR, ST
    DISTRIBUTION_PHASE_B, // Building cause
    DISTRIBUTION_PHASE_C, // UTAD (Upthrust After Distribution)
    DISTRIBUTION_PHASE_D, // SOW, LPSY
    DISTRIBUTION_PHASE_E, // Mark-down
    NEUTRAL_RANGE
}

data class WyckoffResult(
    val phase: WyckoffPhase,
    val primaryEvent: String,
    val compositeManAction: String, // "ACCUMULATING", "DISTRIBUTING", "NEUTRAL"
    val causeCountTargetPrice: Double,
    val wyckoffSignalScore: Double, // -100 to +100
    val springDetected: Boolean,
    val utadDetected: Boolean
)

object WyckoffEngine {

    fun analyze(candles: List<PriceCandle>): WyckoffResult {
        if (candles.size < 30) {
            val lastClose = candles.lastOrNull()?.close ?: 100.0
            return WyckoffResult(
                phase = WyckoffPhase.NEUTRAL_RANGE,
                primaryEvent = "INSUFFICIENT_DATA",
                compositeManAction = "NEUTRAL",
                causeCountTargetPrice = lastClose,
                wyckoffSignalScore = 0.0,
                springDetected = false,
                utadDetected = false
            )
        }

        val recent = candles.takeLast(50)
        val highest = recent.maxOf { it.high }
        val lowest = recent.minOf { it.low }
        val range = (highest - lowest).coerceAtLeast(0.0001)
        val last = recent.last()
        val prev = recent[recent.size - 2]

        val avgVolume = recent.map { it.volume }.average().coerceAtLeast(1.0)
        val currentVolRatio = last.volume / avgVolume

        // Spring check: dip below recent support with quick rejection & volume
        val supportLevel = lowest + range * 0.05
        val resistanceLevel = highest - range * 0.05

        val springDetected = last.low < lowest && last.close > supportLevel && currentVolRatio > 1.2
        val utadDetected = last.high > highest && last.close < resistanceLevel && currentVolRatio > 1.2

        val pricePos = (last.close - lowest) / range

        val (phase, event, compositeAction, score) = when {
            springDetected -> {
                Quadruple(WyckoffPhase.ACCUMULATION_PHASE_C, "SPRING_SHAKEOUT", "ACCUMULATING", 85.0)
            }
            utadDetected -> {
                Quadruple(WyckoffPhase.DISTRIBUTION_PHASE_C, "UTAD_UPTHRUST", "DISTRIBUTING", -85.0)
            }
            pricePos > 0.85 && currentVolRatio > 1.5 && last.close > prev.close -> {
                Quadruple(WyckoffPhase.ACCUMULATION_PHASE_E, "JOC_JUMP_ACROSS_CREEK", "ACCUMULATING", 90.0)
            }
            pricePos < 0.15 && currentVolRatio > 1.5 && last.close < prev.close -> {
                Quadruple(WyckoffPhase.DISTRIBUTION_PHASE_E, "SOW_SIGN_OF_WEAKNESS", "DISTRIBUTING", -90.0)
            }
            pricePos < 0.35 && last.close > prev.close && currentVolRatio > 0.9 -> {
                Quadruple(WyckoffPhase.ACCUMULATION_PHASE_D, "LPS_LAST_POINT_SUPPORT", "ACCUMULATING", 65.0)
            }
            pricePos > 0.65 && last.close < prev.close && currentVolRatio > 0.9 -> {
                Quadruple(WyckoffPhase.DISTRIBUTION_PHASE_D, "LPSY_LAST_POINT_SUPPLY", "DISTRIBUTING", -65.0)
            }
            pricePos in 0.4..0.6 -> {
                Quadruple(WyckoffPhase.ACCUMULATION_PHASE_B, "TRADING_RANGE_CONSOLIDATION", "NEUTRAL", 0.0)
            }
            else -> {
                val sc = if (pricePos > 0.5) 20.0 else -20.0
                val act = if (pricePos > 0.5) "ACCUMULATING" else "DISTRIBUTING"
                Quadruple(WyckoffPhase.NEUTRAL_RANGE, "RANGE_ABSORPTION", act, sc)
            }
        }

        // Point and Figure Cause Count Target
        val target = if (score > 0) {
            highest + range * 0.618
        } else {
            (lowest - range * 0.618).coerceAtLeast(0.0)
        }

        return WyckoffResult(
            phase = phase,
            primaryEvent = event,
            compositeManAction = compositeAction,
            causeCountTargetPrice = target,
            wyckoffSignalScore = score,
            springDetected = springDetected,
            utadDetected = utadDetected
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
