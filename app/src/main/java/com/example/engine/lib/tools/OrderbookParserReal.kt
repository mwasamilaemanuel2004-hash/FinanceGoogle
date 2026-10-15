package com.example.engine.lib.tools

import kotlin.math.*

data class OrderbookLevelKt(
    val price: Double,
    val quantity: Double
)

data class ParsedOrderbookKt(
    val symbol: String,
    val provider: String,
    val timestamp: Long,
    val bids: List<OrderbookLevelKt>,
    val asks: List<OrderbookLevelKt>,
    val bestBid: Double,
    val bestAsk: Double,
    val spread: Double,
    val midPrice: Double,
    val microPrice: Double,
    val ofi: Double,
    val depthSkew: Double,
    val bidDepthTotal: Double,
    val askDepthTotal: Double,
    val icebergDetected: Boolean
)

object OrderbookParserReal {
    fun parseL2(
        symbol: String,
        provider: String,
        rawBids: List<Pair<Double, Double>>,
        rawAsks: List<Pair<Double, Double>>,
        timestamp: Long = System.currentTimeMillis()
    ): ParsedOrderbookKt {
        val bids = rawBids.map { OrderbookLevelKt(it.first, it.second) }.sortedByDescending { it.price }
        val asks = rawAsks.map { OrderbookLevelKt(it.first, it.second) }.sortedBy { it.price }

        val bestBid = bids.firstOrNull()?.price ?: 0.0
        val bestAsk = asks.firstOrNull()?.price ?: 0.0
        val spread = max(0.0, bestAsk - bestBid)
        val midPrice = if (bestBid > 0 && bestAsk > 0) (bestBid + bestAsk) / 2.0 else max(bestBid, bestAsk)

        val top5Bids = bids.take(5)
        val top5Asks = asks.take(5)
        val bidVol5 = top5Bids.sumOf { it.quantity }
        val askVol5 = top5Asks.sumOf { it.quantity }
        val totalVol5 = bidVol5 + askVol5

        val ofi = if (totalVol5 > 0) (bidVol5 - askVol5) / totalVol5 else 0.0

        val qBid = top5Bids.firstOrNull()?.quantity ?: 1.0
        val qAsk = top5Asks.firstOrNull()?.quantity ?: 1.0
        val microPrice = if (qBid + qAsk > 0) {
            (bestBid * qAsk + bestAsk * qBid) / (qBid + qAsk)
        } else midPrice

        val bidTotal = bids.sumOf { it.quantity }
        val askTotal = asks.sumOf { it.quantity }
        val depthSkew = if (bidTotal + askTotal > 0) (bidTotal - askTotal) / (bidTotal + askTotal) else 0.0

        val avgBidQty = if (top5Bids.isNotEmpty()) bidVol5 / top5Bids.size else 1.0
        val avgAskQty = if (top5Asks.isNotEmpty()) askVol5 / top5Asks.size else 1.0
        val iceberg = top5Bids.any { it.quantity > avgBidQty * 3.5 } || top5Asks.any { it.quantity > avgAskQty * 3.5 }

        return ParsedOrderbookKt(
            symbol = symbol,
            provider = provider,
            timestamp = timestamp,
            bids = bids,
            asks = asks,
            bestBid = bestBid,
            bestAsk = bestAsk,
            spread = spread,
            midPrice = midPrice,
            microPrice = microPrice,
            ofi = ofi,
            depthSkew = depthSkew,
            bidDepthTotal = bidTotal,
            askDepthTotal = askTotal,
            icebergDetected = iceberg
        )
    }

    fun parseDerivSynthetic(symbol: String, ask: Double, bid: Double): ParsedOrderbookKt {
        val spread = max(0.0001, ask - bid)
        val rawBids = listOf(
            bid to 15.0,
            (bid - spread) to 30.0,
            (bid - spread * 2.5) to 65.0,
            (bid - spread * 5.0) to 120.0
        )
        val rawAsks = listOf(
            ask to 14.5,
            (ask + spread) to 32.0,
            (ask + spread * 2.5) to 70.0,
            (ask + spread * 5.0) to 115.0
        )
        return parseL2(symbol, "DERIV", rawBids, rawAsks)
    }
}
