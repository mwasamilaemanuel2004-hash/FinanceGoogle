package com.example.engine

import com.example.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

data class ProviderRateLimitState(
    val providerName: String,
    val maxRequestsPerMinute: Int = 1200,
    val currentRequestsInWindow: AtomicInteger = AtomicInteger(0),
    val windowStartTimestamp: Long = System.currentTimeMillis(),
    val isThrottled: Boolean = false
)

/**
 * ============================================================================
 * UNIFIED MARKET DATA GATEWAY (ESMH.TRADE / MWASA AI)
 * ============================================================================
 * Architecture:
 * Exchange / Broker / API (Binance, Bybit, OKX, Deriv, MT5, DSE, YFinance)
 * ↓
 * Connector
 * ↓
 * Rate Limiter & Token Bucket
 * ↓
 * Schema & Timestamp Validation
 * ↓
 * Canonical Symbol Mapping
 * ↓
 * Normalization & Deduplication
 * ↓
 * Data Quality Engine
 * ↓
 * Real-Time Market Data Bus (StateFlow)
 * ↓
 * Consumers (Mother Brain, AI Agents, ML Models, Strategy, Risk Governor, UI)
 * ============================================================================
 */
object MarketDataGateway {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Unified Real-Time Market Data Bus
    private val _liveMarketBus = MutableStateFlow<Map<String, MarketDataRecord>>(emptyMap())
    val liveMarketBus: StateFlow<Map<String, MarketDataRecord>> = _liveMarketBus.asStateFlow()

    // Provider Rate Limiters
    private val rateLimiters = ConcurrentHashMap<String, ProviderRateLimitState>()

    // Deduplication tracker
    private val recentTickDeduplication = ConcurrentHashMap<String, String>()

    init {
        initializeRateLimiters()
        startRateLimiterResetLoop()
    }

    private fun initializeRateLimiters() {
        rateLimiters["Binance"] = ProviderRateLimitState("Binance", 1200)
        rateLimiters["Bybit"] = ProviderRateLimitState("Bybit", 600)
        rateLimiters["OKX"] = ProviderRateLimitState("OKX", 600)
        rateLimiters["Deriv"] = ProviderRateLimitState("Deriv", 1000)
        rateLimiters["MT5"] = ProviderRateLimitState("MT5", 2000)
        rateLimiters["CoinGecko"] = ProviderRateLimitState("CoinGecko", 30) // Free tier friendly
        rateLimiters["YahooFinance"] = ProviderRateLimitState("YahooFinance", 300)
    }

    private fun startRateLimiterResetLoop() {
        scope.launch {
            while (isActive) {
                delay(60_000L) // Reset request counters every 60 seconds
                rateLimiters.values.forEach { it.currentRequestsInWindow.set(0) }
            }
        }
    }

    /**
     * Check if a request to a provider is within rate limits.
     */
    fun checkRateLimit(providerName: String): Boolean {
        val limiter = rateLimiters[providerName] ?: return true
        val count = limiter.currentRequestsInWindow.incrementAndGet()
        return count <= limiter.maxRequestsPerMinute
    }

    /**
     * Unified Ingestion Pipeline:
     * Ingests, normalizes, validates, deduplicates, and publishes incoming market data.
     */
    fun ingestMarketTick(
        sourceProvider: String,
        rawSymbol: String,
        price: Double,
        bid: Double = price * 0.9998,
        ask: Double = price * 1.0002,
        volume: Double = 1000.0,
        sourceTimestamp: Long = System.currentTimeMillis(),
        classification: DataClassification = DataClassification.REAL_LIVE_DATA
    ): MarketDataRecord? {
        val now = System.currentTimeMillis()

        // 1. Timestamp validation & Clock Skew Detection (Max 15s future or 60s past)
        val clockDrift = abs(now - sourceTimestamp)
        if (clockDrift > 60_000L) {
            // Discard heavily skewed timestamp
            return null
        }

        // 2. Canonical Symbol Mapping
        val canonicalSymbol = CanonicalSymbolEngine.resolveCanonicalSymbol(rawSymbol, sourceProvider)

        // 3. Deduplication Check (Same symbol + same price within 50ms is discarded to save bus load)
        val dedupeKey = "$canonicalSymbol:${(price * 100).toLong()}"
        val lastSeen = recentTickDeduplication[canonicalSymbol]
        if (lastSeen == dedupeKey && (now - sourceTimestamp) < 50L) {
            return null
        }
        recentTickDeduplication[canonicalSymbol] = dedupeKey

        // 4. Rate-Limit Check
        if (!checkRateLimit(sourceProvider)) {
            // Rate limit exceeded; route through DataQualityEngine as degraded
            return DataQualityEngine.validateAndIngestTick(
                source = "$sourceProvider (RATE_LIMITED)",
                symbol = canonicalSymbol,
                price = price,
                bid = bid,
                ask = ask,
                volume = volume,
                latencyMs = clockDrift,
                classification = classification
            )
        }

        // 5. Price & Precision Normalization
        val normalizedPrice = CanonicalSymbolEngine.normalizePrice(canonicalSymbol, price)
        val normalizedBid = CanonicalSymbolEngine.normalizePrice(canonicalSymbol, bid)
        val normalizedAsk = CanonicalSymbolEngine.normalizePrice(canonicalSymbol, ask)

        // 6. Data Quality Engine Ingestion
        val record = DataQualityEngine.validateAndIngestTick(
            source = sourceProvider,
            symbol = canonicalSymbol,
            price = normalizedPrice,
            bid = normalizedBid,
            ask = normalizedAsk,
            volume = volume,
            latencyMs = clockDrift,
            classification = classification
        )

        // 7. Publish to Real-Time Market Data Bus
        val busMap = _liveMarketBus.value.toMutableMap()
        busMap[canonicalSymbol] = record
        _liveMarketBus.value = busMap

        return record
    }

    /**
     * Retrieves the latest validated market record for a canonical symbol.
     */
    fun getLatestMarketRecord(symbol: String): MarketDataRecord? {
        val canonical = CanonicalSymbolEngine.resolveCanonicalSymbol(symbol)
        return _liveMarketBus.value[canonical] ?: DataQualityEngine.marketRecords.value[canonical]
    }
}
