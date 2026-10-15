package com.example.engine

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class InfinitePairItem(
    val id: String = UUID.randomUUID().toString(),
    val symbol: String,
    val category: String, // "CRYPTO", "FOREX", "STOCKS", "DERIV", "COMMODITIES", "INDICES"
    val clusterId: String,
    val baseAsset: String,
    val quoteAsset: String,
    val currentPrice: Double,
    val volatility24h: Double,
    val liquidityScore: Double,
    val isTradable: Boolean
)

data class BehaviorCluster(
    val id: String,
    val name: String,
    val descriptionEn: String,
    val descriptionSw: String,
    val primaryMarket: String,
    val defaultStrategy: String,
    val riskMultiplier: Double,
    val instrumentCount: Int
)

data class IndicatorToggleState(
    val id: String,
    val name: String,
    val description: String,
    val isActive: Boolean,
    val latencyMs: Int
)

class InfinitePairsAndClusteringEngine private constructor(context: Context) {

    private val _clusters = MutableStateFlow(
        listOf(
            BehaviorCluster(
                id = "cluster_1",
                name = "High Volatility Momentum",
                descriptionEn = "Aggressive breakout & momentum assets (Crypto Perps, Volatility 75/100, Crash/Boom)",
                descriptionSw = "Masoko yenye kasi kubwa na kuruka kwa bei (Crypto, Volatility 75/100, Crash/Boom)",
                primaryMarket = "DERIV & CRYPTO",
                defaultStrategy = "SessionOpenBreakout_v1",
                riskMultiplier = 1.2,
                instrumentCount = 18450
            ),
            BehaviorCluster(
                id = "cluster_2",
                name = "Range Mean-Reversion",
                descriptionEn = "Stable institutional FX majors & structured indices (EUR/USD, GBP/USD, US500)",
                descriptionSw = "Masoko ya kubadilika mara kwa mara na kulinda mstari (FX Majors & Indices)",
                primaryMarket = "FOREX & INDICES",
                defaultStrategy = "Divergence_CVD_v1",
                riskMultiplier = 0.8,
                instrumentCount = 15200
            ),
            BehaviorCluster(
                id = "cluster_3",
                name = "Liquidity Sweep & SMC / ICT",
                descriptionEn = "Order block & institutional sweep zones (Gold, NAS100, BTC)",
                descriptionSw = "Sehemu za ukwasi mkubwa na maeneo ya ICT / SMC (Gold, NAS100)",
                primaryMarket = "METALS & CRYPTO",
                defaultStrategy = "OrderBlock_Retest_v1",
                riskMultiplier = 1.0,
                instrumentCount = 9800
            ),
            BehaviorCluster(
                id = "cluster_4",
                name = "News-Driven High Spike",
                descriptionEn = "High macroeconomic sensitivity assets (Gold, Crude Oil, USD Pairs)",
                descriptionSw = "Masoko yanayoathiriwa na taarifa za kiuchumi na habari (Gold, Oil)",
                primaryMarket = "COMMODITIES",
                defaultStrategy = "VolumeSpike_Absorption_v1",
                riskMultiplier = 1.5,
                instrumentCount = 4250
            ),
            BehaviorCluster(
                id = "cluster_5",
                name = "Low-Latency Arbitrage & Synthetics",
                descriptionEn = "Algorithmic synthetic pairs & step/jump indices (Deriv Step, Jump, Range Break)",
                descriptionSw = "Masoko ya kiotomatiki ya Step, Jump, na Synthetic Index",
                primaryMarket = "DERIV SYNTHETICS",
                defaultStrategy = "LsfLseSpeedRate_v1",
                riskMultiplier = 1.1,
                instrumentCount = 3100
            )
        )
    )
    val clusters: StateFlow<List<BehaviorCluster>> = _clusters.asStateFlow()

