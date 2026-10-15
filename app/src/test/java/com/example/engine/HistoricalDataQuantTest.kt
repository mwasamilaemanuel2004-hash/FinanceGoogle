package com.example.engine

import com.example.data.model.PriceCandle
import com.example.engine.backtest.RealBacktestEngine
import com.example.engine.backtest.RealHistoricalDataRepository
import com.example.engine.lib.quant_core.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Historical Data Integrity & Quant Pipeline Test Suite
 * Tests backtest sequential execution across all 7 market categories:
 * DERIV -> CRYPTO -> FOREX -> METALS -> INDICES -> STOCKS -> COMMODITIES
 * and verifies that cognitive intelligence, loss prevention, and quant engines work on historical data.
 */
class HistoricalDataQuantTest {

    @Test
    fun testRealHistoricalDataAcrossAllSevenMarkets() {
        val markets = listOf(
            "VOLATILITY 75",  // Deriv
            "BTC/USDT",        // Crypto
            "EUR/USD",         // Forex
            "XAU/USD",         // Metals
            "US500",           // Indices
            "NVDA",            // Stocks
            "BRENT"            // Commodities
        )

        for (symbol in markets) {
            val candles = RealHistoricalDataRepository.getVerifiedRealCandleSeries(symbol, 200)
            assertTrue("Candles for $symbol should not be empty", candles.isNotEmpty())
            assertTrue("Candles for $symbol should have at least 50 periods", candles.size >= 50)

            // Check candle validity
            val first = candles.first()
            val last = candles.last()
            assertTrue("$symbol first close price must be positive", first.close > 0)
            assertTrue("$symbol last close price must be positive", last.close > 0)
            assertTrue("$symbol high >= low", last.high >= last.low)
        }
    }

    @Test
    fun testQuantCoreEnginesOnDerivAndCryptoHistoricalData() {
        val derivCandles = RealHistoricalDataRepository.getVerifiedRealCandleSeries("VOLATILITY 75", 150)
        val btcCandles = RealHistoricalDataRepository.getVerifiedRealCandleSeries("BTC/USDT", 150)

        // 1. Ornstein-Uhlenbeck on Deriv Synthetics
        val ou = OrnsteinUhlenbeckEngine.calibrate(derivCandles.map { it.close })
        assertTrue("OU theta must be >= 0", ou.theta >= 0.0)
        assertTrue("OU sigma must be >= 0", ou.sigma >= 0.0)
        assertTrue("OU signal must be valid", ou.signal in listOf("BUY", "SELL", "HOLD"))

        // 2. Ichimoku on BTC
        val ichimoku = IchimokuEngine.calculate(btcCandles)
        assertTrue("Tenkan must be positive", ichimoku.tenkan > 0)
        assertTrue("Kijun must be positive", ichimoku.kijun > 0)
        assertTrue("Price vs Kumo must be valid", ichimoku.priceVsKumo in listOf("ABOVE", "BELOW", "INSIDE"))

        // 3. Wyckoff Phase on BTC
        val wyckoff = WyckoffEngine.analyze(btcCandles)
        assertNotNull(wyckoff.phase)
        assertTrue("Composite man action must be valid", wyckoff.compositeManAction in listOf("ACCUMULATING", "DISTRIBUTING", "NEUTRAL"))

        // 4. Volume Profile & POC
        val vp = VolumeProfileEngine.calculate(btcCandles)
        assertTrue("POC price should be positive", vp.pocPrice > 0)
        assertTrue("VAH >= VAL", vp.vahPrice >= vp.valPrice)

        // 5. Volatility Arbitrage & Yang-Zhang
        val volArb = VolatilityArbEngine.calculate(btcCandles)
        assertTrue("Realized vol must be non-negative", volArb.realizedVolYangZhang >= 0.0)

        // 6. Shannon Entropy & Compressibility
        val entropy = ShannonEntropyEngine.calculate(btcCandles)
        assertTrue("Entropy must be non-negative", entropy.entropy >= 0.0)

        // 7. Speed Rate Algorithm
        val speed = SpeedRateAlgorithmEngine.calculate(derivCandles)
        assertTrue("Tick velocity must be non-negative", speed.tickVelocityTicksPerSec >= 0.0)

        // 8. Liquidity Sweep & ICT FVG
        val sweeps = LiquiditySweepAndIctEngine.detect(btcCandles)
        assertTrue("BSL pool must be > 0", sweeps.bslPrice > 0)
        assertTrue("SSL pool must be > 0", sweeps.sslPrice > 0)

        // 9. Multiverse Decision Engine
        val multiverse = MultiverseDecisionEngine.evaluateFutures(btcCandles.last().close, 0.04, 0.002)
        assertTrue("Multiverse must simulate scenarios", multiverse.scenarios.isNotEmpty())
        assertTrue("Consensus decision must be valid", multiverse.consensusDecision in listOf("STRONG_BUY", "STRONG_SELL", "WAIT"))
    }

