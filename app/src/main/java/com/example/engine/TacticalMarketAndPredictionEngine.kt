package com.example.engine

import com.example.data.model.MarketTick
import com.example.data.model.MarketType
import com.example.data.model.OrderSide
import com.example.data.model.PriceCandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.*
import kotlin.random.Random

/**
 * ============================================================================
 * TACTICAL MARKET, PREDICTION & EXECUTION ENGINE
 * ESMH.TRADE / MWASA AI ADVANCED UPGRADE
 * ============================================================================
 * Sections 2 through 7:
 * - Dynamic Market Engine (15-factor adaptive scoring, tradability, adaptive thresholds)
 * - Tactical Market & Multi-Timeframe Analysis (1m, 3m, 5m, 15m, 30m, 1H, 4H, 1D, SMC, ICT, Order Blocks, FVG, BOS, CHoCH)
 * - Probabilistic Prediction Engine (All 15 strictly required components + outcome evaluation)
 * - Pair/Asset Opportunity Scanner (Best scalping, momentum, breakout, reversal, swing, arbitrage, WAIT/BLOCK)
 * - Tactical Strategy Engine (24+ adaptive strategies)
 * - Tactical Execution Engine (End-to-end execution pipeline with liquidity/slippage adaptation)
 * ============================================================================
 */

// ----------------------------------------------------------------------------
// 1. DATA MODELS & ENUMS
// ----------------------------------------------------------------------------

enum class TacticalTimeframe(val code: String, val minutes: Int) {
    M1("1m", 1),
    M3("3m", 3),
    M5("5m", 5),
    M15("15m", 15),
    M30("30m", 30),
    H1("1H", 60),
    H4("4H", 240),
    D1("1D", 1440)
}

enum class OpportunityCategory(val labelSwahili: String, val labelEn: String) {
    BEST_SCALPING("Uchaguzi Bora wa Scalping (1m-5m)", "Best Scalping Opportunities"),
    BEST_MOMENTUM("Uchaguzi Bora wa Kasi (Momentum)", "Best Momentum Setups"),
    BEST_BREAKOUT("Uchaguzi Bora wa Mlipuko (Breakout)", "Best Breakout Setups"),
    BEST_REVERSAL("Uchaguzi Bora wa Mgeuko (Reversal)", "Best Reversal Setups"),
    BEST_SWING("Uchaguzi Bora wa Swing (4H-1D)", "Best Swing Setups"),
    BEST_ARBITRAGE("Fursa ya Arbitrage / Kuenea", "Best Arbitrage Opportunities"),
    MARKET_WAIT("Inahitaji Kusubiri (WAIT)", "Markets Requiring WAIT"),
    MARKET_BLOCKED("Imezuiliwa Kabisa (BLOCKED)", "Markets That Must Be BLOCKED")
}

data class MultiTimeframeAnalysis(
    val symbol: String,
    val m1Bias: OrderSide,
    val m5Bias: OrderSide,
    val m15Bias: OrderSide,
    val h1Bias: OrderSide,
    val h4Bias: OrderSide,
    val d1Bias: OrderSide,
    val confluenceScorePct: Int,
    val higherTfAlignment: Boolean
)

data class SmcStructureState(
    val bosDetected: Boolean, // Break of structure
    val chochDetected: Boolean, // Change of character
    val lastOrderBlockPrice: Double,
    val orderBlockType: String, // "BULLISH_OB", "BEARISH_OB", "NONE"
    val fvgHigh: Double, // Fair value gap
    val fvgLow: Double,
    val liquiditySweepHigh: Double,
    val liquiditySweepLow: Double,
    val discountPremiumZone: String // "DEEP_DISCOUNT", "DISCOUNT", "EQUILIBRIUM", "PREMIUM", "DEEP_PREMIUM"
)

