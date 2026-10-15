package com.example.engine.lib.quant_core

import com.example.data.model.PriceCandle
import kotlin.math.*

data class LiquiditySweepResult(
    val bslPrice: Double, // Buy-Side Liquidity pool (equal highs)
    val sslPrice: Double, // Sell-Side Liquidity pool (equal lows)
    val bslSwept: Boolean,
    val sslSwept: Boolean,
    val turtleSoupLong: Boolean, // Sweep of SSL followed by immediate reclaim
    val turtleSoupShort: Boolean, // Sweep of BSL followed by immediate rejection
    val fairValueGapBullish: Boolean, // Imbalance 3-candle FVG
    val fairValueGapBearish: Boolean,
    val fvgUpper: Double,
    val fvgLower: Double,
    val institutionalOrderBlockPrice: Double,
    val ictSignalScore: Double // -100 to +100
)

object LiquiditySweepAndIctEngine {

    fun detect(candles: List<PriceCandle>): LiquiditySweepResult {
        if (candles.size < 20) {
            val p = candles.lastOrNull()?.close ?: 100.0
            return LiquiditySweepResult(
                bslPrice = p * 1.02,
                sslPrice = p * 0.98,
                bslSwept = false,
                sslSwept = false,
                turtleSoupLong = false,
                turtleSoupShort = false,
                fairValueGapBullish = false,
                fairValueGapBearish = false,
                fvgUpper = 0.0,
                fvgLower = 0.0,
                institutionalOrderBlockPrice = p,
                ictSignalScore = 0.0
            )
        }

        val window = candles.takeLast(40)
        val prior = window.dropLast(2)
        val last = window.last()
        val prev = window[window.size - 2]

        // 1. Equal highs (BSL) and Equal lows (SSL)
        val bsl = prior.maxOf { it.high }
        val ssl = prior.minOf { it.low }

        // BSL sweep: Price poked above BSL but closed back inside range
        val bslSwept = (prev.high > bsl || last.high > bsl) && last.close < bsl
        // SSL sweep: Price poked below SSL but closed back inside range
        val sslSwept = (prev.low < ssl || last.low < ssl) && last.close > ssl

        // Turtle Soup setups (classic Linda Raschke / ICT setup: 20-bar breakout failure)
        val turtleSoupShort = bslSwept && last.close < last.open
        val turtleSoupLong = sslSwept && last.close > last.open

        // 2. Fair Value Gap (FVG) detection across last 3 candles: c1, c2, c3
        val c1 = window[window.size - 3]
        val c2 = prev
        val c3 = last

        val fvgBull = c3.low > c1.high // Gap up where candle 2 had massive displacement
        val fvgBear = c3.high < c1.low // Gap down where candle 2 had massive displacement

        val fvgUpper = if (fvgBull) c3.low else if (fvgBear) c1.low else 0.0
        val fvgLower = if (fvgBull) c1.high else if (fvgBear) c3.high else 0.0

        // 3. Institutional Order Block: Last down-candle before strong bullish displacement
        var orderBlockPrice = last.close
        if (last.close > prev.high) {
            // Bullish Order Block
            orderBlockPrice = prev.low
        } else if (last.close < prev.low) {
            // Bearish Order Block
            orderBlockPrice = prev.high
        }

        var ictScore = 0.0
        if (turtleSoupLong) ictScore += 55.0
        if (turtleSoupShort) ictScore -= 55.0
        if (fvgBull) ictScore += 30.0
        if (fvgBear) ictScore -= 30.0
        if (sslSwept && !bslSwept) ictScore += 20.0
        if (bslSwept && !sslSwept) ictScore -= 20.0

        ictScore = ictScore.coerceIn(-100.0, 100.0)

        return LiquiditySweepResult(
            bslPrice = bsl,
            sslPrice = ssl,
            bslSwept = bslSwept,
            sslSwept = sslSwept,
            turtleSoupLong = turtleSoupLong,
            turtleSoupShort = turtleSoupShort,
            fairValueGapBullish = fvgBull,
            fairValueGapBearish = fvgBear,
            fvgUpper = fvgUpper,
            fvgLower = fvgLower,
            institutionalOrderBlockPrice = orderBlockPrice,
            ictSignalScore = ictScore
        )
    }
}
