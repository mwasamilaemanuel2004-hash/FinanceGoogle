package com.example.engine

data class StrategyDefinition(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val winRateHistorical: Double,
    val profitFactor: Double,
    val recommendedHorizon: String,
    val executionType: String
)

object StrategyLibrary {
    val allStrategies = listOf(
        StrategyDefinition(
            id = "smc_smart_money",
            name = "1. SMC Smart Money Concepts",
            category = "Institutional Structure",
            description = "BOS, CHoCH, Order Block, FVG and Liquidity Sweeps with sub-5ms detection.",
            winRateHistorical = 74.8,
            profitFactor = 2.45,
            recommendedHorizon = "15M - 1H",
            executionType = "Limit Orders with Tight SL"
        ),
        StrategyDefinition(
            id = "ict_inner_circle",
            name = "2. ICT Inner Circle Trader",
            category = "Time & Price Theory",
            description = "Killzones (London, NY, Asia), Silver Bullet, Judas Swing, AMD Power of 3.",
            winRateHistorical = 76.2,
            profitFactor = 2.60,
            recommendedHorizon = "1M - 15M",
            executionType = "Killzone Session Snipe"
        ),
        StrategyDefinition(
            id = "bos_break_structure",
            name = "3. BOS Break of Structure",
            category = "Trend Continuation",
            description = "Swing high/low break detection via ZigZag 5% algorithmic filters.",
            winRateHistorical = 68.0,
            profitFactor = 2.15,
            recommendedHorizon = "5M - 1H",
            executionType = "Momentum Breakout"
        ),
        StrategyDefinition(
            id = "choch_change_character",
            name = "4. CHoCH Change of Character",
            category = "Trend Reversal",
            description = "Earliest trend reversal signal: Counter-structural swing level violation.",
            winRateHistorical = 72.5,
            profitFactor = 2.35,
            recommendedHorizon = "15M - 4H",
            executionType = "Reversal Limit Entry"
        ),
        StrategyDefinition(
            id = "fvg_fair_value_gap",
            name = "5. FVG Fair Value Gap",
            category = "Imbalance Exploitation",
            description = "3-bar imbalance: (candle1 low - candle3 high) with 50% Consequent Encroachment retest.",
            winRateHistorical = 71.0,
            profitFactor = 2.25,
            recommendedHorizon = "5M - 1H",
            executionType = "CE Imbalance Fill"
        ),
        StrategyDefinition(
            id = "ob_order_block",
            name = "6. OB Order Block",
            category = "Institutional Footprint",
            description = "Last opposing candle prior to impulse with volume > 1.8x average.",
            winRateHistorical = 73.0,
            profitFactor = 2.40,
            recommendedHorizon = "15M - 1H",
            executionType = "Mitigation Retest Limit"
        ),
        StrategyDefinition(
            id = "liquidity_sweep",
            name = "7. Liquidity Sweeps & Stop Hunts",
            category = "Order Flow Pools",
            description = "Equal highs/lows liquidity purge, stop hunts with wicks > 2x ATR.",
            winRateHistorical = 74.0,
            profitFactor = 2.50,
            recommendedHorizon = "1M - 15M",
            executionType = "Liquidity Reversal Taker"
        ),
        StrategyDefinition(
            id = "mss_structure_shift",
            name = "8. MSS Market Structure Shift",
            category = "Displacement Confirmation",
            description = "CHoCH with high-volume displacement confirming institutional intent.",
            winRateHistorical = 77.5,
            profitFactor = 2.85,
            recommendedHorizon = "5M - 1H",
            executionType = "Displacement Retest"
        ),
        StrategyDefinition(
            id = "rr_risk_reward",
            name = "9. RR Risk Reward Expectancy",
            category = "Expectancy Governor",
            description = "Strict min 1.5:1 to 3:1 RR filter with positive mathematical expectancy.",
            winRateHistorical = 65.0,
            profitFactor = 2.30,
            recommendedHorizon = "All Horizons",
            executionType = "Dynamic OCO Bracket"
        ),
        StrategyDefinition(
            id = "supply_demand_zones",
            name = "10. Supply-Demand Zones",
            category = "Zone Imbalances",
            description = "Rally-Base-Rally and Drop-Base-Drop freshness zones with 3-touch test.",
            winRateHistorical = 70.0,
            profitFactor = 2.20,
            recommendedHorizon = "1H - Daily",
            executionType = "Zone Limit Accumulation"
        ),
        StrategyDefinition(
            id = "support_resistance_fractal",
            name = "11. Support-Resistance (S/R)",
            category = "Dynamic Price Boundaries",
            description = "Dynamic S/R via Fractal channels and Volume Profile POC flips.",
            winRateHistorical = 64.0,
            profitFactor = 1.95,
            recommendedHorizon = "15M - Daily",
            executionType = "Pivot Bounce & Flip"
        ),
        StrategyDefinition(
            id = "price_action_patterns",
            name = "12. Price Action 2-Bar Patterns",
            category = "Raw Bar Behavior",
            description = "Pinbar, bullish/bearish engulfing, and inside bar breakout logic.",
            winRateHistorical = 66.5,
            profitFactor = 2.05,
            recommendedHorizon = "15M - 4H",
            executionType = "Bar Close Execution"
        ),
        StrategyDefinition(
            id = "candlestick_signatures",
            name = "13. Candlestick Rejections",
            category = "Single Candle Signatures",
            description = "Hammer, shooting star, gravestone doji rejection with empirical accuracy.",
            winRateHistorical = 65.0,
            profitFactor = 1.90,
            recommendedHorizon = "15M - 1H",
            executionType = "Confirmation Trigger"
        ),
        StrategyDefinition(
            id = "scalping_micro",
            name = "14. Scalping Micro-Momentum",
            category = "High-Frequency Algorithmic",
            description = "1M/5M tight 0.05% SL and fast 0.10% TP with sub-second trailing exit.",
            winRateHistorical = 81.2,
            profitFactor = 2.80,
            recommendedHorizon = "1M - 5M",
            executionType = "Millisecond Market Snipe"
        ),
        StrategyDefinition(
            id = "day_trading_vwap",
            name = "15. Day Trading Session VWAP",
            category = "Intraday Session Flow",
            description = "Open to close VWAP anchor with RVOL volume surge filter (zero overnight risk).",
            winRateHistorical = 66.0,
            profitFactor = 2.10,
            recommendedHorizon = "15M - 1H",
            executionType = "Intraday Session VWAP"
        ),
        StrategyDefinition(
            id = "swing_trading_ema",
            name = "16. Swing Trading (EMA Cross & ADX)",
            category = "Multi-Day Expansion",
            description = "1 to 5 day position holds with EMA 20/50 cross and ADX > 25 filter.",
            winRateHistorical = 69.0,
            profitFactor = 2.40,
            recommendedHorizon = "1H - 4H",
            executionType = "Multi-Day Trailing Stop"
        ),
        StrategyDefinition(
            id = "position_trading_macro",
            name = "17. Position Trading 200 SMA",
            category = "Macro Trend Holding",
            description = "Secular weekly/monthly trends based on 200 SMA and global macro regime.",
            winRateHistorical = 63.5,
            profitFactor = 2.65,
            recommendedHorizon = "Daily - Weekly",
            executionType = "Algorithmic Limit Accumulation"
        ),
        StrategyDefinition(
            id = "trend_following_donchian",
            name = "18. Trend Following Donchian 20",
            category = "Donchian Breakout",
            description = "Donchian 20 breakout channels with ADX > 25 directional confirmation.",
            winRateHistorical = 61.0,
            profitFactor = 2.35,
            recommendedHorizon = "1H - Daily",
            executionType = "Channel Breakout Fill"
        ),
        StrategyDefinition(
            id = "counter_trend_ou",
            name = "19. Counter Trend Ornstein-Uhlenbeck",
            category = "Statistical Reversion",
            description = "Mean reversion Z-score > 2.2 with RSI divergence and drift correction.",
            winRateHistorical = 72.0,
            profitFactor = 2.25,
            recommendedHorizon = "5M - 1H",
            executionType = "Extremum Mean Reversion"
        ),
        StrategyDefinition(
            id = "breakout_volume",
            name = "20. Breakout High-Volume",
            category = "Volume Expansion",
            description = "Volume > 2x average with clean level close and structural retest.",
            winRateHistorical = 67.0,
            profitFactor = 2.20,
            recommendedHorizon = "15M - 1H",
            executionType = "Retest Limit Order"
        ),
        StrategyDefinition(
            id = "breakdown_support",
            name = "21. Breakdown Support Flush",
            category = "Support Failure",
            description = "Support break with heavy sell volume flush into lower liquidity.",
            winRateHistorical = 67.0,
            profitFactor = 2.20,
            recommendedHorizon = "15M - 1H",
            executionType = "Flush Retest Sell"
        ),
        StrategyDefinition(
            id = "news_trading_forexfactory",
            name = "22. News Trading Straddle",
            category = "Event Volatility",
            description = "ForexFactory high-impact events with 5-minute pre-event bracket straddle.",
            winRateHistorical = 65.0,
            profitFactor = 2.15,
            recommendedHorizon = "1M - 15M",
            executionType = "Bracket Straddle with Slippage Guard"
        ),
        StrategyDefinition(
            id = "grid_trading_xyk",
            name = "23. Grid Trading (x*y=k Invariant)",
            category = "Market Making Density",
            description = "Non-directional geometric grid trading for ranging regimes with ATR step.",
            winRateHistorical = 81.0,
            profitFactor = 2.20,
            recommendedHorizon = "Continuous Intraday",
            executionType = "Multi-Level Grid Ladder"
        ),
        StrategyDefinition(
            id = "hedging_delta_neutral",
            name = "24. Hedging Delta-Neutral Spreads",
            category = "Portfolio Delta Neutral",
            description = "Long/short hedging across -1*TIPS vs Gold, DXY vs Majors.",
            winRateHistorical = 84.0,
            profitFactor = 2.50,
            recommendedHorizon = "4H - Daily",
            executionType = "Dual-Leg Arbitrage Fill"
        ),
        StrategyDefinition(
            id = "algorithmic_execution_twap",
            name = "25. Algorithmic Stealth TWAP/VWAP",
            category = "Execution Engine",
            description = "Time-Weighted (TWAP) and Volume-Weighted (VWAP) iceberg order slicing.",
            winRateHistorical = 88.0,
            profitFactor = 2.95,
            recommendedHorizon = "Continuous Millisecond",
            executionType = "Micro-Slice Iceberg Routing"
        )
    )
}
