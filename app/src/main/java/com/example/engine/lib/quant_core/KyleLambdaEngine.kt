package com.example.engine.lib.quant_core

import kotlin.math.*

data class KyleLambdaResult(
    val lambda: Double,             // Price change per signed volume unit
    val toxicityScore: Double,      // 0 - 100
    val expectedSlippagePct: Double,// Market impact %
    val isToxic: Boolean,           // > 70 => Market makers hunting / informed traders dominant
    val recommendedLotScale: Double // 1.0 down to 0.5 if toxic
)

object KyleLambdaEngine {

    /**
     * Kyle's Lambda = Delta P / (Signed Volume)
     * Market Impact = lambda * sqrt(orderSize)
     */
    fun evaluateImpact(
        priceChange: Double,
        signedVolume: Double,
        orderSizeLots: Double,
        midPrice: Double
    ): KyleLambdaResult {
        val safeVolume = if (abs(signedVolume) > 1e-6) signedVolume else 1000.0
        val rawLambda = abs(priceChange) / abs(safeVolume)
        val normalizedLambda = (rawLambda / (midPrice.coerceAtLeast(1.0) * 1e-4)).coerceIn(0.00001, 0.05)

        val toxicityScore = (normalizedLambda * 2500.0).coerceIn(5.0, 99.0)
        val isToxic = toxicityScore > 70.0

        val sqrtImpact = normalizedLambda * sqrt(orderSizeLots.coerceAtLeast(0.01) * 100.0)
        val expectedSlippage = sqrtImpact.coerceIn(0.0001, 0.02)

        val lotScale = if (isToxic) 0.5 else 1.0

        return KyleLambdaResult(
            lambda = normalizedLambda,
            toxicityScore = toxicityScore,
            expectedSlippagePct = expectedSlippage,
            isToxic = isToxic,
            recommendedLotScale = lotScale
        )
    }
}
