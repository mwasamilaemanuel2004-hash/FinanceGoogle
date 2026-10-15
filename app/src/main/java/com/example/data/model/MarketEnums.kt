package com.example.data.model

enum class MarketType(
    val displayName: String,
    val displayNameSwahili: String,
    val baseHours: String
) {
    CRYPTO("Crypto", "Sarafu za Kidijitali", "24/7 Continuous"),
    FOREX("Forex", "Ubadilishaji wa Fedha", "24/5 Interbank"),
    METALS("Precious Metals", "Madini ya Thamani", "London / NY Session"),
    COMMODITIES("Commodities", "Bidhaa Ghafi", "CME / ICE Hours"),
    INDICES("Global Indices", "Fahirisi za Kimataifa", "Cash / Futures Hours"),
    DERIV("Deriv Synthetics", "Deriv Synthetics", "24/7 Algorithmic"),
    STOCKS("Stocks & Equities", "Hisa za Kimataifa", "US 09:30 - 16:00 EST"),
    ETFS("ETFs", "Mifuko ya Uwekezaji", "US 09:30 - 16:00 EST")
}

enum class AppLanguageMode(
    val code: String,
    val label: String
) {
    ENGLISH_PRO("en", "ENGLISH PRO"),
    SWAHILI_IMARA("sw", "KISWAHILI IMARA")
}

enum class ProfitEngineMode(val displayName: String, val description: String) {
    STEADY_COMPOUND("Steady Compound", "Balanced algorithmic reinvestment"),
    HYPER_VELOCITY("Hyper-Velocity", "Sub-second opportunistic scalping"),
    CONSERVATIVE_PRESERVATION("Capital Preservation", "Ultra-safe drawdown floor"),
    ALPHA_MAXIMIZER("Alpha Maximizer", "Aggressive multi-market capture"),
    GAM_M5_HIGH_FREQUENCY("GAM M5 Scalper (~200 Trades)", "Grid & Accumulation Momentum (~200 trades/cycle on M5)")
}

data class GamModeConfig(
    val isEnabled: Boolean = false,
    val targetTradesPerCycle: Int = 200,
    val executedTradesInCycle: Int = 0,
    val profitTargetPerTradeUsd: Double = 1.0,
    val timeframe: String = "5m",
    val totalAccumulatedProfitUsd: Double = 0.0,
    val winRatePct: Double = 91.5,
    val autoCycleRestart: Boolean = true,
    val maxConcurrentGridOrders: Int = 8,
    val stopLossGuardPct: Double = 1.5,
    val status: String = "STANDBY"
)

enum class RiskProfile(val displayName: String, val maxDrawdownPct: Double) {
    LOW_RISK_CAPITAL_PRESERVATION("Low Risk (Preservation)", 2.5),
    BALANCED_ASYMMETRIC("Balanced Asymmetric", 6.0),
    MAX_ACCELERATION_ALPHA("Max Acceleration Alpha", 12.5)
}

enum class TradePeriodHorizon(val label: String, val minutes: Int) {
    ULTRA_SCALP_1M_5M("1M - 5M Ultra Scalp", 5),
    INTRADAY_SWING_15M_1H("15M - 1H Intraday", 60),
    SWING_POSITION_4H_1D("4H - 1D Swing", 1440),
    INSTITUTIONAL_CYCLE("Institutional Macro", 10080)
}

enum class ProfitAmbitionLevel(val label: String, val multiplier: Double) {
    REALISTIC_CONSISTENT("Consistent Growth", 1.0),
    AGGRESSIVE_COMPOUND("Aggressive Compounding", 2.2),
    MAX_POSSIBLE_ALPHA("Maximum Alpha Surge", 4.5)
}

enum class OrderSide {
    BUY,
    SELL
}

enum class OrderStatus {
    PENDING,
    FILLED,
    CANCELLED,
    REJECTED
}