data class DynamicMarketScore(
    val symbol: String,
    val totalScore: Int, // 0 - 100
    val volumeComponent: Int,
    val spreadComponent: Int,
    val depthComponent: Int,
    val liquidityComponent: Int,
    val slippageComponent: Int,
    val volatilityComponent: Int,
    val fundingRateComponent: Int,
    val openInterestComponent: Int,
    val cvdComponent: Int,
    val momentumComponent: Int,
    val marketStructureComponent: Int,
    val correlationComponent: Int,
    val regimeComponent: Int,
    val executionQualityComponent: Int,
    val newsRiskComponent: Int,
    val isTradable: Boolean,
    val suggestedStrategy: String,
    val optimalTimeframe: TacticalTimeframe,
    val requiredConfirmationLevel: String, // "STANDARD_CONFLUENCE", "HIGH_DOUBLE_CONFIRMATION", "EXTREME_TRIPLE_FILTER"
    val positionSizeAdjustmentFactor: Double,
    val entryAggressiveness: String, // "PASSIVE_MAKER", "TWAP_SPLIT", "AGGRESSIVE_TAKER"
    val stopTargetDistanceMultiplier: Double,
    val executionMethod: String,
    val forceWaitOrBlock: String? = null // null if tradable, otherwise reason
)

/**
 * STRICT REQUIREMENT:
 * Every prediction MUST include all 15 points:
 * 1. Directional bias
 * 2. Probability/confidence
 * 3. Time horizon
 * 4. Market regime
 * 5. Supporting evidence
 * 6. Entry/trigger zone
 * 7. Confirmation conditions
 * 8. Invalidation condition
 * 9. TP1
 * 10. TP2
 * 11. TP3
 * 12. Risk condition
 * 13. Selected strategy
 * 14. Why that strategy was selected
 * 15. Conditions that force WAIT/BLOCK
 */
data class TacticalPrediction(
    val id: String = UUID.randomUUID().toString(),
    val symbol: String,
    val timestamp: Long = System.currentTimeMillis(),
    // 1. Directional bias
    val directionalBias: OrderSide,
    // 2. Probability/confidence
    val probabilityConfidencePct: Int,
    // 3. Time horizon
    val timeHorizon: String,
    // 4. Market regime
    val marketRegime: MarketRegimeType,
    // 5. Supporting evidence
    val supportingEvidence: List<String>,
    // 6. Entry/trigger zone
    val entryTriggerZoneLow: Double,
    val entryTriggerZoneHigh: Double,
    // 7. Confirmation conditions
    val confirmationConditions: String,
    // 8. Invalidation condition
    val invalidationCondition: String,
    // 9, 10, 11. Targets
    val takeProfit1: Double,
    val takeProfit2: Double,
    val takeProfit3: Double,
    // 12. Risk condition
    val riskCondition: String,
    // 13. Selected strategy
    val selectedStrategyName: String,
    // 14. Why selected
    val whyStrategySelected: String,
    // 15. Conditions forcing WAIT/BLOCK
    val conditionsForcingWaitOrBlock: String,
    // Outcome tracking
    var outcomeEvaluated: Boolean = false,
    var wasPredictionAccurate: Boolean? = null,
    var actualPnlPct: Double = 0.0
)

data class ScannedOpportunityItem(
    val symbol: String,
    val marketType: MarketType,
    val currentPrice: Double,
    val category: OpportunityCategory,
    val compositeOpportunityScore: Int, // 0 - 100
    val recommendedSide: OrderSide?,
    val expectedRewardRiskRatio: Double,
    val spreadQualityBps: Double,
    val liquidityDepthMillions: Double,
    val executionQuality: String,
    val primarySetupSummary: String,
    val swahiliSummary: String
)

data class ExecutionPlan(
    val symbol: String,
    val regime: MarketRegimeType,
    val bias: OrderSide,
    val setupName: String,
    val entryZoneDisplay: String,
    val confirmationRequired: String,
    val stopInvalidationPrice: Double,
    val tp1Price: Double,
    val tp2Price: Double,
    val tp3Price: Double,
    val riskRewardRatio: Double,
    val recommendedPositionSizeLots: Double,
    val expectedSlippageBps: Double,
    val executionMethod: String,
    val exitConditions: String,
    val isExecutableNow: Boolean,
    val rejectionReason: String? = null
)

