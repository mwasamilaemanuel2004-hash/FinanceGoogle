package com.example.engine.lib.quant_core

data class KellyOptimalResult(
    val kellyFull: Double,       // Pure Kelly fraction
    val kellyHalf: Double,       // Conservative half Kelly for safety (D.E. Shaw standard)
    val lotSizeOptimal: Double,  // Normalized position sizing
    val shouldTrade: Boolean,    // true if kellyHalf >= 0.02
    val fatTailDiscount: Double, // Multiplier applied due to fat-tail factor
    val reasoning: String
)

object KellyOptimalEngine {

    /**
     * D.E. Shaw style Kelly sizing with Fat-Tail Student-t penalization:
     * f* = (p * b - q) / b
     * where:
     *   p = win probability from logistic meta gate
     *   q = 1 - p
     *   b = win / loss ratio (payoff odds)
     * If fatTailFactor > 3.0: kelly = kelly / fatTailFactor
     */
    fun calculateOptimalKelly(
        winProb: Double,
        winLossRatio: Double = 1.8,
        fatTailFactor: Double = 1.0,
        baseCapitalUsd: Double = 1000.0
    ): KellyOptimalResult {
        val p = winProb.coerceIn(0.1, 0.99)
        val q = 1.0 - p
        val b = winLossRatio.coerceAtLeast(0.5)

        // Raw Kelly criterion
        val rawKelly = ((p * b - q) / b).coerceAtLeast(0.0)

        // Fat-tail adjustment
        val discount = if (fatTailFactor > 3.0) (1.0 / fatTailFactor).coerceIn(0.1, 1.0) else 1.0
        val adjustedKellyFull = (rawKelly * discount).coerceIn(0.0, 0.5)
        val kellyHalf = adjustedKellyFull * 0.5

        val shouldTrade = kellyHalf >= 0.02 && p >= 0.55
        // Standard lot size calculation: 1 lot = ~100k, so fractional lot based on capital fraction
        val lotSize = ((baseCapitalUsd * kellyHalf * 10.0) / 10000.0).coerceIn(0.01, 5.0)

        val reasoning = if (shouldTrade) {
            "Kelly Half: ${String.format("%.2f", kellyHalf * 100)}% (Full: ${String.format("%.2f", adjustedKellyFull * 100)}%, Fat-tail discount: ${String.format("%.2f", discount)}x, Lot: ${String.format("%.2f", lotSize)})"
        } else {
            "Kelly edge insufficient (<2% or prob <55%): NO POSITION SIZED"
        }

        return KellyOptimalResult(
            kellyFull = adjustedKellyFull,
            kellyHalf = kellyHalf,
            lotSizeOptimal = lotSize,
            shouldTrade = shouldTrade,
            fatTailDiscount = discount,
            reasoning = reasoning
        )
    }
}
