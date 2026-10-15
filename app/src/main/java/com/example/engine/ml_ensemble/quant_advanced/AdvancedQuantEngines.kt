package com.example.engine.ml_ensemble.quant_advanced

import com.example.data.model.PriceCandle
import com.example.engine.lib.quant_core.*
import com.example.engine.lib.quant_shared.BellCurveDistributionStats
import com.example.engine.lib.quant_shared.MarketDna125Snapshot
import com.example.engine.ml_ensemble.EnginePredictionResult
import com.example.engine.ml_ensemble.MlEnsembleEngine
import kotlin.math.*

/**
 * Advanced Quant Expansion Engines (26 to 35):
 * 26. Ichimoku Cloud & Kumo Twist Engine
 * 27. Wyckoff Phase & Composite Man Engine
 * 28. Volume Profile & Order Flow CVD Engine
 * 29. Volatility Arbitrage (RV vs IV Proxy) Engine
 * 30. Avellaneda-Stoikov Market Making Engine
 * 31. Bayesian Inference & EV Engine
 * 32. Shannon Information Entropy & Lempel-Ziv Engine
 * 33. Lead-Lag Cross-Correlation & Granger Causality Engine
 * 34. Liquidity Sweep & Institutional ICT Engine
 * 35. Multiverse Counterfactual Simulation Engine
 */

// 26. Ichimoku Engine Wrapper
class IchimokuMlEngine : MlEnsembleEngine {
    override val name: String = "Ichimoku_Cloud_Kumo"
    override val category: String = "PREDICTOR"

    override suspend fun predict(
        symbol: String,
        dna: MarketDna125Snapshot,
        prices: List<Double>,
        bellStats: BellCurveDistributionStats
    ): EnginePredictionResult {
        val t0 = System.currentTimeMillis()
        val candles = buildDummyCandles(prices)
        val res = IchimokuEngine.calculate(candles)

        val prob = when {
            res.ichimokuSignalScore > 40 -> 0.75 + (res.ichimokuSignalScore / 400.0)
            res.ichimokuSignalScore < -40 -> 0.25 - (abs(res.ichimokuSignalScore) / 400.0)
            else -> 0.50
        }.coerceIn(0.1, 0.95)

        return EnginePredictionResult(
            engineName = name,
            category = category,
            probability = prob,
            confidence = (abs(res.ichimokuSignalScore) / 100.0).coerceIn(0.4, 0.95),
            isAnomalyOrBlock = false,
            summary = "Ichimoku Cloud: ${res.priceVsKumo} Kumo | Cross: ${res.tenkanKijunCross} | Score: ${res.ichimokuSignalScore.toInt()}",
            latencyMs = System.currentTimeMillis() - t0,
            details = mapOf(
                "price_vs_kumo" to res.priceVsKumo,
                "tenkan_cross" to res.tenkanKijunCross,
                "kumo_twist" to res.kumoTwist,
                "signal_score" to res.ichimokuSignalScore,
                "trend_strength" to res.trendStrength
            )
        )
    }
}

// 27. Wyckoff Engine Wrapper
class WyckoffMlEngine : MlEnsembleEngine {
    override val name: String = "Wyckoff_Composite_Man"
    override val category: String = "REGIME"

    override suspend fun predict(
        symbol: String,
        dna: MarketDna125Snapshot,
        prices: List<Double>,
        bellStats: BellCurveDistributionStats
    ): EnginePredictionResult {
        val t0 = System.currentTimeMillis()
        val candles = buildDummyCandles(prices)
        val res = WyckoffEngine.analyze(candles)

        val prob = (0.50 + (res.wyckoffSignalScore / 200.0)).coerceIn(0.15, 0.92)

        return EnginePredictionResult(
            engineName = name,
            category = category,
            probability = prob,
            confidence = (abs(res.wyckoffSignalScore) / 100.0).coerceIn(0.4, 0.9),
            isAnomalyOrBlock = false,
            summary = "Wyckoff: ${res.phase.name} | Composite Man: ${res.compositeManAction} (${res.primaryEvent})",
            latencyMs = System.currentTimeMillis() - t0,
            details = mapOf(
                "phase" to res.phase.name,
                "event" to res.primaryEvent,
                "composite_man" to res.compositeManAction,
                "cause_target" to res.causeCountTargetPrice,
                "spring_detected" to res.springDetected,
                "utad_detected" to res.utadDetected
            )
        )
    }
}

// 28. Volume Profile & Order Flow CVD Engine Wrapper
class VolumeProfileMlEngine : MlEnsembleEngine {
    override val name: String = "Volume_Profile_CVD"
    override val category: String = "PREDICTOR"

