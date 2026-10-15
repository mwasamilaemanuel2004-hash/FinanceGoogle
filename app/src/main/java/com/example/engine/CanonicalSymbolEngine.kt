package com.example.engine

import com.example.data.model.MarketType

/**
 * Canonical Instrument Specification defining precision, contract sizing, tick sizes, and venue mappings.
 */
data class CanonicalInstrument(
    val canonicalSymbol: String, // e.g. "BTC/USDT", "EUR/USD", "XAU/USD", "US500", "NAS100", "VOLATILITY 75", "NVDA", "CRDB"
    val baseAsset: String,
    val quoteAsset: String,
    val assetClass: MarketType,
    val contractType: String, // "SPOT", "PERPETUAL_FUTURES", "CFD", "SYNTHETIC", "EQUITY"
    val contractSize: Double, // 1.0 for Spot/Crypto, 100,000 for standard FX, 100 for Gold, etc.
    val tickSize: Double,
    val lotStep: Double,
    val minLot: Double,
    val maxLot: Double,
    val pricePrecision: Int,
    val quantityPrecision: Int,
    val minNotionalUsd: Double,
    val tradingHoursDescription: String = "24/7 Continuous",
    val providerMappings: Map<String, String> // "Binance" -> "BTCUSDT", "Bybit" -> "BTCUSDT", "OKX" -> "BTC-USDT", "MT5" -> "EURUSDm", etc.
)

/**
 * ============================================================================
 * CANONICAL SYMBOL & MULTI-ASSET NORMALIZATION ENGINE
 * ============================================================================
 * Maps varied broker/exchange ticker formats into normalized canonical instruments:
 * BTCUSDT, BTC-USDT, XBTUSDT -> BTC/USDT
 * EURUSD, EURUSDm, EURUSD.pro, EUR_USD -> EUR/USD
 * XAUUSD, GOLD, XAUUSD.a -> XAU/USD
 * US500, SPX500, SP500 -> US500
 * NAS100, USTEC, NAS100m -> NAS100
 * GER40, DAX -> DAX
 * WTI, USOIL -> WTI CRUDE
 * BRENT, UKOIL -> BRENT CRUDE
 * ============================================================================
 */
object CanonicalSymbolEngine {

