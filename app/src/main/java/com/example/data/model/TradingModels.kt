package com.example.data.model

enum class ExecutionInstrumentMode {
    SPOT,
    FUTURE,
    AUTO
}

enum class BotCapitalTier(val labelSwahili: String, val labelEn: String, val minCap: Double, val maxCap: Double) {
    MICRO_CENT_TIER("Senti & Ndogo ($1 - $5)", "Micro & Cent Tier ($1 - $5)", 1.0, 5.0),
    RETAIL_MEDIUM_TIER("Standard ($5 - $15)", "Standard Retail ($5 - $15)", 5.0, 15.0),
    LEVEL_0_TO_1K("Level 0 - 1K ($0 - $1,000)", "Level 0 to 1K ($0 - $1,000)", 0.0, 1000.0),
    LEVEL_1K_TO_10K("Level 1K - 10K ($1,000 - $10,000)", "Level 1K to 10K ($1,000 - $10,000)", 1000.0, 10000.0),
    LEVEL_10K_TO_100K("Level 10K - 100K ($10,000 - $100,000)", "Level 10K to 100K ($10,000 - $100,000)", 10000.0, 100000.0),
    ADVANCED_INSTITUTIONAL_TIER("Taasisi / Kubwa ($15+)", "Advanced Institutional ($15+)", 15.0, 100000.0)
}

data class BotNode(
    val id: Int,
    val name: String,
    val strategy: String,
    val marketType: MarketType,
    val targetPair: String,
    val allocatedCapital: Double,
    val currentPnl: Double,
    val winRate: Double,
    val totalTrades: Int,
    val isActive: Boolean,
    val periodHorizon: TradePeriodHorizon = TradePeriodHorizon.INTRADAY_SWING_15M_1H,
    val profitAmbition: ProfitAmbitionLevel = ProfitAmbitionLevel.AGGRESSIVE_COMPOUND,
    val trailingTpSlEnabled: Boolean = true,
    val trailingStepPct: Double = 0.8,
    val maxDrawdownPct: Double = 5.0,
    val alphaBoostActive: Boolean = false,
    val microFluctuationScalp: Boolean = true,
    val minCapitalUsd: Double = 1.0,
    val capabilityBadge: String = "Mtaji: $1+",
    val detailedDescriptionSwahili: String = "Boti ya kiasi inayofanya biashara kwa kufuata kanuni za ulinzi wa mtaji na kuzuia hasara.",
    val detailedDescriptionEn: String = "Quantitative algorithm with automated capital protection and loss-prevention gates.",
    val recommendedLeverage: String = "1:20",
    val microLotSupported: Boolean = true,
    val capitalTier: BotCapitalTier = BotCapitalTier.RETAIL_MEDIUM_TIER,
    val targetProfitPct: Double = 5.0,
    val targetProfitUsd: Double = 0.0
)

data class FleetDeploymentSummary(
    val deployedBotsCount: Int,
    val totalAllocatedCapital: Double,
    val targetProfitUsd: Double,
    val targetProfitPct: Double,
    val activeBotNames: List<String>,
    val explanationSwahili: String,
    val explanationEn: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Opportunity(
    val id: String,
    val symbol: String,
    val marketType: MarketType,
    val expectedReturnPct: Double,
    val winProbability: Double,
    val riskRewardRatio: Double,
    val direction: OrderSide,
    val strategyName: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ActiveTrade(
    val id: String,
    val symbol: String,
    val marketType: MarketType,
    val side: OrderSide,
    val entryPrice: Double,
    val currentPrice: Double,
    val lotSize: Double,
    val unrealizedPnl: Double,
    val takeProfit: Double,
    val stopLoss: Double,
    val openTime: Long = System.currentTimeMillis()
)

data class ConnectorCard(
    val id: String,
    val name: String,
    val marketCoverage: String,
    val isConnected: Boolean,
    val pingMs: Int,
    val status: String,
    val credentialsConfigured: Boolean = true
)

data class MarketScheduleInfo(
    val marketType: MarketType,
    val status: String,
    val isOpen: Boolean,
    val nextEventTime: String,
    val sessionName: String
)

data class NewsEventItem(
    val id: String,
    val headline: String,
    val market: String,
    val impact: String, // HIGH, MEDIUM, LOW
    val timeAgo: String
)

data class BotAlertItem(
    val id: String,
    val botId: Int,
    val botName: String,
    val message: String,
    val severity: String, // INFO, WARNING, CRITICAL
    val timestamp: Long = System.currentTimeMillis()
)

data class PriceCandle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class MarketTick(
    val symbol: String,
    val marketType: MarketType,
    val price: Double,
    val change24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume: Double,
    val rsi: Double
)