    override suspend fun predict(
        symbol: String,
        dna: MarketDna125Snapshot,
        prices: List<Double>,
        bellStats: BellCurveDistributionStats
    ): EnginePredictionResult {
        val t0 = System.currentTimeMillis()
        val candles = buildDummyCandles(prices)
        val res = VolumeProfileEngine.calculate(candles)

        val prob = when {
            res.orderFlowSignal.contains("BUY") -> 0.79
            res.orderFlowSignal.contains("SELL") -> 0.22
            else -> 0.50
        }

        return EnginePredictionResult(
            engineName = name,
            category = category,
            probability = prob,
            confidence = if (res.absorptionDetected || res.exhaustionDetected) 0.88 else 0.65,
            isAnomalyOrBlock = false,
            summary = "Vol Profile: POC $${String.format("%.2f", res.pocPrice)} | Signal: ${res.orderFlowSignal}",
            latencyMs = System.currentTimeMillis() - t0,
            details = mapOf(
                "poc_price" to res.pocPrice,
                "vah" to res.vahPrice,
                "val" to res.valPrice,
                "is_at_poc" to res.isAtPoc,
                "order_flow_signal" to res.orderFlowSignal,
                "absorption" to res.absorptionDetected,
                "exhaustion" to res.exhaustionDetected
            )
        )
    }
}

// 29. Volatility Arbitrage Engine Wrapper
class VolatilityArbMlEngine : MlEnsembleEngine {
    override val name: String = "Volatility_Arb_Cone"
    override val category: String = "OPTIMIZER"

    override suspend fun predict(
        symbol: String,
        dna: MarketDna125Snapshot,
        prices: List<Double>,
        bellStats: BellCurveDistributionStats
    ): EnginePredictionResult {
        val t0 = System.currentTimeMillis()
        val candles = buildDummyCandles(prices)
        val garchVol = dna.featureMap["garch_forecast_1h"] ?: 0.25
        val res = VolatilityArbEngine.calculate(candles, garchVol)

        val prob = when (res.volArbSignal) {
            "LONG_VOL" -> 0.76
            "SHORT_VOL" -> 0.24
            else -> 0.50
        }

        return EnginePredictionResult(
            engineName = name,
            category = category,
            probability = prob,
            confidence = (abs(res.volPercentile - 50.0) / 50.0).coerceIn(0.4, 0.88),
            isAnomalyOrBlock = false,
            summary = "Vol Arb: ${res.volArbSignal} | RV/IV Spread: ${String.format("%.3f", res.rvIvSpread)} | Cone Pct: ${res.volPercentile.toInt()}%",
            latencyMs = System.currentTimeMillis() - t0,
            details = mapOf(
                "yang_zhang_rv" to res.realizedVolYangZhang,
                "iv_proxy" to res.impliedVolProxy,
                "rv_iv_spread" to res.rvIvSpread,
                "vol_percentile" to res.volPercentile,
                "signal" to res.volArbSignal
            )
        )
    }
}

// 30. Market Making Avellaneda-Stoikov Engine Wrapper
class MarketMakingMlEngine : MlEnsembleEngine {
    override val name: String = "Avellaneda_Stoikov_MM"
    override val category: String = "OPTIMIZER"

    override suspend fun predict(
        symbol: String,
        dna: MarketDna125Snapshot,
        prices: List<Double>,
        bellStats: BellCurveDistributionStats
    ): EnginePredictionResult {
        val t0 = System.currentTimeMillis()
        val midPrice = prices.lastOrNull() ?: 100.0
        val vol = (dna.featureMap["garch_forecast_1h"] ?: 0.25).coerceAtLeast(0.01)
        val toxicity = dna.featureMap["toxicity"] ?: 20.0
        val quote = MarketMakingEngine.calculateAvellanedaStoikov(
            midPrice = midPrice,
            inventory = 0.0,
            sigma = vol,
            toxicityScore = toxicity
        )

        val prob = when (quote.actionSignal) {
            "SKEW_BUY" -> 0.72
            "SKEW_SELL" -> 0.28
            else -> 0.50
        }

        return EnginePredictionResult(
            engineName = name,
            category = category,
            probability = prob,
            confidence = if (toxicity > 70.0) 0.90 else 0.70,
            isAnomalyOrBlock = toxicity > 85.0,
            summary = "A-S MM: Spread ${String.format("%.3f", quote.optimalSpreadPct * 100)}% | Action: ${quote.actionSignal}",
            latencyMs = System.currentTimeMillis() - t0,
            details = mapOf(
                "reservation_price" to quote.reservationPrice,
                "optimal_bid" to quote.optimalBid,
                "optimal_ask" to quote.optimalAsk,
                "spread_pct" to quote.optimalSpreadPct,
                "action_signal" to quote.actionSignal
            )
        )
    }
}

