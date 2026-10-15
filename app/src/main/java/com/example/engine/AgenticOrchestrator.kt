package com.example.engine

import com.example.engine.lib.execution.*
import com.example.engine.lib.execution.ExecutionPlan as SmartExecutionPlan
import com.example.engine.lib.quant_shared.BellCurveDistributionStats
import com.example.engine.lib.quant_shared.BellCurveEngine
import com.example.engine.lib.quant_shared.MarketDna125Snapshot
import com.example.engine.lib.quant_shared.MarketDnaExtendedEngine
import com.example.engine.ml_ensemble.EnginePredictionResult
import com.example.engine.ml_ensemble.MlEnsembleEngine
import com.example.engine.ml_ensemble.gate.*
import com.example.engine.ml_ensemble.optimizers.*
import com.example.engine.ml_ensemble.predictors.*
import com.example.engine.ml_ensemble.quant_advanced.*
import com.example.engine.ml_ensemble.regime.*
import com.example.engine.ml_ensemble.risk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EnsembleMarketEvaluation(
    val symbol: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dna125: MarketDna125Snapshot,
    val bellStats: BellCurveDistributionStats,
    val engineResults: List<EnginePredictionResult>,
    val riskPass: Boolean,
    val killSwitchAllowed: Boolean,
    val finalWinProbability: Double,
    val monteCarloProbability: Double,
    val isApproved: Boolean,
    val executionPlan: SmartExecutionPlan?,
    val decisionRationale: String,
    val statArbDetail: com.example.engine.ml_ensemble.optimizers.StatArbDetailedResult? = null
)

object AgenticOrchestrator {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 25 Isolated Engines (Including Engine 25 Stat-Arb MIT / D.E. Shaw)
    private val statArbEngine = StatArbEngine()

    private val engines: List<MlEnsembleEngine> = listOf(
        // Risk (4)
        IsolationForestEngine(),
        AutoencoderEngine(),
        BellCurveRiskEngine(),
        CopulaTailRiskEngine(),
        // Predictors (7)
        KnnEngine(),
        RandomForestEngine(),
        SvmEngine(),
        LstmEngine(),
        ProphetEngine(),
        ArimaSarimaEngine(),
        LightGbmCatBoostEngine(),
        // Regime & Graph (6)
        GmmRegimeEngine(),
        HmmRegimeEngine(),
        DbscanEngine(),
        MatrixFactorizationEngine(),
        AttentionEngine(),
        GnnContagionEngine(),
        // Optimizers (6 - including Engine 25)
        QLearningAgent(),
        PpoAgent(),
        GeneticOptimizer(),
        BayesianOptimizer(),
        MonteCarloEngine(),
        statArbEngine,
        // Gate & Explainers (3)
        NaiveBayesNewsGate(),
        LogisticMetaGate(),
        ShapLimeExplainerEngine(),
        // Ultimate Quant Expansion Engines 26-35 (MIT Physics & D.E. Shaw Core)
        IchimokuMlEngine(),
        WyckoffMlEngine(),
        VolumeProfileMlEngine(),
        VolatilityArbMlEngine(),
        MarketMakingMlEngine(),
        BayesianEvMlEngine(),
        ShannonEntropyMlEngine(),
        LeadLagMlEngine(),
        LiquiditySweepMlEngine(),
        MultiverseMlEngine()
    )

