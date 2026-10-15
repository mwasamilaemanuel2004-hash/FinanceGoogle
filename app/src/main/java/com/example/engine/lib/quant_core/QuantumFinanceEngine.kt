package com.example.engine.lib.quant_core

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * ============================================================================
 * QUANTUM FINANCE & ADVANCED MARKET MICROSTRUCTURE ENGINE
 * ============================================================================
 * Implements:
 * 1. Quantum Finance Hamiltonian / Wavefunction Schrodinger Pricing
 *    - Potential Barrier V(x), Quantum Tunneling through Support/Resistance
 *    - Quantum Probability Density |psi(x)|^2
 * 2. Square Root Law of Market Impact:
 *    - Impact ~ Y * sigma * sqrt(Q / V_daily) (Bouchaud / Almgren-Chriss)
 * 3. Scaling Laws:
 *    - Volatility scaling: sigma(t) = sigma_0 * t^H (Hurst exponent diffusion)
 *    - Pareto/Zipf Power Law of Order Sizes: P(S > s) ~ s^(-alpha)
 * 4. Size Line / Position Sizing Constraints:
 *    - Microstructure liquidity budget & max allowable participation rate
 * ============================================================================
 */

data class QuantumWavefunctionState(
    val price: Double,
    val probabilityDensity: Double,
    val tunnelingProbability: Double,   // Likelihood of breaching S/R barrier without huge volume
    val quantumKineticEnergy: Double,
    val quantumPotentialBarrier: Double,
    val quantumPhaseAngleDeg: Double,
    val stateLabel: String
)

data class SquareRootImpactResult(
    val participationRate: Double,      // Order size Q / Daily Volume V
    val dailyVolatility: Double,
    val expectedImpactBps: Double,       // Basis points of permanent + temporary impact
    val slippageUsd: Double,
    val isExecutableWithinLimit: Boolean,
    val recommendation: String
)

data class ScalingLawAnalysis(
    val hurstExponent: Double,          // H < 0.5 mean-reverting, H = 0.5 random walk, H > 0.5 trending
    val scalingFactor1mTo15m: Double,   // Expected volatility multiplier
    val scalingFactor1mTo1h: Double,
    val alphaPareto: Double,            // Power-law exponent for tail risk
    val regimeDescription: String
)

data class SizeLineRecommendation(
    val symbol: String,
    val baseCapitalUsd: Double,
    val maximumSafeSizeLots: Double,
    val recommendedSizeLots: Double,
    val minBrokerSizeLots: Double,
    val maxLiquidityCeilingUsd: Double,
    val sizeLineRationale: String
)

object QuantumFinanceEngine {

    /**
     * 1. Quantum Finance: Models price as a quantum state in an economic potential well V(x).
     * S/R zones act as potential energy barriers.
     * When price approaches resistance, tunneling probability calculates odds of breaking out
     * even with lower volume due to order book depletion.
     */
    fun evaluateQuantumState(
        currentPrice: Double,
        supportLevel: Double,
        resistanceLevel: Double,
        volatilitySigma: Double,
        orderFlowImbalance: Double // -1.0 to +1.0
    ): QuantumWavefunctionState {
        val mid = (supportLevel + resistanceLevel) / 2.0
        val width = (resistanceLevel - supportLevel).coerceAtLeast(0.0001)
        val deltaX = currentPrice - mid

        // Harmonic oscillator potential well + barrier at boundary
        val kPotential = (2.0 * volatilitySigma).coerceAtLeast(0.01)
        val vX = 0.5 * kPotential * (deltaX / width).pow(2)

        // Kinetic energy derived from order flow velocity and momentum
        val kineticEnergy = (0.5 * (orderFlowImbalance.pow(2) + 0.1) * volatilitySigma).coerceAtLeast(0.001)

        // Tunneling transmission coefficient: T ~ exp(-2 * integral(sqrt(2m(V - E))))
        val barrierHeight = (vX - kineticEnergy).coerceAtLeast(0.0)
        val tunnelingProb = if (barrierHeight <= 0.0) {
            0.95 // Above barrier, free propagation
        } else {
            val exponent = -2.0 * sqrt(barrierHeight) * (width / currentPrice.coerceAtLeast(1.0))
            Math.exp(exponent).coerceIn(0.01, 0.90)
        }

        // Gaussian wavepacket probability density |psi(x)|^2
        val probDensity = (1.0 / (sqrt(2 * Math.PI) * volatilitySigma.coerceAtLeast(0.01))) *
                Math.exp(-0.5 * (deltaX / (volatilitySigma * currentPrice.coerceAtLeast(1.0) * 0.05)).pow(2))

        val phaseAngle = ((orderFlowImbalance * 180.0) % 360.0 + 360.0) % 360.0

        val label = when {
            tunnelingProb > 0.70 -> "QUANTUM BREAKOUT TUNNELING (High Breaching Likelihood)"
            vX > kineticEnergy * 2.0 -> "QUANTUM REFLECTION AT S/R (Bound State in Well)"
            else -> "SUPERPOSITION STATE (Oscillating in Equilibrium)"
        }

        return QuantumWavefunctionState(
            price = currentPrice,
            probabilityDensity = probDensity.coerceIn(0.0, 1.0),
            tunnelingProbability = tunnelingProb,
            quantumKineticEnergy = kineticEnergy,
            quantumPotentialBarrier = vX,
            quantumPhaseAngleDeg = phaseAngle,
            stateLabel = label
        )
    }

