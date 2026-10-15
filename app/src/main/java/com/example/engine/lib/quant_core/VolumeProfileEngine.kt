package com.example.engine.lib.quant_core

import com.example.data.model.PriceCandle
import kotlin.math.*

data class VolumeProfileLevel(
    val price: Double,
    val volume: Double,
    val isPoc: Boolean,
    val isVah: Boolean,
    val isVal: Boolean
)

data class VolumeProfileResult(
    val pocPrice: Double,
    val vahPrice: Double, // Value Area High 70%
    val valPrice: Double, // Value Area Low 70%
    val isAtPoc: Boolean,
    val isAtHvn: Boolean, // High Volume Node
    val isAtLvn: Boolean, // Low Volume Node (clean air / slippage)
    val volumeDelta: Double, // Buy Vol - Sell Vol
    val cvdTrend: String, // "BULLISH_CVD", "BEARISH_CVD", "FLAT"
    val absorptionDetected: Boolean,
    val exhaustionDetected: Boolean,
    val orderFlowSignal: String // "STRONG_BUY_ABSORPTION", "STRONG_SELL_ABSORPTION", "DELTA_DIV_BUY", "DELTA_DIV_SELL", "NEUTRAL"
)

object VolumeProfileEngine {

    fun calculate(candles: List<PriceCandle>, binsCount: Int = 30): VolumeProfileResult {
        if (candles.isEmpty()) {
            return VolumeProfileResult(
                pocPrice = 100.0,
                vahPrice = 102.0,
                valPrice = 98.0,
                isAtPoc = false,
                isAtHvn = false,
                isAtLvn = false,
                volumeDelta = 0.0,
                cvdTrend = "FLAT",
                absorptionDetected = false,
                exhaustionDetected = false,
                orderFlowSignal = "NEUTRAL"
            )
        }

        val minPrice = candles.minOf { it.low }
        val maxPrice = candles.maxOf { it.high }
        val priceRange = (maxPrice - minPrice).coerceAtLeast(0.0001)
        val binSize = priceRange / binsCount

        val profile = DoubleArray(binsCount)
        var totalVolume = 0.0
        var cumulativeDelta = 0.0

        for (c in candles) {
            val candleMid = (c.high + c.low + c.close) / 3.0
            val binIdx = ((candleMid - minPrice) / binSize).toInt().coerceIn(0, binsCount - 1)
            profile[binIdx] += c.volume
            totalVolume += c.volume

            // Delta proxy: if close > open, buyers dominated; else sellers
            val delta = if (c.close >= c.open) {
                c.volume * ((c.close - c.open) / (c.high - c.low).coerceAtLeast(0.0001))
            } else {
                -c.volume * ((c.open - c.close) / (c.high - c.low).coerceAtLeast(0.0001))
            }
            cumulativeDelta += delta
        }

        // POC (Point of Control) is bin with maximum volume
        var maxBinIdx = 0
        var maxVol = 0.0
        for (i in 0 until binsCount) {
            if (profile[i] > maxVol) {
                maxVol = profile[i]
                maxBinIdx = i
            }
        }
        val pocPrice = minPrice + (maxBinIdx + 0.5) * binSize

        // Value Area (70% total volume around POC)
        val targetVaVol = totalVolume * 0.70
        var currentVaVol = maxVol
        var lowVaIdx = maxBinIdx
        var highVaIdx = maxBinIdx

        while (currentVaVol < targetVaVol && (lowVaIdx > 0 || highVaIdx < binsCount - 1)) {
            val nextLowVol = if (lowVaIdx > 0) profile[lowVaIdx - 1] else 0.0
            val nextHighVol = if (highVaIdx < binsCount - 1) profile[highVaIdx + 1] else 0.0

            if (nextHighVol >= nextLowVol && highVaIdx < binsCount - 1) {
                highVaIdx++
                currentVaVol += profile[highVaIdx]
            } else if (lowVaIdx > 0) {
                lowVaIdx--
                currentVaVol += profile[lowVaIdx]
            } else {
                break
            }
        }

        val valPrice = minPrice + (lowVaIdx + 0.5) * binSize
        val vahPrice = minPrice + (highVaIdx + 0.5) * binSize

        val currentPrice = candles.last().close
        val currentBin = ((currentPrice - minPrice) / binSize).toInt().coerceIn(0, binsCount - 1)
        val currentBinVol = profile[currentBin]
        val avgBinVol = totalVolume / binsCount

        val isAtPoc = abs(currentPrice - pocPrice) <= binSize * 0.8
        val isAtHvn = currentBinVol > avgBinVol * 1.4
        val isAtLvn = currentBinVol < avgBinVol * 0.5

        val cvdTrend = when {
            cumulativeDelta > totalVolume * 0.15 -> "BULLISH_CVD"
            cumulativeDelta < -totalVolume * 0.15 -> "BEARISH_CVD"
            else -> "FLAT"
        }

        // Absorption: heavy volume but small price movement near VAH/VAL
        val lastCandle = candles.last()
        val isHighVol = lastCandle.volume > (totalVolume / candles.size) * 1.5
        val smallBody = abs(lastCandle.close - lastCandle.open) < (lastCandle.high - lastCandle.low) * 0.3
        val absorptionDetected = isHighVol && smallBody && (isAtHvn || isAtPoc)

        // Exhaustion: climax volume at extremities followed by wick
        val exhaustionDetected = isHighVol && ((lastCandle.high >= maxPrice && lastCandle.close < lastCandle.open) ||
                (lastCandle.low <= minPrice && lastCandle.close > lastCandle.open))

        val orderFlowSignal = when {
            absorptionDetected && currentPrice <= valPrice -> "STRONG_BUY_ABSORPTION"
            absorptionDetected && currentPrice >= vahPrice -> "STRONG_SELL_ABSORPTION"
            cvdTrend == "BULLISH_CVD" && currentPrice < valPrice -> "DELTA_DIV_BUY"
            cvdTrend == "BEARISH_CVD" && currentPrice > vahPrice -> "DELTA_DIV_SELL"
            isAtLvn && cvdTrend == "BULLISH_CVD" -> "LVN_ACCELERATION_BUY"
            isAtLvn && cvdTrend == "BEARISH_CVD" -> "LVN_ACCELERATION_SELL"
            else -> "NEUTRAL"
        }

        return VolumeProfileResult(
            pocPrice = pocPrice,
            vahPrice = vahPrice,
            valPrice = valPrice,
            isAtPoc = isAtPoc,
            isAtHvn = isAtHvn,
            isAtLvn = isAtLvn,
            volumeDelta = cumulativeDelta,
            cvdTrend = cvdTrend,
            absorptionDetected = absorptionDetected,
            exhaustionDetected = exhaustionDetected,
            orderFlowSignal = orderFlowSignal
        )
    }
}
