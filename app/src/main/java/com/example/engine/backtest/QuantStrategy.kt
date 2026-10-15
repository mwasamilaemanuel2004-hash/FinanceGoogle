package com.example.engine.backtest

/**
 * ============================================================================
 * QUANT STRATEGY DEFINITIONS & BACKTEST DOMAIN MODELS
 * ============================================================================
 */

enum class StrategyType(
    val code: String,
    val nameEn: String,
    val nameSwahili: String,
    val primaryMarket: String,
    val recommendedTimeframe: String,
    val descriptionSwahili: String,
    val descriptionEn: String
) {
    SMC_ORDER_BLOCK(
        code = "SMC_ICT",
        nameEn = "SMC Institutional Order Block & FVG",
        nameSwahili = "SMC na Maeneo ya Taasisi (FVG & Order Block)",
        primaryMarket = "EUR/USD, BTC/USDT",
        recommendedTimeframe = "1h",
        descriptionSwahili = "Inagundua maeneo ya ukwasi wa taasisi (Smart Money Liquidity Sweep) na mapengo ya bei (Fair Value Gap) kwa usahihi mkubwa.",
        descriptionEn = "Detects institutional order blocks, liquidity sweeps, and fair value gaps for high-probability structural entries."
    ),
    BOLLINGER_MEAN_REVERSION(
        code = "MEAN_REV",
        nameEn = "Bollinger Squeeze & Mean Reversion",
        nameSwahili = "Minyweo ya Bollinger & Kurudi Katikati",
        primaryMarket = "USD/JPY, ETH/USDT",
        recommendedTimeframe = "15m",
        descriptionSwahili = "Hutumia upanuzi wa mikanda ya Bollinger na mkengeuko wa bei ili kuingia pale bei inaporudi kwenye wastani wake wa kweli.",
        descriptionEn = "Exploits Bollinger Band extremes and statistical standard deviation to catch mean-reversion swings back to baseline."
    ),
    EMA_TREND_FOLLOWING(
        code = "EMA_TREND",
        nameEn = "Multi-Timeframe EMA Trend Following + ADX",
        nameSwahili = "Kufuata Mwelekeo wa EMA & Kichujio cha ADX",
        primaryMarket = "BTC/USDT, XAU/USD",
        recommendedTimeframe = "1h",
        descriptionSwahili = "Huvuka mikondo ya EMA (21/55) na kutumia kichujio thabiti cha ADX (>25) ili kuthibitisha uwepo wa msukumo mkali wa soko kabla ya kufungua biashara.",
        descriptionEn = "Dual EMA crossover system filtered by ADX (>25) strength to capture strong macro trend impulses."
    ),
    MOMENTUM_BREAKOUT(
        code = "MOMENTUM_BO",
        nameEn = "Volume Surge & Momentum Breakout",
        nameSwahili = "Mlipuko wa Kasi & Ongezeko la Volume",
        primaryMarket = "SOL/USDT, NVDA",
        recommendedTimeframe = "1h",
        descriptionSwahili = "Hutambua mivunjiko ya viwango vya kilele (Highs/Lows) ikiambatana na ongezeko la ghafla la ujazo (Volume spike 2.5x).",
        descriptionEn = "Trades key resistance/support breaks confirmed by abnormal volume expansion (2.5x average)."
    ),
    VOLATILITY_SCALPING(
        code = "VOL_SCALP",
        nameEn = "Continuous Tick Volatility Scalper",
        nameSwahili = "Uwindaji wa Volatiliti ya Haraka (Scalper)",
        primaryMarket = "VOLATILITY 75",
        recommendedTimeframe = "15m",
        descriptionSwahili = "Hutumia mabadiliko ya haraka ya spikes na ATR kuingia na kutoka haraka sana kwa faida ndogo ndogo zilizolindwa.",
        descriptionEn = "Captures micro-spikes and quick volatility shifts using tight ATR stops and fast execution."
    ),
    MACRO_YIELD_DIFFERENTIAL(
        code = "MACRO_DIFF",
        nameEn = "Macro Sovereign Yield & Inflation Hedge",
        nameSwahili = "Mawimbi ya Uchumi & Ulinzi wa Dhahabu",
        primaryMarket = "XAU/USD, BRENT",
        recommendedTimeframe = "4h",
        descriptionSwahili = "Inachanganua uwiano wa riba halisi za Benki Kuu (Real Yields) na bei ya dhahabu na nishati duniani.",
        descriptionEn = "Trades real yield spreads and inflation trends across precious metals and energy commodities."
    ),
    CROSS_ARBITRAGE_SPREAD(
        code = "CROSS_ARB",
        nameEn = "Cross-Exchange Spread Arbitrage",
        nameSwahili = "Ulinganishi wa Bei & Arbitrage ya Masoko",
        primaryMarket = "BTC/ETH/USDT",
        recommendedTimeframe = "15m",
        descriptionSwahili = "Hutambua tofauti ndogo za bei baina ya masoko na kulinda mtaji kwa njia ya delta-neutral.",
        descriptionEn = "Identifies cross-venue and triangular price discrepancies for market-neutral returns."
    ),
    DYNAMIC_GRID_HEDGING(
        code = "GRID_HEDGE",
        nameEn = "Dynamic Grid Hedging & Loss Guard",
        nameSwahili = "Grid ya Kasi Yenye Ulinzi wa Hasara",
        primaryMarket = "EUR/USD, US500",
        recommendedTimeframe = "1h",
        descriptionSwahili = "Huunda mtandao wa maagizo ya kununua na kuuza yenye ulinzi wa circuit-breaker ili kuzuia hasara ya mteremko mkali.",
        descriptionEn = "Structured grid of rebalanced orders with hard circuit breakers to prevent runaway drawdown."
    ),
    GAM_M5_200_TRADES_SCALPER(
        code = "GAM_M5_200",
        nameEn = "GAM Mode: ~200 Trades M5 Scalper",
        nameSwahili = "Mode ya GAM (Takribani Trade 200 kwa Chati ya M5)",
        primaryMarket = "BTC/USDT, VOLATILITY 75, EUR/USD, XAU/USD (All 20 Bots)",
        recommendedTimeframe = "5m",
        descriptionSwahili = "Inafanya mzunguko wa takribani trade 200 kwenye chati ya M5 (dakika 5) kwa faida iliyochaguliwa kwa kila trade ($0.20 - $25) na ulinzi wa zero-liquidation.",
        descriptionEn = "High-velocity ~200 trades cycle on 5-minute bars with user-selected target profit per trade and micro-grid loss prevention."
    ),
    DERIV_MIND_SYNTHETICS(
        code = "DERIV_MIND",
        nameEn = "Deriv Mind: Deep Synthetics (1m, 5m)",
        nameSwahili = "Akili ya Deriv (Deriv Mind & Uchambuzi Imara 1m, 5m)",
        primaryMarket = "VOLATILITY 75, VOLATILITY 100, BOOM 1000, CRASH 1000, STEP INDEX",
        recommendedTimeframe = "1m",
        descriptionSwahili = "Uchambuzi imara na thabiti wa masoko ya synthetic indices ya Deriv kwenye vipindi vifupi vya 1m, 5m, 15m ukichunguza spike velocity na tick micro-clusters.",
        descriptionEn = "High-precision synthetics algorithmic intelligence analyzing 1-minute and 5-minute volatility micro-clusters, tick velocity, and spike predictions."
    )
}

