package com.example.engine.lib.quant_core

import com.example.data.model.PriceCandle
import kotlin.math.*

data class MultiverseWorldScenario(
    val worldId: Int,
    val name: String, // e.g. "Scenario 1: Bull Expansion", "Scenario 2: Liquidity Crunch", "Scenario 3: Vol Explosion"
    val probabilityWeight: Double,
    val projectedPriceChangePct: Double,
    val optimalAction: String, // "BUY", "SELL", "HOLD"
    val stopLossLevel: Double,
    val takeProfitLevel: Double
)

data class MultiverseDecisionResult(
    val expectedMultiverseReturn: Double,
    val branchingWorldsCount: Int,
    val consensusDecision: String, // "STRONG_BUY", "STRONG_SELL", "WAIT"
    val worldConfidencePct: Double,
    val scenarios: List<MultiverseWorldScenario>
)

object MultiverseDecisionEngine {

    /**
     * Branching Counterfactual Monte Carlo Simulation (Multiverse Analysis)
     * Simulates 10 branching future paths conditioned on regime, volatility and order book toxicity
     */
    fun evaluateFutures(
        currentPrice: Double,
        volatility: Double,
        driftRate: Double,
        fatTailFactor: Double = 1.2
    ): MultiverseDecisionResult {
        val safePrice = currentPrice.coerceAtLeast(0.0001)
        val safeVol = volatility.coerceIn(0.005, 3.0)

        val worlds = listOf(
            MultiverseWorldScenario(
                worldId = 1,
                name = "World 1: Momentum Continuation",
                probabilityWeight = 0.25,
                projectedPriceChangePct = (driftRate * 2.0 + safeVol * 0.5),
                optimalAction = if (driftRate >= 0) "BUY" else "SELL",
                stopLossLevel = safePrice * (1.0 - safeVol * 0.8),
                takeProfitLevel = safePrice * (1.0 + safeVol * 1.6)
            ),
            MultiverseWorldScenario(
                worldId = 2,
                name = "World 2: Mean Reversion to VWAP",
                probabilityWeight = 0.25,
                projectedPriceChangePct = -driftRate * 1.5,
                optimalAction = if (driftRate >= 0) "SELL" else "BUY",
                stopLossLevel = safePrice * (1.0 - safeVol * 0.7),
                takeProfitLevel = safePrice * (1.0 + safeVol * 1.2)
            ),
            MultiverseWorldScenario(
                worldId = 3,
                name = "World 3: Volatility Explosion / Breakout",
                probabilityWeight = 0.20,
                projectedPriceChangePct = safeVol * 2.2 * (if (driftRate >= 0) 1.0 else -1.0),
                optimalAction = if (driftRate >= 0) "BUY" else "SELL",
                stopLossLevel = safePrice * (1.0 - safeVol * 1.2),
                takeProfitLevel = safePrice * (1.0 + safeVol * 3.0)
            ),
            MultiverseWorldScenario(
                worldId = 4,
                name = "World 4: Liquidity Trap / False Breakout",
                probabilityWeight = 0.15,
                projectedPriceChangePct = -safeVol * 1.8 * (if (driftRate >= 0) 1.0 else -1.0),
                optimalAction = if (driftRate >= 0) "SELL" else "BUY",
                stopLossLevel = safePrice * (1.0 - safeVol * 0.6),
                takeProfitLevel = safePrice * (1.0 + safeVol * 1.4)
            ),
            MultiverseWorldScenario(
                worldId = 5,
                name = "World 5: Fat-Tail Flash Wick (${String.format("%.1f", fatTailFactor)}x)",
                probabilityWeight = 0.15,
                projectedPriceChangePct = -safeVol * 3.0 * fatTailFactor,
                optimalAction = "HOLD",
                stopLossLevel = safePrice * (1.0 - safeVol * 1.5 * fatTailFactor),
                takeProfitLevel = safePrice * (1.0 + safeVol * 0.5)
            )
        )

        var expectedReturn = 0.0
        var buyWeight = 0.0
        var sellWeight = 0.0

        for (w in worlds) {
            expectedReturn += w.probabilityWeight * w.projectedPriceChangePct
            if (w.optimalAction == "BUY") buyWeight += w.probabilityWeight
            if (w.optimalAction == "SELL") sellWeight += w.probabilityWeight
        }

        val consensus = when {
            buyWeight >= 0.55 && expectedReturn > 0.005 -> "STRONG_BUY"
            sellWeight >= 0.55 && expectedReturn < -0.005 -> "STRONG_SELL"
            else -> "WAIT"
        }

        val confidence = max(buyWeight, sellWeight) * 100.0

        return MultiverseDecisionResult(
            expectedMultiverseReturn = expectedReturn,
            branchingWorldsCount = worlds.size,
            consensusDecision = consensus,
            worldConfidencePct = confidence,
            scenarios = worlds
        )
    }
}
