package com.example.engine.lib.quant_core

import kotlin.math.*

data class MarketMakingQuote(
    val reservationPrice: Double, // Neutral price accounting for inventory risk
    val optimalBid: Double,
    val optimalAsk: Double,
    val optimalSpread: Double,
    val optimalSpreadPct: Double,
    val inventoryRiskPenalty: Double,
    val bidSkewOffset: Double,
    val askSkewOffset: Double,
    val actionSignal: String // "POST_BOTH", "SKEW_BUY", "SKEW_SELL", "WIDEN_AND_WAIT"
)

object MarketMakingEngine {

    /**
     * Avellaneda-Stoikov Model for High-Frequency / Market-Making execution
     *
     * r(s, q, t) = s - q * gamma * sigma^2 * (T - t)
     * delta_a + delta_b = gamma * sigma^2 * (T - t) + (2/gamma) * ln(1 + gamma/kappa)
     *
     * @param midPrice Current mid market price
     * @param inventory Current net inventory in base units (q)
     * @param sigma Volatility of asset (annualized or per-minute)
     * @param gamma Risk aversion parameter (e.g. 0.1 to 0.5)
     * @param kappa Order book liquidity parameter / arrival intensity (e.g. 1.5)
     * @param toxicityScore Kyle Lambda toxicity score 0-100
     * @param timeRemaining Normalized trading horizon (T - t) in [0, 1]
     */
    fun calculateAvellanedaStoikov(
        midPrice: Double,
        inventory: Double,
        sigma: Double,
        gamma: Double = 0.25,
        kappa: Double = 1.5,
        toxicityScore: Double = 20.0,
        timeRemaining: Double = 1.0
    ): MarketMakingQuote {
        val safePrice = midPrice.coerceAtLeast(0.0001)
        val safeSigma = sigma.coerceIn(0.001, 5.0)

        // Toxicity adjustment: if Kyle lambda toxicity is high, increase risk aversion and widen spread
        val effectiveGamma = if (toxicityScore > 60.0) gamma * (1.0 + (toxicityScore - 60.0) / 40.0) else gamma

        // 1. Reservation Price: r = mid - q * gamma * sigma^2 * (T - t)
        val inventoryShift = inventory * effectiveGamma * (safeSigma * safeSigma) * timeRemaining
        val reservationPrice = safePrice - inventoryShift

        // 2. Optimal Half-Spread (delta_a + delta_b)
        val volComponent = effectiveGamma * (safeSigma * safeSigma) * timeRemaining
        val intensityComponent = (2.0 / effectiveGamma) * ln(1.0 + effectiveGamma / kappa)
        val baseSpread = max(safePrice * 0.0002, (volComponent + intensityComponent) * 0.05 * safePrice)

        // Spread widening for toxic order flow
        val toxicMultiplier = if (toxicityScore > 70.0) 2.5 else if (toxicityScore > 50.0) 1.5 else 1.0
        val finalSpread = baseSpread * toxicMultiplier

        val halfSpread = finalSpread / 2.0

        // Asymmetric quotes centered on reservation price
        val optimalBid = reservationPrice - halfSpread
        val optimalAsk = reservationPrice + halfSpread

        val optimalSpreadPct = (optimalAsk - optimalBid) / safePrice

        val bidSkewOffset = reservationPrice - safePrice
        val askSkewOffset = safePrice - reservationPrice

        val actionSignal = when {
            toxicityScore > 80.0 -> "WIDEN_AND_WAIT"
            inventory > 2.0 -> "SKEW_SELL" // Heavily long inventory -> lower ask to offload inventory
            inventory < -2.0 -> "SKEW_BUY" // Heavily short inventory -> raise bid to accumulate
            else -> "POST_BOTH" // Balanced inventory -> harvest two-way spread
        }

        return MarketMakingQuote(
            reservationPrice = reservationPrice,
            optimalBid = optimalBid,
            optimalAsk = optimalAsk,
            optimalSpread = finalSpread,
            optimalSpreadPct = optimalSpreadPct,
            inventoryRiskPenalty = abs(inventoryShift),
            bidSkewOffset = bidSkewOffset,
            askSkewOffset = askSkewOffset,
            actionSignal = actionSignal
        )
    }
}
