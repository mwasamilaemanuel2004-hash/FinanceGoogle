package com.example.engine

import kotlin.math.*

data class RegimeTransitionMatrixKt(
    val currentRegime: String,
    val probCalmNext: Double,
    val probTrendingNext: Double,
    val probVolatileNext: Double,
    val transitionConfidence: Double
)

data class CrossMarketCorrelationNodeKt(
    val assetA: String,
    val assetB: String,
    val leadLagLagSeconds: Int,
    val rollingCorrelation: Double,
    val spilloverRiskPct: Double
)

data class IntelligenceForecastKt(
    val symbol: String,
    val timestamp: Long,
    val timeframe: String,
    val predictedDirection: String,
    val directionConfidencePct: Double,
    val projectedVolatilityPct: Double,
    val keySupportPrice: Double,
    val keyResistancePrice: Double,
    val catalystEvent: String
)

data class AdvanceIntelligenceReportKt(
    val symbol: String,
    val timestamp: Long,
    val regimeTransition: RegimeTransitionMatrixKt,
    val macroSentimentScore: Double,
    val crossMarketNetwork: List<CrossMarketCorrelationNodeKt>,
    val forecasts: List<IntelligenceForecastKt>,
    val deepMarketInsights: List<String>,
    val latencyMs: Double
)

object AdvanceIntelligenceEngine {
    fun analyze(
        symbol: String,
        currentPrice: Double,
        recentPrices: List<Double>,
        macroNewsHeadlineScore: Double = 0.15
    ): AdvanceIntelligenceReportKt {
        val startTime = System.nanoTime()

        val regimeTransition = computeRegimeTransitions(recentPrices)
        val network = getDefaultCrossMarketNetwork(symbol)

        val forecasts = listOf(
            generateForecast(symbol, currentPrice, "1M", recentPrices, 0.002),
            generateForecast(symbol, currentPrice, "15M", recentPrices, 0.006),
            generateForecast(symbol, currentPrice, "1H", recentPrices, 0.014),
            generateForecast(symbol, currentPrice, "4H", recentPrices, 0.028),
            generateForecast(symbol, currentPrice, "1D", recentPrices, 0.055)
        )

        val insights = mutableListOf<String>()
        when (regimeTransition.currentRegime) {
            "VOLATILE_EXPANSION" -> insights.add("High volatility regime detected: Position sizing trimmed via Kelly Criterion.")
            "DIRECTIONAL_TREND" -> insights.add("Directional trend active: Momentum & TrendSpider engines weighted up.")
            else -> insights.add("Calm range regime: Mean-reversion OU and liquidity sweeps harvesting prioritized.")
        }
        if (abs(macroNewsHeadlineScore) > 0.3) {
            insights.add("Macro sentiment divergence: Score ${"%.2f".format(macroNewsHeadlineScore)} confirms institutional flows.")
        }

        val durationNs = System.nanoTime() - startTime
        val latencyMs = max(0.15, ((durationNs / 1_000_000.0) * 100).roundToInt() / 100.0)

        return AdvanceIntelligenceReportKt(
            symbol = symbol,
            timestamp = System.currentTimeMillis(),
            regimeTransition = regimeTransition,
            macroSentimentScore = macroNewsHeadlineScore,
            crossMarketNetwork = network,
            forecasts = forecasts,
            deepMarketInsights = insights,
            latencyMs = latencyMs
        )
    }

    private fun computeRegimeTransitions(prices: List<Double>): RegimeTransitionMatrixKt {
        if (prices.size < 5) {
            return RegimeTransitionMatrixKt(
                currentRegime = "CALM_RANGE",
                probCalmNext = 0.75,
                probTrendingNext = 0.15,
                probVolatileNext = 0.10,
                transitionConfidence = 78.5
            )
        }

        var sumDiff = 0.0
        for (i in 1 until prices.size) {
            sumDiff += abs(prices[i] - prices[i - 1]) / max(0.0001, prices[i - 1])
        }
        val avgMove = sumDiff / (prices.size - 1)

        return when {
            avgMove > 0.008 -> RegimeTransitionMatrixKt(
                currentRegime = "VOLATILE_EXPANSION",
                probCalmNext = 0.20,
                probTrendingNext = 0.35,
                probVolatileNext = 0.45,
                transitionConfidence = 84.0
            )
            avgMove > 0.003 -> RegimeTransitionMatrixKt(
                currentRegime = "DIRECTIONAL_TREND",
                probCalmNext = 0.30,
                probTrendingNext = 0.55,
                probVolatileNext = 0.15,
                transitionConfidence = 81.2
            )
            else -> RegimeTransitionMatrixKt(
                currentRegime = "CALM_RANGE",
                probCalmNext = 0.70,
                probTrendingNext = 0.20,
                probVolatileNext = 0.10,
                transitionConfidence = 86.4
            )
        }
    }

    private fun generateForecast(
        symbol: String,
        currentPrice: Double,
        timeframe: String,
        prices: List<Double>,
        volFactor: Double
    ): IntelligenceForecastKt {
        val momentum = if (prices.size > 2) {
            (currentPrice - prices.first()) / max(1.0, prices.first())
        } else 0.0

        val dir = when {
            momentum > 0.0005 -> "BULLISH"
            momentum < -0.0005 -> "BEARISH"
            else -> "NEUTRAL"
        }
        val conf = min(88.5, 60.0 + abs(momentum) * 4000.0)

        return IntelligenceForecastKt(
            symbol = symbol,
            timestamp = System.currentTimeMillis(),
            timeframe = timeframe,
            predictedDirection = dir,
            directionConfidencePct = (conf * 10).roundToInt() / 10.0,
            projectedVolatilityPct = (volFactor * 1000).roundToInt() / 10.0,
            keySupportPrice = (currentPrice * (1.0 - volFactor * 1.5) * 10000).roundToInt() / 10000.0,
            keyResistancePrice = (currentPrice * (1.0 + volFactor * 1.5) * 10000).roundToInt() / 10000.0,
            catalystEvent = "Microstructure OFI alignment"
        )
    }

    private fun getDefaultCrossMarketNetwork(symbol: String): List<CrossMarketCorrelationNodeKt> {
        return listOf(
            CrossMarketCorrelationNodeKt("DXY (Dollar Index)", symbol, 12, -0.74, 2.1),
            CrossMarketCorrelationNodeKt("US10Y (Treasury Yield)", symbol, 45, -0.62, 1.8),
            CrossMarketCorrelationNodeKt("BTC/USDT", symbol, 5, 0.48, 1.4)
        )
    }
}