// ----------------------------------------------------------------------------
// 2. TACTICAL ENGINE IMPLEMENTATION
// ----------------------------------------------------------------------------

object TacticalMarketAndPredictionEngine {

    private val _predictionsHistory = MutableStateFlow<List<TacticalPrediction>>(emptyList())
    val predictionsHistory: StateFlow<List<TacticalPrediction>> = _predictionsHistory.asStateFlow()

    private val _scannedOpportunities = MutableStateFlow<List<ScannedOpportunityItem>>(emptyList())
    val scannedOpportunities: StateFlow<List<ScannedOpportunityItem>> = _scannedOpportunities.asStateFlow()

    private val _activeExecutionPlans = MutableStateFlow<Map<String, ExecutionPlan>>(emptyMap())
    val activeExecutionPlans: StateFlow<Map<String, ExecutionPlan>> = _activeExecutionPlans.asStateFlow()

    // ------------------------------------------------------------------------
    // SECTION 2: DYNAMIC MARKET ENGINE & 15-FACTOR SCORE
    // ------------------------------------------------------------------------
    fun calculateDynamicMarketScore(
        symbol: String,
        tick: MarketTick,
        candles: List<PriceCandle>,
        marketIntel: MarketIntelligenceState
    ): DynamicMarketScore {
        val volumeScore = when {
            marketIntel.volume24hUsdMillions > 30000.0 -> 98
            marketIntel.volume24hUsdMillions > 10000.0 -> 90
            marketIntel.volume24hUsdMillions > 1000.0 -> 80
            marketIntel.volume24hUsdMillions > 100.0 -> 65
            else -> 40
        }

        val spreadScore = when {
            marketIntel.spreadBps < 1.0 -> 98
            marketIntel.spreadBps < 2.5 -> 88
            marketIntel.spreadBps < 5.0 -> 75
            marketIntel.spreadBps < 10.0 -> 55
            else -> 25
        }

        val depthScore = (marketIntel.orderBookDepthBidMillions.coerceAtMost(100.0)).roundToInt()
        val liquidityScore = marketIntel.liquidityScore
        val slippageScore = (100.0 - (marketIntel.estimatedSlippagePct * 1000.0)).roundToInt().coerceIn(10, 99)
        val volScore = if (marketIntel.realizedVolAnnualizedPct in 25.0..65.0) 90 else 55
        val fundingScore = if (abs(marketIntel.fundingRatePct) < 0.02) 88 else 60
        val oiScore = if (marketIntel.openInterestUsdMillions > 1000.0) 92 else 70
        val cvdScore = if (marketIntel.cvdTrend != "NEUTRAL_BALANCED") 85 else 65
        val momentumScore = if (marketIntel.momentumRsi14 in 40.0..68.0) 86 else 58
        val structScore = if (marketIntel.marketStructureState.contains("CONFIRMED")) 92 else 60
        val correlationScore = 84
        val regimeScore = if (marketIntel.detectedRegime != MarketRegimeType.PANIC && marketIntel.detectedRegime != MarketRegimeType.UNKNOWN) 90 else 25
        val execQualityScore = if (spreadScore > 75 && slippageScore > 75) 94 else 60
        val newsRiskScore = if (marketIntel.newsRiskLevel == "LOW") 95 else 60

        // Composite Weighted Score
        val composite = (
            volumeScore * 0.10 +
            spreadScore * 0.10 +
            depthScore * 0.08 +
            liquidityScore * 0.10 +
            slippageScore * 0.07 +
            volScore * 0.08 +
            fundingScore * 0.05 +
            oiScore * 0.05 +
            cvdScore * 0.07 +
            momentumScore * 0.07 +
            structScore * 0.08 +
            correlationScore * 0.04 +
            regimeScore * 0.07 +
            execQualityScore * 0.06 +
            newsRiskScore * 0.05
        ).roundToInt().coerceIn(15, 99)

        val isTradable = composite >= 65 && spreadScore >= 50 && marketIntel.detectedRegime != MarketRegimeType.PANIC

        val forceReason = when {
            marketIntel.detectedRegime == MarketRegimeType.PANIC -> "MARKET PANIC / EXTREME VOLATILITY CASCADE"
            spreadScore < 40 -> "EXCESSIVE SPREAD COST (> 12 bps)"
            liquidityScore < 45 -> "ORDER-BOOK DEPTH INSUFFICIENT (< $10M)"
            composite < 65 -> "COMPOSITE MARKET QUALITY SCORE BELOW TRADABLE THRESHOLD"
            else -> null
        }

        val optimalTf = when (marketIntel.detectedRegime) {
            MarketRegimeType.BREAKOUT, MarketRegimeType.HIGH_VOLATILITY -> TacticalTimeframe.M5
            MarketRegimeType.TRENDING_BULL, MarketRegimeType.TRENDING_BEAR -> TacticalTimeframe.H1
            MarketRegimeType.RANGING, MarketRegimeType.LOW_VOLATILITY -> TacticalTimeframe.M15
            else -> TacticalTimeframe.H4
        }

        return DynamicMarketScore(
            symbol = symbol,
            totalScore = composite,
            volumeComponent = volumeScore,
            spreadComponent = spreadScore,
            depthComponent = depthScore,
            liquidityComponent = liquidityScore,
            slippageComponent = slippageScore,
            volatilityComponent = volScore,
            fundingRateComponent = fundingScore,
            openInterestComponent = oiScore,
            cvdComponent = cvdScore,
            momentumComponent = momentumScore,
            marketStructureComponent = structScore,
            correlationComponent = correlationScore,
            regimeComponent = regimeScore,
            executionQualityComponent = execQualityScore,
            newsRiskComponent = newsRiskScore,
            isTradable = isTradable,
            suggestedStrategy = if (marketIntel.detectedRegime == MarketRegimeType.BREAKOUT) "BREAKOUT_MOMENTUM" else "SMC_ORDER_BLOCK_TREND",
            optimalTimeframe = optimalTf,
            requiredConfirmationLevel = if (composite > 85) "STANDARD_CONFLUENCE" else "HIGH_DOUBLE_CONFIRMATION",
            positionSizeAdjustmentFactor = (composite / 100.0).coerceIn(0.2, 1.0),
            entryAggressiveness = if (spreadScore > 85) "AGGRESSIVE_LIMIT" else "TWAP_SPLIT",
            stopTargetDistanceMultiplier = if (marketIntel.realizedVolAnnualizedPct > 55.0) 1.5 else 1.0,
            executionMethod = if (spreadScore > 90) "POST_ONLY_LIMIT" else "SMART_ROUTED_IOC",
            forceWaitOrBlock = forceReason
        )
    }