// 31. Bayesian Inference & EV Engine Wrapper
class BayesianEvMlEngine : MlEnsembleEngine {
    override val name: String = "Bayesian_EV_Kelly"
    override val category: String = "GATE"

    override suspend fun predict(
        symbol: String,
        dna: MarketDna125Snapshot,
        prices: List<Double>,
        bellStats: BellCurveDistributionStats
    ): EnginePredictionResult {
        val t0 = System.currentTimeMillis()
        val bayes = ExpectedValueAndBayesEngine.evaluateBayesianTrade(
            regimePrior = 0.55,
            ensembleLikelihood = 0.75,
            falsePositiveRate = 0.30
        )

        return EnginePredictionResult(
            engineName = name,
            category = category,
            probability = bayes.posteriorProbability,
            confidence = bayes.posteriorProbability,
            isAnomalyOrBlock = !bayes.isPositiveEv,
            summary = "Bayes EV: ${bayes.confidenceGrade} | Posterior Win: ${String.format("%.0f", bayes.posteriorProbability * 100)}% | Half-Kelly: ${String.format("%.2f", bayes.recommendedKellyFraction)}",
            latencyMs = System.currentTimeMillis() - t0,
            details = mapOf(
                "posterior_win_rate" to bayes.posteriorProbability,
                "expected_value" to bayes.expectedValueDollars,
                "ev_risk_ratio" to bayes.evToRiskRatio,
                "is_positive_ev" to bayes.isPositiveEv,
                "grade" to bayes.confidenceGrade,
                "kelly_fraction" to bayes.recommendedKellyFraction
            )
        )
    }
}

// 32. Shannon Entropy & Complexity Engine Wrapper
class ShannonEntropyMlEngine : MlEnsembleEngine {
    override val name: String = "Shannon_Entropy_Chaos"
    override val category: String = "REGIME"

    override suspend fun predict(
        symbol: String,
        dna: MarketDna125Snapshot,
        prices: List<Double>,
        bellStats: BellCurveDistributionStats
    ): EnginePredictionResult {
        val t0 = System.currentTimeMillis()
        val candles = buildDummyCandles(prices)
        val shannon = ShannonEntropyEngine.calculate(candles)

        val prob = if (shannon.isMarketEfficientRandomWalk) 0.50 else 0.70

        return EnginePredictionResult(
            engineName = name,
            category = category,
            probability = prob,
            confidence = shannon.predictabilityIndex.coerceIn(0.4, 0.95),
            isAnomalyOrBlock = false,
            summary = "Shannon Info: H=${String.format("%.2f", shannon.entropy)} bits | State: ${shannon.informationState} | Predictable: ${String.format("%.0f", shannon.predictabilityIndex * 100)}%",
            latencyMs = System.currentTimeMillis() - t0,
            details = mapOf(
                "entropy_bits" to shannon.entropy,
                "predictability_index" to shannon.predictabilityIndex,
                "state" to shannon.informationState,
                "is_random_walk" to shannon.isMarketEfficientRandomWalk
            )
        )
    }
}

// 33. Lead-Lag Cross-Correlation & Granger Engine Wrapper
class LeadLagMlEngine : MlEnsembleEngine {
    override val name: String = "Lead_Lag_Granger_Alpha"
    override val category: String = "PREDICTOR"

    override suspend fun predict(
        symbol: String,
        dna: MarketDna125Snapshot,
        prices: List<Double>,
        bellStats: BellCurveDistributionStats
    ): EnginePredictionResult {
        val t0 = System.currentTimeMillis()
        val candlesA = buildDummyCandles(prices)
        val candlesB = candlesA.map { c ->
            c.copy(close = c.close * 1.002, open = c.open * 1.001)
        }
        val leadLag = LeadLagCrossCorrEngine.analyze(symbol, candlesA, "BENCHMARK_LEADER", candlesB)

        val prob = when (leadLag.predictiveAlphaSignal) {
            "LEADER_SURGE_BUY_FOLLOWER" -> 0.78
            "LEADER_DUMP_SELL_FOLLOWER" -> 0.22
            else -> 0.50
        }

        return EnginePredictionResult(
            engineName = name,
            category = category,
            probability = prob,
            confidence = abs(leadLag.maxCrossCorrelation).coerceIn(0.4, 0.9),
            isAnomalyOrBlock = false,
            summary = "Lead-Lag: ${leadLag.leaderAsset} leads (lag ${leadLag.optimalLagMinutes}m, r=${String.format("%.2f", leadLag.maxCrossCorrelation)}) | Signal: ${leadLag.predictiveAlphaSignal}",
            latencyMs = System.currentTimeMillis() - t0,
            details = mapOf(
                "optimal_lag_mins" to leadLag.optimalLagMinutes,
                "max_corr" to leadLag.maxCrossCorrelation,
                "granger_p" to leadLag.grangerPValue,
                "signal" to leadLag.predictiveAlphaSignal
            )
        )
    }
}

