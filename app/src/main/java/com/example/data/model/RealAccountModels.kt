package com.example.data.model

import kotlinx.serialization.Serializable

enum class ConnectorProvider(val displayName: String, val category: String) {
    BINANCE("Binance Global", "Crypto Spot & Futures"),
    BYBIT("Bybit Pro", "Crypto Derivatives"),
    OKX("OKX Global", "Crypto Spot & Derivatives"),
    COINBASE("Coinbase Advanced", "Crypto Spot"),
    KUCOIN("KuCoin", "Crypto Spot & Futures"),
    KRAKEN("Kraken Pro", "Crypto Spot & Futures"),
    BITGET("Bitget", "Crypto Derivatives & Copy"),
    GATEIO("Gate.io", "Crypto Spot & Futures"),
    MEXC("MEXC Global", "Crypto Spot & Futures"),
    DERIV("Deriv WebSocket", "Synthetics & Volatility Indices"),
    MT5_BROKER("MetaTrader 5 (MT5)", "Forex, Indices & Metals"),
    MT4_BROKER("MetaTrader 4 (MT4)", "Forex, Indices & Metals"),
    EXNESS("Exness Pro", "Forex & Commodities"),
    XM_GLOBAL("XM Global", "Forex & Micro Accounts"),
    IC_MARKETS("IC Markets ECN", "Forex Raw Spreads"),
    PEPPERSTONE("Pepperstone", "Forex Razor Spreads"),
    FXTM("FXTM (ForexTime)", "Forex & Cent Accounts"),
    OCTAFX("Octa (OctaFX)", "Forex Standard & Micro"),
    HFM("HFM (HotForex)", "Forex & Cent Accounts"),
    TICKMILL("Tickmill ECN", "Forex Algorithmic"),
    DSE_TANZANIA("DSE Tanzania Gateway", "Equities & Bonds")
}

@Serializable
data class PingTestResult(
    val isSuccess: Boolean,
    val latencyMs: Long,
    val httpCode: Int = 200,
    val message: String
)

enum class AccountHealthStatus(val displayName: String, val colorHex: Long) {
    HEALTHY("HEALTHY / ONLINE", 0xFF00E676),
    DEGRADED("DEGRADED / SLOW", 0xFFFFD600),
    RATE_LIMITED("RATE LIMITED", 0xFFFF6D00),
    OFFLINE("OFFLINE", 0xFFFF1744)
}

@Serializable
data class TradingPermissions(
    val canReadMarketData: Boolean = true,
    val canSpotTrade: Boolean = true,
    val canFuturesTrade: Boolean = false,
    val canWithdraw: Boolean = false, // Always false for trading bot safety
    val ipRestricted: Boolean = true
)

@Serializable
data class RealPosition(
    val ticket: String,
    val symbol: String,
    val side: OrderSide,
    val volume: Double,
    val entryPrice: Double,
    val currentPrice: Double,
    val unrealizedPnl: Double,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null
)

@Serializable
data class RealPendingOrder(
    val orderId: String,
    val symbol: String,
    val type: String,
    val price: Double,
    val volume: Double,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AccountRiskLimits(
    val maxDailyLossUsd: Double = 500.0,
    val maxLotSize: Double = 2.0,
    val maxOpenPositions: Int = 5,
    val isKillSwitchTriggered: Boolean = false
)

@Serializable
data class AuditLogEntry(
    val id: String = "LOG-${System.currentTimeMillis()}",
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val provider: String,
    val details: String,
    val severity: String = "INFO" // INFO, WARN, ALERT, RISK
)

@Serializable
data class RealAccountConnection(
    val id: String,
    val provider: ConnectorProvider,
    val accountName: String,
    val serverOrUrl: String = "",
    val maskedLoginOrKey: String = "",
    val encryptedApiKeyOrLogin: String = "",
    val encryptedSecretOrPassword: String = "",
    val encryptedPassphraseOrToken: String = "",
    val balance: Double = 0.0,
    val equity: Double = 0.0,
    val margin: Double = 0.0,
    val freeMargin: Double = 0.0,
    val currency: String = "USD",
    val healthStatus: AccountHealthStatus = AccountHealthStatus.OFFLINE,
    val pingLatencyMs: Long = 0,
    val permissions: TradingPermissions = TradingPermissions(),
    val positions: List<RealPosition> = emptyList(),
    val orders: List<RealPendingOrder> = emptyList(),
    val riskLimits: AccountRiskLimits = AccountRiskLimits(),
    val isLiveTradingAuthorized: Boolean = false,
    val isConnected: Boolean = false,
    val lastSyncTime: Long = 0
)
