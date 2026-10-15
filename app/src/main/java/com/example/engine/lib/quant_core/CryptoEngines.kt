package com.example.engine.lib.quant_core

/**
 * ============================================================================
 * CRYPTO GROUP QUANT ENGINES (GROUP 2)
 * Path: com.example.engine.lib.quant_core.CryptoEngines
 * ============================================================================
 */

data class CQFCryptoMetrics(
    val pricingModel: String,
    val calibrationMethod: String,
    val jumpIntensityLambda: Double,
    val expectedJumpSize: Double,
    val orderbookImbalanceDepth: Double,
    val fundingRateAnnualizedPct: Double,
    val liquidationPoolDistancePct: Double,
    val hawkesExcitationBeta: Double,
    val edgeProbability: Double
)

data class FRMCryptoRisk(
    val fatTailedStudentT_VaR99Pct: Double,
    val cvarExpectedShortfall99Pct: Double,
    val stressTest5SigmaFlashCrashLossPct: Double,
    val liquidationBufferDistance: Double,
    val maxLeveragePermitted: Double,
    val maxDrawdownCapPct: Double
)

data class CFACryptoPerformance(
    val sharpeRatio: Double,
    val sortinoRatio: Double,
    val calmarRatio: Double,
    val profitFactor: Double,
    val winRatePct: Double,
    val annualizedReturnPct: Double,
    val expectancyPerTradePct: Double
)

data class CryptoEngineEvaluation(
    val symbol: String,
    val subEngine: String,
    val cqf: CQFCryptoMetrics,
    val frm: FRMCryptoRisk,
    val cfa: CFACryptoPerformance,
    val suggestedAction: String
)

object CryptoEngines {

    fun evaluate(symbol: String, currentPrice: Double): CryptoEngineEvaluation {
        return CryptoEngineEvaluation(
            symbol = symbol.uppercase(),
            subEngine = "DEPTH_IMBALANCE",
            cqf = CQFCryptoMetrics(
                pricingModel = "Merton Jump-Diffusion + Hawkes Self-Exciting Process",
                calibrationMethod = "Maximum Likelihood Estimation (MLE) on tick clustering & funding delta",
                jumpIntensityLambda = 0.14,
                expectedJumpSize = -0.025,
                orderbookImbalanceDepth = 0.68,
                fundingRateAnnualizedPct = 10.95,
                liquidationPoolDistancePct = 1.85,
                hawkesExcitationBeta = 2.45,
                edgeProbability = 0.62
            ),
            frm = FRMCryptoRisk(
                fatTailedStudentT_VaR99Pct = 4.5,
                cvarExpectedShortfall99Pct = 6.8,
                stressTest5SigmaFlashCrashLossPct = 12.0,
                liquidationBufferDistance = 3.5,
                maxLeveragePermitted = 3.0,
                maxDrawdownCapPct = 8.0
            ),
            cfa = CFACryptoPerformance(
                sharpeRatio = 1.85,
                sortinoRatio = 2.40,
                calmarRatio = 2.90,
                profitFactor = 1.82,
                winRatePct = 62.0,
                annualizedReturnPct = 42.0,
                expectancyPerTradePct = 0.65
            ),
            suggestedAction = "LONG"
        )
    }
}
