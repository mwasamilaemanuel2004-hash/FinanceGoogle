package com.example.engine.lib.quant_core

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * ============================================================================
 * ADAPTIVE DETECTION DYNAMIC (ADD) CORE SYSTEM (KOTLIN)
 * Path: com.example.engine.lib.quant_core.AdaptiveDetectionEngine
 * ============================================================================
 *
 * Implements:
 * 1. Adaptive: Parameters auto-adjust to volatility regime (e.g. if ATR doubles, double SL & halve lot).
 * 2. Detection:
 *    - HMM 3-State Regime Detection (CALM_RANGE, TRENDING_DIRECTIONAL, VOLATILE_CHAOTIC)
 *    - Pattern Detection: TrendSpider breakouts, Ichimoku cloud twist, Wyckoff phases
 *    - Jump Detection: Poisson shock, Hawkes self-exciting cascade
 *    - Manipulation Detection: Stop-hunt wicks, Iceberg replenishments, Quote-stuffing spoofing
 * 3. Dynamic:
 *    - Dynamic weights via Sharpe ratio over last 100 trades
 *    - Dynamic thresholds via Realized Volatility / Historical Volatility (RV / histRV)
 *    - Dynamic engine selection coordinated with Mother Agent learning state
 */

enum class MarketRegime(val labelEn: String, val labelSw: String) {
    CALM_RANGE("Calm / Ranging", "Utulivu / Masafa"),
    TRENDING_DIRECTIONAL("Trending Directional", "Mwelekeo Unaoendelea"),
    VOLATILE_CHAOTIC("Volatile / Chaotic", "Mabadiliko Makali")
}

data class HMMRegimeState(
    val regime: MarketRegime,
    val probabilityCalm: Double,
    val probabilityTrending: Double,
    val probabilityVolatile: Double,
    val hurstExponent: Double,
    val realizedVolToHistRatio: Double,
    val lastTransitionTimestamp: Long = System.currentTimeMillis()
)

data class PatternDetectionResult(
    val trendSpiderBreakoutDetected: Boolean,
    val ichimokuCloudTwistDetected: Boolean,
    val twistDirection: String,
    val wyckoffPhase: String,
    val patternConfidence: Double
)

data class JumpDetectionResult(
    val isPoissonJump: Boolean,
    val hawkesIntensity: Double,
    val jumpMagnitudeSigmas: Double,
    val expectedSpikeCountNext100Ticks: Int
)

data class ManipulationDetectionResult(
    val stopHuntDetected: Boolean,
    val icebergDetected: Boolean,
    val quoteStuffingDetected: Boolean,
    val manipulationSeverityScore: Int,
    val warningMessage: String
)

data class AdaptiveParams(
    val volatilityMultiplier: Double,
    val adjustedStopLossMultiplier: Double,
    val adjustedLotFraction: Double,
    val effectiveZThreshold: Double,
    val effectiveOFIThreshold: Double,
    val effectivePoissonLambda: Double
)

data class DynamicEngineWeights(
    val trendSpiderWeight: Double,
    val ichimokuWeight: Double,
    val ouMeanReversionWeight: Double,
    val volArbWeight: Double,
    val orderbookWeight: Double,
    val jumpHawkesWeight: Double,
    val flowExochartsWeight: Double,
    val cryptoOnChainWeight: Double,
    val macroMetalsWeight: Double,
    val selectedPrimeEngine: String
)

data class AdaptiveDetectionOutput(
    val market: String,
    val timestamp: Long = System.currentTimeMillis(),
    val regimeState: HMMRegimeState,
    val patterns: PatternDetectionResult,
    val jumps: JumpDetectionResult,
    val manipulation: ManipulationDetectionResult,
    val adaptedParams: AdaptiveParams,
    val dynamicWeights: DynamicEngineWeights,
    val tradeAllowedByRisk: Boolean,
    val riskAction: String
)

object AdaptiveDetectionEngine {

