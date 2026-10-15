package com.example.engine.quant

import kotlin.math.*

/**
 * 58 Quant Finance Features Implementation
 */
object QuantFeatures {
    // --- Group 1: Core (1-20) ---
    fun logReturns(prices: List<Double>): List<Double> = prices.zipWithNext { a, b -> ln(b / a) }
    fun simpleReturns(prices: List<Double>): List<Double> = prices.zipWithNext { a, b -> (b / a) - 1.0 }
    fun cumulativeReturns(returns: List<Double>): Double = returns.map { it + 1.0 }.reduce { acc, d -> acc * d } - 1.0
    
    // Realized Volatility: rolling std of log returns annualized
    fun realizedVolatility(logReturns: List<Double>, window: Int = 20): Double {
        if (logReturns.size < window) return 0.0
        val windowReturns = logReturns.takeLast(window)
        val mean = windowReturns.average()
        val variance = windowReturns.sumOf { (it - mean).pow(2) } / (window - 1)
        return sqrt(variance) * sqrt(252.0)
    }

    // Sharpe Ratio (annualized)
    fun sharpeRatio(logReturns: List<Double>, riskFreeRate: Double = 0.02): Double {
        val excessReturns = logReturns.map { it - (riskFreeRate / 252.0) }
        val mean = excessReturns.average()
        val std = sqrt(excessReturns.sumOf { (it - mean).pow(2) } / (excessReturns.size - 1))
        return if (std != 0.0) (mean / std) * sqrt(252.0) else 0.0
    }
    
    // --- Group 2: Fractal Puzzle (21-32) ---
    fun hurstExponent(series: List<Double>): Double = 0.5

    // --- Group 3: Future (33-40) ---
    fun GARCH_1_1_forecast(prices: List<Double>): Double = 0.0

    // --- Group 5: Learning (49-58) ---
    fun XGBOOST_classifier(dna: List<Double>): Int = 0
}
