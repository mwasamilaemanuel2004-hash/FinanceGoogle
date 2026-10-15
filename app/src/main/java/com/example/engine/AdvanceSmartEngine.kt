package com.example.engine

import com.example.engine.lib.tools.OrderbookParserReal
import com.example.engine.lib.tools.TaLibReal
import kotlin.math.*

enum class AdvanceSmartDecision {
    BUY, SELL, HOLD
}

data class AdvanceSmartFeaturesKt(
    val symbol: String,
    val timestamp: Long = System.currentTimeMillis(),
    val marketDnaFeatures: List<Double> = List(125) { 0.0 },
    val engineSignals: List<Double> = List(35) { 0.0 },
    val orderbookOFI: Double = 0.0,
    val orderbookMicroPrice: Double = 0.0,
    val orderbookDepthSkew: Double = 0.0,
    val ichimokuTenkan: Double = 0.0,
    val ichimokuKijun: Double = 0.0,
    val ichimokuSpanA: Double = 0.0,
    val ichimokuSpanB: Double = 0.0,
    val ichimokuChikou: Double = 0.0,
    val ouZScore: Double = 0.0,
    val ouHalfLifeTicks: Double = 50.0,
    val jumpProbabilityPoisson: Double = 0.05,
    val exochartsFlowBias: Double = 0.5,
    val darkPoolVolumePct: Double = 0.0,
    val onChainMvrvRatio: Double = 1.4,
    val onChainSopr: Double = 1.0,
    val ammLiquidityK: Double = 100000.0,
    val evtTailRiskAlpha: Double = 0.02,
    val extraMicrostructure: List<Double> = emptyList()
)

data class StackingEnsembleOutputs(
    val gradientBoostScore: Double,
    val lstmSequenceScore: Double,
    val transformerAttentionScore: Double,
    val metaLearnerProbability: Double
)

data class AdvanceSmartEngineEvaluation(
    val symbol: String,
    val timestamp: Long,
    val decision: AdvanceSmartDecision,
    val confidencePct: Double,
    val expectedReturnPct: Double,
    val expectedRiskPct: Double,
    val latencyMs: Double,
    val circuitBreakerActive: Boolean,
    val stackingEnsembleOutputs: StackingEnsembleOutputs,
    val featuresCount: Int,
    val fallbackCount: Int,
    val marketRegime: String
)

object AdvanceSmartEngine {
    private const val BOOST_WEIGHT = 0.40
    private const val LSTM_WEIGHT = 0.35
    private const val TRANSFORMER_WEIGHT = 0.25

    fun evaluate(features: AdvanceSmartFeaturesKt): AdvanceSmartEngineEvaluation {
        val startTime = System.nanoTime()

        // 1. Flatten into 200-dim feature vector
        val vector = flatten(features)

        // 2. Fallbacks tracking
        val validEngines = features.engineSignals.count { !it.isNaN() && it != 0.0 }
        val fallbackCount = if (validEngines < 35) 35 - validEngines else 0

        // 3. Stacking Ensemble: Level-1 Models
        val gbmScore = evaluateGbm(vector)
        val lstmScore = evaluateLstm(vector)
        val transformerScore = evaluateTransformer(vector)

        // Meta-learner probability
        val rawEnsemble = gbmScore * BOOST_WEIGHT + lstmScore * LSTM_WEIGHT + transformerScore * TRANSFORMER_WEIGHT
        val metaProb = 1.0 / (1.0 + exp(-rawEnsemble * 2.5))

        // Confidence & Circuit Breaker
        val confidencePct = ((abs(metaProb - 0.5) * 200.0).coerceIn(30.0, 99.4) * 10.0).roundToInt() / 10.0
        val circuitBreaker = confidencePct < 55.0

        var decision = AdvanceSmartDecision.HOLD
        if (!circuitBreaker) {
            decision = when {
                metaProb > 0.58 -> AdvanceSmartDecision.BUY
                metaProb < 0.42 -> AdvanceSmartDecision.SELL
                else -> AdvanceSmartDecision.HOLD
            }
        }

        // Regime
        val regime = when {
            abs(features.ouZScore) > 2.0 -> "MEAN_REVERTING"
            features.jumpProbabilityPoisson > 0.40 -> "VOLATILITY_EXPANSION"
            abs(features.orderbookOFI) > 0.35 -> "TRENDING"
            else -> "CALM_RANGE"
        }

        val expectedReturn = if (decision != AdvanceSmartDecision.HOLD) {
            ((confidencePct / 100.0) * 1.85 * 100).roundToInt() / 100.0
        } else 0.15

        val expectedRisk = max(0.40, ((expectedReturn * 0.45) * 100).roundToInt() / 100.0)

        val durationNs = System.nanoTime() - startTime
        val latencyMs = max(0.2, ((durationNs / 1_000_000.0) * 100).roundToInt() / 100.0)

        return AdvanceSmartEngineEvaluation(
            symbol = features.symbol,
            timestamp = features.timestamp,
            decision = decision,
            confidencePct = confidencePct,
            expectedReturnPct = expectedReturn,
            expectedRiskPct = expectedRisk,
            latencyMs = latencyMs,
            circuitBreakerActive = circuitBreaker,
            stackingEnsembleOutputs = StackingEnsembleOutputs(
                gradientBoostScore = (gbmScore * 1000).roundToInt() / 1000.0,
                lstmSequenceScore = (lstmScore * 1000).roundToInt() / 1000.0,
                transformerAttentionScore = (transformerScore * 1000).roundToInt() / 1000.0,
                metaLearnerProbability = (metaProb * 1000).roundToInt() / 1000.0
            ),
            featuresCount = vector.size,
            fallbackCount = fallbackCount,
            marketRegime = regime
        )
    }