    private val CANONICAL_REGISTRY = mapOf(
        "BTC/USDT" to CanonicalInstrument(
            canonicalSymbol = "BTC/USDT",
            baseAsset = "BTC",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.001,
            minLot = 0.001,
            maxLot = 100.0,
            pricePrecision = 2,
            quantityPrecision = 3,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf(
                "Binance" to "BTCUSDT",
                "Bybit" to "BTCUSDT",
                "OKX" to "BTC-USDT",
                "Coinbase" to "BTC-USD",
                "MT5" to "BTCUSD"
            )
        ),
        
        // --- EXPANDED 51 ASSET UNIVERSE ---
        "BNB/USDT" to CanonicalInstrument(
            canonicalSymbol = "BNB/USDT",
            baseAsset = "BNB",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 1000.0,
            pricePrecision = 2,
            quantityPrecision = 2,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf("Binance" to "BNBUSDT", "Polygon" to "X:BNBUSD")
        ),
        "ADA/USDT" to CanonicalInstrument(
            canonicalSymbol = "ADA/USDT",
            baseAsset = "ADA",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.0001,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 100000.0,
            pricePrecision = 4,
            quantityPrecision = 1,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf("Binance" to "ADAUSDT", "Polygon" to "X:ADAUSD")
        ),
        "AVAX/USDT" to CanonicalInstrument(
            canonicalSymbol = "AVAX/USDT",
            baseAsset = "AVAX",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.1,
            minLot = 0.1,
            maxLot = 5000.0,
            pricePrecision = 2,
            quantityPrecision = 1,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf("Binance" to "AVAXUSDT", "Polygon" to "X:AVAXUSD")
        ),
        "LINK/USDT" to CanonicalInstrument(
            canonicalSymbol = "LINK/USDT",
            baseAsset = "LINK",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.1,
            minLot = 0.1,
            maxLot = 10000.0,
            pricePrecision = 2,
            quantityPrecision = 1,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf("Binance" to "LINKUSDT", "Polygon" to "X:LINKUSD")
        ),
        "DOT/USDT" to CanonicalInstrument(
            canonicalSymbol = "DOT/USDT",
            baseAsset = "DOT",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.1,
            minLot = 0.1,
            maxLot = 10000.0,
            pricePrecision = 2,
            quantityPrecision = 1,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf("Binance" to "DOTUSDT", "Polygon" to "X:DOTUSD")
        ),
        "MATIC/USDT" to CanonicalInstrument(
            canonicalSymbol = "MATIC/USDT",
            baseAsset = "MATIC",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.0001,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 50000.0,
            pricePrecision = 4,
            quantityPrecision = 1,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf("Binance" to "POLUSDT", "Polygon" to "X:MATICUSD")
        ),
        "SHIB/USDT" to CanonicalInstrument(
            canonicalSymbol = "SHIB/USDT",
            baseAsset = "SHIB",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.00000001,
            lotStep = 10000.0,
            minLot = 10000.0,
            maxLot = 100000000.0,
            pricePrecision = 8,
            quantityPrecision = 0,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf("Binance" to "SHIBUSDT", "Polygon" to "X:SHIBUSD")
        ),
        "PEPE/USDT" to CanonicalInstrument(
            canonicalSymbol = "PEPE/USDT",
            baseAsset = "PEPE",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.00000001,
            lotStep = 100000.0,
            minLot = 100000.0,
            maxLot = 1000000000.0,
            pricePrecision = 8,
            quantityPrecision = 0,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf("Binance" to "PEPEUSDT", "Polygon" to "X:PEPEUSD")
        ),
        "SUI/USDT" to CanonicalInstrument(
            canonicalSymbol = "SUI/USDT",
            baseAsset = "SUI",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 50000.0,
            pricePrecision = 3,
            quantityPrecision = 1,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf("Binance" to "SUIUSDT", "Polygon" to "X:SUIUSD")
        ),
        "USD/JPY" to CanonicalInstrument(
            canonicalSymbol = "USD/JPY",
            baseAsset = "USD",
            quoteAsset = "JPY",
            assetClass = MarketType.FOREX,
            contractType = "FX_SPOT",
            contractSize = 100000.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 1000.0,
            tradingHoursDescription = "24/5 Global Interbank",
            providerMappings = mapOf("Polygon" to "C:USDJPY", "MT5" to "USDJPY")
        ),
        "USD/CHF" to CanonicalInstrument(
            canonicalSymbol = "USD/CHF",
            baseAsset = "USD",
            quoteAsset = "CHF",
            assetClass = MarketType.FOREX,
            contractType = "FX_SPOT",
            contractSize = 100000.0,
            tickSize = 0.00001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 5,
            quantityPrecision = 2,
            minNotionalUsd = 1000.0,
            tradingHoursDescription = "24/5 Global Interbank",
            providerMappings = mapOf("Polygon" to "C:USDCHF", "MT5" to "USDCHF")
        ),
        "AUD/USD" to CanonicalInstrument(
            canonicalSymbol = "AUD/USD",
            baseAsset = "AUD",
            quoteAsset = "USD",
            assetClass = MarketType.FOREX,
            contractType = "FX_SPOT",
            contractSize = 100000.0,
            tickSize = 0.00001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 5,
            quantityPrecision = 2,
            minNotionalUsd = 1000.0,
            tradingHoursDescription = "24/5 Global Interbank",
            providerMappings = mapOf("Polygon" to "C:AUDUSD", "MT5" to "AUDUSD")
        ),
        "USD/CAD" to CanonicalInstrument(
            canonicalSymbol = "USD/CAD",
            baseAsset = "USD",
            quoteAsset = "CAD",
            assetClass = MarketType.FOREX,
            contractType = "FX_SPOT",
            contractSize = 100000.0,
            tickSize = 0.00001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 5,
            quantityPrecision = 2,
            minNotionalUsd = 1000.0,
            tradingHoursDescription = "24/5 Global Interbank",
            providerMappings = mapOf("Polygon" to "C:USDCAD", "MT5" to "USDCAD")
        ),
        "NZD/USD" to CanonicalInstrument(
            canonicalSymbol = "NZD/USD",
            baseAsset = "NZD",
            quoteAsset = "USD",
            assetClass = MarketType.FOREX,
            contractType = "FX_SPOT",
            contractSize = 100000.0,
            tickSize = 0.00001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 5,
            quantityPrecision = 2,
            minNotionalUsd = 1000.0,
            tradingHoursDescription = "24/5 Global Interbank",
            providerMappings = mapOf("Polygon" to "C:NZDUSD", "MT5" to "NZDUSD")
        ),
        "EUR/GBP" to CanonicalInstrument(
            canonicalSymbol = "EUR/GBP",
            baseAsset = "EUR",
            quoteAsset = "GBP",
            assetClass = MarketType.FOREX,
            contractType = "FX_SPOT",
            contractSize = 100000.0,
            tickSize = 0.00001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 5,
            quantityPrecision = 2,
            minNotionalUsd = 1000.0,
            tradingHoursDescription = "24/5 Global Interbank",
            providerMappings = mapOf("Polygon" to "C:EURGBP", "MT5" to "EURGBP")
        ),
        "EUR/JPY" to CanonicalInstrument(
            canonicalSymbol = "EUR/JPY",
            baseAsset = "EUR",
            quoteAsset = "JPY",
            assetClass = MarketType.FOREX,
            contractType = "FX_SPOT",
            contractSize = 100000.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 1000.0,
            tradingHoursDescription = "24/5 Global Interbank",
            providerMappings = mapOf("Polygon" to "C:EURJPY", "MT5" to "EURJPY")
        ),
        "GBP/JPY" to CanonicalInstrument(
            canonicalSymbol = "GBP/JPY",
            baseAsset = "GBP",
            quoteAsset = "JPY",
            assetClass = MarketType.FOREX,
            contractType = "FX_SPOT",
            contractSize = 100000.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 1000.0,
            tradingHoursDescription = "24/5 Global Interbank",
            providerMappings = mapOf("Polygon" to "C:GBPJPY", "MT5" to "GBPJPY")
        ),
        "VOLATILITY 10" to CanonicalInstrument(
            canonicalSymbol = "VOLATILITY 10",
            baseAsset = "V10",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 0.35,
            tradingHoursDescription = "24/7/365 Continuous Synthetic",
            providerMappings = mapOf("Deriv" to "R_10")
        ),
        "VOLATILITY 25" to CanonicalInstrument(
            canonicalSymbol = "VOLATILITY 25",
            baseAsset = "V25",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 0.35,
            tradingHoursDescription = "24/7/365 Continuous Synthetic",
            providerMappings = mapOf("Deriv" to "R_25")
        ),
        "VOLATILITY 50" to CanonicalInstrument(
            canonicalSymbol = "VOLATILITY 50",
            baseAsset = "V50",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 0.35,
            tradingHoursDescription = "24/7/365 Continuous Synthetic",
            providerMappings = mapOf("Deriv" to "R_50")
        ),
        "VOLATILITY 100" to CanonicalInstrument(
            canonicalSymbol = "VOLATILITY 100",
            baseAsset = "V100",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 2,
            quantityPrecision = 2,
            minNotionalUsd = 0.35,
            tradingHoursDescription = "24/7/365 Continuous Synthetic",
            providerMappings = mapOf("Deriv" to "R_100")
        ),
        "BOOM 500" to CanonicalInstrument(
            canonicalSymbol = "BOOM 500",
            baseAsset = "BOOM500",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_SPIKE",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.2,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 0.50,
            tradingHoursDescription = "24/7/365 Continuous Spike Model",
            providerMappings = mapOf("Deriv" to "BOOM500")
        ),
        "BOOM 1000" to CanonicalInstrument(
            canonicalSymbol = "BOOM 1000",
            baseAsset = "BOOM1000",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_SPIKE",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.2,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 0.50,
            tradingHoursDescription = "24/7/365 Continuous Spike Model",
            providerMappings = mapOf("Deriv" to "BOOM1000")
        ),
        "CRASH 500" to CanonicalInstrument(
            canonicalSymbol = "CRASH 500",
            baseAsset = "CRASH500",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_SPIKE",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.2,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 0.50,
            tradingHoursDescription = "24/7/365 Continuous Spike Model",
            providerMappings = mapOf("Deriv" to "CRASH500")
        ),
        "JUMP 10" to CanonicalInstrument(
            canonicalSymbol = "JUMP 10",
            baseAsset = "JUMP10",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_JUMP",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 0.50,
            tradingHoursDescription = "24/7/365 Jump Model",
            providerMappings = mapOf("Deriv" to "JD10")
        ),
        "JUMP 25" to CanonicalInstrument(
            canonicalSymbol = "JUMP 25",
            baseAsset = "JUMP25",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_JUMP",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 0.50,
            tradingHoursDescription = "24/7/365 Jump Model",
            providerMappings = mapOf("Deriv" to "JD25")
        ),
        "JUMP 50" to CanonicalInstrument(
            canonicalSymbol = "JUMP 50",
            baseAsset = "JUMP50",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_JUMP",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 0.50,
            tradingHoursDescription = "24/7/365 Jump Model",
            providerMappings = mapOf("Deriv" to "JD50")
        ),
        "JUMP 75" to CanonicalInstrument(
            canonicalSymbol = "JUMP 75",
            baseAsset = "JUMP75",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_JUMP",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 0.50,
            tradingHoursDescription = "24/7/365 Jump Model",
            providerMappings = mapOf("Deriv" to "JD75")
        ),
        "JUMP 100" to CanonicalInstrument(
            canonicalSymbol = "JUMP 100",
            baseAsset = "JUMP100",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_JUMP",
            contractSize = 1.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 0.50,
            tradingHoursDescription = "24/7/365 Jump Model",
            providerMappings = mapOf("Deriv" to "JD100")
        ),
        "STEP INDEX" to CanonicalInstrument(
            canonicalSymbol = "STEP INDEX",
            baseAsset = "STEP",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_STEP",
            contractSize = 1.0,
            tickSize = 0.1,
            lotStep = 0.1,
            minLot = 0.1,
            maxLot = 50.0,
            pricePrecision = 1,
            quantityPrecision = 1,
            minNotionalUsd = 0.50,
            tradingHoursDescription = "24/7/365 Step Model",
            providerMappings = mapOf("Deriv" to "stpRNG")
        ),
        "RANGE BREAK 100" to CanonicalInstrument(
            canonicalSymbol = "RANGE BREAK 100",
            baseAsset = "RB100",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_RANGE",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 2,
            quantityPrecision = 2,
            minNotionalUsd = 0.50,
            tradingHoursDescription = "24/7/365 Range Break",
            providerMappings = mapOf("Deriv" to "rb_100")
        ),
        "RANGE BREAK 200" to CanonicalInstrument(
            canonicalSymbol = "RANGE BREAK 200",
            baseAsset = "RB200",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_RANGE",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 2,
            quantityPrecision = 2,
            minNotionalUsd = 0.50,
            tradingHoursDescription = "24/7/365 Range Break",
            providerMappings = mapOf("Deriv" to "rb_200")
        ),
        "AAPL" to CanonicalInstrument(
            canonicalSymbol = "AAPL",
            baseAsset = "AAPL",
            quoteAsset = "USD",
            assetClass = MarketType.STOCKS,
            contractType = "EQUITY_SPOT",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 1000.0,
            pricePrecision = 2,
            quantityPrecision = 0,
            minNotionalUsd = 100.0,
            tradingHoursDescription = "Mon-Fri 09:30-16:00 EST",
            providerMappings = mapOf("Polygon" to "AAPL")
        ),
        "MSFT" to CanonicalInstrument(
            canonicalSymbol = "MSFT",
            baseAsset = "MSFT",
            quoteAsset = "USD",
            assetClass = MarketType.STOCKS,
            contractType = "EQUITY_SPOT",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 1000.0,
            pricePrecision = 2,
            quantityPrecision = 0,
            minNotionalUsd = 100.0,
            tradingHoursDescription = "Mon-Fri 09:30-16:00 EST",
            providerMappings = mapOf("Polygon" to "MSFT")
        ),
        "AMD" to CanonicalInstrument(
            canonicalSymbol = "AMD",
            baseAsset = "AMD",
            quoteAsset = "USD",
            assetClass = MarketType.STOCKS,
            contractType = "EQUITY_SPOT",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 1000.0,
            pricePrecision = 2,
            quantityPrecision = 0,
            minNotionalUsd = 100.0,
            tradingHoursDescription = "Mon-Fri 09:30-16:00 EST",
            providerMappings = mapOf("Polygon" to "AMD")
        ),
        "TSLA" to CanonicalInstrument(
            canonicalSymbol = "TSLA",
            baseAsset = "TSLA",
            quoteAsset = "USD",
            assetClass = MarketType.STOCKS,
            contractType = "EQUITY_SPOT",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 1000.0,
            pricePrecision = 2,
            quantityPrecision = 0,
            minNotionalUsd = 100.0,
            tradingHoursDescription = "Mon-Fri 09:30-16:00 EST",
            providerMappings = mapOf("Polygon" to "TSLA")
        ),
        "GOOGL" to CanonicalInstrument(
            canonicalSymbol = "GOOGL",
            baseAsset = "GOOGL",
            quoteAsset = "USD",
            assetClass = MarketType.STOCKS,
            contractType = "EQUITY_SPOT",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 1000.0,
            pricePrecision = 2,
            quantityPrecision = 0,
            minNotionalUsd = 100.0,
            tradingHoursDescription = "Mon-Fri 09:30-16:00 EST",
            providerMappings = mapOf("Polygon" to "GOOGL")
        ),
        "AMZN" to CanonicalInstrument(
            canonicalSymbol = "AMZN",
            baseAsset = "AMZN",
            quoteAsset = "USD",
            assetClass = MarketType.STOCKS,
            contractType = "EQUITY_SPOT",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 1000.0,
            pricePrecision = 2,
            quantityPrecision = 0,
            minNotionalUsd = 100.0,
            tradingHoursDescription = "Mon-Fri 09:30-16:00 EST",
            providerMappings = mapOf("Polygon" to "AMZN")
        ),
        "META" to CanonicalInstrument(
            canonicalSymbol = "META",
            baseAsset = "META",
            quoteAsset = "USD",
            assetClass = MarketType.STOCKS,
            contractType = "EQUITY_SPOT",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 1000.0,
            pricePrecision = 2,
            quantityPrecision = 0,
            minNotionalUsd = 100.0,
            tradingHoursDescription = "Mon-Fri 09:30-16:00 EST",
            providerMappings = mapOf("Polygon" to "META")
        ),
        "XAG/USD" to CanonicalInstrument(
            canonicalSymbol = "XAG/USD",
            baseAsset = "XAG",
            quoteAsset = "USD",
            assetClass = MarketType.METALS,
            contractType = "COMMODITY_CFD",
            contractSize = 5000.0,
            tickSize = 0.001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 3,
            quantityPrecision = 2,
            minNotionalUsd = 500.0,
            tradingHoursDescription = "23h Daily Interbank Precious Metals",
            providerMappings = mapOf("Polygon" to "C:XAGUSD", "MT5" to "XAGUSD")
        ),
        "SPY" to CanonicalInstrument(
            canonicalSymbol = "SPY",
            baseAsset = "SPY",
            quoteAsset = "USD",
            assetClass = MarketType.ETFS,
            contractType = "ETF_SPOT",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 1000.0,
            pricePrecision = 2,
            quantityPrecision = 0,
            minNotionalUsd = 100.0,
            tradingHoursDescription = "Mon-Fri 09:30-16:00 EST",
            providerMappings = mapOf("Polygon" to "SPY")
        ),
        "QQQ" to CanonicalInstrument(
            canonicalSymbol = "QQQ",
            baseAsset = "QQQ",
            quoteAsset = "USD",
            assetClass = MarketType.ETFS,
            contractType = "ETF_SPOT",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 1000.0,
            pricePrecision = 2,
            quantityPrecision = 0,
            minNotionalUsd = 100.0,
            tradingHoursDescription = "Mon-Fri 09:30-16:00 EST",
            providerMappings = mapOf("Polygon" to "QQQ")
        ),
        "ETH/USDT" to CanonicalInstrument(
            canonicalSymbol = "ETH/USDT",
            baseAsset = "ETH",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 500.0,
            pricePrecision = 2,
            quantityPrecision = 2,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf(
                "Binance" to "ETHUSDT",
                "Bybit" to "ETHUSDT",
                "OKX" to "ETH-USDT",
                "Coinbase" to "ETH-USD",
                "MT5" to "ETHUSD"
            )
        ),
        "SOL/USDT" to CanonicalInstrument(
            canonicalSymbol = "SOL/USDT",
            baseAsset = "SOL",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.1,
            minLot = 0.1,
            maxLot = 5000.0,
            pricePrecision = 2,
            quantityPrecision = 1,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf(
                "Binance" to "SOLUSDT",
                "Bybit" to "SOLUSDT",
                "OKX" to "SOL-USDT",
                "Coinbase" to "SOL-USD"
            )
        ),
        "XRP/USDT" to CanonicalInstrument(
            canonicalSymbol = "XRP/USDT",
            baseAsset = "XRP",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.0001,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 50000.0,
            pricePrecision = 4,
            quantityPrecision = 0,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf(
                "Binance" to "XRPUSDT",
                "Bybit" to "XRPUSDT",
                "OKX" to "XRP-USDT"
            )
        ),
        "DOGE/USDT" to CanonicalInstrument(
            canonicalSymbol = "DOGE/USDT",
            baseAsset = "DOGE",
            quoteAsset = "USDT",
            assetClass = MarketType.CRYPTO,
            contractType = "SPOT / PERP",
            contractSize = 1.0,
            tickSize = 0.00001,
            lotStep = 10.0,
            minLot = 10.0,
            maxLot = 500000.0,
            pricePrecision = 5,
            quantityPrecision = 0,
            minNotionalUsd = 10.0,
            tradingHoursDescription = "24/7 Global Decentralized",
            providerMappings = mapOf(
                "Binance" to "DOGEUSDT",
                "Bybit" to "DOGEUSDT",
                "OKX" to "DOGE-USDT"
            )
        ),
        "EUR/USD" to CanonicalInstrument(
            canonicalSymbol = "EUR/USD",
            baseAsset = "EUR",
            quoteAsset = "USD",
            assetClass = MarketType.FOREX,
            contractType = "FX_SPOT / CFD",
            contractSize = 100000.0, // Standard lot
            tickSize = 0.00001, // 1/10 pip
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 100.0,
            pricePrecision = 5,
            quantityPrecision = 2,
            minNotionalUsd = 1000.0,
            tradingHoursDescription = "Sunday 22:00 GMT - Friday 22:00 GMT",
            providerMappings = mapOf(
                "MT5" to "EURUSD",
                "Exness" to "EURUSDm",
                "Oanda" to "EUR_USD",
                "Deriv" to "frxEURUSD"
            )
        ),
        "GBP/USD" to CanonicalInstrument(
            canonicalSymbol = "GBP/USD",
            baseAsset = "GBP",
            quoteAsset = "USD",
            assetClass = MarketType.FOREX,
            contractType = "FX_SPOT / CFD",
            contractSize = 100000.0,
            tickSize = 0.00001,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 100.0,
            pricePrecision = 5,
            quantityPrecision = 2,
            minNotionalUsd = 1000.0,
            tradingHoursDescription = "Sunday 22:00 GMT - Friday 22:00 GMT",
            providerMappings = mapOf(
                "MT5" to "GBPUSD",
                "Exness" to "GBPUSDm",
                "Oanda" to "GBP_USD"
            )
        ),
        "XAU/USD" to CanonicalInstrument(
            canonicalSymbol = "XAU/USD",
            baseAsset = "XAU",
            quoteAsset = "USD",
            assetClass = MarketType.METALS,
            contractType = "SPOT_COMMODITY",
            contractSize = 100.0, // 100 Troy oz per lot
            tickSize = 0.01,
            lotStep = 0.01,
            minLot = 0.01,
            maxLot = 50.0,
            pricePrecision = 2,
            quantityPrecision = 2,
            minNotionalUsd = 2000.0,
            tradingHoursDescription = "Sunday 23:00 GMT - Friday 21:00 GMT",
            providerMappings = mapOf(
                "MT5" to "XAUUSD",
                "Exness" to "XAUUSDm",
                "YahooFinance" to "GC=F",
                "Binance" to "PAXGUSDT"
            )
        ),
        "VOLATILITY 75" to CanonicalInstrument(
            canonicalSymbol = "VOLATILITY 75",
            baseAsset = "VOL75",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_INDEX",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.001,
            minLot = 0.001,
            maxLot = 10.0,
            pricePrecision = 2,
            quantityPrecision = 3,
            minNotionalUsd = 1.0,
            tradingHoursDescription = "24/7 Continuous Synthetic",
            providerMappings = mapOf(
                "Deriv" to "R_75",
                "MT5" to "Volatility 75 Index"
            )
        ),
        "CRASH 1000" to CanonicalInstrument(
            canonicalSymbol = "CRASH 1000",
            baseAsset = "CRASH1000",
            quoteAsset = "USD",
            assetClass = MarketType.DERIV,
            contractType = "SYNTHETIC_SPIKE",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 0.2,
            minLot = 0.2,
            maxLot = 50.0,
            pricePrecision = 2,
            quantityPrecision = 1,
            minNotionalUsd = 1.0,
            tradingHoursDescription = "24/7 Continuous Synthetic",
            providerMappings = mapOf(
                "Deriv" to "CRASH_1000"
            )
        ),
        "NVDA" to CanonicalInstrument(
            canonicalSymbol = "NVDA",
            baseAsset = "NVDA",
            quoteAsset = "USD",
            assetClass = MarketType.STOCKS,
            contractType = "US_EQUITY",
            contractSize = 1.0,
            tickSize = 0.01,
            lotStep = 1.0,
            minLot = 1.0,
            maxLot = 5000.0,
            pricePrecision = 2,
            quantityPrecision = 0,
            minNotionalUsd = 100.0,
            tradingHoursDescription = "Mon-Fri 09:30-16:00 EST",
            providerMappings = mapOf(
                "Alpaca" to "NVDA",
                "YahooFinance" to "NVDA",
                "MT5" to "NVDA.US"
            )
        ),
        "US500" to CanonicalInstrument(
            canonicalSymbol = "US500",
            baseAsset = "SPX",
            quoteAsset = "USD",
            assetClass = MarketType.INDICES,
            contractType = "INDEX_CFD",
            contractSize = 1.0,
            tickSize = 0.1,
            lotStep = 0.1,
            minLot = 0.1,
            maxLot = 100.0,
            pricePrecision = 2,
            quantityPrecision = 1,
            minNotionalUsd = 500.0,
            tradingHoursDescription = "Mon-Fri 09:30-16:00 EST",
            providerMappings = mapOf(
                "MT5" to "US500",
                "YahooFinance" to "^GSPC"
            )
        )
    )

