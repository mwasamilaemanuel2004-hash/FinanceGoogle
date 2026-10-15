package com.example.engine.lib.quant_core

/**
 * ============================================================================
 * FOREX & METALS GROUP ENGINES (GROUP 1)
 * Path: com.example.engine.lib.quant_core.ForexEngines
 * ============================================================================
 */

data class ForexGroupMetrics(
    val pair: String,
    val trendSpider: TrendSpiderEvaluation,
    val ichimoku: UniversalIchimokuEvaluation,
    val cqfCompositeScore: Double,
    val frmCompositeVaR95: Double,
    val cfaCompositeSharpe: Double,
    val cfaCompositeReturnAnnual: Double,
    val isTradeEligible: Boolean,
    val recommendation: String // "EXECUTE_LONG", "EXECUTE_SHORT", "STAND_ASIDE"
)

object ForexEngines {

    val SUPPORTED_PAIRS = listOf(
        "EURUSD", "GBPUSD", "USDJPY", "AUDUSD", "USDCAD", "NZDUSD",
        "EURGBP", "EURJPY", "GBPJPY", "XAUUSD", "XAGUSD"
    )

    fun evaluatePair(pair: String, currentPrice: Double, atr: Double): ForexGroupMetrics {
        val trendSpider = TrendSpiderEngine.evaluate(pair, currentPrice, atr)
        val ichimoku = IchimokuUniversalEngine.evaluate(pair, currentPrice, atr)

        val cqfCompositeScore = (trendSpider.cqf.trendStrengthScore * 10.0) + (ichimoku.cqf.ichimokuScore * 0.6)
        val frmCompositeVaR95 = maxOf(trendSpider.frm.valueAtRisk95Pct, ichimoku.frm.valueAtRisk95Pct)
        val cfaCompositeSharpe = (trendSpider.cfa.sharpeRatio + ichimoku.cfa.sharpeRatio) / 2.0
        val cfaCompositeReturnAnnual = (trendSpider.cfa.annualizedReturnPct + ichimoku.cfa.annualizedReturnPct) / 2.0

        val isTradeEligible = frmCompositeVaR95 <= 2.0 && cfaCompositeSharpe >= 1.3
        val recommendation = if (cqfCompositeScore > 50 && isTradeEligible) "EXECUTE_LONG" else "STAND_ASIDE"

        return ForexGroupMetrics(
            pair = pair.uppercase(),
            trendSpider = trendSpider,
            ichimoku = ichimoku,
            cqfCompositeScore = cqfCompositeScore,
            frmCompositeVaR95 = frmCompositeVaR95,
            cfaCompositeSharpe = cfaCompositeSharpe,
            cfaCompositeReturnAnnual = cfaCompositeReturnAnnual,
            isTradeEligible = isTradeEligible,
            recommendation = recommendation
        )
    }

    fun evaluateAll(): Map<String, ForexGroupMetrics> {
        val samplePrices = mapOf(
            "EURUSD" to (1.0850 to 0.0045),
            "GBPUSD" to (1.2980 to 0.0060),
            "USDJPY" to (148.50 to 0.85),
            "AUDUSD" to (0.6650 to 0.0040),
            "USDCAD" to (1.3620 to 0.0050),
            "NZDUSD" to (0.6050 to 0.0038),
            "EURGBP" to (0.8360 to 0.0030),
            "EURJPY" to (161.20 to 0.95),
            "GBPJPY" to (192.80 to 1.20),
            "XAUUSD" to (2650.0 to 28.5),
            "XAGUSD" to (31.80 to 0.65)
        )

        return SUPPORTED_PAIRS.associateWith { pair ->
            val (price, atr) = samplePrices[pair] ?: (1.0 to 0.01)
            evaluatePair(pair, price, atr)
        }
    }
}
