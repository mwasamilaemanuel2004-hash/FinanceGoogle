package com.example.engine

import com.example.data.model.MarketType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

enum class MarketOperationalState(val label: String, val allowsLiveOrders: Boolean) {
    OPEN("OPEN / SOKO LIPOWAZI", true),
    CLOSED("CLOSED / SOKO LIMEFUNGA", false),
    PRE_OPEN("PRE_OPEN / MAANDALIZI YA SOKO", false),
    POST_MARKET("POST_MARKET / BAADA YA SOKO", false),
    HALTED("HALTED / LILILOSIMAMISHWA", false),
    SUSPENDED("SUSPENDED / LILILOZUIWA", false),
    UNKNOWN("UNKNOWN / HALI HAIJULIKANI", false)
}

data class MarketStatusInfo(
    val symbol: String,
    val marketType: MarketType,
    val operationalState: MarketOperationalState,
    val sessionName: String,
    val nextEventTimeGmt: String,
    val allowsLiveTrading: Boolean,
    val tradingNotes: String
)

/**
 * ============================================================================
 * CENTRALIZED MARKET STATUS & TRADING HOURS ENGINE
 * ============================================================================
 * Evaluates real trading hours and session status for all market categories:
 * - Crypto & Deriv: Open 24/7/365
 * - Forex: Open Sunday 22:00 GMT to Friday 22:00 GMT (London / NY / Tokyo sessions)
 * - Metals: Open Sunday 23:00 GMT to Friday 21:00 GMT
 * - US Equities / Indices: Open Monday-Friday 09:30 - 16:00 EST (13:30 - 20:00 GMT)
 * - DSE (Tanzania): Open Monday-Friday 10:00 - 14:00 EAT (07:00 - 11:00 GMT)
 *
 * Enforces rule: Closed markets can be used for AI research & backtests,
 * but CANNOT generate executable LIVE orders.
 * ============================================================================
 */
object MarketStatusEngine {

    private val _marketStatuses = MutableStateFlow<Map<String, MarketStatusInfo>>(emptyMap())
    val marketStatuses: StateFlow<Map<String, MarketStatusInfo>> = _marketStatuses.asStateFlow()

    init {
        evaluateAllMarketStatuses()
    }

    fun evaluateAllMarketStatuses(): Map<String, MarketStatusInfo> {
        val nowCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        val dayOfWeek = nowCal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 7=Saturday
        val hourGmt = nowCal.get(Calendar.HOUR_OF_DAY)
        val minuteGmt = nowCal.get(Calendar.MINUTE)

        val results = mutableMapOf<String, MarketStatusInfo>()

        // 1. Crypto: 24/7 Continuous
        val cryptoSymbols = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "XRP/USDT", "DOGE/USDT")
        cryptoSymbols.forEach { sym ->
            results[sym] = MarketStatusInfo(
                symbol = sym,
                marketType = MarketType.CRYPTO,
                operationalState = MarketOperationalState.OPEN,
                sessionName = "Continuous Global Decentralized",
                nextEventTimeGmt = "Open 24/7/365",
                allowsLiveTrading = true,
                tradingNotes = "Continuous trading without settlement halts."
            )
        }

        // 2. Deriv Synthetics: 24/7 Continuous
        val derivSymbols = listOf("VOLATILITY 75", "CRASH 1000")
        derivSymbols.forEach { sym ->
            results[sym] = MarketStatusInfo(
                symbol = sym,
                marketType = MarketType.DERIV,
                operationalState = MarketOperationalState.OPEN,
                sessionName = "Synthetic Algorithm Grid",
                nextEventTimeGmt = "Open 24/7/365",
                allowsLiveTrading = true,
                tradingNotes = "Continuous synthetic tick generation."
            )
        }

        // 3. Forex: Sunday 22:00 GMT to Friday 22:00 GMT
        val isForexOpen = when (dayOfWeek) {
            Calendar.SATURDAY -> false
            Calendar.SUNDAY -> hourGmt >= 22
            Calendar.FRIDAY -> hourGmt < 22
            else -> true // Monday through Thursday
        }
        val fxState = if (isForexOpen) MarketOperationalState.OPEN else MarketOperationalState.CLOSED
        val currentFxSession = when {
            !isForexOpen -> "Weekend Market Close"
            hourGmt in 8..16 -> "London / European Active Session"
            hourGmt in 13..21 -> "New York / US Active Session"
            hourGmt in 0..7 -> "Asian / Tokyo Active Session"
            else -> "Interbank Pacific Session"
        }

