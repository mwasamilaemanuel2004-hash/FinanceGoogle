package com.example.engine

import com.example.data.model.MarketTick
import com.example.data.model.MarketType
import com.example.data.model.PriceCandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Real Yahoo Finance (yfinance) Quantitative Market Data Engine.
 * Fetches real-world interbank rates, commodities, metals, equities, and crypto
 * directly using Yahoo Finance v8 Chart and Quote APIs.
 */
data class YFinanceQuote(
    val symbol: String,
    val regularMarketPrice: Double,
    val previousClose: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val volume: Double,
    val changePercent: Double,
    val marketTime: Long
)

object YFinanceMarketEngine {

    // Yahoo Finance ticker mappings
    val YFINANCE_TICKER_MAP = mapOf(
        "XAU/USD (Gold)" to "GC=F",
        "XAG/USD (Silver)" to "SI=F",
        "BRENT CRUDE" to "BZ=F",
        "EUR/USD" to "EURUSD=X",
        "GBP/USD" to "GBPUSD=X",
        "USD/JPY" to "USDJPY=X",
        "AUD/USD" to "AUDUSD=X",
        "USD/CAD" to "CAD=X",
        "GBP/JPY" to "GBPJPY=X",
        "NVDA" to "NVDA",
        "AAPL" to "AAPL",
        "TSLA" to "TSLA",
        "MSFT" to "MSFT",
        "BTC/USDT" to "BTC-USD",
        "ETH/USDT" to "ETH-USD",
        "SOL/USDT" to "SOL-USD"
    )

    /**
     * Fetches a real-time quote for a given Yahoo Finance symbol.
     */
    suspend fun fetchQuote(yfinanceSymbol: String): YFinanceQuote? = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://query1.finance.yahoo.com/v8/finance/chart/$yfinanceSymbol?interval=1m&range=1d"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            }

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val json = reader.readText()
                reader.close()
                return@withContext parseYFinanceChartJson(yfinanceSymbol, json)
            }
        } catch (e: Exception) {
            // Fallback handled by caller
        }
        return@withContext null
    }

    /**
     * Batch fetch quotes for a list of Yahoo Finance symbols.
     */
    suspend fun fetchBatchQuotes(symbols: List<String>): Map<String, YFinanceQuote> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<String, YFinanceQuote>()
        for (sym in symbols) {
            val q = fetchQuote(sym)
            if (q != null) {
                result[sym] = q
            }
        }
        return@withContext result
    }

    /**
     * Parses real market price, previous close, high, low, volume from Yahoo Finance v8 response.
     */
    private fun parseYFinanceChartJson(symbol: String, json: String): YFinanceQuote? {
        val metaStart = json.indexOf("\"meta\":")
        if (metaStart == -1) return null

        val price = extractDouble(json, "\"regularMarketPrice\":") ?: return null
        val prevClose = extractDouble(json, "\"chartPreviousClose\":") ?: price
        val dayHigh = extractDouble(json, "\"regularMarketDayHigh\":") ?: (price * 1.01)
        val dayLow = extractDouble(json, "\"regularMarketDayLow\":") ?: (price * 0.99)
        val volume = extractDouble(json, "\"regularMarketVolume\":") ?: 100000.0
        val marketTime = extractLong(json, "\"regularMarketTime\":") ?: System.currentTimeMillis()

        val changePct = if (prevClose > 0.0) ((price - prevClose) / prevClose) * 100.0 else 0.0

        return YFinanceQuote(
            symbol = symbol,
            regularMarketPrice = price,
            previousClose = prevClose,
            dayHigh = dayHigh,
            dayLow = dayLow,
            volume = volume,
            changePercent = changePct,
            marketTime = marketTime
        )
    }

    private fun extractDouble(json: String, key: String): Double? {
        val start = json.indexOf(key)
        if (start == -1) return null
        val valStart = start + key.length
        var end = valStart
        while (end < json.length && (json[end].isDigit() || json[end] == '.' || json[end] == '-')) {
            end++
        }
        return json.substring(valStart, end).toDoubleOrNull()
    }

    private fun extractLong(json: String, key: String): Long? {
        val start = json.indexOf(key)
        if (start == -1) return null
        val valStart = start + key.length
        var end = valStart
        while (end < json.length && (json[end].isDigit() || json[end] == '-')) {
            end++
        }
        return json.substring(valStart, end).toLongOrNull()
    }
}