/**
 * Loss Prevention & Guardrail Configuration
 * Directly prevents bad decisions and unnecessary drawdown.
 */
data class LossPreventionConfig(
    val enableDynamicAtrStopLoss: Boolean = true,
    val enableChoppyRegimeFilter: Boolean = true, // ADX < 20 blocks trade
    val enableCircuitBreaker: Boolean = true,    // 3 consecutive losses pause bot
    val minRiskRewardRatio: Double = 2.0,        // Minimum 1:2.0 TP to SL ratio
    val enableSpreadSlippageGate: Boolean = true, // Blocks during abnormal volatility/wick spikes
    val maxRiskPerTradePct: Double = 2.0         // Max 2% equity risk per position
)

/**
 * An individual backtest trade record
 */
data class BacktestTrade(
    val id: String,
    val symbol: String,
    val strategyName: String,
    val side: String, // "BUY" or "SELL"
    val entryTime: Long,
    val exitTime: Long,
    val entryPrice: Double,
    val exitPrice: Double,
    val quantity: Double,
    val pnlUsd: Double,
    val pnlPct: Double,
    val feeUsd: Double,
    val exitReason: String, // "TAKE_PROFIT", "STOP_LOSS", "CIRCUIT_BREAKER", "REGIME_EXIT"
    val isWin: Boolean,
    val maxRunupPct: Double = 0.0,
    val maxDrawdownPct: Double = 0.0
)