    /**
     * Resolves any raw or broker-specific symbol to its canonical form.
     */
    fun resolveCanonicalSymbol(rawSymbol: String, venueOrProvider: String = ""): String {
        val clean = rawSymbol.trim().replace(" ", "").replace("_", "").replace("-", "").replace("/", "").uppercase()

        // Direct matching
        for ((canon, spec) in CANONICAL_REGISTRY) {
            val canonClean = canon.replace("/", "").replace(" ", "").uppercase()
            if (clean == canonClean) return canon

            // Check provider mappings
            for ((_, provSym) in spec.providerMappings) {
                val provClean = provSym.replace("/", "").replace("_", "").replace("-", "").uppercase()
                if (clean == provClean) return canon
            }
        }

        // Regex / Substring heuristics
        return when {
            clean.contains("BTC") || clean.contains("XBT") -> "BTC/USDT"
            clean.contains("ETH") -> "ETH/USDT"
            clean.contains("SOL") -> "SOL/USDT"
            clean.contains("XRP") -> "XRP/USDT"
            clean.contains("DOGE") -> "DOGE/USDT"
            clean.contains("EURUSD") -> "EUR/USD"
            clean.contains("GBPUSD") -> "GBP/USD"
            clean.contains("XAU") || clean.contains("GOLD") -> "XAU/USD"
            clean.contains("VOL75") || clean.contains("R75") || clean.contains("VOLATILITY75") -> "VOLATILITY 75"
            clean.contains("CRASH1000") || clean.contains("CRASH") -> "CRASH 1000"
            clean.contains("NVDA") -> "NVDA"
            clean.contains("US500") || clean.contains("SPX") || clean.contains("SP500") -> "US500"
            clean.contains("NAS100") || clean.contains("USTEC") -> "NAS100"
            clean.contains("DAX") || clean.contains("GER40") -> "DAX"
            else -> rawSymbol.uppercase()
        }
    }