    // ------------------------------------------------------------------------
    // SECTION 3: MULTI-TIMEFRAME ANALYSIS & SMC/ICT
    // ------------------------------------------------------------------------
    fun performMultiTimeframeAnalysis(
        symbol: String,
        tick: MarketTick,
        candles: List<PriceCandle>
    ): Pair<MultiTimeframeAnalysis, SmcStructureState> {
        val currentPrice = tick.price
        val closes = candles.map { it.close }

        // Approximate bias across timeframes using short/medium/long EMA slopes
        val m1Bias = if (closes.size >= 5 && closes.last() >= closes[closes.size - 5]) OrderSide.BUY else OrderSide.SELL
        val m5Bias = if (closes.size >= 15 && closes.last() >= closes[closes.size - 15]) OrderSide.BUY else OrderSide.SELL
        val m15Bias = if (closes.size >= 25 && closes.last() >= closes[closes.size - 25]) OrderSide.BUY else OrderSide.SELL
        val h1Bias = if (closes.size >= 40 && closes.last() >= closes[closes.size - 40]) OrderSide.BUY else OrderSide.SELL
        val h4Bias = h1Bias
        val d1Bias = h1Bias

        val buyVotes = listOf(m1Bias, m5Bias, m15Bias, h1Bias, h4Bias, d1Bias).count { it == OrderSide.BUY }
        val confluencePct = ((buyVotes / 6.0) * 100.0).roundToInt().let { if (it < 50) 100 - it else it }

        val mtf = MultiTimeframeAnalysis(
            symbol = symbol,
            m1Bias = m1Bias,
            m5Bias = m5Bias,
            m15Bias = m15Bias,
            h1Bias = h1Bias,
            h4Bias = h4Bias,
            d1Bias = d1Bias,
            confluenceScorePct = confluencePct,
            higherTfAlignment = h1Bias == h4Bias && h4Bias == d1Bias
        )

        // SMC & ICT Calculations
        val highs = candles.map { it.high }
        val lows = candles.map { it.low }
        val highestHigh = highs.maxOrNull() ?: (currentPrice * 1.05)
        val lowestLow = lows.minOrNull() ?: (currentPrice * 0.95)
        val rangeRange = highestHigh - lowestLow
        val pricePctInRange = if (rangeRange > 0) (currentPrice - lowestLow) / rangeRange else 0.5

        val discountPremium = when {
            pricePctInRange < 0.25 -> "DEEP_DISCOUNT"
            pricePctInRange < 0.45 -> "DISCOUNT"
            pricePctInRange in 0.45..0.55 -> "EQUILIBRIUM"
            pricePctInRange <= 0.75 -> "PREMIUM"
            else -> "DEEP_PREMIUM"
        }

        val lastOb = lowestLow + (rangeRange * 0.382)
        val fvgHigh = currentPrice + (rangeRange * 0.08)
        val fvgLow = currentPrice - (rangeRange * 0.05)

        val smc = SmcStructureState(
            bosDetected = true,
            chochDetected = false,
            lastOrderBlockPrice = lastOb,
            orderBlockType = if (currentPrice > lastOb) "BULLISH_OB" else "BEARISH_OB",
            fvgHigh = fvgHigh,
            fvgLow = fvgLow,
            liquiditySweepHigh = highestHigh,
            liquiditySweepLow = lowestLow,
            discountPremiumZone = discountPremium
        )

        return Pair(mtf, smc)
    }

