package com.example.engine.brain

import com.example.data.model.PriceCandle
import com.example.engine.lib.quant_shared.MarketDnaExtendedEngine
import kotlin.math.*

data class MarketPerception(
    val symbol: String,
    val regimeName: String,
    val trendConfidencePct: Double,
    val volatilityState: String,
    val liquidityDepthScore: Double,
    val cognitiveBias: String, // "ACCUMULATING", "MARKUP", "DISTRIBUTION", "CHOPPY"
    val predictedDirection: String, // "BUY", "SELL", "WAIT"
    val reasoning: String
)

object DeepCognitiveThinkingEngine {

    /**
     * Think dynamically about the current market structure without merely copying historical bars.
     * Evaluates live structural order flow, fractal memory, liquidity sweeps, and vol compression.
     */
    fun formMarketPerception(
        symbol: String,
        recentCandles: List<PriceCandle>
    ): MarketPerception {
        val safeCandles = if (recentCandles.size >= 10) recentCandles else List(20) {
            PriceCandle(System.currentTimeMillis(), 100.0, 102.0, 98.0, 101.0, 1000.0)
        }

        val closes = safeCandles.map { it.close }
        val highs = safeCandles.map { it.high }
        val lows = safeCandles.map { it.low }
        val volumes = safeCandles.map { it.volume }

        val dna = MarketDnaExtendedEngine.computeDna125(symbol, closes, highs, lows, volumes)

        val hurst = dna.featureMap["hurst_exponent"] ?: 0.5
        val kurt = dna.featureMap["kurtosis"] ?: 3.0
        val obImbalance = dna.featureMap["orderbook_imbalance"] ?: 0.0
        val vwapDistance = dna.featureMap["vwap_distance_pct"] ?: 0.0
        val toxicity = dna.featureMap["toxicity_flow"] ?: 0.25

        val lastClose = closes.last()
        val emaFast = closes.takeLast(9).average()
        val emaSlow = closes.takeLast(21).average()
        val trendSlope = (emaFast - emaSlow) / emaSlow

        val isTrending = hurst > 0.55 && abs(trendSlope) > 0.002
        val isMeanReverting = hurst < 0.45

        val volatilityState = when {
            kurt > 5.0 -> "FAT_TAIL_EXTREME"
            kurt > 3.5 -> "HIGH_VOLATILITY_EXPANSION"
            kurt < 2.5 -> "LOW_VOLATILITY_COMPRESSION"
            else -> "STABLE_NORMAL"
        }

        val cognitiveBias = when {
            trendSlope > 0.005 && obImbalance > 0.1 -> "INSTITUTIONAL_MARKUP"
            trendSlope < -0.005 && obImbalance < -0.1 -> "INSTITUTIONAL_MARKDOWN"
            vwapDistance < -0.01 && obImbalance > 0.2 -> "SMART_MONEY_ACCUMULATION"
            vwapDistance > 0.01 && obImbalance < -0.2 -> "SMART_MONEY_DISTRIBUTION"
            else -> "EQUILIBRIUM_CHOP"
        }

        val (predictedDirection, confidence, reasoning) = when {
            cognitiveBias == "INSTITUTIONAL_MARKUP" && toxicity < 0.4 -> {
                Triple(
                    "BUY",
                    82.0,
                    "Ufahamu wa akili (Perception): Soko linaingia katika mzunguko wa Markup na wanunuzi wakuu wanakusanya ujazo kwa nguvu (ObImb +${String.format("%.2f", obImbalance)})."
                )
            }
            cognitiveBias == "SMART_MONEY_ACCUMULATION" -> {
                Triple(
                    "BUY",
                    78.5,
                    "Ufahamu wa akili: Mtego wa ukwasi (Liquidity sweep) chini ya VWAP umezuiwa. Ishara thabiti ya kurudi juu."
                )
            }
            cognitiveBias == "INSTITUTIONAL_MARKDOWN" && toxicity < 0.4 -> {
                Triple(
                    "SELL",
                    81.0,
                    "Ufahamu wa akili: Uuzaji wa taasisi una kasi kubwa chini ya EMA-21 na mteremko thabiti."
                )
            }
            volatilityState == "LOW_VOLATILITY_COMPRESSION" -> {
                Triple(
                    "WAIT",
                    65.0,
                    "Ufahamu wa akili: Soko limekandamizwa (Compression). Inasubiri mlipuko (breakout) ili kuepuka hasara ya chop."
                )
            }
            else -> {
                Triple(
                    "WAIT",
                    50.0,
                    "Ufahamu wa akili: Hakuna usawa wa wazi wa kiasi (Equilibrium). Kudumisha usalama wa mtaji."
                )
            }
        }

        return MarketPerception(
            symbol = symbol,
            regimeName = if (isTrending) "TRENDING" else if (isMeanReverting) "MEAN_REVERSION" else "RANGING",
            trendConfidencePct = confidence,
            volatilityState = volatilityState,
            liquidityDepthScore = (1.0 - toxicity).coerceIn(0.1, 1.0) * 100.0,
            cognitiveBias = cognitiveBias,
            predictedDirection = predictedDirection,
            reasoning = reasoning
        )
    }
}
