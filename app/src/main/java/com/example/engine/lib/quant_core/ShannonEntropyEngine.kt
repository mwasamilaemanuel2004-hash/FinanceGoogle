package com.example.engine.lib.quant_core

import com.example.data.model.PriceCandle
import kotlin.math.*

data class ShannonInformationResult(
    val entropy: Double, // 0 to 8 bits
    val compressionRatio: Double, // Proxy for algorithmic complexity
    val lempelZivComplexity: Double,
    val predictabilityIndex: Double, // 1.0 - (H / H_max)
    val informationState: String, // "HIGH_PREDICTABILITY_ORDER", "EQUILIBRIUM", "MAX_NOISE_CHAOS"
    val isMarketEfficientRandomWalk: Boolean
)

object ShannonEntropyEngine {

    /**
     * Shannon Information Entropy H(X) = - sum( p(x) * log2(p(x)) )
     * Discretizes returns into symbol bins (alphabets 1..K)
     */
    fun calculate(candles: List<PriceCandle>, bins: Int = 16): ShannonInformationResult {
        if (candles.size < 30) {
            return ShannonInformationResult(
                entropy = 3.5,
                compressionRatio = 0.5,
                lempelZivComplexity = 0.5,
                predictabilityIndex = 0.12,
                informationState = "EQUILIBRIUM",
                isMarketEfficientRandomWalk = true
            )
        }

        val returns = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            val r = ln(candles[i].close / candles[i - 1].close.coerceAtLeast(1e-6))
            returns.add(r)
        }

        val minR = returns.minOrNull() ?: -0.01
        val maxR = returns.maxOrNull() ?: 0.01
        val range = (maxR - minR).coerceAtLeast(1e-6)
        val binSize = range / bins

        val counts = IntArray(bins)
        val discretizedSymbols = StringBuilder()

        for (r in returns) {
            val b = ((r - minR) / binSize).toInt().coerceIn(0, bins - 1)
            counts[b]++
            discretizedSymbols.append(('A' + b))
        }

        val total = returns.size.toDouble()
        var entropy = 0.0
        val maxEntropy = log2(bins.toDouble())

        for (c in counts) {
            if (c > 0) {
                val p = c / total
                entropy -= p * log2(p)
            }
        }

        // Normalized Predictability: 0 means purely random white noise; 1 means deterministic repetitive cycle
        val predictabilityIndex = (1.0 - (entropy / maxEntropy)).coerceIn(0.0, 1.0)

        // Simple Lempel-Ziv-like distinct word dictionary estimate
        val dict = HashSet<String>()
        var i = 0
        val s = discretizedSymbols.toString()
        while (i < s.length) {
            var j = i + 1
            while (j <= s.length && dict.contains(s.substring(i, j))) {
                j++
            }
            if (j <= s.length) {
                dict.add(s.substring(i, j))
            }
            i = j
        }
        val lzComplexity = (dict.size.toDouble() / (s.length / log2(s.length.toDouble().coerceAtLeast(2.0)))).coerceIn(0.0, 2.0)
        val compressionRatio = (1.0 - (dict.size.toDouble() / s.length)).coerceIn(0.0, 1.0)

        val isEfficientRandomWalk = entropy > maxEntropy * 0.90

        val state = when {
            entropy < maxEntropy * 0.65 -> "HIGH_PREDICTABILITY_ORDER"
            entropy > maxEntropy * 0.92 -> "MAX_NOISE_CHAOS"
            else -> "EQUILIBRIUM"
        }

        return ShannonInformationResult(
            entropy = entropy,
            compressionRatio = compressionRatio,
            lempelZivComplexity = lzComplexity,
            predictabilityIndex = predictabilityIndex,
            informationState = state,
            isMarketEfficientRandomWalk = isEfficientRandomWalk
        )
    }
}