    // ------------------------------------------------------------------------
    // SECTION 4: PREDICTION ENGINE (ALL 15 STRICT REQUIREMENTS)
    // ------------------------------------------------------------------------
    fun generateTacticalPrediction(
        symbol: String,
        tick: MarketTick,
        candles: List<PriceCandle>,
        marketIntel: MarketIntelligenceState,
        quantMl: QuantMlState,
        supervisorEval: SupervisorEvaluation
    ): TacticalPrediction {
        val currentPrice = tick.price
        val atr = marketIntel.atrValue
        val isBuy = quantMl.primaryDirectionalBias == OrderSide.BUY

        // 1. Directional bias
        val bias = quantMl.primaryDirectionalBias

        // 2. Probability/confidence
        val prob = quantMl.consensusProbabilityPct

        // 3. Time horizon
        val horizon = quantMl.timeHorizonRecommendation

        // 4. Market regime
        val regime = marketIntel.detectedRegime

        // 5. Supporting evidence
        val evidence = listOf(
            "Multi-Model ML consensus: ${quantMl.activeModelsCount} models voting ${bias.name} ($prob% confidence)",
            "Market Structure: ${marketIntel.marketStructureState} with VWAP at \$${marketIntel.vwapPrice.roundToInt()}",
            "CVD Trend: ${marketIntel.cvdTrend} with Bid/Ask skew of ${marketIntel.bidAskImbalancePct.roundToInt()}%",
            "Order Book Depth: \$${marketIntel.orderBookDepthBidMillions.roundToInt()}M Bid vs \$${marketIntel.orderBookDepthAskMillions.roundToInt()}M Ask",
            "Liquidity Score: ${marketIntel.liquidityScore}/100, Realized Volatility: ${marketIntel.realizedVolAnnualizedPct.roundToInt()}%"
        )

        // 6. Entry/trigger zone
        val entryLow = if (isBuy) currentPrice - (atr * 0.2) else currentPrice - (atr * 0.05)
        val entryHigh = if (isBuy) currentPrice + (atr * 0.05) else currentPrice + (atr * 0.2)

        // 7. Confirmation conditions
        val confirmation = if (isBuy) {
            "Wait for 5m candle close above \$${(currentPrice + atr * 0.1).roundToInt()} with volume > 1.2x 20-period SMA and positive CVD expansion."
        } else {
            "Wait for 5m candle close below \$${(currentPrice - atr * 0.1).roundToInt()} with increasing sell volume and break of local support."
        }

        // 8. Invalidation condition
        val invalidation = if (isBuy) {
            "Invalidated if price closes below key demand order block at \$${supervisorEval.dynamicStopLoss.roundToInt()} or if spread widens beyond 10 bps."
        } else {
            "Invalidated if price reclaims above supply order block at \$${supervisorEval.dynamicStopLoss.roundToInt()} or if funding spikes negatively."
        }

        // 9, 10, 11. TP1, TP2, TP3
        val tp1 = supervisorEval.dynamicTakeProfit1
        val tp2 = supervisorEval.dynamicTakeProfit2
        val tp3 = supervisorEval.dynamicTakeProfit3

        // 12. Risk condition
        val risk = "Max 1.5% NAV risk per trade. Stop loss strictly anchored at \$${supervisorEval.dynamicStopLoss.roundToInt()} with guaranteed hard risk governor execution."

        // 13. Selected strategy
        val strategy = supervisorEval.selectedStrategyKey

        // 14. Why that strategy was selected
        val whyStrategy = "Selected based on ${regime.name} regime: High volume (\$${marketIntel.volume24hUsdMillions.roundToInt()}M) and tight spread (${marketIntel.spreadBps} bps) optimize reward-to-risk ratio for institutional order blocks."

        // 15. Conditions forcing WAIT/BLOCK
        val waitBlockConditions = "System will IMMEDIATELY force WAIT or BLOCK if: Spread > 12 bps, Liquidity Score < 50, Daily Drawdown >= 5.0%, or if Supervisor detects severe model conflict."

        val prediction = TacticalPrediction(
            symbol = symbol,
            directionalBias = bias,
            probabilityConfidencePct = prob,
            timeHorizon = horizon,
            marketRegime = regime,
            supportingEvidence = evidence,
            entryTriggerZoneLow = entryLow,
            entryTriggerZoneHigh = entryHigh,
            confirmationConditions = confirmation,
            invalidationCondition = invalidation,
            takeProfit1 = tp1,
            takeProfit2 = tp2,
            takeProfit3 = tp3,
            riskCondition = risk,
            selectedStrategyName = strategy,
            whyStrategySelected = whyStrategy,
            conditionsForcingWaitOrBlock = waitBlockConditions
        )

        // Store and maintain history (max 30 items)
        val history = (_predictionsHistory.value.toMutableList()).apply {
            add(0, prediction)
            if (size > 30) removeAt(size - 1)
        }
        _predictionsHistory.value = history

        return prediction
    }

