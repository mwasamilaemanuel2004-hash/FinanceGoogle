package com.example.engine.lib.quant_core

import com.example.data.model.PriceCandle
import kotlin.math.*

data class VolatilityArbResult(
    val realizedVolYangZhang: Double,
    val impliedVolProxy: Double,
    val rvIvSpread: Double, // RV - IV
    val volRiskPremium: Double, // IV - RV (positive = options market charges premium)
    val volPercentile: Double, // 0 to 100% of 100-candle vol cone
    val garchForecastVsRv: Double,
    val volArbSignal: String, // "LONG_VOL", "SHORT_VOL", "NEUTRAL"
    val expectedVolMovePct: Double
)

object VolatilityArbEngine {

    fun calculate(candles: List<PriceCandle>, garchVolForecast: Double? = null): VolatilityArbResult {
        if (candles.size < 20) {
            return VolatilityArbResult(
                realizedVolYangZhang = 0.25,
                impliedVolProxy = 0.28,
                rvIvSpread = -0.03,
                volRiskPremium = 0.03,
                volPercentile = 50.0,
                garchForecastVsRv = 1.0,
                volArbSignal = "NEUTRAL",
                expectedVolMovePct = 0.0
            )
        }

        val n = candles.size
        // Yang-Zhang Realized Volatility calculation (handles jumps, drift and open-close gaps)
        val k = 0.34 / (1.34 + (n + 1.0) / (n - 1.0))

        var sumOvernight = 0.0
        var sumOpenToClose = 0.0
        var sumRogersSatchell = 0.0

        for (i in 1 until n) {
            val curr = candles[i]
            val prev = candles[i - 1]

            val overnight = ln(curr.open / prev.close)
            sumOvernight += overnight * overnight

            val openClose = ln(curr.close / curr.open)
            sumOpenToClose += openClose * openClose

            val u = ln(curr.high / curr.open)
            val d = ln(curr.low / curr.open)
            val c = ln(curr.close / curr.open)
            val rs = u * (u - c) + d * (d - c)
            sumRogersSatchell += rs
        }

        val vo = sumOvernight / (n - 1.0)
        val vc = sumOpenToClose / (n - 1.0)
        val vrs = sumRogersSatchell / (n - 1.0)

        val yzVariance = (vo + k * vc + (1.0 - k) * vrs).coerceAtLeast(1e-8)
        val annualizedRv = sqrt(yzVariance * 365.0 * 24.0 * 60.0) // 1-minute to annualized

        // Parkinson volatility as secondary estimator
        var parkinsonSum = 0.0
        for (c in candles) {
            val hl = ln(c.high / c.low.coerceAtLeast(1e-6))
            parkinsonSum += hl * hl
        }
        val parkinsonVol = sqrt((parkinsonSum / (4.0 * ln(2.0) * n)) * 365.0 * 24.0 * 60.0)

        // Implied Volatility Proxy from Parkinson / Historical Vol premium
        val ivProxy = max(annualizedRv, parkinsonVol) * 1.12 // Historical average 12% Volatility Risk Premium

        val rvIvSpread = annualizedRv - ivProxy
        val volRiskPremium = ivProxy - annualizedRv

        // Volatility Cone percentile (rolling 10-candle windows)
        val rollingVols = mutableListOf<Double>()
        for (i in 10..n step 5) {
            val sub = candles.subList(i - 10, i)
            var sumR = 0.0
            for (j in 1 until sub.size) {
                val r = ln(sub[j].close / sub[j - 1].close)
                sumR += r * r
            }
            val volWindow = sqrt((sumR / 9.0) * 365.0 * 24.0 * 60.0)
            rollingVols.add(volWindow)
        }

        val volPercentile = if (rollingVols.isNotEmpty()) {
            val countLower = rollingVols.count { it <= annualizedRv }
            (countLower.toDouble() / rollingVols.size) * 100.0
        } else 50.0

        val garchF = garchVolForecast ?: annualizedRv
        val garchVsRv = if (annualizedRv > 0) garchF / annualizedRv else 1.0

        val volArbSignal = when {
            volPercentile >= 85.0 && volRiskPremium > 0.05 -> "SHORT_VOL" // Sell straddle / expect mean reversion compression
            volPercentile <= 15.0 && garchVsRv > 1.25 -> "LONG_VOL" // Buy straddle / breakout imminent
            else -> "NEUTRAL"
        }

        val expectedVolMove = (garchF - annualizedRv) / annualizedRv.coerceAtLeast(0.01)

        return VolatilityArbResult(
            realizedVolYangZhang = annualizedRv,
            impliedVolProxy = ivProxy,
            rvIvSpread = rvIvSpread,
            volRiskPremium = volRiskPremium,
            volPercentile = volPercentile,
            garchForecastVsRv = garchVsRv,
            volArbSignal = volArbSignal,
            expectedVolMovePct = expectedVolMove
        )
    }
}