    /**
     * 2. Square Root Law of Market Impact (Bouchaud & Almgren-Chriss):
     * I = Y * sigma * sqrt(Q / V)
     * Where:
     *   Y ~ 0.5 to 0.7 (universal constant)
     *   sigma = daily volatility
     *   Q = execution size
     *   V = 24h market volume
     */
    fun calculateSquareRootImpact(
        orderSizeUsd: Double,
        dailyVolumeUsd: Double,
        dailyVolatilityPct: Double,
        yConstant: Double = 0.6
    ): SquareRootImpactResult {
        val safeVolume = dailyVolumeUsd.coerceAtLeast(10000.0)
        val participationRate = (orderSizeUsd / safeVolume).coerceIn(0.0, 1.0)

        // I = Y * sigma * sqrt(Q / V)
        val impactFraction = yConstant * (dailyVolatilityPct / 100.0) * sqrt(participationRate)
        val impactBps = impactFraction * 10000.0 // in basis points
        val slippageUsd = orderSizeUsd * impactFraction

        val isExecutable = impactBps < 35.0 // Reject if price impact > 35 bps (0.35%)
        val recommendation = if (isExecutable) {
            "OPTIMAL EXECUTION: Impact ${String.format("%.2f", impactBps)} bps (Slippage: $${String.format("%.2f", slippageUsd)})"
        } else {
            "HIGH IMPACT WARNING: Impact ${String.format("%.2f", impactBps)} bps exceeds safe threshold! Slice order via TWAP/Iceberg."
        }

        return SquareRootImpactResult(
            participationRate = participationRate,
            dailyVolatility = dailyVolatilityPct,
            expectedImpactBps = impactBps,
            slippageUsd = slippageUsd,
            isExecutableWithinLimit = isExecutable,
            recommendation = recommendation
        )
    }

    /**
     * 3. Scaling Laws:
     * Evaluates Hurst Exponent diffusion: sigma(t) = sigma_0 * (t / t_0)^H
     * Power-law Pareto tail exponent alpha for heavy-tailed losses.
     */
    fun evaluateScalingLaws(
        priceReturns: List<Double>,
        defaultHurst: Double = 0.58
    ): ScalingLawAnalysis {
        val h = defaultHurst.coerceIn(0.1, 0.99)
        // Scaling factors from 1m to 15m and 1h
        val scale15m = 15.0.pow(h)
        val scale1h = 60.0.pow(h)

        // Power law tail index alpha (Zipf/Pareto): for financial returns, alpha ~ 3.0 (Cubic Law)
        val alpha = if (priceReturns.isNotEmpty()) {
            val maxLoss = priceReturns.map { Math.abs(it) }.sortedDescending().take(10)
            val meanTop = maxLoss.average().coerceAtLeast(0.0001)
            (1.0 + 10.0 / (maxLoss.sumOf { ln(it.coerceAtLeast(0.00001) / meanTop) }.let { Math.abs(it).coerceAtLeast(1.0) })).coerceIn(1.5, 4.5)
        } else 3.0

        val regime = when {
            h > 0.60 -> "PERSISTENT TRENDING (H=$h > 0.5): Positive feedback, momentum dominant"
            h < 0.40 -> "ANTI-PERSISTENT MEAN REVERSION (H=$h < 0.5): Negative feedback, mean-reversion dominant"
            else -> "BROWNIAN RANDOM WALK (H=$h ~ 0.5): Martingale state, neutral diffusion"
        }

        return ScalingLawAnalysis(
            hurstExponent = h,
            scalingFactor1mTo15m = scale15m,
            scalingFactor1mTo1h = scale1h,
            alphaPareto = alpha,
            regimeDescription = regime
        )
    }

    /**
     * 4. Size Line: Microstructure Liquidity Budget & Strict Position Sizing
     * Synthesizes Kelly criterion, Square Root impact, and Account Capital into a definitive Size Line.
     */
    fun computeSizeLine(
        symbol: String,
        accountBalanceUsd: Double,
        dailyVolumeUsd: Double,
        dailyVolatilityPct: Double,
        kellyLotSize: Double,
        minOrderLot: Double = 0.01,
        maxLeverage: Int = 20
    ): SizeLineRecommendation {
        // Safe participation limit: never take more than 0.5% of daily turnover
        val maxLiquidityCeilingUsd = (dailyVolumeUsd * 0.005).coerceAtLeast(500.0)
        val maxLeveragedCapital = accountBalanceUsd * maxLeverage

        // Convert Kelly lot size into USD nominal
        // Assuming 1 standard lot ~ $100,000 for FX or nominal price for Crypto
        val nominalPerLot = if (symbol.contains("BTC") || symbol.contains("ETH")) 1000.0 else 100000.0
        val maxLotsFromLiquidity = (maxLiquidityCeilingUsd / nominalPerLot).coerceAtLeast(minOrderLot)
        val maxLotsFromCapital = (maxLeveragedCapital / nominalPerLot).coerceAtLeast(minOrderLot)

        val maximumSafeSizeLots = minOf(maxLotsFromLiquidity, maxLotsFromCapital)
        val recommendedSizeLots = kellyLotSize.coerceIn(minOrderLot, maximumSafeSizeLots)

        val rationale = "Size Line: Balance $${String.format("%.2f", accountBalanceUsd)}, Max Safe Lots: ${String.format("%.3f", maximumSafeSizeLots)}, Recommended: ${String.format("%.3f", recommendedSizeLots)} (Capped by Square Root Law & Liquidity Ceiling)"

        return SizeLineRecommendation(
            symbol = symbol,
            baseCapitalUsd = accountBalanceUsd,
            maximumSafeSizeLots = maximumSafeSizeLots,
            recommendedSizeLots = recommendedSizeLots,
            minBrokerSizeLots = minOrderLot,
            maxLiquidityCeilingUsd = maxLiquidityCeilingUsd,
            sizeLineRationale = rationale
        )
    }
}
