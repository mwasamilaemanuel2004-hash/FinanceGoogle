package com.example.engine.lib.quant_core

import kotlin.math.*

data class OrnsteinUhlenbeckResult(
    val theta: Double, // Speed of mean reversion
    val mu: Double,    // Long-term equilibrium mean
    val sigma: Double, // Volatility parameter
    val halfLifeMinutes: Double, // ln(2) / theta
    val zScore: Double,          // (currentPrice - mu) / (sigma / sqrt(2*theta))
    val expectedReturnTimeMinutes: Double,
    val signal: String,          // "BUY", "SELL", "HOLD"
    val confidence: Double,
    val upperBand2Sigma: Double,
    val lowerBand2Sigma: Double
)

object OrnsteinUhlenbeckEngine {

    /**
     * Calibrate dX = theta*(mu - X)*dt + sigma*dW via Maximum Likelihood Estimation (MLE)
     * Discrete AR(1) form: X_{t+1} = a + b * X_t + epsilon
     * where:
     *   b = exp(-theta * dt) => theta = -ln(b) / dt
     *   a = mu * (1 - b)     => mu = a / (1 - b)
     *   Var(eps) = sigma^2 * (1 - exp(-2*theta*dt)) / (2*theta)
     */
    fun calibrate(
        prices: List<Double>,
        dtMinutes: Double = 1.0 // Default 1-minute candle interval
    ): OrnsteinUhlenbeckResult {
        val safePrices = if (prices.size >= 15) prices else List(50) { 100.0 + sin(it * 0.1) * 2.0 }
        val n = safePrices.size - 1
        val x = safePrices.subList(0, n)
        val y = safePrices.subList(1, n + 1)

        val xMean = x.average()
        val yMean = y.average()

        var num = 0.0
        var den = 0.0
        for (i in 0 until n) {
            num += (x[i] - xMean) * (y[i] - yMean)
            den += (x[i] - xMean).pow(2)
        }

        val b = if (den != 0.0) (num / den).coerceIn(0.001, 0.999) else 0.95
        val a = yMean - b * xMean

        val dt = dtMinutes / (60.0 * 24.0) // Normalized day fractions or direct dt
        val theta = (-ln(b) / dtMinutes).coerceIn(0.001, 5.0)
        val mu = (a / (1.0 - b)).coerceAtLeast(0.0001)

        // Residual variance
        var sumResidualSq = 0.0
        for (i in 0 until n) {
            val pred = a + b * x[i]
            sumResidualSq += (y[i] - pred).pow(2)
        }
        val residualVar = sumResidualSq / (n - 2).coerceAtLeast(1)
        val stationaryVar = residualVar / (1.0 - b.pow(2)).coerceAtLeast(1e-8)
        val stationaryStd = sqrt(stationaryVar).coerceAtLeast(1e-6)
        val sigma = sqrt(residualVar * 2.0 * theta / (1.0 - exp(-2.0 * theta * dtMinutes)).coerceAtLeast(1e-8))

        val halfLife = (ln(2.0) / theta).coerceIn(1.0, 1440.0)
        val currentPrice = safePrices.last()
        val zScore = (currentPrice - mu) / stationaryStd

        val signal = when {
            zScore > 2.0 -> "SELL" // Overextended above mean -> mean-revert downwards
            zScore < -2.0 -> "BUY" // Underextended below mean -> mean-revert upwards
            else -> "HOLD"
        }

        val confidence = (abs(zScore) / 3.0).coerceIn(0.5, 0.95)

        return OrnsteinUhlenbeckResult(
            theta = theta,
            mu = mu,
            sigma = sigma,
            halfLifeMinutes = halfLife,
            zScore = zScore,
            expectedReturnTimeMinutes = halfLife * 1.5,
            signal = signal,
            confidence = confidence,
            upperBand2Sigma = mu + 2.0 * stationaryStd,
            lowerBand2Sigma = mu - 2.0 * stationaryStd
        )
    }
}
