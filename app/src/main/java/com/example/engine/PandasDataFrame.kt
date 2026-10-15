package com.example.engine

import com.example.data.model.PriceCandle
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A native Pandas-like Quantitative Time-Series DataFrame in Kotlin.
 * Provides rolling statistics, EWMA, Bollinger Bands, RSI, MACD, ATR, VWAP,
 * and Smart Money ICT concepts (FVG & Order Blocks).
 */
data class DataPoint(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

class PandasDataFrame(candles: List<PriceCandle>) {
    val rows: List<DataPoint> = candles.map {
        DataPoint(it.timestamp, it.open, it.high, it.low, it.close, it.volume)
    }

    val size: Int get() = rows.size
    val closeSeries: List<Double> get() = rows.map { it.close }
    val volumeSeries: List<Double> get() = rows.map { it.volume }

    /**
     * Rolling Window calculation (similar to df['close'].rolling(window))
     */
    inner class RollingWindow(private val window: Int) {
        fun mean(): List<Double> {
            val result = mutableListOf<Double>()
            for (i in rows.indices) {
                if (i < window - 1) {
                    val sub = rows.subList(0, i + 1).map { it.close }
                    result.add(sub.average())
                } else {
                    val sub = rows.subList(i - window + 1, i + 1).map { it.close }
                    result.add(sub.average())
                }
            }
            return result
        }

        fun std(): List<Double> {
            val means = mean()
            val result = mutableListOf<Double>()
            for (i in rows.indices) {
                val start = maxOf(0, i - window + 1)
                val sub = rows.subList(start, i + 1).map { it.close }
                val m = means[i]
                val variance = sub.map { (it - m).pow(2) }.average()
                result.add(sqrt(variance))
            }
            return result
        }
    }

    fun rolling(window: Int): RollingWindow = RollingWindow(window)

    /**
     * Exponential Weighted Moving Average (similar to df['close'].ewm(span=span).mean())
     */
    fun ewm(span: Int): List<Double> {
        val result = mutableListOf<Double>()
        if (rows.isEmpty()) return result
        val alpha = 2.0 / (span + 1.0)
        var lastEma = rows.first().close

        for (row in rows) {
            lastEma = alpha * row.close + (1.0 - alpha) * lastEma
            result.add(lastEma)
        }
        return result
    }

    /**
     * Relative Strength Index (RSI 14)
     */
    fun rsi(period: Int = 14): Double {
        if (rows.size < period + 1) return 50.0
        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val change = rows[i].close - rows[i - 1].close
            if (change > 0) gains += change else losses += abs(change)
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        for (i in (period + 1) until rows.size) {
            val change = rows[i].close - rows[i - 1].close
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) abs(change) else 0.0
            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    /**
     * Moving Average Convergence Divergence (MACD 12, 26, 9)
     */
    data class MacdResult(
        val macdLine: Double,
        val signalLine: Double,
        val histogram: Double,
        val isBullishCrossover: Boolean
    )

    fun macd(fast: Int = 12, slow: Int = 26, signalPeriod: Int = 9): MacdResult {
        if (rows.size < slow) {
            return MacdResult(0.0, 0.0, 0.0, false)
        }
        val emaFast = ewm(fast)
        val emaSlow = ewm(slow)
        val macdSeries = emaFast.zip(emaSlow) { f, s -> f - s }

        // Compute signal line as EMA of macdSeries
        val alpha = 2.0 / (signalPeriod + 1.0)
        var sigEma = macdSeries.firstOrNull() ?: 0.0
        for (m in macdSeries) {
            sigEma = alpha * m + (1.0 - alpha) * sigEma
        }

        val currentMacd = macdSeries.lastOrNull() ?: 0.0
        val prevMacd = if (macdSeries.size >= 2) macdSeries[macdSeries.size - 2] else currentMacd
        val hist = currentMacd - sigEma
        val isCross = prevMacd <= sigEma && currentMacd > sigEma

        return MacdResult(currentMacd, sigEma, hist, isCross)
    }

    /**
     * Bollinger Bands (20, 2.0)
     */
    data class BollingerBandsResult(
        val upper: Double,
        val middle: Double,
        val lower: Double,
        val bandwidthPct: Double,
        val percentB: Double
    )

    fun bollingerBands(period: Int = 20, multiplier: Double = 2.0): BollingerBandsResult {
        if (rows.isEmpty()) return BollingerBandsResult(0.0, 0.0, 0.0, 0.0, 0.5)
        val roll = rolling(period)
        val sma = roll.mean().lastOrNull() ?: rows.last().close
        val std = roll.std().lastOrNull() ?: (sma * 0.01)

        val upper = sma + (std * multiplier)
        val lower = sma - (std * multiplier)
        val lastClose = rows.last().close
        val bandRange = if (upper > lower) upper - lower else 1.0
        val bandwidthPct = if (sma > 0) (bandRange / sma) * 100.0 else 0.0
        val percentB = (lastClose - lower) / bandRange

        return BollingerBandsResult(upper, sma, lower, bandwidthPct, percentB)
    }

    /**
     * Average True Range (ATR 14)
     */
    fun atr(period: Int = 14): Double {
        if (rows.size < 2) return rows.lastOrNull()?.close?.times(0.01) ?: 1.0
        val trList = mutableListOf<Double>()
        for (i in 1 until rows.size) {
            val curr = rows[i]
            val prev = rows[i - 1]
            val hl = curr.high - curr.low
            val hcp = abs(curr.high - prev.close)
            val lcp = abs(curr.low - prev.close)
            val tr = maxOf(hl, maxOf(hcp, lcp))
            trList.add(tr)
        }

        if (trList.size <= period) return trList.average()
        var currentAtr = trList.take(period).average()
        for (i in period until trList.size) {
            currentAtr = (currentAtr * (period - 1) + trList[i]) / period
        }
        return currentAtr
    }

    /**
     * Volume Weighted Average Price (VWAP)
     */
    fun vwap(): Double {
        if (rows.isEmpty()) return 0.0
        var cumulativeTpVol = 0.0
        var cumulativeVol = 0.0
        for (r in rows) {
            val typicalPrice = (r.high + r.low + r.close) / 3.0
            cumulativeTpVol += typicalPrice * r.volume
            cumulativeVol += r.volume
        }
        return if (cumulativeVol > 0) cumulativeTpVol / cumulativeVol else rows.last().close
    }

    /**
     * ICT Smart Money Concepts: Fair Value Gap (FVG) & Order Block Detection
     */
    data class IctSetupResult(
        val hasBullishFvg: Boolean,
        val hasBearishFvg: Boolean,
        val fvgPriceLevel: Double,
        val orderBlockLevel: Double,
        val bias: String
    )

    fun ictSmartMoneyAnalysis(): IctSetupResult {
        if (rows.size < 4) return IctSetupResult(false, false, 0.0, 0.0, "NEUTRAL")

        // 3-candle FVG detection: Candle 1 High < Candle 3 Low (Bullish FVG)
        val c1 = rows[rows.size - 3]
        val c2 = rows[rows.size - 2]
        val c3 = rows[rows.size - 1]

        val isBullishFvg = c1.high < c3.low
        val isBearishFvg = c1.low > c3.high
        val fvgLevel = if (isBullishFvg) (c1.high + c3.low) / 2.0 else (c1.low + c3.high) / 2.0
        val obLevel = if (isBullishFvg) c2.low else c2.high

        val bias = when {
            isBullishFvg -> "BULLISH DISPLACEMENT (FVG)"
            isBearishFvg -> "BEARISH DISPLACEMENT (FVG)"
            c3.close > vwap() -> "ABOVE VWAP INSTITUTIONAL ACCUMULATION"
            else -> "BELOW VWAP DISTRIBUTION"
        }

        return IctSetupResult(isBullishFvg, isBearishFvg, fvgLevel, obLevel, bias)
    }

    /**
     * Statistical Summary (Pandas df.describe())
     */
    data class DescribeStats(
        val count: Int,
        val mean: Double,
        val std: Double,
        val min: Double,
        val max: Double,
        val sharpeRatio: Double
    )

    fun describe(): DescribeStats {
        if (rows.isEmpty()) return DescribeStats(0, 0.0, 0.0, 0.0, 0.0, 0.0)
        val closes = closeSeries
        val mean = closes.average()
        val variance = closes.map { (it - mean).pow(2) }.average()
        val std = sqrt(variance)
        val min = closes.minOrNull() ?: 0.0
        val max = closes.maxOrNull() ?: 0.0

        // Daily returns for annualized Sharpe ratio calculation (assuming 2% risk-free rate)
        val returns = mutableListOf<Double>()
        for (i in 1 until closes.size) {
            if (closes[i - 1] > 0) {
                returns.add((closes[i] - closes[i - 1]) / closes[i - 1])
            }
        }
        val avgReturn = returns.average()
        val returnStd = if (returns.size > 1) sqrt(returns.map { (it - avgReturn).pow(2) }.average()) else 0.001
        val sharpe = if (returnStd > 0) (avgReturn - (0.02 / 365.0)) / returnStd * sqrt(252.0) else 1.5

        return DescribeStats(rows.size, mean, std, min, max, sharpe)
    }
}