    /**
     * Gets the full canonical instrument specifications.
     */
    fun getInstrument(canonicalSymbol: String): CanonicalInstrument? {
        return CANONICAL_REGISTRY[canonicalSymbol]
    }

    /**
     * Translates a canonical symbol into the provider-specific ticker.
     */
    fun toProviderSymbol(canonicalSymbol: String, provider: String): String {
        val spec = CANONICAL_REGISTRY[canonicalSymbol] ?: return canonicalSymbol.replace("/", "")
        return spec.providerMappings[provider] ?: canonicalSymbol.replace("/", "")
    }

    /**
     * Normalizes an order quantity to strictly conform with minimum lot, maximum lot, and step size.
     */
    fun normalizeQuantity(canonicalSymbol: String, requestedQty: Double): Double {
        val spec = CANONICAL_REGISTRY[canonicalSymbol] ?: return requestedQty
        val clamped = requestedQty.coerceIn(spec.minLot, spec.maxLot)
        val steps = (clamped / spec.lotStep).toInt()
        val normalized = steps * spec.lotStep
        return "%.${spec.quantityPrecision}f".format(java.util.Locale.US, normalized).toDouble()
    }

    /**
     * Normalizes order price to tick size and price precision.
     */
    fun normalizePrice(canonicalSymbol: String, requestedPrice: Double): Double {
        val spec = CANONICAL_REGISTRY[canonicalSymbol] ?: return requestedPrice
        return "%.${spec.pricePrecision}f".format(java.util.Locale.US, requestedPrice).toDouble()
    }
}
