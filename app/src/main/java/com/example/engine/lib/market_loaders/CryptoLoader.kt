package com.example.engine.lib.market_loaders

import com.example.data.model.MarketTick
import com.example.data.model.MarketType
import kotlinx.coroutines.flow.MutableStateFlow

object CryptoLoader {
    // This simulates dynamic loading
    fun fetchDynamicMarketList(): List<MarketTick> {
        // Real implementation would fetch from Binance/CoinGecko here
        return listOf(
            MarketTick("BTC/USDT", MarketType.CRYPTO, 60000.0, 0.5, 61000.0, 59000.0, 1000000.0, 55.0),
            MarketTick("ETH/USDT", MarketType.CRYPTO, 3000.0, -0.2, 3100.0, 2900.0, 500000.0, 48.0)
        )
    }
}