/**
 * Record of a dangerous/bad decision successfully intercepted and blocked by the system
 */
data class BadDecisionRecord(
    val id: String,
    val timestamp: Long,
    val symbol: String,
    val blockedSignal: String,
    val reasonSwahili: String,
    val reasonEn: String,
    val potentialLossSavedUsd: Double
)

/**
 * Comprehensive Backtest Result for a Strategy, Bot, or Full System
 */
data class BacktestResult(
    val title: String,
    val symbol: String,
    val timeframe: String,
    val initialCapitalUsd: Double,
    val finalEquityUsd: Double,
    val netProfitUsd: Double,
    val netProfitPct: Double,
    val winRatePct: Double,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val profitFactor: Double,
    val sharpeRatio: Double,
    val maxDrawdownPct: Double,
    val averageWinUsd: Double,
    val averageLossUsd: Double,
    val riskRewardAchieved: Double,
    val trades: List<BacktestTrade>,
    val badDecisionsPrevented: List<BadDecisionRecord>,
    val capitalSavedUsd: Double,
    val equityCurve: List<Double>,
    val lossPreventionActive: Boolean,
    val candleCount: Int = 0,
    val executionDurationMs: Long = 0L
)

/**
 * AI Agent Training Result with Before vs After Comparison
 */
data class AgentTrainingResult(
    val epochsCompleted: Int,
    val baselineWinRatePct: Double,
    val trainedWinRatePct: Double,
    val baselineDrawdownPct: Double,
    val trainedDrawdownPct: Double,
    val badDecisionsEliminatedCount: Int,
    val totalCapitalSavedUsd: Double,
    val confidenceScore: Double,
    val optimizationDate: Long = System.currentTimeMillis(),
    val statusMessageSwahili: String,
    val statusMessageEn: String,
    val isAppliedToMotherBrain: Boolean = false
)

data class MarketCategoryBacktestResult(
    val categoryName: String,
    val categoryNameSw: String,
    val primarySymbol: String,
    val timeframe: String,
    val backtestResult: BacktestResult,
    val modelTrainingAccuracyPct: Double,
    val cognitivePerceptionScore: Double,
    val statusText: String
)

data class MultiMarketSequentialReport(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val results: List<MarketCategoryBacktestResult>,
    val averageWinRatePct: Double,
    val totalNetProfitUsd: Double,
    val overallSharpeRatio: Double,
    val overallMaxDrawdownPct: Double,
    val cognitiveAdaptabilityScore: Double
)

/**
 * Real Out-Of-Sample Forward Test Result (Live/Simulated Out-of-Sample Forward Testing)
 * Evaluates performance on unseen recent forward market periods without lookahead bias.
 */
data class ForwardTestResult(
    val id: String = java.util.UUID.randomUUID().toString(),
    val strategyOrBotName: String,
    val symbol: String,
    val timeframe: String,
    val testPeriodDescription: String,
    val initialCapitalUsd: Double,
    val finalEquityUsd: Double,
    val netProfitUsd: Double,
    val netProfitPct: Double,
    val winRatePct: Double,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val profitFactor: Double,
    val maxDrawdownPct: Double,
    val forwardWalkScore: Double, // Robustness score comparing out-of-sample to in-sample
    val statusSummarySwahili: String,
    val statusSummaryEn: String,
    val isProfitVerified: Boolean = true,
    val completedAt: Long = System.currentTimeMillis()
)