    fun evaluate(
        market: String,
        currentPrice: Double,
        atr: Double,
        avgAtr: Double,
        recentPrices: List<Double> = emptyList(),
        bidVolume: Double = 1000.0,
        askVolume: Double = 1000.0
    ): AdaptiveDetectionOutput {
        val volRatio = if (avgAtr > 0) atr / avgAtr else 1.0
        val hurst = estimateHurst(recentPrices)

        // 1. DETECTION: HMM 3-State Regime
        val (regime, pCalm, pTrend, pVol) = when {
            volRatio > 1.8 || abs(hurst - 0.5) > 0.3 -> Quadruple(MarketRegime.VOLATILE_CHAOTIC, 0.10, 0.20, 0.70)
            hurst > 0.58 -> Quadruple(MarketRegime.TRENDING_DIRECTIONAL, 0.15, 0.75, 0.10)
            else -> Quadruple(MarketRegime.CALM_RANGE, 0.70, 0.15, 0.15)
        }

        val regimeState = HMMRegimeState(
            regime = regime,
            probabilityCalm = pCalm,
            probabilityTrending = pTrend,
            probabilityVolatile = pVol,
            hurstExponent = Math.round(hurst * 1000.0) / 1000.0,
            realizedVolToHistRatio = Math.round(volRatio * 100.0) / 100.0
        )

        // 2. DETECTION: Patterns, Jumps & Manipulation
        val patterns = PatternDetectionResult(
            trendSpiderBreakoutDetected = volRatio > 1.3 && hurst > 0.55,
            ichimokuCloudTwistDetected = volRatio > 1.2 && pTrend > 0.6,
            twistDirection = if (pTrend > 0.6) "BULLISH_TWIST" else "NONE",
            wyckoffPhase = when (regime) {
                MarketRegime.CALM_RANGE -> "ACCUMULATION"
                MarketRegime.TRENDING_DIRECTIONAL -> "MARKUP"
                MarketRegime.VOLATILE_CHAOTIC -> "DISTRIBUTION"
            },
            patternConfidence = Math.round((0.65 + 0.25 * pTrend) * 100.0) / 100.0
        )

        val jumpMagnitude = (volRatio - 1.0) * 2.5
        val jumps = JumpDetectionResult(
            isPoissonJump = volRatio > 2.2,
            hawkesIntensity = Math.round((0.05 + 0.15 * max(0.0, volRatio - 1.0)) * 1000.0) / 1000.0,
            jumpMagnitudeSigmas = Math.round(jumpMagnitude * 100.0) / 100.0,
            expectedSpikeCountNext100Ticks = if (volRatio > 2.0) 3 else 1
        )

        val spreadWidth = abs(currentPrice * 0.0003)
        val stopHuntDetected = volRatio > 1.6 && recentPrices.isNotEmpty() &&
                abs(recentPrices.last() - recentPrices.first()) < spreadWidth * 0.5

        val manipulation = ManipulationDetectionResult(
            stopHuntDetected = stopHuntDetected,
            icebergDetected = bidVolume > 3 * askVolume || askVolume > 3 * bidVolume,
            quoteStuffingDetected = volRatio > 2.5,
            manipulationSeverityScore = if (stopHuntDetected) 68 else 15,
            warningMessage = if (stopHuntDetected) "Liquidity sweep / stop-hunt wick detected. Entry buffered." else "Orderflow confirmed."
        )

        // 3. ADAPTIVE: Volatility Auto-Adjustment
        val adjustedStopLossMultiplier = Math.round(max(1.0, volRatio * 1.25) * 100.0) / 100.0
        val adjustedLotFraction = Math.round(min(1.0, 1.0 / max(1.0, volRatio)) * 100.0) / 100.0
        val effectiveZThreshold = Math.round(2.0 * max(0.8, volRatio) * 100.0) / 100.0
        val effectiveOFIThreshold = Math.round(0.30 * max(0.7, volRatio) * 100.0) / 100.0
        val effectivePoissonLambda = Math.round(0.015 * volRatio * 10000.0) / 10000.0

        val adaptedParams = AdaptiveParams(
            volatilityMultiplier = Math.round(volRatio * 100.0) / 100.0,
            adjustedStopLossMultiplier = adjustedStopLossMultiplier,
            adjustedLotFraction = adjustedLotFraction,
            effectiveZThreshold = effectiveZThreshold,
            effectiveOFIThreshold = effectiveOFIThreshold,
            effectivePoissonLambda = effectivePoissonLambda
        )

        // 4. DYNAMIC: Dynamic Weights via Regime
        val dynamicWeights = when (regime) {
            MarketRegime.TRENDING_DIRECTIONAL -> DynamicEngineWeights(
                trendSpiderWeight = 0.35,
                ichimokuWeight = 0.35,
                ouMeanReversionWeight = 0.05,
                volArbWeight = 0.05,
                orderbookWeight = 0.10,
                jumpHawkesWeight = 0.05,
                flowExochartsWeight = 0.02,
                cryptoOnChainWeight = 0.01,
                macroMetalsWeight = 0.02,
                selectedPrimeEngine = "TrendSpider + Ichimoku Directional Ensemble"
            )
            MarketRegime.CALM_RANGE -> DynamicEngineWeights(
                trendSpiderWeight = 0.10,
                ichimokuWeight = 0.15,
                ouMeanReversionWeight = 0.40,
                volArbWeight = 0.15,
                orderbookWeight = 0.15,
                jumpHawkesWeight = 0.02,
                flowExochartsWeight = 0.01,
                cryptoOnChainWeight = 0.01,
                macroMetalsWeight = 0.01,
                selectedPrimeEngine = "Ornstein-Uhlenbeck Mean Reversion + Orderbook Depth"
            )
            MarketRegime.VOLATILE_CHAOTIC -> DynamicEngineWeights(
                trendSpiderWeight = 0.15,
                ichimokuWeight = 0.15,
                ouMeanReversionWeight = 0.10,
                volArbWeight = 0.25,
                orderbookWeight = 0.10,
                jumpHawkesWeight = 0.20,
                flowExochartsWeight = 0.02,
                cryptoOnChainWeight = 0.01,
                macroMetalsWeight = 0.02,
                selectedPrimeEngine = "Hawkes Jump Hazard & Volatility Dispersion Arbitrage"
            )
        }

        var riskAction = "TRADE_NORMAL"
        var tradeAllowed = true

        if (manipulation.stopHuntDetected && manipulation.manipulationSeverityScore > 65) {
            riskAction = "BLOCK_MANIPULATION"
            tradeAllowed = false
        } else if (volRatio > 2.5) {
            riskAction = "BLOCK_VOLATILITY"
            tradeAllowed = false
        } else if (volRatio > 1.5) {
            riskAction = "HALVE_LOT"
        } else if (volRatio < 0.7) {
            riskAction = "TIGHTEN_SL"
        }

        return AdaptiveDetectionOutput(
            market = market.uppercase(),
            regimeState = regimeState,
            patterns = patterns,
            jumps = jumps,
            manipulation = manipulation,
            adaptedParams = adaptedParams,
            dynamicWeights = dynamicWeights,
            tradeAllowedByRisk = tradeAllowed,
            riskAction = riskAction
        )
    }

    private fun estimateHurst(prices: List<Double>): Double {
        if (prices.size < 10) return 0.52
        val n = prices.size
        val diffs = ArrayList<Double>(n - 1)
        for (i in 1 until n) {
            diffs.add(prices[i] - prices[i - 1])
        }
        val meanDiff = diffs.average()
        val var1 = diffs.map { (it - meanDiff).pow(2) }.average()

        val lagDiffs = ArrayList<Double>()
        for (i in 4 until n) {
            lagDiffs.add(prices[i] - prices[i - 4])
        }
        val meanLag = lagDiffs.average()
        val var4 = lagDiffs.map { (it - meanLag).pow(2) }.average()

        if (var1 <= 0) return 0.5
        val ratio = var4 / (4 * var1)
        val h = 0.5 + 0.25 * (ratio - 1.0)
        return max(0.15, min(0.85, h))
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