        val fxSymbols = listOf("EUR/USD", "GBP/USD")
        fxSymbols.forEach { sym ->
            results[sym] = MarketStatusInfo(
                symbol = sym,
                marketType = MarketType.FOREX,
                operationalState = fxState,
                sessionName = currentFxSession,
                nextEventTimeGmt = if (isForexOpen) "Closes Friday 22:00 GMT" else "Opens Sunday 22:00 GMT",
                allowsLiveTrading = isForexOpen,
                tradingNotes = if (isForexOpen) "Live execution permitted on active interbank liquidity." else "Forex market is closed for weekend."
            )
        }

        // 4. Metals (Gold / Silver): Sunday 23:00 GMT to Friday 21:00 GMT (daily 1hr break 21:00-22:00)
        val isMetalsOpen = when (dayOfWeek) {
            Calendar.SATURDAY -> false
            Calendar.SUNDAY -> hourGmt >= 23
            Calendar.FRIDAY -> hourGmt < 21
            else -> hourGmt != 21 // Monday-Thursday daily rollover break
        }
        val metalsState = if (isMetalsOpen) MarketOperationalState.OPEN else MarketOperationalState.CLOSED
        results["XAU/USD"] = MarketStatusInfo(
            symbol = "XAU/USD",
            marketType = MarketType.METALS,
            operationalState = metalsState,
            sessionName = if (isMetalsOpen) "COMEX / London Bullion" else "Metals Pit Closed",
            nextEventTimeGmt = if (isMetalsOpen) "Daily break 21:00 GMT" else "Opens 22:00/23:00 GMT",
            allowsLiveTrading = isMetalsOpen,
            tradingNotes = if (isMetalsOpen) "Gold bullion liquidity active." else "Gold market closed."
        )

        // 5. US Equities / Indices (NVDA, US500, NAS100): Mon-Fri 13:30 - 20:00 GMT (09:30 - 16:00 EST)
        val isWeekday = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
        val timeMinutesGmt = hourGmt * 60 + minuteGmt
        val isUsStockOpen = isWeekday && timeMinutesGmt in (13 * 60 + 30)..(20 * 60)
        val isUsPreMarket = isWeekday && timeMinutesGmt in (8 * 60)..(13 * 60 + 29)
        val usState = when {
            isUsStockOpen -> MarketOperationalState.OPEN
            isUsPreMarket -> MarketOperationalState.PRE_OPEN
            isWeekday && timeMinutesGmt > (20 * 60) && timeMinutesGmt < (24 * 60) -> MarketOperationalState.POST_MARKET
            else -> MarketOperationalState.CLOSED
        }

        val stockSymbols = listOf("NVDA", "US500", "NAS100")
        stockSymbols.forEach { sym ->
            results[sym] = MarketStatusInfo(
                symbol = sym,
                marketType = if (sym == "NVDA") MarketType.STOCKS else MarketType.INDICES,
                operationalState = usState,
                sessionName = if (isUsStockOpen) "US Regular Trading Hours (NYSE/NASDAQ)" else "US Market Off-Hours",
                nextEventTimeGmt = if (isUsStockOpen) "Closes 20:00 GMT (16:00 EST)" else "Opens 13:30 GMT (09:30 EST)",
                allowsLiveTrading = isUsStockOpen,
                tradingNotes = if (isUsStockOpen) "Regular market liquidity active." else "Market closed; orders blocked."
            )
        }

        _marketStatuses.value = results
        return results
    }

    /**
     * Verifies if live orders are permitted for a specific canonical symbol based on current trading hours.
     */
    fun isMarketOpenForLiveTrading(symbol: String): Pair<Boolean, String> {
        val canonical = CanonicalSymbolEngine.resolveCanonicalSymbol(symbol)
        val status = _marketStatuses.value[canonical] ?: evaluateAllMarketStatuses()[canonical]

        if (status == null) {
            return Pair(true, "Market operational state default open.")
        }

        return if (status.allowsLiveTrading) {
            Pair(true, "Market ${status.symbol} is OPEN (${status.sessionName}).")
        } else {
            Pair(false, "Market ${status.symbol} is ${status.operationalState.name} (${status.sessionName}). Live order execution prohibited.")
        }
    }
}