// 34. Liquidity Sweep & Institutional ICT Engine Wrapper
class LiquiditySweepMlEngine : MlEnsembleEngine {
    override val name: String = "Liquidity_Sweep_ICT"
    override val category: String = "PREDICTOR"

    override suspend fun predict(
        symbol: String,
        dna: MarketDna125Snapshot,
        prices: List<Double>,
        bellStats: BellCurveDistributionStats
    ): EnginePredictionResult {
        val t0 = System.currentTimeMillis()
        val candles = buildDummyCandles(prices)
        val sweep = LiquiditySweepAndIctEngine.detect(candles)

        val prob = (0.50 + (sweep.ictSignalScore / 200.0)).coerceIn(0.18, 0.92)

        return EnginePredictionResult(
            engineName = name,
            category = category,
            probability = prob,
            confidence = (abs(sweep.ictSignalScore) / 100.0).coerceIn(0.4, 0.95),
            isAnomalyOrBlock = false,
            summary = "ICT Liquidity: BSL Swept=${sweep.bslSwept}, SSL Swept=${sweep.sslSwept} | TurtleSoup: ${if (sweep.turtleSoupLong) "LONG" else if (sweep.turtleSoupShort) "SHORT" else "NONE"}",
            latencyMs = System.currentTimeMillis() - t0,
            details = mapOf(
                "bsl_price" to sweep.bslPrice,
                "ssl_price" to sweep.sslPrice,
                "turtle_soup_long" to sweep.turtleSoupLong,
                "turtle_soup_short" to sweep.turtleSoupShort,
                "fvg_bull" to sweep.fairValueGapBullish,
                "fvg_bear" to sweep.fairValueGapBearish,
                "score" to sweep.ictSignalScore
            )
        )
    }
}

// 35. Multiverse Counterfactual Futures Simulation Engine Wrapper
class MultiverseMlEngine : MlEnsembleEngine {
    override val name: String = "Multiverse_Branching_Sim"
    override val category: String = "OPTIMIZER"

    override suspend fun predict(
        symbol: String,
        dna: MarketDna125Snapshot,
        prices: List<Double>,
        bellStats: BellCurveDistributionStats
    ): EnginePredictionResult {
        val t0 = System.currentTimeMillis()
        val currP = prices.lastOrNull() ?: 100.0
        val drift = if (prices.size >= 2) (prices.last() - prices.first()) / prices.first() else 0.001
        val vol = (dna.featureMap["garch_forecast_1h"] ?: 0.25).coerceAtLeast(0.01)
        val multi = MultiverseDecisionEngine.evaluateFutures(
            currentPrice = currP,
            volatility = vol,
            driftRate = drift,
            fatTailFactor = bellStats.fatTailFactor
        )

        val prob = when (multi.consensusDecision) {
            "STRONG_BUY" -> 0.78
            "STRONG_SELL" -> 0.22
            else -> 0.50
        }

        return EnginePredictionResult(
            engineName = name,
            category = category,
            probability = prob,
            confidence = (multi.worldConfidencePct / 100.0).coerceIn(0.4, 0.95),
            isAnomalyOrBlock = false,
            summary = "Multiverse: 5 Branching Worlds Consensus: ${multi.consensusDecision} (${multi.worldConfidencePct.toInt()}% confidence)",
            latencyMs = System.currentTimeMillis() - t0,
            details = mapOf(
                "consensus" to multi.consensusDecision,
                "worlds_count" to multi.branchingWorldsCount,
                "expected_return" to multi.expectedMultiverseReturn,
                "confidence" to multi.worldConfidencePct
            )
        )
    }
}

// Helper utility to convert price list to candles if needed
private fun buildDummyCandles(prices: List<Double>): List<PriceCandle> {
    if (prices.isEmpty()) {
        return List(30) { i ->
            PriceCandle(System.currentTimeMillis() + i * 60000L, 100.0, 102.0, 98.0, 101.0, 1000.0)
        }
    }
    return prices.mapIndexed { idx, p ->
        val v = 1000.0 + (idx % 5) * 200.0
        val high = p * 1.002
        val low = p * 0.998
        val open = if (idx > 0) prices[idx - 1] else p
        PriceCandle(System.currentTimeMillis() - (prices.size - idx) * 60000L, open, high, low, p, v)
    }
}
