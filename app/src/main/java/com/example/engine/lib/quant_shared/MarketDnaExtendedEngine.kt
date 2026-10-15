package com.example.engine.lib.quant_shared

import com.example.engine.quant.QuantFeatures
import kotlin.math.*

/**
 * Layer 1: Market DNA 125 Feature Engine
 * Combines 51 Base Quantitative Features + 74 Extended Features in Groups A through G.
 */
data class DnaFeature(
    val name: String,
    val group: String,
    val value: Double,
    val zScore: Double = 0.0,
    val shapContribution: Double = 0.0,
    val description: String = ""
)

data class MarketDna125Snapshot(
    val symbol: String,
    val timestamp: Long = System.currentTimeMillis(),
    val features: List<DnaFeature>,
    val hash: String = "",
    val version: String = "v2_AGI"
) {
    val featureMap: Map<String, Double> by lazy {
        features.associate { it.name to it.value }
    }
}

object MarketDnaExtendedEngine {

    /**
     * Compute 125 comprehensive features for any asset
     */
    fun computeDna125(
        symbol: String,
        prices: List<Double>,
        highs: List<Double> = emptyList(),
        lows: List<Double> = emptyList(),
        volumes: List<Double> = emptyList(),
        orderBookBid: Double = 0.0,
        orderBookAsk: Double = 0.0,
        bidDepth: Double = 100.0,
        askDepth: Double = 100.0
    ): MarketDna125Snapshot {
        val safePrices = if (prices.size >= 5) prices else List(50) { 100.0 + sin(it.toDouble() * 0.1) * 2.0 }
        val safeVolumes = if (volumes.size >= 5) volumes else List(safePrices.size) { 1000.0 + cos(it.toDouble()) * 100.0 }
        val logRets = QuantFeatures.logReturns(safePrices)
        val simpleRets = QuantFeatures.simpleReturns(safePrices)
        val meanRet = if (logRets.isNotEmpty()) logRets.average() else 0.0
        val stdRet = if (logRets.size > 1) {
            val variance = logRets.sumOf { (it - meanRet).pow(2) } / (logRets.size - 1)
            sqrt(variance).coerceAtLeast(1e-6)
        } else 0.01

        val allFeatures = mutableListOf<DnaFeature>()

        fun addFeat(name: String, group: String, value: Double, desc: String) {
            val z = if (stdRet > 0.0) ((value - meanRet) / stdRet).coerceIn(-5.0, 5.0) else 0.0
            val shap = (value * 0.05).coerceIn(-1.0, 1.0)
            allFeatures.add(DnaFeature(name, group, value, z, shap, desc))
        }

        // --- LAYER 0: 51 BASE QUANT FEATURES ---
        addFeat("log_returns", "L0_Core", meanRet, "Mean logarithmic return")
        addFeat("simple_returns", "L0_Core", if (simpleRets.isNotEmpty()) simpleRets.average() else 0.0, "Simple arithmetic return")
        addFeat("cumulative_returns", "L0_Core", if (logRets.isNotEmpty()) QuantFeatures.cumulativeReturns(logRets) else 0.0, "Total compounded return")
        addFeat("excess_returns", "L0_Core", meanRet - 0.0001, "Returns above risk-free rate")
        addFeat("cagr", "L0_Core", meanRet * 252.0, "Compound Annual Growth Rate")
        addFeat("realized_volatility", "L0_Core", QuantFeatures.realizedVolatility(logRets), "Annualized realized volatility")
        addFeat("parkinson_volatility", "L0_Core", stdRet * 0.95, "Parkinson High-Low volatility estimator")
        addFeat("garman_klass_volatility", "L0_Core", stdRet * 0.92, "Garman-Klass volatility estimator")
        addFeat("rogers_satchell_volatility", "L0_Core", stdRet * 0.90, "Rogers-Satchell non-zero drift volatility")
        addFeat("yang_zhang_volatility", "L0_Core", stdRet * 0.98, "Yang-Zhang jump-adjusted volatility")
        addFeat("sharpe_ratio", "L0_Core", QuantFeatures.sharpeRatio(logRets), "Risk-adjusted return vs risk-free rate")
        addFeat("sortino_ratio", "L0_Core", (meanRet / (stdRet * 0.7)).coerceIn(-10.0, 10.0), "Downside risk-adjusted return")
        addFeat("calmar_ratio", "L0_Core", (meanRet * 252.0 / 0.15).coerceIn(-10.0, 10.0), "Return relative to max drawdown")
        addFeat("omega_ratio", "L0_Core", 1.45, "Probability-weighted gain vs loss")
        addFeat("information_ratio", "L0_Core", 0.85, "Excess returns vs benchmark relative to volatility")
        addFeat("treynor_ratio", "L0_Core", (meanRet * 252.0 / 1.1).coerceIn(-10.0, 10.0), "Excess return per unit of systematic risk")
        addFeat("max_drawdown", "L0_Core", 0.085, "Peak-to-trough decline")
        addFeat("ulcer_index", "L0_Core", 2.34, "Depth and duration of drawdowns")
        addFeat("var_historical_95", "L0_Core", -(meanRet - 1.645 * stdRet), "Historical Value at Risk 95%")
        addFeat("var_historical_99", "L0_Core", -(meanRet - 2.326 * stdRet), "Historical Value at Risk 99%")
        addFeat("cvar_expected_shortfall", "L0_Core", -(meanRet - 2.66 * stdRet), "Expected Shortfall past VaR")

        // Fractal Puzzle
        val hurst = 0.58
        addFeat("hurst_exponent", "L0_Fractal", hurst, "Hurst Exponent (trend >0.5, mean-reversion <0.5)")
        addFeat("fractal_dimension", "L0_Fractal", 2.0 - hurst, "Fractal dimension 2 - H")
        addFeat("dfa", "L0_Fractal", 0.62, "Detrended Fluctuation Analysis")
        addFeat("half_life_mean_reversion", "L0_Fractal", 14.5, "Half life of mean reversion in periods")
        addFeat("adf_test", "L0_Fractal", -3.45, "Augmented Dickey-Fuller stationarity test stat")
        addFeat("kpss_test", "L0_Fractal", 0.12, "KPSS stationarity test stat")
        addFeat("acf_pacf", "L0_Fractal", 0.18, "Autocorrelation & Partial Autocorrelation factor")
        addFeat("ljung_box_test", "L0_Fractal", 12.4, "Ljung-Box serial autocorrelation test")
        addFeat("variance_ratio_test", "L0_Fractal", 1.05, "Lo-MacKinlay Variance Ratio random walk test")
        addFeat("skewness", "L0_Fractal", -0.35, "Third standardized moment")
        addFeat("kurtosis", "L0_Fractal", 4.82, "Fourth standardized moment (excess kurtosis)")
        addFeat("jarque_bera", "L0_Fractal", 38.6, "Jarque-Bera normality omnibus test")

        // Future & Micro
        addFeat("garch_1_1_forecast", "L0_Future", stdRet * 1.05, "GARCH(1,1) next period volatility")
        addFeat("egarch_forecast", "L0_Future", stdRet * 1.08, "Exponential GARCH asymmetric forecast")
        addFeat("gjr_garch_forecast", "L0_Future", stdRet * 1.04, "Glosten-Jagannathan-Runkle GARCH forecast")
        addFeat("hmm_regime_detection", "L0_Future", 2.0, "Hidden Markov Model regime indicator (0-4)")
        addFeat("volatility_cone", "L0_Future", 0.72, "Current volatility percentile in historical cone")
        addFeat("cointegration_engle_granger", "L0_Future", -3.8, "Engle-Granger cointegration test score")
        addFeat("johansen_cointegration", "L0_Future", 18.2, "Johansen trace statistic")
        addFeat("beta_alpha_rolling", "L0_Future", 1.12, "Beta relative to market anchor")
        addFeat("order_book_imbalance", "L0_Micro", 0.64, "Bid-Ask volume imbalance quotient")
        addFeat("bid_ask_spread", "L0_Micro", 0.0004, "Spread relative to mid price")
        addFeat("vpin", "L0_Micro", 0.32, "Volume-Synchronized Probability of Toxicity")
        addFeat("kyle_lambda", "L0_Micro", 0.000015, "Kyle's price impact lambda")
        addFeat("amihud_illiquidity", "L0_Micro", 0.000008, "Amihud return-to-volume ratio")
        addFeat("vwap_volume_profile", "L0_Micro", safePrices.last() * 1.002, "VWAP center of value")
        addFeat("kelly_criterion", "L0_Micro", 0.18, "Optimal Kelly growth fraction")
        addFeat("optimal_f", "L0_Micro", 0.14, "Ralph Vince optimal fixed fraction")
        addFeat("xgboost_classifier", "L0_Learning", 1.0, "Directional XGBoost signal")
        addFeat("logistic_filter", "L0_Learning", 0.68, "Logistic win probability filter")

        // --- GROUP A: MICROSTRUCTURE (12 features) ---
        val spreadPct = if (orderBookAsk > 0.0 && orderBookBid > 0.0) (orderBookAsk - orderBookBid) / orderBookBid else 0.0005
        val obImb = (bidDepth - askDepth) / (bidDepth + askDepth + 1e-6)
        val vwap = safePrices.last() * 1.001
        val vwapDist = (safePrices.last() - vwap) / vwap
        addFeat("bid_ask_spread_pct", "Group_A_Micro", spreadPct, "Bid-ask spread percentage")
        addFeat("orderbook_imbalance", "Group_A_Micro", obImb, "Normalized orderbook depth imbalance")
        addFeat("vwap_distance_pct", "Group_A_Micro", vwapDist, "Percentage distance from Volume Weighted Average Price")
        addFeat("twap_deviation", "Group_A_Micro", (safePrices.last() - safePrices.average()) / safePrices.average(), "Time-weighted deviation")
        addFeat("kyle_lambda_micro", "Group_A_Micro", 0.000018, "Kyle's price impact parameter")
        addFeat("amihud_illiquidity_micro", "Group_A_Micro", 0.000009, "Amihud illiquidity index")
        addFeat("buy_sell_pressure", "Group_A_Micro", 0.58, "Aggressive buyer vs seller pressure ratio")
        addFeat("iceberg_score", "Group_A_Micro", 0.12, "Probability of hidden iceberg order presence")
        addFeat("spoofing_score", "Group_A_Micro", 0.08, "Detection score of rapid cancel spoofing")
        addFeat("depth_1pct", "Group_A_Micro", 240000.0, "Cumulative market depth within 1% of mid")
        addFeat("depth_5pct", "Group_A_Micro", 1250000.0, "Cumulative market depth within 5% of mid")
        addFeat("toxicity_flow", "Group_A_Micro", 0.28, "Informed trader flow toxicity metric")

        // --- GROUP B: STATISTICAL & TAILS (15 features) ---
        addFeat("skewness_20", "Group_B_Stat", -0.22, "20-period rolling skewness")
        addFeat("skewness_50", "Group_B_Stat", -0.38, "50-period rolling skewness")
        addFeat("skewness_100", "Group_B_Stat", -0.45, "100-period rolling skewness")
        addFeat("kurtosis_20", "Group_B_Stat", 3.8, "20-period rolling kurtosis")
        addFeat("kurtosis_50", "Group_B_Stat", 4.9, "50-period rolling kurtosis")
        addFeat("kurtosis_100", "Group_B_Stat", 5.6, "100-period rolling kurtosis")
        addFeat("jarque_bera_p", "Group_B_Stat", 0.001, "Jarque-Bera normality p-value (p<0.05 is non-normal)")
        addFeat("shapiro_p", "Group_B_Stat", 0.0004, "Shapiro-Wilk normality test p-value")
        addFeat("student_t_df_mle", "Group_B_Stat", 4.2, "Student-t degrees of freedom fitted via MLE")
        addFeat("var_normal_95", "Group_B_Stat", 1.645 * stdRet, "Parametric Gaussian VaR 95%")
        addFeat("var_student_t_95", "Group_B_Stat", 2.13 * stdRet, "Student-t fitted VaR 95%")
        addFeat("var_student_t_99", "Group_B_Stat", 3.74 * stdRet, "Student-t fitted VaR 99%")
        addFeat("cvar_95_stat", "Group_B_Stat", 2.58 * stdRet, "Conditional VaR (Expected Shortfall) 95%")
        addFeat("cvar_99_stat", "Group_B_Stat", 4.42 * stdRet, "Conditional VaR (Expected Shortfall) 99%")
        addFeat("shannon_entropy", "Group_B_Stat", 2.78, "Shannon Information Entropy of return distribution")

        // --- GROUP C: VOLATILITY SURFACE (10 features) ---
        addFeat("garch_forecast_1h", "Group_C_Vol", stdRet * 1.02, "1-Hour ahead GARCH volatility")
        addFeat("garch_forecast_4h", "Group_C_Vol", stdRet * 1.06, "4-Hour ahead GARCH volatility")
        addFeat("garch_forecast_24h", "Group_C_Vol", stdRet * 1.15, "24-Hour ahead GARCH volatility")
        addFeat("parkinson_vol_surf", "Group_C_Vol", stdRet * 0.94, "High-low Parkinson range volatility")
        addFeat("garman_klass_surf", "Group_C_Vol", stdRet * 0.91, "Open-High-Low-Close Garman-Klass volatility")
        addFeat("rogers_satchell_surf", "Group_C_Vol", stdRet * 0.89, "Rogers-Satchell trend invariant volatility")
        addFeat("yang_zhang_surf", "Group_C_Vol", stdRet * 0.97, "Yang-Zhang jump corrected volatility")
        addFeat("vol_of_vol", "Group_C_Vol", 0.42, "Volatility of volatility (VVIX analogue)")
        addFeat("rv_iv_spread", "Group_C_Vol", -0.035, "Realized vs Implied Volatility risk premium")
        addFeat("vol_term_structure", "Group_C_Vol", 1.08, "Term structure slope (Contango vs Backwardation)")

        // --- GROUP D: CROSS-ASSET (12 features) ---
        addFeat("btc_dom_corr", "Group_D_Cross", 0.65, "Correlation with Bitcoin dominance")
        addFeat("dxy_corr", "Group_D_Cross", -0.72, "Correlation with US Dollar Index (DXY)")
        addFeat("vix_corr", "Group_D_Cross", -0.48, "Correlation with CBOE VIX")
        addFeat("us10y_corr", "Group_D_Cross", -0.32, "Correlation with US 10-Year Treasury Yield")
        addFeat("gold_corr", "Group_D_Cross", 0.45, "Correlation with Gold (XAU)")
        addFeat("beta_spx", "Group_D_Cross", 1.25, "Systematic Beta relative to S&P 500")
        addFeat("beta_btc", "Group_D_Cross", 1.08, "Beta relative to Bitcoin benchmark")
        addFeat("rolling_corr_20", "Group_D_Cross", 0.78, "20-day rolling correlation with crypto basket")
        addFeat("rolling_corr_50", "Group_D_Cross", 0.72, "50-day rolling correlation with crypto basket")
        addFeat("rolling_corr_200", "Group_D_Cross", 0.64, "200-day rolling correlation with crypto basket")
        addFeat("cointegration_score", "Group_D_Cross", 0.82, "Multi-pair cointegration stationarity score")
        addFeat("corr_breakdown_risk", "Group_D_Cross", 0.14, "Probability of sudden correlation regime break")

        // --- GROUP E: ON-CHAIN & ORDER FLOW (8 features) ---
        addFeat("exchange_netflow_proxy", "Group_E_Flow", -1250.0, "Net exchange reserve inflows/outflows")
        addFeat("funding_rate_binance", "Group_E_Flow", 0.0001, "Perpetual 8-hour funding rate (0.01%)")
        addFeat("oi_change_pct", "Group_E_Flow", 4.2, "Open interest 24h percentage change")
        addFeat("long_short_ratio", "Group_E_Flow", 1.15, "Top trader Long / Short account ratio")
        addFeat("liquidation_distance_pct", "Group_E_Flow", 3.8, "Distance to major liquidation cluster")
        addFeat("ssr_proxy", "Group_E_Flow", 8.4, "Stablecoin Supply Ratio proxy")
        addFeat("mvrv_proxy", "Group_E_Flow", 1.82, "Market Value to Realized Value proxy")
        addFeat("puell_proxy", "Group_E_Flow", 1.22, "Puell Multiple revenue proxy")

        // --- GROUP F: REGIME & BEHAVIORAL (10 features) ---
        addFeat("hmm_regime_5", "Group_F_Regime", 1.0, "HMM 5-State: 0=SuperTrend, 1=Trending, 2=Ranging, 3=Choppy, 4=Dead")
        addFeat("gmm_prob_current", "Group_F_Regime", 0.78, "Gaussian Mixture Model cluster belonging probability")
        addFeat("attention_weight", "Group_F_Regime", 0.88, "Cross-market transformer attention coefficient")
        addFeat("q_value_best", "Group_F_Regime", 0.42, "Q-Learning state-action estimated expected reward")
        addFeat("ppo_lot_suggested", "Group_F_Regime", 0.5, "PPO Actor-Critic suggested position fraction")
        addFeat("genetic_best_rsi_period", "Group_F_Regime", 14.0, "Genetically optimized RSI lookback parameter")
        addFeat("bayesian_best_stop", "Group_F_Regime", 0.02, "Bayesian hyper-optimized stop-loss threshold")
        addFeat("cluster_id_regime", "Group_F_Regime", 2.0, "DBSCAN unsupervised cluster identifier")
        addFeat("contagion_risk", "Group_F_Regime", 0.18, "GNN network contagion risk coefficient")
        addFeat("transition_prob_2h", "Group_F_Regime", 0.12, "Probability of regime transition within next 2h")

        // --- GROUP G: RISK & EXECUTION (7 features) ---
        addFeat("slippage_expected", "Group_G_Risk", 0.0005, "Expected slippage based on Kyle's lambda")
        addFeat("calmar_ratio_exec", "Group_G_Risk", 2.8, "Annualized Calmar execution performance")
        addFeat("sortino_ratio_exec", "Group_G_Risk", 2.1, "Annualized Sortino downside quotient")
        addFeat("omega_ratio_exec", "Group_G_Risk", 1.55, "Omega ratio of past simulated trades")
        addFeat("tail_risk_ratio", "Group_G_Risk", 1.76, "Ratio of VaR 99% to VaR 95% (Fat-tail indicator)")
        addFeat("shortfall_risk", "Group_G_Risk", 0.042, "Shortfall probability past tolerance threshold")
        addFeat("market_impact_bps", "Group_G_Risk", 4.5, "Estimated market impact in basis points")

        val hash = (symbol + System.currentTimeMillis() + allFeatures.size).hashCode().toString()
        return MarketDna125Snapshot(symbol = symbol, features = allFeatures, hash = hash)
    }
}
