package com.example.engine.lib.tools

/**
 * Indicator Kit: Unified Interface for All Quant Indicators
 * Used for production-grade strategy calculation and signaling.
 */
object IndicatorKit {
    
    // Technical Indicators
    val RSI = TaLibReal::rsi
    val EMA = TaLibReal::ema
    val MACD = TaLibReal::macd
    val BollingerBands = TaLibReal::bollingerBands
    val ATR = TaLibReal::atr
    val VWAP = TaLibReal::vwap
    val ADX = TaLibReal::adx
    val CVD = TaLibReal::cvd
    
    // Order Flow
    val OrderFlowImbalance = TaLibReal::orderFlowImbalance
    
    // Advanced Analytics
    val ConfluenceEvaluator = TaLibReal::evaluateScalp10Confluence
    
    fun getIndicatorNames() = listOf(
        "RSI", "EMA", "MACD", "Bollinger Bands", "ATR", "VWAP", "ADX", "CVD", "OrderFlow"
    )
}

/**
 * Strategy Kit: Aggregates pre-defined Quant Strategies for easy production access
 */
object StrategyKit {

    fun getTop5Strategies(
        symbol: String,
        price: Double,
        candles: List<Candle>,
        targetProfitPct: Double,
        stopLossPct: Double
    ) = TaLibReal.evaluateTop5Strategies(symbol, price, candles, targetProfitPct, stopLossPct)
    
    fun getStrategyNames() = listOf(
        "Scalping 0.01% Zone",
        "Mean Reversion (Bollinger + RSI)",
        "Trend Momentum (EMA Ribbon + ADX)",
        "Funding Rate Arbitrage",
        "Breakout Liquidity Sweep"
    )
}
