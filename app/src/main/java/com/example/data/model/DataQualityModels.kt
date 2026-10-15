package com.example.data.model

/**
 * Data classification indicating the authenticity and source of market data.
 */
enum class DataClassification(val label: String, val isRealWorldData: Boolean) {
    REAL_LIVE_DATA("LIVE / DATA HALISI", true),
    HISTORICAL_DATA("HISTORICAL / HISTORIA", true),
    PAPER_TRADING_DATA("PAPER / MAZOEZI NA DATA HALISI", true),
    BACKTEST_DATA("BACKTEST / MAJARIBIO YA DATA", true),
    SIMULATION_DATA("SIMULATION / KISIMULIZI", false),
    MOCK_DEMO_DATA("DEMO / MOCK DATA", false)
}

/**
 * Health status assigned by the Data Quality Engine.
 */
enum class DataHealthStatus(val label: String, val allowsLiveTrading: Boolean) {
    HEALTHY("HEALTHY / BORA", true),
    DEGRADED("DEGRADED / ILEGEVU", false),
    STALE("STALE / ILIYOPITWA NA WAKATI", false),
    OFFLINE("OFFLINE / IMEKATIKA", false)
}

/**
 * Freshness status of incoming ticks and candles.
 */
enum class DataFreshnessStatus(val label: String) {
    REALTIME_LIVE("REAL-TIME (< 500ms)"),
    SLIGHTLY_DELAYED("DELAYED (500ms - 3s)"),
    STALE_CACHED("STALE (> 3s)"),
    DISCONNECTED("DISCONNECTED (> 15s)")
}

/**
 * Full Market Data Record capturing audit trail and data quality metrics.
 */
data class MarketDataRecord(
    val source: String, // e.g., "Binance REST/WS", "MT5 ECN Gateway", "Deriv WS", "CoinGecko API", "DSE Gateway", "Yahoo Finance"
    val symbol: String,
    val timestamp: Long = System.currentTimeMillis(),
    val receivedAt: Long = System.currentTimeMillis(),
    val bid: Double,
    val ask: Double,
    val lastPrice: Double,
    val volume: Double,
    val spread: Double,
    val latencyMs: Long,
    val sequenceId: Long,
    val dataQuality: DataHealthStatus = DataHealthStatus.HEALTHY,
    val freshnessStatus: DataFreshnessStatus = DataFreshnessStatus.REALTIME_LIVE,
    val classification: DataClassification = DataClassification.REAL_LIVE_DATA
)

/**
 * Data Quality Audit Report produced by the Data Quality Engine.
 */
data class DataQualityReport(
    val totalTicksEvaluated: Long = 0,
    val staleTicksDetected: Int = 0,
    val sequenceGapsDetected: Int = 0,
    val abnormalSpreadsDetected: Int = 0,
    val timestampAnomalies: Int = 0,
    val averageLatencyMs: Long = 45,
    val overallHealthStatus: DataHealthStatus = DataHealthStatus.HEALTHY,
    val activeDataSourceCount: Int = 6,
    val lastAuditTimestamp: Long = System.currentTimeMillis(),
    val notes: String = "All official exchange feeds operating within nominal latency thresholds."
)

/**
 * LSF / LSE Speed Rate Algorithm & Indicator metrics.
 */
data class LsfLseSpeedRate(
    val symbol: String,
    val liquiditySweepFrequency: Double, // Sweeps per 5-minute rolling window
    val liquiditySqueezeExpansionRatio: Double, // Compression vs Expansion ratio
    val tickVelocityAcceleration: Double, // Price points per second^2
    val deltaSpeedRatePerSec: Double, // Cumulative volume delta rate
    val speedScore: Int, // 0 - 100
    val regimeTag: String = "SPEED_ACCELERATION_EXPANSION"
)

/**
 * End-to-end Execution & Fill Reconciliation event.
 */
data class ExecutionReconciliationEvent(
    val internalOrderId: String,
    val brokerOrderId: String,
    val symbol: String,
    val side: OrderSide,
    val requestedLot: Double,
    val filledLot: Double,
    val requestedPrice: Double,
    val executedPrice: Double,
    val slippagePips: Double,
    val executionLatencyMs: Long,
    val brokerStatus: String, // "ACKNOWLEDGED", "FILLED", "PARTIALLY_FILLED", "REJECTED", "CANCELLED"
    val reconciliationPassed: Boolean,
    val discrepancyNotes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
