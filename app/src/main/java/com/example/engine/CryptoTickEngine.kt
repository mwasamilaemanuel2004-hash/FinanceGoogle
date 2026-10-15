package com.example.engine

import com.example.data.model.MarketTick
import com.example.data.model.MarketType
import com.example.data.model.PriceCandle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

class CryptoTickEngine(private val connectorFlow: StateFlow<ExchangeConnector>? = null) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _ticks = MutableStateFlow<Map<String, MarketTick>>(emptyMap())
    val ticks: StateFlow<Map<String, MarketTick>> = _ticks.asStateFlow()

    private val _candlesBySymbol = MutableStateFlow<Map<String, List<PriceCandle>>>(emptyMap())
    val candlesBySymbol: StateFlow<Map<String, List<PriceCandle>>> = _candlesBySymbol.asStateFlow()

    private val _isLiveFeedActive = MutableStateFlow(true)
    val isLiveFeedActive: StateFlow<Boolean> = _isLiveFeedActive.asStateFlow()

    private val _pingLatencyMs = MutableStateFlow<Long>(120L)
    val pingLatencyMs: StateFlow<Long> = _pingLatencyMs.asStateFlow()

    // Symbol mapping for Binance
    private val binanceCryptoSymbols = listOf(
        "BTCUSDT" to "BTC/USDT",
        "ETHUSDT" to "ETH/USDT",
        "SOLUSDT" to "SOL/USDT",
        "XRPUSDT" to "XRP/USDT",
        "DOGEUSDT" to "DOGE/USDT",
        "BNBUSDT" to "BNB/USDT",
        "ADAUSDT" to "ADA/USDT",
        "AVAXUSDT" to "AVAX/USDT",
        "LINKUSDT" to "LINK/USDT",
        "DOTUSDT" to "DOT/USDT"
    )

    private val encodedSymbolsParam = "%5B%22BTCUSDT%22,%22ETHUSDT%22,%22SOLUSDT%22,%22XRPUSDT%22,%22DOGEUSDT%22,%22BNBUSDT%22,%22ADAUSDT%22,%22AVAXUSDT%22,%22LINKUSDT%22,%22DOTUSDT%22%5D"

    init {
        initDefaultTicks()
        startFastBinancePricePolling()
        startBackground24hStatsPolling()
        startRealForexPolling()
        startYFinancePolling()
    }

    private fun initDefaultTicks() {
        val initial = linkedMapOf(
            // --- TOP CRYPTO MARKETS (Real Fallbacks) ---
            "BTC/USDT" to MarketTick("BTC/USDT", MarketType.CRYPTO, 83789.00, +0.89, 84500.0, 82875.0, 24115.0, 52.4),
            "ETH/USDT" to MarketTick("ETH/USDT", MarketType.CRYPTO, 2673.98, +0.45, 2715.0, 2620.0, 268873.0, 55.1),
            "SOL/USDT" to MarketTick("SOL/USDT", MarketType.CRYPTO, 116.23, +1.15, 118.4, 112.5, 2838800.0, 58.2),
            "XRP/USDT" to MarketTick("XRP/USDT", MarketType.CRYPTO, 1.5287, +1.95, 1.56, 1.48, 214815300.0, 52.4),
            "DOGE/USDT" to MarketTick("DOGE/USDT", MarketType.CRYPTO, 0.0949, +0.95, 0.098, 0.092, 1342300000.0, 50.1),
            "BNB/USDT" to MarketTick("BNB/USDT", MarketType.CRYPTO, 773.32, +0.78, 785.0, 762.0, 142665.0, 59.8),
            "ADA/USDT" to MarketTick("ADA/USDT", MarketType.CRYPTO, 0.2470, +3.12, 0.254, 0.235, 172882300.0, 60.5),
            "AVAX/USDT" to MarketTick("AVAX/USDT", MarketType.CRYPTO, 10.147, -0.85, 10.50, 9.92, 4300800.0, 47.3),
            "LINK/USDT" to MarketTick("LINK/USDT", MarketType.CRYPTO, 13.351, +7.20, 13.65, 12.10, 2912500.0, 64.1),
            "DOT/USDT" to MarketTick("DOT/USDT", MarketType.CRYPTO, 1.149, +3.85, 1.19, 1.08, 10814900.0, 61.0),
            
            // --- LARGE CAP & ACTIVE CRYPTO ---
            "TRX/USDT" to MarketTick("TRX/USDT", MarketType.CRYPTO, 0.1625, +1.42, 0.165, 0.158, 48200000.0, 56.4),
            "TON/USDT" to MarketTick("TON/USDT", MarketType.CRYPTO, 5.125, -0.45, 5.31, 4.98, 12400000.0, 49.2),
            "LTC/USDT" to MarketTick("LTC/USDT", MarketType.CRYPTO, 78.42, +2.15, 80.20, 76.10, 2300000.0, 57.0),
            "BCH/USDT" to MarketTick("BCH/USDT", MarketType.CRYPTO, 324.50, -1.10, 335.00, 318.00, 940000.0, 44.5),
            "UNI/USDT" to MarketTick("UNI/USDT", MarketType.CRYPTO, 7.82, +4.20, 8.10, 7.42, 4500000.0, 62.1),
            "NEAR/USDT" to MarketTick("NEAR/USDT", MarketType.CRYPTO, 4.152, +3.10, 4.31, 3.98, 8100000.0, 59.3),
            "ATOM/USDT" to MarketTick("ATOM/USDT", MarketType.CRYPTO, 6.35, +0.85, 6.52, 6.21, 3200000.0, 51.0),
            "PEPE/USDT" to MarketTick("PEPE/USDT", MarketType.CRYPTO, 0.0000185, +9.40, 0.0000195, 0.0000168, 891000000.0, 68.2),
            "SHIB/USDT" to MarketTick("SHIB/USDT", MarketType.CRYPTO, 0.0000248, +5.22, 0.0000261, 0.0000231, 412000000.0, 63.5),

            // --- MAJOR FOREX PAIRS ---
            "EUR/USD" to MarketTick("EUR/USD", MarketType.FOREX, 1.1387, +0.35, 1.1420, 1.1350, 682000.0, 52.3),
            "GBP/USD" to MarketTick("GBP/USD", MarketType.FOREX, 1.2930, -0.24, 1.2980, 1.2910, 340000.0, 45.1),
            "USD/JPY" to MarketTick("USD/JPY", MarketType.FOREX, 153.40, +0.45, 154.10, 152.80, 610000.0, 56.7),
            "AUD/USD" to MarketTick("AUD/USD", MarketType.FOREX, 0.6540, +0.35, 0.6580, 0.6510, 280000.0, 51.2),
            "USD/CAD" to MarketTick("USD/CAD", MarketType.FOREX, 1.3980, -0.15, 1.4020, 1.3940, 220000.0, 48.0),
            "USD/CHF" to MarketTick("USD/CHF", MarketType.FOREX, 0.8842, -0.10, 0.8890, 0.8810, 185000.0, 46.2),
            "NZD/USD" to MarketTick("NZD/USD", MarketType.FOREX, 0.5912, +0.22, 0.5950, 0.5880, 142000.0, 50.5),

            // --- EUR & GBP CROSSES ---
            "EUR/GBP" to MarketTick("EUR/GBP", MarketType.FOREX, 0.8805, +0.58, 0.8840, 0.8750, 195000.0, 58.4),
            "EUR/JPY" to MarketTick("EUR/JPY", MarketType.FOREX, 174.65, +0.82, 175.20, 173.80, 280000.0, 60.1),
            "EUR/CHF" to MarketTick("EUR/CHF", MarketType.FOREX, 1.0068, +0.25, 1.0110, 1.0030, 115000.0, 51.5),
            "EUR/AUD" to MarketTick("EUR/AUD", MarketType.FOREX, 1.7410, +0.02, 1.7480, 1.7350, 94000.0, 49.8),
            "EUR/CAD" to MarketTick("EUR/CAD", MarketType.FOREX, 1.5915, +0.20, 1.5960, 1.5870, 85000.0, 53.0),
            "GBP/JPY" to MarketTick("GBP/JPY", MarketType.FOREX, 198.35, +0.58, 199.10, 197.60, 390000.0, 58.0),
            "GBP/CHF" to MarketTick("GBP/CHF", MarketType.FOREX, 1.1432, +0.15, 1.1480, 1.1390, 78000.0, 52.1),
            "GBP/AUD" to MarketTick("GBP/AUD", MarketType.FOREX, 1.9772, -0.60, 1.9890, 1.9680, 62000.0, 40.5),

            // --- AUD, NZD & EXOTICS ---
            "AUD/JPY" to MarketTick("AUD/JPY", MarketType.FOREX, 100.31, +0.80, 100.80, 99.42, 125000.0, 59.2),
            "NZD/JPY" to MarketTick("NZD/JPY", MarketType.FOREX, 90.72, +0.68, 91.15, 90.10, 89000.0, 57.5),
            "CAD/JPY" to MarketTick("CAD/JPY", MarketType.FOREX, 109.72, +0.62, 110.15, 109.10, 91000.0, 56.4),
            "CHF/JPY" to MarketTick("CHF/JPY", MarketType.FOREX, 173.48, +0.55, 174.10, 172.60, 84000.0, 55.2),
            "USD/SGD" to MarketTick("USD/SGD", MarketType.FOREX, 1.3412, -0.05, 1.3450, 1.3380, 52000.0, 48.1),
            "USD/ZAR" to MarketTick("USD/ZAR", MarketType.FOREX, 18.2540, +1.22, 18.5200, 18.1000, 42000.0, 61.2),
            "USD/TRY" to MarketTick("USD/TRY", MarketType.FOREX, 34.1250, +0.85, 34.3500, 33.9500, 38000.0, 64.0),
            "USD/MXN" to MarketTick("USD/MXN", MarketType.FOREX, 19.8520, -0.42, 20.1200, 19.7200, 41000.0, 43.1),

            // --- DERIV SYNTHETICS: VOLATILITY FAMILY ---
            "VOLATILITY 10 (Deriv)" to MarketTick("VOLATILITY 10 (Deriv)", MarketType.DERIV, 9845.20, +1.12, 9920.0, 9710.0, 84000.0, 52.1),
            "VOLATILITY 25 (Deriv)" to MarketTick("VOLATILITY 25 (Deriv)", MarketType.DERIV, 2415.80, +2.45, 2460.0, 2380.0, 92000.0, 58.5),
            "VOLATILITY 50 (Deriv)" to MarketTick("VOLATILITY 50 (Deriv)", MarketType.DERIV, 51240.0, -0.85, 52100.0, 50800.0, 110000.0, 46.2),
            "VOLATILITY 75 (Deriv)" to MarketTick("VOLATILITY 75 (Deriv)", MarketType.DERIV, 428930.0, +6.40, 435000.0, 412000.0, 150000.0, 74.2),
            "VOLATILITY 100 (Deriv)" to MarketTick("VOLATILITY 100 (Deriv)", MarketType.DERIV, 185420.0, +4.25, 189000.0, 181000.0, 120000.0, 68.1),
            "VOLATILITY 10 1S (Deriv)" to MarketTick("VOLATILITY 10 1S (Deriv)", MarketType.DERIV, 9912.40, +1.08, 9980.0, 9820.0, 71000.0, 53.0),
            "VOLATILITY 25 1S (Deriv)" to MarketTick("VOLATILITY 25 1S (Deriv)", MarketType.DERIV, 2432.50, +2.38, 2475.0, 2390.0, 85000.0, 59.0),
            "VOLATILITY 50 1S (Deriv)" to MarketTick("VOLATILITY 50 1S (Deriv)", MarketType.DERIV, 51480.0, -0.72, 52200.0, 51000.0, 93000.0, 47.1),
            "VOLATILITY 75 1S (Deriv)" to MarketTick("VOLATILITY 75 1S (Deriv)", MarketType.DERIV, 430500.0, +6.25, 436000.0, 414000.0, 134000.0, 72.5),
            "VOLATILITY 100 1S (Deriv)" to MarketTick("VOLATILITY 100 1S (Deriv)", MarketType.DERIV, 186200.0, +4.10, 190000.0, 182000.0, 111000.0, 66.4),

            // --- DERIV SYNTHETICS: CRASH & BOOM ---
            "CRASH 300 (Deriv)" to MarketTick("CRASH 300 (Deriv)", MarketType.DERIV, 3145.20, -5.40, 3280.0, 3090.0, 98000.0, 31.2),
            "CRASH 500 (Deriv)" to MarketTick("CRASH 500 (Deriv)", MarketType.DERIV, 4850.50, -4.10, 5020.0, 4790.0, 102000.0, 35.8),
            "CRASH 1000 (Deriv)" to MarketTick("CRASH 1000 (Deriv)", MarketType.DERIV, 6245.10, -3.20, 6450.0, 6190.0, 94000.0, 39.5),
            "BOOM 300 (Deriv)" to MarketTick("BOOM 300 (Deriv)", MarketType.DERIV, 3250.80, +7.15, 3310.0, 3110.0, 118000.0, 72.1),
            "BOOM 500 (Deriv)" to MarketTick("BOOM 500 (Deriv)", MarketType.DERIV, 5410.20, +5.35, 5500.0, 5280.0, 108000.0, 69.4),
            "BOOM 1000 (Deriv)" to MarketTick("BOOM 1000 (Deriv)", MarketType.DERIV, 8912.40, +4.80, 9100.0, 8720.0, 112000.0, 66.8),

            // --- DERIV SYNTHETICS: JUMP, STEP, RANGE BREAK, DEX & OTHERS ---
            "JUMP 10 (Deriv)" to MarketTick("JUMP 10 (Deriv)", MarketType.DERIV, 10125.0, +1.82, 10250.0, 9980.0, 52000.0, 55.4),
            "JUMP 25 (Deriv)" to MarketTick("JUMP 25 (Deriv)", MarketType.DERIV, 25240.0, +3.10, 25600.0, 24800.0, 64000.0, 61.2),
            "JUMP 50 (Deriv)" to MarketTick("JUMP 50 (Deriv)", MarketType.DERIV, 50410.0, -1.15, 51300.0, 49800.0, 72000.0, 45.1),
            "JUMP 75 (Deriv)" to MarketTick("JUMP 75 (Deriv)", MarketType.DERIV, 75620.0, +4.20, 76900.0, 74200.0, 85000.0, 66.8),
            "JUMP 100 (Deriv)" to MarketTick("JUMP 100 (Deriv)", MarketType.DERIV, 100850.0, +5.12, 102400.0, 98900.0, 94000.0, 69.5),
            "STEP INDEX (Deriv)" to MarketTick("STEP INDEX (Deriv)", MarketType.DERIV, 8430.50, +1.10, 8510.0, 8390.0, 85000.0, 53.4),
            "SKEWED STEP 10 (Deriv)" to MarketTick("SKEWED STEP 10 (Deriv)", MarketType.DERIV, 8512.20, +1.32, 8590.0, 8460.0, 42000.0, 54.8),
            "SKEWED STEP 20 (Deriv)" to MarketTick("SKEWED STEP 20 (Deriv)", MarketType.DERIV, 8642.50, +1.48, 8720.0, 8580.0, 48000.0, 56.2),
            "MULTI-STEP INDEX (Deriv)" to MarketTick("MULTI-STEP INDEX (Deriv)", MarketType.DERIV, 9152.00, +1.20, 9240.0, 9080.0, 31000.0, 54.1),
            "RANGE BREAK 100 (Deriv)" to MarketTick("RANGE BREAK 100 (Deriv)", MarketType.DERIV, 10245.0, +2.15, 10380.0, 10120.0, 63000.0, 56.4),
            "RANGE BREAK 200 (Deriv)" to MarketTick("RANGE BREAK 200 (Deriv)", MarketType.DERIV, 20450.0, +3.52, 20850.0, 20100.0, 69000.0, 61.5),
            "DEX 600 (Deriv)" to MarketTick("DEX 600 (Deriv)", MarketType.DERIV, 612.40, +1.95, 624.0, 601.0, 28000.0, 58.2),
            "DEX 900 (Deriv)" to MarketTick("DEX 900 (Deriv)", MarketType.DERIV, 918.50, +2.48, 932.0, 904.0, 34000.0, 60.1),
            "DRIFT SWITCH 10 (Deriv)" to MarketTick("DRIFT SWITCH 10 (Deriv)", MarketType.DERIV, 1018.50, +1.25, 1032.0, 1005.0, 19000.0, 53.0),
            "DRIFT SWITCH 30 (Deriv)" to MarketTick("DRIFT SWITCH 30 (Deriv)", MarketType.DERIV, 3042.00, +1.72, 3090.0, 2980.0, 22000.0, 55.4),
            "HYBRID INDEX (Deriv)" to MarketTick("HYBRID INDEX (Deriv)", MarketType.DERIV, 52412.0, +3.12, 53400.0, 51500.0, 56000.0, 61.8),
            "AUD/USD (DFX)" to MarketTick("AUD/USD (DFX)", MarketType.DERIV, 0.6552, +0.32, 0.6590, 0.6520, 12000.0, 51.5),
            "EUR/USD (DFX)" to MarketTick("EUR/USD (DFX)", MarketType.DERIV, 1.1395, +0.42, 1.1440, 1.1360, 15000.0, 53.1),
            "GBP/USD (DFX)" to MarketTick("GBP/USD (DFX)", MarketType.DERIV, 1.2942, -0.18, 1.2990, 1.2910, 14000.0, 46.5),
            "USD Basket (Deriv)" to MarketTick("USD Basket (Deriv)", MarketType.DERIV, 102.45, +0.15, 102.80, 102.10, 8000.0, 52.4),
            "EUR Basket (Deriv)" to MarketTick("EUR Basket (Deriv)", MarketType.DERIV, 104.12, +0.22, 104.60, 103.80, 9000.0, 54.1),
            "GBP Basket (Deriv)" to MarketTick("GBP Basket (Deriv)", MarketType.DERIV, 101.85, -0.12, 102.30, 101.50, 7500.0, 48.0),

            // --- METALS & COMMODITIES ---
            "XAU/USD (Gold)" to MarketTick("XAU/USD (Gold)", MarketType.METALS, 4310.20, +0.95, 4325.0, 4280.0, 452000.0, 67.5),
            "XAG/USD (Silver)" to MarketTick("XAG/USD (Silver)", MarketType.METALS, 33.50, +1.40, 34.10, 32.80, 190000.0, 61.0),
            "XPT/USD (Platinum)" to MarketTick("XPT/USD (Platinum)", MarketType.METALS, 984.50, +0.82, 995.0, 972.0, 34000.0, 55.4),
            "XPD/USD (Palladium)" to MarketTick("XPD/USD (Palladium)", MarketType.METALS, 1042.00, -0.45, 1060.0, 1025.0, 12000.0, 47.8),
            "BRENT CRUDE" to MarketTick("BRENT CRUDE", MarketType.COMMODITIES, 74.80, -0.65, 76.10, 73.90, 89000.0, 43.8),
            "WTI CRUDE" to MarketTick("WTI CRUDE", MarketType.COMMODITIES, 71.42, -0.82, 72.80, 70.30, 94000.0, 41.5),
            "NATURAL GAS" to MarketTick("NATURAL GAS", MarketType.COMMODITIES, 2.342, +3.15, 2.41, 2.28, 142000.0, 60.1),
            "HEATING OIL" to MarketTick("HEATING OIL", MarketType.COMMODITIES, 2.152, -0.45, 2.19, 2.11, 28000.0, 46.5),
            "COPPER" to MarketTick("COPPER", MarketType.COMMODITIES, 4.124, +1.02, 4.18, 4.07, 39000.0, 54.2),

            // --- GLOBAL INDICES ---
            "US500" to MarketTick("US500", MarketType.INDICES, 5842.50, +0.45, 5875.0, 5810.0, 840000.0, 56.4),
            "NAS100" to MarketTick("NAS100", MarketType.INDICES, 20412.0, +0.82, 20600.0, 20250.0, 950000.0, 61.2),
            "US30" to MarketTick("US30", MarketType.INDICES, 42815.0, +0.15, 43050.0, 42600.0, 410000.0, 51.5),
            "GER40" to MarketTick("GER40", MarketType.INDICES, 19412.0, +0.32, 19520.0, 19310.0, 290000.0, 54.1),
            "UK100" to MarketTick("UK100", MarketType.INDICES, 8254.0, -0.12, 8295.0, 8210.0, 142000.0, 48.0),
            "JPN225" to MarketTick("JPN225", MarketType.INDICES, 38450.0, +0.92, 38800.0, 38100.0, 310000.0, 58.4),
            "HK50" to MarketTick("HK50", MarketType.INDICES, 20125.0, -1.45, 20450.0, 19980.0, 240000.0, 41.2),

            // --- GLOBAL STOCKS & EQUITIES ---
            "NVDA" to MarketTick("NVDA", MarketType.STOCKS, 138.25, +4.10, 140.50, 134.00, 2450000.0, 71.2),
            "AAPL" to MarketTick("AAPL", MarketType.STOCKS, 232.50, +0.85, 234.00, 230.10, 1200000.0, 52.0),
            "TSLA" to MarketTick("TSLA", MarketType.STOCKS, 248.90, -1.30, 255.00, 244.20, 1980000.0, 48.3),
            "MSFT" to MarketTick("MSFT", MarketType.STOCKS, 428.10, +1.20, 431.50, 425.00, 950000.0, 56.4),
            "CRDB (DSE)" to MarketTick("CRDB (DSE)", MarketType.STOCKS, 580.0, +1.75, 590.0, 570.0, 85000.0, 54.0),
            "NMB (DSE)" to MarketTick("NMB (DSE)", MarketType.STOCKS, 4850.0, +2.10, 4900.0, 4780.0, 62000.0, 59.0),
            "TBL (DSE)" to MarketTick("TBL (DSE)", MarketType.STOCKS, 10900.0, 0.0, 10900.0, 10900.0, 15000.0, 50.0),

            // --- ETFs (EXCHANGE-TRADED FUNDS) ---
            "SPY" to MarketTick("SPY", MarketType.ETFS, 582.40, +0.44, 585.0, 579.5, 480000.0, 55.8),
            "QQQ" to MarketTick("QQQ", MarketType.ETFS, 488.15, +0.81, 491.2, 484.5, 510000.0, 60.5),
            "DIA" to MarketTick("DIA", MarketType.ETFS, 428.25, +0.12, 430.5, 426.1, 142000.0, 51.0),
            "GLD" to MarketTick("GLD", MarketType.ETFS, 252.12, +0.94, 253.5, 250.2, 195000.0, 67.2),
            "USO" to MarketTick("USO", MarketType.ETFS, 74.12, -0.78, 75.4, 73.2, 85000.0, 42.4)
        )
        _ticks.value = initial

        val candleMap = mutableMapOf<String, List<PriceCandle>>()
        initial.forEach { (sym, tick) ->
            candleMap[sym] = generateCandlesForPrice(tick.price)
        }
        _candlesBySymbol.value = candleMap
    }

    /**
     * High-speed polling (650ms interval) for real spot prices using the ultra-lightweight
     * Binance ticker/price endpoint (only ~360 bytes response payload).
     */
    private fun startFastBinancePricePolling() {
        scope.launch {
            while (isActive) {
                fetchFastCryptoPrices()
                delay(650L) // Ultra-fast 650ms polling loop
            }
        }
    }

    /**
     * Executes immediate fast price fetch and latency measurement.
     */
    private fun fetchFastCryptoPrices() {
        val domains = listOf("api.binance.com", "api1.binance.com", "api2.binance.com", "api3.binance.com")
        var success = false
        val startTime = System.currentTimeMillis()

        for (domain in domains) {
            try {
                // Primary and backup endpoints: batch spot prices
                val url = URL("https://$domain/api/v3/ticker/price?symbols=$encodedSymbolsParam")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 2000
                    readTimeout = 2000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0")
                }

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val json = reader.readText()
                    reader.close()
                    val latency = System.currentTimeMillis() - startTime
                    _pingLatencyMs.value = latency.coerceAtLeast(15L)

                    parseAndUpdateFastPrices(json)
                    _isLiveFeedActive.value = true
                    success = true
                    break // Succeeded, exit domain retry loop
                }
            } catch (e: Exception) {
                // Continue to try next available backup domain
            }
        }

        if (!success) {
            // All primary domains failed, fall back to secondary mirror endpoint
            fetchMirrorPrices()
        }
    }

    private fun fetchMirrorPrices() {
        try {
            val startTime = System.currentTimeMillis()
            val url = URL("https://data-api.binance.vision/api/v3/ticker/price?symbol=BTCUSDT")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 2000
                readTimeout = 2000
                requestMethod = "GET"
            }
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val txt = reader.readText()
                reader.close()
                val price = extractJsonField(txt, "price")?.toDoubleOrNull()
                val latency = System.currentTimeMillis() - startTime
                _pingLatencyMs.value = latency.coerceAtLeast(20L)

                if (price != null && price > 1000.0) {
                    val current = _ticks.value.toMutableMap()
                    current["BTC/USDT"]?.let {
                        current["BTC/USDT"] = it.copy(price = price)
                        _ticks.value = current
                        _isLiveFeedActive.value = true
                        syncCandleWithPrice("BTC/USDT", price)
                    }
                }
            }
        } catch (ignored: Exception) {
            // Maintained by onTick micro-smoothing
        }
    }

    /**
     * Fast parsing of lightweight json: [{"symbol":"BTCUSDT","price":"84028.50"},...]
     */
    private fun parseAndUpdateFastPrices(json: String) {
        val current = _ticks.value.toMutableMap()
        var updatedAny = false

        binanceCryptoSymbols.forEach { (binanceSym, slashSym) ->
            val symIdx = json.indexOf("\"symbol\":\"$binanceSym\"")
            if (symIdx != -1) {
                val sub = json.substring(symIdx, minOf(json.length, symIdx + 90))
                val price = extractJsonField(sub, "price")?.toDoubleOrNull()
                if (price != null && isValidPriceFeed(slashSym, price)) {
                    val existing = current[slashSym]
                    if (existing != null) {
                        DataQualityEngine.validateAndIngestTick(
                            source = "Binance Direct Spot REST",
                            symbol = slashSym,
                            price = price,
                            volume = existing.volume,
                            latencyMs = _pingLatencyMs.value
                        )
                        current[slashSym] = existing.copy(
                            price = price,
                            high24h = maxOf(existing.high24h, price),
                            low24h = if (existing.low24h > 0) minOf(existing.low24h, price) else price
                        )
                        updatedAny = true
                        syncCandleWithPrice(slashSym, price)
                    }
                }
            }
        }

        if (updatedAny) {
            _ticks.value = current
        }
    }

    /**
     * Background polling every 12 seconds for 24h high/low and 24h change %, so heavy
     * stats don't introduce latency on fast spot price execution.
     */
    private fun startBackground24hStatsPolling() {
        scope.launch {
            while (isActive) {
                try {
                    val url = URL("https://api.binance.com/api/v3/ticker/24hr?symbols=$encodedSymbolsParam")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 4000
                        readTimeout = 4000
                        requestMethod = "GET"
                    }
                    if (conn.responseCode == 200) {
                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        val json = reader.readText()
                        reader.close()
                        parseAndUpdate24hStats(json)
                    }
                } catch (ignored: Exception) {}
                delay(12000L) // 12 seconds
            }
        }
    }

    private fun parseAndUpdate24hStats(json: String) {
        val current = _ticks.value.toMutableMap()
        binanceCryptoSymbols.forEach { (binanceSym, slashSym) ->
            val idx = json.indexOf("\"symbol\":\"$binanceSym\"")
            if (idx != -1) {
                val sub = json.substring(idx, minOf(json.length, idx + 450))
                val change = extractJsonField(sub, "priceChangePercent")?.toDoubleOrNull()
                val high = extractJsonField(sub, "highPrice")?.toDoubleOrNull()
                val low = extractJsonField(sub, "lowPrice")?.toDoubleOrNull()
                val vol = extractJsonField(sub, "volume")?.toDoubleOrNull()

                val existing = current[slashSym]
                if (existing != null) {
                    current[slashSym] = existing.copy(
                        change24h = change ?: existing.change24h,
                        high24h = high ?: existing.high24h,
                        low24h = low ?: existing.low24h,
                        volume = vol ?: existing.volume
                    )
                }
            }
        }
        _ticks.value = current
    }

    /**
     * Polls live interbank exchange rates every 4 seconds with robust failover.
     */
    private fun startRealForexPolling() {
        scope.launch {
            while (isActive) {
                var success = false
                try {
                    val url = URL("https://open.er-api.com/v6/latest/USD")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 3000
                        readTimeout = 3000
                        requestMethod = "GET"
                    }
                    if (conn.responseCode == 200) {
                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        val response = reader.readText()
                        reader.close()
                        parseAndUpdateForexRates(response)
                        success = true
                    }
                } catch (ignored: Exception) {}

                if (!success) {
                    // Failover endpoint for high-availability forex rates
                    try {
                        val url = URL("https://api.exchangerate-api.com/v4/latest/USD")
                        val conn = (url.openConnection() as HttpURLConnection).apply {
                            connectTimeout = 3000
                            readTimeout = 3000
                            requestMethod = "GET"
                        }
                        if (conn.responseCode == 200) {
                            val reader = BufferedReader(InputStreamReader(conn.inputStream))
                            val response = reader.readText()
                            reader.close()
                            parseAndUpdateForexRates(response)
                        }
                    } catch (ignored: Exception) {}
                }

                delay(4000L) // Poll forex rates every 4s
            }
        }
    }

    /**
     * Polls real market data from Yahoo Finance (yfinance) for Metals, Oil, and Equities.
     */
    private fun startYFinancePolling() {
        scope.launch {
            while (isActive) {
                try {
                    for ((displayName, yfTicker) in YFinanceMarketEngine.YFINANCE_TICKER_MAP) {
                        val quote = YFinanceMarketEngine.fetchQuote(yfTicker)
                        if (quote != null && quote.regularMarketPrice > 0.0) {
                            val current = _ticks.value.toMutableMap()
                            current[displayName]?.let { existing ->
                                current[displayName] = existing.copy(
                                    price = quote.regularMarketPrice,
                                    change24h = quote.changePercent,
                                    high24h = maxOf(existing.high24h, quote.dayHigh),
                                    low24h = if (existing.low24h > 0) minOf(existing.low24h, quote.dayLow) else quote.dayLow,
                                    volume = quote.volume
                                )
                                _ticks.value = current
                                syncCandleWithPrice(displayName, quote.regularMarketPrice)
                            }
                        }
                        delay(250L) // Smooth stagger to avoid rate limits
                    }
                } catch (ignored: Exception) {}
                delay(3000L) // Repeat every 3s
            }
        }
    }

    private fun parseAndUpdateForexRates(json: String) {
        val current = _ticks.value.toMutableMap()
        val eurRate = extractJsonRate(json, "EUR")
        val gbpRate = extractJsonRate(json, "GBP")
        val jpyRate = extractJsonRate(json, "JPY")
        val audRate = extractJsonRate(json, "AUD")
        val cadRate = extractJsonRate(json, "CAD")

        if (eurRate != null && eurRate > 0.0) {
            val eurUsd = 1.0 / eurRate
            current["EUR/USD"]?.let {
                current["EUR/USD"] = it.copy(price = eurUsd)
                syncCandleWithPrice("EUR/USD", eurUsd)
            }
        }
        if (gbpRate != null && gbpRate > 0.0) {
            val gbpUsd = 1.0 / gbpRate
            current["GBP/USD"]?.let {
                current["GBP/USD"] = it.copy(price = gbpUsd)
                syncCandleWithPrice("GBP/USD", gbpUsd)
            }
        }
        if (jpyRate != null && jpyRate > 0.0) {
            current["USD/JPY"]?.let {
                current["USD/JPY"] = it.copy(price = jpyRate)
                syncCandleWithPrice("USD/JPY", jpyRate)
            }
        }
        if (audRate != null && audRate > 0.0) {
            val audUsd = 1.0 / audRate
            current["AUD/USD"]?.let {
                current["AUD/USD"] = it.copy(price = audUsd)
                syncCandleWithPrice("AUD/USD", audUsd)
            }
        }
        if (cadRate != null && cadRate > 0.0) {
            current["USD/CAD"]?.let {
                current["USD/CAD"] = it.copy(price = cadRate)
                syncCandleWithPrice("USD/CAD", cadRate)
            }
        }
        _ticks.value = current
    }

    private fun extractJsonRate(json: String, currency: String): Double? {
        val pattern = "\"$currency\":"
        val start = json.indexOf(pattern)
        if (start == -1) return null
        val valStart = start + pattern.length
        var end = json.indexOf(",", valStart)
        if (end == -1) end = json.indexOf("}", valStart)
        if (end == -1) return null
        return json.substring(valStart, end).trim().toDoubleOrNull()
    }

    private fun extractJsonField(jsonObj: String, field: String): String? {
        val pattern = "\"$field\":\""
        val start = jsonObj.indexOf(pattern)
        if (start == -1) return null
        val valStart = start + pattern.length
        val end = jsonObj.indexOf("\"", valStart)
        if (end == -1) return null
        return jsonObj.substring(valStart, end)
    }

    /**
     * Sub-second high frequency micro-tick loop (called every 500ms from telemetry).
     * Simulates continuous live inter-tick fluctuations for Deriv Synthetics, Gold,
     * Commodities, and currencies so the terminal feels alive and never delayed!
     */
    fun onTick() {
        val current = _ticks.value.toMutableMap()
        var updated = false

        // 1. DERIV SYNTHETICS: Live Algorithmic High-Frequency Ticking
        current["VOLATILITY 75 (Deriv)"]?.let { t ->
            val deltaPct = (Random.nextDouble() - 0.498) * 0.0006
            val newP = t.price * (1.0 + deltaPct)
            current["VOLATILITY 75 (Deriv)"] = t.copy(price = newP)
            syncCandleWithPrice("VOLATILITY 75 (Deriv)", newP)
            updated = true
        }

        current["VOLATILITY 100 (Deriv)"]?.let { t ->
            val deltaPct = (Random.nextDouble() - 0.498) * 0.0008
            val newP = t.price * (1.0 + deltaPct)
            current["VOLATILITY 100 (Deriv)"] = t.copy(price = newP)
            syncCandleWithPrice("VOLATILITY 100 (Deriv)", newP)
            updated = true
        }

        current["BOOM 1000 (Deriv)"]?.let { t ->
            // Boom 1000: small regular ticks down, occasional large explosive spikes up
            val isSpike = Random.nextDouble() < 0.035
            val newP = if (isSpike) {
                t.price + Random.nextDouble(45.0, 120.0)
            } else {
                t.price - Random.nextDouble(0.4, 1.2)
            }
            current["BOOM 1000 (Deriv)"] = t.copy(price = newP)
            syncCandleWithPrice("BOOM 1000 (Deriv)", newP)
            updated = true
        }

        current["CRASH 1000 (Deriv)"]?.let { t ->
            // Crash 1000: small regular ticks up, occasional rapid crashes down
            val isCrash = Random.nextDouble() < 0.035
            val newP = if (isCrash) {
                t.price - Random.nextDouble(45.0, 120.0)
            } else {
                t.price + Random.nextDouble(0.4, 1.2)
            }
            current["CRASH 1000 (Deriv)"] = t.copy(price = newP)
            syncCandleWithPrice("CRASH 1000 (Deriv)", newP)
            updated = true
        }

        current["STEP INDEX (Deriv)"]?.let { t ->
            val step = if (Random.nextBoolean()) 0.10 else -0.10
            val newP = t.price + step
            current["STEP INDEX (Deriv)"] = t.copy(price = newP)
            syncCandleWithPrice("STEP INDEX (Deriv)", newP)
            updated = true
        }

        // 2. METALS: Gold & Silver Live Bullion Inter-Tick Micro Movement
        current["XAU/USD (Gold)"]?.let { t ->
            val goldDelta = (Random.nextDouble() - 0.495) * 0.35 // ±$0.35 micro-tick
            val newP = t.price + goldDelta
            current["XAU/USD (Gold)"] = t.copy(price = newP)
            syncCandleWithPrice("XAU/USD (Gold)", newP)
            updated = true
        }

        current["XAG/USD (Silver)"]?.let { t ->
            val silverDelta = (Random.nextDouble() - 0.495) * 0.02
            val newP = t.price + silverDelta
            current["XAG/USD (Silver)"] = t.copy(price = newP)
            syncCandleWithPrice("XAG/USD (Silver)", newP)
            updated = true
        }

        // 3. COMMODITIES: Brent Crude Micro Movement
        current["BRENT CRUDE"]?.let { t ->
            val oilDelta = (Random.nextDouble() - 0.495) * 0.03
            val newP = t.price + oilDelta
            current["BRENT CRUDE"] = t.copy(price = newP)
            syncCandleWithPrice("BRENT CRUDE", newP)
            updated = true
        }

        if (updated) {
            _ticks.value = current
        }
    }

    /**
     * Keep candles 100% synchronized in real time with the active price so that the
     * candlestick chart never lags behind the price banner.
     */
    private fun syncCandleWithPrice(symbol: String, currentPrice: Double) {
        val existingMap = _candlesBySymbol.value
        val list = existingMap[symbol] ?: return
        if (list.isEmpty()) return

        val last = list.last()
        val now = System.currentTimeMillis()

        val updatedList = list.toMutableList()

        // Check if candle interval expired (e.g. 60 seconds)
        if (now - last.timestamp > 60000L) {
            // Finalize candle and create new one
            if (updatedList.size >= 45) {
                updatedList.removeAt(0)
            }
            updatedList.add(PriceCandle(now, currentPrice, currentPrice, currentPrice, currentPrice, 100.0))
        } else {
            // Update active forming candle in real-time
            val updatedLast = last.copy(
                close = currentPrice,
                high = maxOf(last.high, currentPrice),
                low = minOf(last.low, currentPrice)
            )
            updatedList[updatedList.size - 1] = updatedLast
        }

        val updatedMap = existingMap.toMutableMap()
        updatedMap[symbol] = updatedList
        _candlesBySymbol.value = updatedMap
    }

    private fun generateCandlesForPrice(basePrice: Double, high24h: Double? = null, low24h: Double? = null): List<PriceCandle> {
        val list = mutableListOf<PriceCandle>()
        var current = basePrice
        val now = System.currentTimeMillis()
        val volatilityFactor = basePrice * 0.0012
        val minLimit = low24h ?: (basePrice * 0.95)
        val maxLimit = high24h ?: (basePrice * 1.05)

        for (i in 40 downTo 0) {
            val delta = (Random.nextDouble() - 0.495) * volatilityFactor * 1.8
            val open = current
            var close = open + delta
            close = close.coerceIn(minLimit, maxLimit)
            val high = maxOf(open, close) + Random.nextDouble() * (volatilityFactor * 0.4)
            val low = minOf(open, close) - Random.nextDouble() * (volatilityFactor * 0.4)
            val volume = 150.0 + Random.nextDouble() * 850.0
            list.add(PriceCandle(now - i * 60000L, open, high, low, close, volume))
            current = close
        }
        return list
    }

    fun getCandlesForSymbol(symbol: String): List<PriceCandle> {
        val existing = _candlesBySymbol.value[symbol]
        if (existing != null && existing.isNotEmpty()) {
            val tick = _ticks.value[symbol]
            if (tick != null) {
                // Ensure latest candle reflects exact current tick price immediately
                val last = existing.last()
                val updatedLast = last.copy(
                    close = tick.price,
                    high = maxOf(last.high, tick.price),
                    low = minOf(last.low, tick.price)
                )
                val list = existing.toMutableList()
                list[list.size - 1] = updatedLast
                return list
            }
            return existing
        }

        val price = _ticks.value[symbol]?.price ?: 100.0
        val generated = generateCandlesForPrice(price)
        val updated = _candlesBySymbol.value.toMutableMap()
        updated[symbol] = generated
        _candlesBySymbol.value = updated
        return generated
    }

    /**
     * User-triggered immediate refresh without waiting for loop interval.
     */
    fun forceRefreshNow() {
        scope.launch {
            fetchFastCryptoPrices()
        }
    }

    fun addCustomPair(
        rawSymbol: String,
        marketType: MarketType? = null,
        customPrice: Double? = null
    ): MarketTick {
        val trimmed = rawSymbol.trim().uppercase(Locale.ROOT)
        val cleanSymbol = if (trimmed.contains("/") || trimmed.contains(" ") || trimmed.length <= 4) trimmed else "$trimmed/USDT"

        val existing = _ticks.value[cleanSymbol]
        if (existing != null) {
            return existing
        }

        val inferredType = marketType ?: inferMarketType(cleanSymbol)
        val price = customPrice ?: inferInitialPrice(cleanSymbol, inferredType)

        val newTick = MarketTick(
            symbol = cleanSymbol,
            marketType = inferredType,
            price = price,
            change24h = (Random.nextDouble() - 0.45) * 5.0,
            high24h = price * 1.02,
            low24h = price * 0.98,
            volume = Random.nextDouble(5000.0, 150000.0),
            rsi = Random.nextDouble(42.0, 68.0)
        )

        val updatedTicks = linkedMapOf<String, MarketTick>()
        updatedTicks[cleanSymbol] = newTick
        updatedTicks.putAll(_ticks.value)
        _ticks.value = updatedTicks

        val updatedCandles = _candlesBySymbol.value.toMutableMap()
        updatedCandles[cleanSymbol] = generateCandlesForPrice(price)
        _candlesBySymbol.value = updatedCandles

        return newTick
    }

    private fun inferMarketType(symbol: String): MarketType {
        val s = symbol.uppercase(Locale.ROOT)
        return when {
            s.contains("VOLATILITY") || s.contains("CRASH") || s.contains("BOOM") || s.contains("STEP") || s.contains("DERIV") -> MarketType.DERIV
            s.contains("EUR") || s.contains("GBP") || s.contains("JPY") || s.contains("AUD") || s.contains("NZD") || s.contains("CAD") || s.contains("CHF") -> MarketType.FOREX
            s.contains("GOLD") || s.contains("XAU") || s.contains("SILVER") || s.contains("XAG") -> MarketType.METALS
            s.contains("OIL") || s.contains("CRUDE") || s.contains("GAS") -> MarketType.COMMODITIES
            s.contains("DSE") || s.contains("CRDB") || s.contains("NMB") || s.contains("TBL") || s.contains("VODA") -> MarketType.STOCKS
            s.contains("USDT") || s.contains("BTC") || s.contains("ETH") || s.contains("SOL") || s.contains("DOGE") || s.contains("XRP") -> MarketType.CRYPTO
            else -> MarketType.CRYPTO
        }
    }

    private fun inferInitialPrice(symbol: String, marketType: MarketType): Double {
        val s = symbol.uppercase(Locale.ROOT)
        return when (marketType) {
            MarketType.FOREX -> if (s.contains("JPY")) 153.40 else 1.0845
            MarketType.DERIV -> if (s.contains("VOLATILITY")) 428930.0 else 6245.0
            MarketType.METALS -> if (s.contains("GOLD") || s.contains("XAU")) 4265.80 else 33.50
            MarketType.COMMODITIES -> 74.80
            MarketType.STOCKS -> if (s.contains("DSE")) 580.0 else 138.25
            MarketType.CRYPTO -> when {
                s.contains("BTC") -> 84028.50
                s.contains("ETH") -> 2673.98
                s.contains("SOL") -> 116.23
                s.contains("XRP") -> 1.5287
                s.contains("DOGE") -> 0.0949
                s.contains("SHIB") || s.contains("PEPE") -> 0.000021
                else -> 42.50
            }
            MarketType.INDICES -> 5850.0
            MarketType.ETFS -> 245.00
        }
    }

    private fun isValidPriceFeed(symbol: String, newPrice: Double): Boolean {
        if (newPrice <= 0.0 || newPrice.isNaN() || newPrice.isInfinite()) return false
        return true
    }
}
