package com.example.engine.ml_ensemble

import com.example.engine.lib.quant_shared.BellCurveDistributionStats
import com.example.engine.lib.quant_shared.MarketDna125Snapshot

data class EnginePredictionResult(
    val engineName: String,
    val category: String, // "RISK", "PREDICTOR", "REGIME", "OPTIMIZER", "GATE"
    val probability: Double, // 0.0 to 1.0 (win probability or signal)
    val confidence: Double,  // 0.0 to 1.0
    val isAnomalyOrBlock: Boolean = false,
    val summary: String = "",
    val details: Map<String, Any> = emptyMap(),
    val latencyMs: Long = 0L
)

interface MlEnsembleEngine {
    val name: String
    val category: String
    suspend fun predict(
        symbol: String,
        dna: MarketDna125Snapshot,
        prices: List<Double>,
        bellStats: BellCurveDistributionStats
    ): EnginePredictionResult
}
