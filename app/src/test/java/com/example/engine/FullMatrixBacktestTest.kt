package com.example.engine

import com.example.data.model.BotNode
import com.example.data.model.MarketType
import com.example.engine.backtest.LossPreventionConfig
import com.example.engine.backtest.RealBacktestEngine
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FullMatrixBacktestTest {

    @Test
    fun runFullCapitalMatrixBacktest() = runBlocking {
        val engine = RealBacktestEngine()
        val capitalTiers = listOf(1.0, 10.0, 50.0, 100.0, 1000.0)

        val bots = listOf(
            BotNode(1, "Crypto Scalper", "Sub-Second Micro Delta Scalping", MarketType.CRYPTO, "BTC/USDT", 0.0, 0.0, 0.0, 0, false),
            BotNode(2, "Crypto Swing", "Multi-Day Volume Profile Trend", MarketType.CRYPTO, "ETH/USDT", 0.0, 0.0, 0.0, 0, false),
            BotNode(3, "Crypto Momentum", "Breakout Speed & CVD Velocity", MarketType.CRYPTO, "SOL/USDT", 0.0, 0.0, 0.0, 0, false),
            BotNode(4, "Forex SMC/ICT", "Institutional Order Block & FVG", MarketType.FOREX, "EUR/USD", 0.0, 0.0, 0.0, 0, false),
            BotNode(5, "Forex Macro", "Central Bank Yield Differentials", MarketType.FOREX, "GBP/USD", 0.0, 0.0, 0.0, 0, false),
            BotNode(6, "Forex Mean Reversion", "Bollinger Squeeze & VWAP Band", MarketType.FOREX, "USD/JPY", 0.0, 0.0, 0.0, 0, false),
            BotNode(7, "Gold/Silver Breaker", "Real Yield & Inflation Hedge", MarketType.METALS, "XAU/USD", 0.0, 0.0, 0.0, 0, false),
            BotNode(8, "Crude Oil Alpha", "OPEC Flow & Inventory Delta", MarketType.COMMODITIES, "BRENT CRUDE", 0.0, 0.0, 0.0, 0, false),
            BotNode(9, "US500/NAS100/DAX Rider", "Market Breadth & Gamma Imbalance", MarketType.INDICES, "US500 / NAS100", 0.0, 0.0, 0.0, 0, false),
            BotNode(10, "Deriv Volatility/Digital", "Continuous Tick Spike Reversal", MarketType.DERIV, "VOLATILITY 75", 0.0, 0.0, 0.0, 0, false),
            BotNode(11, "Equities Valuation", "Tanzania DSE Value Compounder", MarketType.STOCKS, "CRDB / NMB (DSE)", 0.0, 0.0, 0.0, 0, false),
            BotNode(12, "Equities Breakout/Swing", "Blackwell Compute Tech Expansion", MarketType.STOCKS, "NVDA", 0.0, 0.0, 0.0, 0, false),
            BotNode(13, "Cross-Market Arbitrage", "Triangular & Cross-Exchange Spread", MarketType.CRYPTO, "BTC/ETH/USDT", 0.0, 0.0, 0.0, 0, false),
            BotNode(14, "Global Alpha Hunting", "Multi-Market Sovereign Neural Grid", MarketType.METALS, "XAU/BTC/NVDA", 0.0, 0.0, 0.0, 0, false)
        )

        println("================================================================================")
        println("REAL HISTORICAL BACKTEST PERFORMANCE MATRIX — ALL 14 BOTS & ALL MARKETS")
        println("================================================================================")

        for (bot in bots) {
            println("\n>>> BOT #${bot.id}: ${bot.name} | Soko: ${bot.marketType} | Jozi: ${bot.targetPair} <<<")
            println("Capital | Final Eq ($) | Net PnL ($) | Return (%) | Win Rate (%) | Trades (W/L) | Profit Factor | Max DD (%) | Bad Trades Blocked")
            println("---------------------------------------------------------------------------------------------------------------------------------")
            for (cap in capitalTiers) {
                val res = engine.runBotBacktest(
                    bot = bot,
                    timeframe = "1h",
                    initialCapital = cap,
                    lossGuard = LossPreventionConfig(
                        enableDynamicAtrStopLoss = true,
                        enableChoppyRegimeFilter = true,
                        enableCircuitBreaker = true,
                        minRiskRewardRatio = 2.0,
                        enableSpreadSlippageGate = true,
                        maxRiskPerTradePct = 2.0
                    )
                )
                val sign = if (res.netProfitUsd >= 0) "+" else ""
                println(
                    String.format(
                        "$%-6.0f | $%-11.2f | %-11s | %-10s | %-12.1f | %-12s | %-13.2f | -%-9.2f | %-18d",
                        cap,
                        res.finalEquityUsd,
                        "$sign$${res.netProfitUsd}",
                        "$sign${res.netProfitPct}%",
                        res.winRatePct,
                        "${res.totalTrades} (${res.winningTrades}/${res.losingTrades})",
                        res.profitFactor,
                        res.maxDrawdownPct,
                        res.badDecisionsPrevented.size
                    )
                )
            }
        }

        println("\n================================================================================")
        println("FULL MULTI-ASSET SYSTEM PORTFOLIO BACKTEST (ALL 14 BOTS COMBINED)")
        println("================================================================================")
        println("Portfolio Cap | Final Eq ($) | Net PnL ($) | Return (%) | Win Rate (%) | Total Trades | Profit Factor | Max DD (%) | Blocked Decisions")
        println("---------------------------------------------------------------------------------------------------------------------------------")
        for (cap in listOf(50.0, 100.0, 500.0, 1000.0, 10000.0)) {
            val sysRes = engine.runFullSystemBacktest(
                bots = bots,
                timeframe = "1h",
                totalPortfolioCapital = cap,
                lossGuard = LossPreventionConfig(
                    enableDynamicAtrStopLoss = true,
                    enableChoppyRegimeFilter = true,
                    enableCircuitBreaker = true,
                    minRiskRewardRatio = 2.0,
                    enableSpreadSlippageGate = true,
                    maxRiskPerTradePct = 2.0
                )
            )
            val sign = if (sysRes.netProfitUsd >= 0) "+" else ""
            println(
                String.format(
                    "$%-12.0f | $%-11.2f | %-11s | %-10s | %-12.1f | %-12d | %-13.2f | -%-9.2f | %-18d",
                    cap,
                    sysRes.finalEquityUsd,
                    "$sign$${sysRes.netProfitUsd}",
                    "$sign${sysRes.netProfitPct}%",
                    sysRes.winRatePct,
                    sysRes.totalTrades,
                    sysRes.profitFactor,
                    sysRes.maxDrawdownPct,
                    sysRes.badDecisionsPrevented.size
                )
            )
        }
        println("================================================================================\n")
    }
}
