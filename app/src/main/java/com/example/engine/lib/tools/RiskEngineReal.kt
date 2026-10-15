package com.example.engine.lib.tools

import kotlin.math.*

data class RiskMetricsKt(
    val var95: Double,
    val var99: Double,
    val cvar95: Double,
    val stressTest3Sigma: Double,
    val volatilityAnnualized: Double,
    val skewness: Double,
    val kurtosis: Double,
    val cornishFisherVaR99: Double,
    val recommendedKellyFraction: Double,
    val maxAllowedPositionUsd: Double,
    val isHardRiskBlocked: Boolean,
    val blockReason: String? = null
)

object RiskEngineReal {
    fun evaluateRisk(
        returns: List<Double>,
        accountEquity: Double,
        openExposureUsd: Double,
        winRate: Double,
        winLossRatio: Double,
        dailyLossLimitPct: Double = 0.04
    ): RiskMetricsKt {
        if (returns.size < 10) {
            return RiskMetricsKt(
                var95 = 0.015,
                var99 = 0.025,
                cvar95 = 0.030,
                stressTest3Sigma = 0.045,
                volatilityAnnualized = 0.18,
                skewness = 0.0,
                kurtosis = 3.0,
                cornishFisherVaR99 = 0.025,
                recommendedKellyFraction = 0.02,
                maxAllowedPositionUsd = accountEquity * 0.05,
                isHardRiskBlocked = false
            )
        }

        val n = returns.size
        val mean = returns.average()
        val variance = returns.map { (it - mean).pow(2) }.sum() / (n - 1)
        val stdDev = sqrt(variance)
        val volAnnual = stdDev * sqrt(252.0)

        val m3 = returns.map { (it - mean).pow(3) }.sum() / n
        val m4 = returns.map { (it - mean).pow(4) }.sum() / n
        val skew = if (variance > 0.0) m3 / stdDev.pow(3) else 0.0
        val kurt = if (variance > 0.0) m4 / stdDev.pow(4) else 3.0

        val z95 = 1.64485
        val z99 = 2.32635
        val var95 = max(0.0, z95 * stdDev - mean)
        val var99 = max(0.0, z99 * stdDev - mean)

        // Cornish-Fisher non-Gaussian expansion
        val z = z99
        val w = z +
                ((z.pow(2) - 1.0) * skew) / 6.0 +
                ((z.pow(3) - 3.0 * z) * (kurt - 3.0)) / 24.0 -
                ((2.0 * z.pow(3) - 5.0 * z) * skew.pow(2)) / 36.0
        val cfVaR99 = max(0.0, w * stdDev - mean)

        // Historical CVaR
        val sorted = returns.sorted()
        val cutoffIdx = max(1, (n * 0.05).toInt())
        val tailLosses = sorted.take(cutoffIdx)
        val cvar95 = if (tailLosses.isNotEmpty()) -tailLosses.average() else var95 * 1.25

        val stressTest3Sigma = max(0.0, 3.0 * stdDev - mean)

        // Fractional Kelly: (p * b - (1 - p)) / b * 0.5
        val p = winRate.coerceIn(0.01, 0.99)
        val b = winLossRatio.coerceAtLeast(0.1)
        val fullKelly = (p * b - (1.0 - p)) / b
        val halfKelly = (fullKelly * 0.5).coerceIn(0.005, 0.08)

        val maxPositionPct = min(halfKelly, 0.05)
        val maxAllowedPositionUsd = max(1.0, accountEquity * maxPositionPct)

        var isBlocked = false
        var reason: String? = null

        if (openExposureUsd > accountEquity * 0.6) {
            isBlocked = true
            reason = "Max leverage exceeded (Open exposure \$${"%.2f".format(openExposureUsd)} > 60% equity)"
        } else if (stressTest3Sigma > dailyLossLimitPct) {
            isBlocked = true
            reason = "3-Sigma stress test ${(stressTest3Sigma * 100).toInt()}% > daily limit ${(dailyLossLimitPct * 100).toInt()}%"
        }

        return RiskMetricsKt(
            var95 = var95,
            var99 = var99,
            cvar95 = max(0.0, cvar95),
            stressTest3Sigma = stressTest3Sigma,
            volatilityAnnualized = volAnnual,
            skewness = skew,
            kurtosis = kurt,
            cornishFisherVaR99 = cfVaR99,
            recommendedKellyFraction = halfKelly,
            maxAllowedPositionUsd = maxAllowedPositionUsd,
            isHardRiskBlocked = isBlocked,
            blockReason = reason
        )
    }
}