    private val _latestEvaluations = MutableStateFlow<Map<String, EnsembleMarketEvaluation>>(emptyMap())
    val latestEvaluations: StateFlow<Map<String, EnsembleMarketEvaluation>> = _latestEvaluations.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    /**
     * Run full 4-layer evaluation for a symbol
     */
    suspend fun evaluateSymbol(
        symbol: String,
        prices: List<Double> = emptyList(),
        highs: List<Double> = emptyList(),
        lows: List<Double> = emptyList(),
        volumes: List<Double> = emptyList(),
        currentPrice: Double = 65000.0
    ): EnsembleMarketEvaluation = withContext(Dispatchers.Default) {
        _isAnalyzing.value = true
        val safePrices = if (prices.size >= 5) prices else List(60) { currentPrice * (1.0 + Math.sin(it.toDouble() * 0.1) * 0.02) }

        // Layer 1: DNA 125 + Bell Curve
        val dna125 = MarketDnaExtendedEngine.computeDna125(symbol, safePrices, highs, lows, volumes)
        val bellStats = BellCurveEngine.analyzeDistribution(symbol, safePrices)

        // Layer 2: 24 Engines in isolated async tasks (Promise.allSettled pattern)
        val results = coroutineScope {
            engines.map { eng ->
                async {
                    try {
                        eng.predict(symbol, dna125, safePrices, bellStats)
                    } catch (e: Exception) {
                        EnginePredictionResult(
                            engineName = eng.name,
                            category = eng.category,
                            probability = 0.50,
                            confidence = 0.50,
                            isAnomalyOrBlock = false,
                            summary = "Engine isolated fallback: ${e.message}"
                        )
                    }
                }
            }.awaitAll()
        }

        // Layer 3: Risk Evaluation
        val isoScore = (results.find { it.engineName == "Isolation_Forest" }?.details?.get("anomaly_score") as? Double) ?: 10.0
        val reconError = (results.find { it.engineName == "Autoencoder_Recon" }?.details?.get("recon_error") as? Double) ?: 0.02
        val tailCorr = (results.find { it.engineName == "Copula_TailRisk" }?.details?.get("tail_corr") as? Double) ?: 0.5

        val riskPass = isoScore < 90.0 && reconError < 0.10 && bellStats.fatTailFactor < 5.0 && tailCorr < 0.95
        val killAllowed = KillSwitchEngine.checkIsTradingAllowed(symbol, bellStats.fatTailFactor, isoScore)

        val logisticResult = results.find { it.engineName == "Logistic_Meta_Gate" }
        val finalWinProb = logisticResult?.probability ?: 0.68
        val mcResult = results.find { it.engineName == "Monte_Carlo_10k_StudentT" }
        val mcProb = mcResult?.probability ?: 0.64

        // D.E. Shaw & MIT Physics Core (Engine 25 Evaluation)
        val statArbDetail = statArbEngine.evaluateStatArb(symbol, dna125, safePrices, bellStats)
        val hurst = dna125.featureMap["hurst_exponent"] ?: 0.50

        // Decision Override Logic:
        // If Deriv synthetic and Hurst < 0.45 (ranging) and OU zScore > 2.0 or < -2.0, StatArb overrides trend
        val isOuOverride = (symbol.contains("VOLATILITY", true) || symbol.contains("CRASH", true) || symbol.contains("BOOM", true)) &&
                hurst < 0.45 && (statArbDetail.ouResult?.zScore ?: 0.0).let { Math.abs(it) > 2.0 }

        val determinedAction = when {
            isOuOverride -> statArbDetail.ouResult?.signal ?: "BUY"
            statArbDetail.strategy == com.example.engine.ml_ensemble.optimizers.StatArbStrategyType.FUNDING_ARB -> "SELL"
            statArbDetail.signal == "SHORT_SPREAD" -> "SELL"
            statArbDetail.signal == "LONG_SPREAD" -> "BUY"
            else -> "BUY"
        }

        val optimalLot = statArbDetail.kellyOptimal?.lotSizeOptimal ?: 0.50
        val shouldApprove = (finalWinProb > 0.60 || isOuOverride) && mcProb > 0.55 && riskPass && killAllowed

        val executionPlan = if (shouldApprove) {
            SmartExecutionEngine.evaluateExecution(
                symbol = symbol,
                action = determinedAction,
                requestedLot = optimalLot,
                spreadPct = dna125.featureMap["bid_ask_spread_pct"] ?: 0.0004
            )
        } else null

        val rationale = if (shouldApprove) {
            if (isOuOverride) {
                "APPROVED STAT-ARB OU OVERRIDE (Z: ${String.format("%.2f", statArbDetail.ouResult?.zScore)}, Half-Life: ${statArbDetail.ouResult?.halfLifeMinutes?.toInt()}m, Kelly Lot: ${String.format("%.2f", optimalLot)})"
            } else {
                "APPROVED (Win Prob: ${String.format("%.0f", finalWinProb * 100)}%, MC: ${String.format("%.0f", mcProb * 100)}%, Kelly Lot: ${String.format("%.2f", optimalLot)}, Fat-tail: ${String.format("%.1f", bellStats.fatTailFactor)}x)"
            }
        } else {
            "BLOCKED (${if (!riskPass) "RISK VIOLATION" else if (!killAllowed) "KILL SWITCH ACTIVE" else "PROB < 60%"})"
        }

        // Record Paper Trade if approved
        val paperTrade = if (shouldApprove && executionPlan?.shouldExecute == true) {
            PaperTradingEngine.recordPaperTrade(
                symbol = symbol,
                action = determinedAction,
                prob = finalWinProb,
                lot = executionPlan.adjustedLot,
                entryPrice = currentPrice
            )
        } else null

        // Audit Trail (include StatArb, OU, Kelly)
        val fullAuditRationale = "$rationale | StatArb: ${statArbDetail.strategy} Z=${String.format("%.2f", statArbDetail.zScore)} | Kelly Lot: ${String.format("%.2f", optimalLot)}"
        AuditAndAlertEngine.logAudit(
            symbol = symbol,
            action = if (shouldApprove) determinedAction else "HOLD",
            dnaHash = dna125.hash,
            fatTail = bellStats.fatTailFactor,
            finalProb = finalWinProb,
            riskPass = riskPass,
            killSwitch = killAllowed,
            executionRoute = executionPlan?.executionType?.name ?: "NONE",
            paperId = paperTrade?.id ?: "N/A",
            rationale = fullAuditRationale
        )

        val eval = EnsembleMarketEvaluation(
            symbol = symbol,
            dna125 = dna125,
            bellStats = bellStats,
            engineResults = results,
            riskPass = riskPass,
            killSwitchAllowed = killAllowed,
            finalWinProbability = finalWinProb,
            monteCarloProbability = mcProb,
            isApproved = shouldApprove,
            executionPlan = executionPlan,
            decisionRationale = rationale,
            statArbDetail = statArbDetail
        )

        _latestEvaluations.value = _latestEvaluations.value + (symbol to eval)
        _isAnalyzing.value = false
        eval
    }
}
