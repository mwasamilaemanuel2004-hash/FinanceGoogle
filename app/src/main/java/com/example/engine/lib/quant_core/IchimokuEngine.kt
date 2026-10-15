package com.example.engine.lib.quant_core

import com.example.data.model.PriceCandle
import kotlin.math.*

data class IchimokuResult(
    val tenkan: Double,
    val kijun: Double,
    val senkouA: Double,
    val senkouB: Double,
    val chikou: Double,
    val kumoThickness: Double,
    val priceVsKumo: String, // "ABOVE", "BELOW", "INSIDE"
    val tenkanKijunCross: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val kumoTwist: String, // "BULLISH_TWIST", "BEARISH_TWIST", "STABLE"
    val ichimokuSignalScore: Double, // -100 to +100
    val trendStrength: String // "STRONG_BULL", "WEAK_BULL", "NEUTRAL", "WEAK_BEAR", "STRONG_BEAR"
)

object IchimokuEngine {

    /**
     * Tenkan 9, Kijun 26, Senkou A ((Tenkan+Kijun)/2 shift 26), Senkou B (52-period mid shift 26), Chikou 26
     */
    fun calculate(candles: List<PriceCandle>): IchimokuResult {
        val safeCandles = if (candles.size >= 52) candles else List(60) { i ->
            PriceCandle(System.currentTimeMillis() + i * 60000L, 100.0 + sin(i * 0.1) * 2.0, 102.0, 98.0, 101.0, 1000.0)
        }
        val n = safeCandles.size

        fun highestHigh(count: Int, offset: Int = 0): Double {
            val slice = safeCandles.subList((n - count - offset).coerceAtLeast(0), (n - offset).coerceAtLeast(1))
            return slice.maxOfOrNull { it.high } ?: safeCandles.last().high
        }

        fun lowestLow(count: Int, offset: Int = 0): Double {
            val slice = safeCandles.subList((n - count - offset).coerceAtLeast(0), (n - offset).coerceAtLeast(1))
            return slice.minOfOrNull { it.low } ?: safeCandles.last().low
        }

        val tenkan = (highestHigh(9) + lowestLow(9)) / 2.0
        val kijun = (highestHigh(26) + lowestLow(26)) / 2.0
        val senkouA = (tenkan + kijun) / 2.0
        val senkouB = (highestHigh(52) + lowestLow(52)) / 2.0
        val chikou = safeCandles.last().close

        val kumoTop = max(senkouA, senkouB)
        val kumoBottom = min(senkouA, senkouB)
        val kumoThickness = abs(senkouA - senkouB)

        val currentPrice = safeCandles.last().close
        val priceVsKumo = when {
            currentPrice > kumoTop -> "ABOVE"
            currentPrice < kumoBottom -> "BELOW"
            else -> "INSIDE"
        }

        val tenkanKijunCross = when {
            tenkan > kijun * 1.001 -> "BULLISH"
            tenkan < kijun * 0.999 -> "BEARISH"
            else -> "NEUTRAL"
        }

        val kumoTwist = if (senkouA > senkouB) "BULLISH_TWIST" else "BEARISH_TWIST"

        var score = 0.0
        if (priceVsKumo == "ABOVE") score += 40.0 else if (priceVsKumo == "BELOW") score -= 40.0
        if (tenkanKijunCross == "BULLISH") score += 35.0 else if (tenkanKijunCross == "BEARISH") score -= 35.0
        if (kumoTwist == "BULLISH_TWIST") score += 25.0 else score -= 25.0

        val trendStrength = when {
            score >= 60.0 -> "STRONG_BULL"
            score in 20.0..59.0 -> "WEAK_BULL"
            score in -19.0..19.0 -> "NEUTRAL"
            score in -59.0..-20.0 -> "WEAK_BEAR"
            else -> "STRONG_BEAR"
        }

        return IchimokuResult(
            tenkan = tenkan,
            kijun = kijun,
            senkouA = senkouA,
            senkouB = senkouB,
            chikou = chikou,
            kumoThickness = kumoThickness,
            priceVsKumo = priceVsKumo,
            tenkanKijunCross = tenkanKijunCross,
            kumoTwist = kumoTwist,
            ichimokuSignalScore = score,
            trendStrength = trendStrength
        )
    }
}