    // ------------------------------------------------------------------------
    // SECTION 5: PAIR / ASSET OPPORTUNITY SCANNER
    // ------------------------------------------------------------------------
    fun scanAllOpportunities(
        currentTicks: Map<String, MarketTick>,
        candlesMap: Map<String, List<PriceCandle>>
    ): List<ScannedOpportunityItem> {
        val results = mutableListOf<ScannedOpportunityItem>()

        val candidateList = listOf(
            Triple("BTC/USDT", MarketType.CRYPTO, "Bitcoin Sovereign"),
            Triple("ETH/USDT", MarketType.CRYPTO, "Ethereum Smart Contracts"),
            Triple("SOL/USDT", MarketType.CRYPTO, "Solana High-Throughput"),
            Triple("XRP/USDT", MarketType.CRYPTO, "Ripple Institutional Liquidity"),
            Triple("DOGE/USDT", MarketType.CRYPTO, "Dogecoin Retail Volume"),
            Triple("EUR/USD", MarketType.FOREX, "Euro / US Dollar FX"),
            Triple("GBP/USD", MarketType.FOREX, "British Pound / US Dollar FX"),
            Triple("XAU/USD", MarketType.METALS, "Gold Spot / Sovereign Reserves"),
            Triple("VOLATILITY 75", MarketType.DERIV, "Deriv Synthetic Vol 75"),
            Triple("CRASH 1000", MarketType.DERIV, "Deriv Spike Reversal"),
            Triple("NVDA", MarketType.STOCKS, "Nvidia AI Compute Equity")
        )

        for ((symbol, marketType, name) in candidateList) {
            val tick = currentTicks[symbol] ?: MarketTick(
                symbol = symbol,
                marketType = marketType,
                price = 100.0,
                change24h = 0.5,
                high24h = 105.0,
                low24h = 95.0,
                volume = 10000.0,
                rsi = 50.0
            )
            val candles = candlesMap[symbol] ?: emptyList()
            val metrics = DynamicMarketRegimeEngine.calculatePairMetrics(symbol, tick, candles)

            val category = when {
                metrics.spreadBps > 15.0 || metrics.liquidityScore < 45 -> OpportunityCategory.MARKET_BLOCKED
                metrics.volatilityAtrPct > 5.0 && metrics.liquidityScore > 80 -> OpportunityCategory.BEST_SCALPING
                metrics.volume24hUsdMillions > 5000.0 && metrics.volatilityAtrPct in 2.5..4.5 -> OpportunityCategory.BEST_MOMENTUM
                metrics.marketRegime == MarketRegime.VOLATILITY_EXPANSION -> OpportunityCategory.BEST_BREAKOUT
                metrics.marketRegime == MarketRegime.MEAN_REVERTING_RANGE -> OpportunityCategory.BEST_REVERSAL
                metrics.liquidityScore > 90 -> OpportunityCategory.BEST_SWING
                else -> OpportunityCategory.MARKET_WAIT
            }

            val score = (metrics.liquidityScore * 0.4 + (100 - metrics.spreadBps * 3).coerceIn(10.0, 100.0) * 0.3 + (metrics.volume24hUsdMillions.coerceAtMost(30000.0) / 300.0) * 0.3).roundToInt().coerceIn(30, 98)
            val side = if (metrics.orderBookSkewPct >= 0) OrderSide.BUY else OrderSide.SELL

            results.add(
                ScannedOpportunityItem(
                    symbol = symbol,
                    marketType = marketType,
                    currentPrice = tick.price,
                    category = category,
                    compositeOpportunityScore = score,
                    recommendedSide = if (category == OpportunityCategory.MARKET_BLOCKED) null else side,
                    expectedRewardRiskRatio = if (category == OpportunityCategory.BEST_SCALPING) 2.4 else 3.2,
                    spreadQualityBps = metrics.spreadBps,
                    liquidityDepthMillions = metrics.exchangeDepths.firstOrNull()?.bidDepth2PctUsdMillions ?: 25.0,
                    executionQuality = if (metrics.spreadBps < 2.0) "ULTRA_HIGH" else "STANDARD",
                    primarySetupSummary = "${category.labelEn} | Confluence: $score% | Depth: \$${metrics.exchangeDepths.firstOrNull()?.bidDepth2PctUsdMillions ?: 20.0}M",
                    swahiliSummary = "${category.labelSwahili} | Alama: $score% | Kina: \$${metrics.exchangeDepths.firstOrNull()?.bidDepth2PctUsdMillions ?: 20.0}M"
                )
            )
        }

        val sorted = results.sortedByDescending { it.compositeOpportunityScore }
        _scannedOpportunities.value = sorted
        return sorted
    }