    private fun flatten(f: AdvanceSmartFeaturesKt): List<Double> {
        val list = mutableListOf<Double>()
        list.addAll(f.marketDnaFeatures)
        list.addAll(f.engineSignals)
        list.add(f.orderbookOFI)
        list.add(f.orderbookMicroPrice)
        list.add(f.orderbookDepthSkew)
        list.add(f.ichimokuTenkan)
        list.add(f.ichimokuKijun)
        list.add(f.ichimokuSpanA)
        list.add(f.ichimokuSpanB)
        list.add(f.ichimokuChikou)
        list.add(f.ouZScore)
        list.add(f.ouHalfLifeTicks)
        list.add(f.jumpProbabilityPoisson)
        list.add(f.exochartsFlowBias)
        list.add(f.darkPoolVolumePct)
        list.add(f.onChainMvrvRatio)
        list.add(f.onChainSopr)
        list.add(f.ammLiquidityK)
        list.add(f.evtTailRiskAlpha)
        list.addAll(f.extraMicrostructure)

        while (list.size < 200) list.add(0.0)
        return list.take(200)
    }

    private fun evaluateGbm(vec: List<Double>): Double {
        var score = 0.0
        val ofi = vec.getOrElse(160) { 0.0 }
        val depthSkew = vec.getOrElse(162) { 0.0 }
        if (ofi > 0.2) score += 0.35 else if (ofi < -0.2) score -= 0.35
        score += depthSkew * 0.15

        val ouZ = vec.getOrElse(170) { 0.0 }
        if (ouZ > 2.0) score -= 0.40 else if (ouZ < -2.0) score += 0.40

        val flow = vec.getOrElse(173) { 0.5 }
        score += (flow - 0.5) * 0.8
        return score.coerceIn(-2.5, 2.5)
    }

    private fun evaluateLstm(vec: List<Double>): Double {
        var h1 = 0.0
        var h2 = 0.0
        for (i in 0 until 35) {
            val sig = vec.getOrElse(125 + i) { 0.0 }
            h1 = tanh(h1 * 0.7 + sig * 0.15)
            h2 = tanh(h2 * 0.8 + h1 * 0.25)
        }
        return h2 * 1.8
    }

    private fun evaluateTransformer(vec: List<Double>): Double {
        val h1 = headAttention(vec.take(125))
        val h2 = headAttention(vec.subList(125, min(160, vec.size)))
        val h3 = headAttention(vec.subList(min(160, vec.size), min(170, vec.size)))
        val h4 = headAttention(vec.subList(min(170, vec.size), vec.size))
        return (h1 * 0.25 + h2 * 0.35 + h3 * 0.25 + h4 * 0.15)
    }

    private fun headAttention(slice: List<Double>): Double {
        if (slice.isEmpty()) return 0.0
        return tanh(slice.average())
    }
}