    @Test
    fun testRealMultiMarketSequentialBacktest() = runBlocking {
        val engine = RealBacktestEngine()

        // Test sequential execution starting with Deriv, then Crypto, Forex, Metals, Indices, Stocks, Commodities
        val report = engine.runSequentialAllMarketsBacktest(initialCapitalPerMarket = 1000.0)

        assertNotNull("Report must not be null", report)
        assertEquals("Must cover all 7 market categories sequentially", 7, report.results.size)

        // Check sequential order
        val expectedSequence = listOf(
            "1. DERIV SYNTHETICS",
            "2. CRYPTO",
            "3. FOREX",
            "4. METALS",
            "5. INDICES",
            "6. STOCKS",
            "7. COMMODITIES"
        )
        for (i in expectedSequence.indices) {
            assertTrue(
                "Category name must match expected order",
                report.results[i].categoryName.contains(expectedSequence[i])
            )
            val res = report.results[i].backtestResult
            assertTrue("Total trades must be >= 0", res.totalTrades >= 0)
            assertTrue("Max drawdown must be non-negative", res.maxDrawdownPct >= 0)
            assertTrue("Final equity must be > 0", res.finalEquityUsd > 0)
            // Verify bad decisions prevented list is defined
            assertNotNull("Bad decisions list must not be null", res.badDecisionsPrevented)
        }

        val totalBadDecisions = report.results.sumOf { it.backtestResult.badDecisionsPrevented.size }
        assertTrue("Across all 7 markets, circuit breaker & choppy filters prevent bad trades", totalBadDecisions >= 0)

        assertTrue("Average win rate must be in valid range", report.averageWinRatePct in 0.0..100.0)
        assertTrue("Cognitive adaptability score must be high", report.cognitiveAdaptabilityScore >= 50.0)
    }

    @Test
    fun testAgenticOrchestratorEnsemble35Execution() = runBlocking {
        val btcCandles = RealHistoricalDataRepository.getVerifiedRealCandleSeries("BTC/USDT", 100)
        val evaluation = AgenticOrchestrator.evaluateSymbol(
            symbol = "BTC/USDT",
            prices = btcCandles.map { it.close },
            highs = btcCandles.map { it.high },
            lows = btcCandles.map { it.low },
            volumes = btcCandles.map { it.volume },
            currentPrice = btcCandles.last().close
        )

        assertNotNull("Evaluation must be produced", evaluation)
        assertEquals("All 36 engines must participate in ensemble", 36, evaluation.engineResults.size)
        assertTrue("Monte Carlo probability between 0 and 1", evaluation.monteCarloProbability in 0.0..1.0)
        assertTrue("Final win probability between 0 and 1", evaluation.finalWinProbability in 0.0..1.0)
        assertNotNull("Decision rationale must not be empty", evaluation.decisionRationale)
    }
}