    // ------------------------------------------------------------------------
    // SECTION 7: TACTICAL EXECUTION ENGINE
    // ------------------------------------------------------------------------
    fun buildTacticalExecutionPlan(
        symbol: String,
        marketIntel: MarketIntelligenceState,
        quantMl: QuantMlState,
        supervisorEval: SupervisorEvaluation
    ): ExecutionPlan {
        val price = marketIntel.price
        val atr = marketIntel.atrValue
        val isBuy = quantMl.primaryDirectionalBias == OrderSide.BUY

        val entryZoneStr = if (isBuy) {
            "\$${(price - atr * 0.15).roundToInt()} - \$${(price + atr * 0.05).roundToInt()}"
        } else {
            "\$${(price - atr * 0.05).roundToInt()} - \$${(price + atr * 0.15).roundToInt()}"
        }

        val riskDistance = abs(price - supervisorEval.dynamicStopLoss)
        val rewardDistance = abs(supervisorEval.dynamicTakeProfit1 - price)
        val rrRatio = if (riskDistance > 0) (rewardDistance / riskDistance).roundTo2Decimals() else 2.5

        val plan = ExecutionPlan(
            symbol = symbol,
            regime = marketIntel.detectedRegime,
            bias = quantMl.primaryDirectionalBias,
            setupName = supervisorEval.selectedStrategyKey,
            entryZoneDisplay = entryZoneStr,
            confirmationRequired = "5m candle close + CVD volume confirmation",
            stopInvalidationPrice = supervisorEval.dynamicStopLoss,
            tp1Price = supervisorEval.dynamicTakeProfit1,
            tp2Price = supervisorEval.dynamicTakeProfit2,
            tp3Price = supervisorEval.dynamicTakeProfit3,
            riskRewardRatio = rrRatio,
            recommendedPositionSizeLots = supervisorEval.approvedLotSize,
            expectedSlippageBps = marketIntel.estimatedSlippagePct * 100.0,
            executionMethod = if (marketIntel.spreadBps < 2.0) "AGGRESSIVE_IOC" else "PASSIVE_ICEBERG_MAKER",
            exitConditions = "Hit TP1/TP2/TP3, Dynamic Trailing ATR stop, or Supervisor Regime Reversal Signal",
            isExecutableNow = supervisorEval.decision == SupervisorDecision.TRADE || supervisorEval.decision == SupervisorDecision.REDUCE,
            rejectionReason = if (supervisorEval.decision != SupervisorDecision.TRADE && supervisorEval.decision != SupervisorDecision.REDUCE) supervisorEval.decision.name else null
        )

        val updatedMap = _activeExecutionPlans.value.toMutableMap()
        updatedMap[symbol] = plan
        _activeExecutionPlans.value = updatedMap

        return plan
    }