    private val _samplePairs = MutableStateFlow(
        listOf(
            InfinitePairItem(symbol = "BTC/USDT", category = "CRYPTO", clusterId = "cluster_1", baseAsset = "BTC", quoteAsset = "USDT", currentPrice = 67450.25, volatility24h = 4.2, liquidityScore = 99.8, isTradable = true),
            InfinitePairItem(symbol = "ETH/USDT", category = "CRYPTO", clusterId = "cluster_1", baseAsset = "ETH", quoteAsset = "USDT", currentPrice = 3520.10, volatility24h = 5.1, liquidityScore = 97.5, isTradable = true),
            InfinitePairItem(symbol = "Volatility 75 Index", category = "DERIV", clusterId = "cluster_1", baseAsset = "V75", quoteAsset = "USD", currentPrice = 485120.40, volatility24h = 8.4, liquidityScore = 95.0, isTradable = true),
            InfinitePairItem(symbol = "EUR/USD", category = "FOREX", clusterId = "cluster_2", baseAsset = "EUR", quoteAsset = "USD", currentPrice = 1.0892, volatility24h = 0.6, liquidityScore = 99.9, isTradable = true),
            InfinitePairItem(symbol = "GBP/USD", category = "FOREX", clusterId = "cluster_2", baseAsset = "GBP", quoteAsset = "USD", currentPrice = 1.3045, volatility24h = 0.8, liquidityScore = 98.4, isTradable = true),
            InfinitePairItem(symbol = "XAU/USD (Gold)", category = "METALS", clusterId = "cluster_3", baseAsset = "XAU", quoteAsset = "USD", currentPrice = 2385.50, volatility24h = 1.8, liquidityScore = 99.2, isTradable = true),
            InfinitePairItem(symbol = "NAS100", category = "INDICES", clusterId = "cluster_3", baseAsset = "NAS", quoteAsset = "USD", currentPrice = 18450.00, volatility24h = 1.5, liquidityScore = 98.1, isTradable = true),
            InfinitePairItem(symbol = "Crude Oil (WTI)", category = "COMMODITIES", clusterId = "cluster_4", baseAsset = "WTI", quoteAsset = "USD", currentPrice = 78.40, volatility24h = 2.4, liquidityScore = 96.0, isTradable = true),
            InfinitePairItem(symbol = "Crash 1000 Index", category = "DERIV", clusterId = "cluster_5", baseAsset = "CR1000", quoteAsset = "USD", currentPrice = 12450.80, volatility24h = 7.1, liquidityScore = 94.2, isTradable = true),
            InfinitePairItem(symbol = "Step Index", category = "DERIV", clusterId = "cluster_5", baseAsset = "STEP", quoteAsset = "USD", currentPrice = 5420.10, volatility24h = 2.1, liquidityScore = 93.8, isTradable = true)
        )
    )
    val samplePairs: StateFlow<List<InfinitePairItem>> = _samplePairs.asStateFlow()

    private val _indicators = MutableStateFlow(
        listOf(
            IndicatorToggleState("ind_1", "Session Open & Breakout", "London/NY/Asia open bands & ATR 1.5x expansion", true, 2),
            IndicatorToggleState("ind_2", "Volume Spike & Absorption", "Volume 20 SMA + 3.2x multiplier & iceberg detection", true, 3),
            IndicatorToggleState("ind_3", "Order Block (OB) FVG", "Institutional bearish/bullish OB retest zones", true, 2),
            IndicatorToggleState("ind_4", "CVD & Footprint Divergence", "Delta divergence & CVD price flat detection", true, 4),
            IndicatorToggleState("ind_5", "LSF/LSE Speed Rate Algorithm", "High speed tick velocity & acceleration burst", true, 1),
            IndicatorToggleState("ind_6", "Wyckoff Spring & SOS", "Spring sweeps SSL & sign of strength volume surge", true, 3),
            IndicatorToggleState("ind_7", "ICT Liquidity Sweep BSL/SSL", "Buy-side and sell-side liquidity grab detection", true, 2),
            IndicatorToggleState("ind_8", "Quantum Volatility Band", "Dynamic standard deviation & option Greeks volatility", true, 3),
            IndicatorToggleState("ind_9", "SuperTrend Multi-Timeframe", "MTF trend filter across 1m, 5m, 1h, 4h", true, 2),
            IndicatorToggleState("ind_10", "VWAP Institutional Anchor", "Volume weighted average price session bands", true, 1),
            IndicatorToggleState("ind_11", "Macd Momentum Histogram", "Zero-lag MACD crossover & divergence detector", true, 2)
        )
    )
    val indicators: StateFlow<List<IndicatorToggleState>> = _indicators.asStateFlow()

    private val _selectedAgentStrategy = MutableStateFlow("Autonomous Hybrid Multi-Strategy AI (Dynamic Cluster Selection)")
    val selectedAgentStrategy: StateFlow<String> = _selectedAgentStrategy.asStateFlow()

    fun toggleIndicator(id: String) {
        _indicators.value = _indicators.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
    }

    fun setAgentStrategy(strategyName: String) {
        _selectedAgentStrategy.value = strategyName
    }

    companion object {
        @Volatile
        private var INSTANCE: InfinitePairsAndClusteringEngine? = null

        fun getInstance(context: Context): InfinitePairsAndClusteringEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: InfinitePairsAndClusteringEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