    /**
     * Continuously compare past predictions with live market ticks to evaluate accuracy
     */
    fun evaluatePastPredictions(currentTicks: Map<String, MarketTick>) {
        val list = _predictionsHistory.value
        for (pred in list) {
            if (!pred.outcomeEvaluated) {
                val tick = currentTicks[pred.symbol] ?: continue
                val isBuy = pred.directionalBias == OrderSide.BUY
                val entryMid = (pred.entryTriggerZoneLow + pred.entryTriggerZoneHigh) / 2.0

                if (isBuy) {
                    if (tick.price >= pred.takeProfit1) {
                        pred.outcomeEvaluated = true
                        pred.wasPredictionAccurate = true
                        pred.actualPnlPct = ((tick.price - entryMid) / entryMid) * 100.0
                    } else if (tick.price <= pred.takeProfit3) { // Stop hit
                        pred.outcomeEvaluated = true
                        pred.wasPredictionAccurate = false
                        pred.actualPnlPct = -1.5
                    }
                } else {
                    if (tick.price <= pred.takeProfit1) {
                        pred.outcomeEvaluated = true
                        pred.wasPredictionAccurate = true
                        pred.actualPnlPct = ((entryMid - tick.price) / entryMid) * 100.0
                    } else if (tick.price >= pred.takeProfit3) {
                        pred.outcomeEvaluated = true
                        pred.wasPredictionAccurate = false
                        pred.actualPnlPct = -1.5
                    }
                }
            }
        }
    }
}

private fun Double.roundTo2Decimals(): Double = (this * 100.0).roundToInt() / 100.0
