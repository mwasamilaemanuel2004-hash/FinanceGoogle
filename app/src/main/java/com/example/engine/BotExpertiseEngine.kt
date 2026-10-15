package com.example.engine

import com.example.data.model.BotCapitalTier
import com.example.data.model.BotNode

data class BotExpertiseReport(
    val botId: Int,
    val botName: String,
    val capitalTier: BotCapitalTier,
    val minRequiredUsd: Double,
    val maxRecommendedUsd: Double,
    val recommendedLotSize: Double,
    val recommendedLeverage: String,
    val maxDrawdownLimitPct: Double,
    val isQualified: Boolean,
    val operationalMode: String,
    val unlockedFeaturesSwahili: List<String>,
    val unlockedFeaturesEn: List<String>,
    val expertiseLevelSwahili: String,
    val expertiseLevelEn: String,
    val customGuardReasonSwahili: String,
    val customGuardReasonEn: String
)

object BotExpertiseEngine {

    /**
     * Strictly evaluates a bot's capability, sizing guidelines, and unlocks advanced "umahiri"
     * based on actual verified capital.
     */
    fun evaluateBotCapability(bot: BotNode, verifiedAccountBalance: Double): BotExpertiseReport {
        val minCap = bot.capitalTier.minCap
        val maxCap = bot.capitalTier.maxCap
        val isQualified = verifiedAccountBalance >= minCap

        // Calculate dynamic recommended lot size based on tier and available balance
        val recommendedLot = when (bot.capitalTier) {
            BotCapitalTier.MICRO_CENT_TIER -> {
                // High precision micro lotting: 0.01 lot per .00
                (0.01 * (verifiedAccountBalance / 5.0)).coerceIn(0.01, 0.02)
            }
            BotCapitalTier.RETAIL_MEDIUM_TIER -> {
                // Medium tier sizing: 0.01 lot per 0.00
                (0.01 * (verifiedAccountBalance / 10.0)).coerceIn(0.01, 0.05)
            }
            BotCapitalTier.LEVEL_0_TO_1K -> {
                // Level 0 to 1K Starter sizing: 0.01 to 0.10 lots
                (0.01 * ((verifiedAccountBalance.coerceAtLeast(10.0)) / 100.0)).coerceIn(0.01, 0.10)
            }
            BotCapitalTier.LEVEL_1K_TO_10K -> {
                // Level 1K to 10K Growth sizing: 0.10 to 1.00 lots with risk controls
                (0.10 * ((verifiedAccountBalance.coerceAtLeast(1000.0)) / 1000.0)).coerceIn(0.10, 1.00)
            }
            BotCapitalTier.LEVEL_10K_TO_100K -> {
                // Level 10K to 100K Institutional sizing: 1.00 to 10.00 lots
                (1.00 * ((verifiedAccountBalance.coerceAtLeast(10000.0)) / 10000.0)).coerceIn(1.00, 10.00)
            }
            BotCapitalTier.ADVANCED_INSTITUTIONAL_TIER -> {
                // Advanced institutional size: 0.02 lot per 5.00
                (0.02 * (verifiedAccountBalance / 15.0)).coerceIn(0.02, 5.00)
            }
        }

        // Custom expertise level labels
        val (levelSw, levelEn) = when (bot.capitalTier) {
            BotCapitalTier.MICRO_CENT_TIER -> 
                "Umahiri wa Cent/Micro (Ulinzi wa Kwanza)" to "Micro/Cent Expertise (Strict Defense First)"
            BotCapitalTier.RETAIL_MEDIUM_TIER -> 
                "Umahiri wa Swing Standard (Kasi ya Kati)" to "Standard Retail Swing Expertise (Medium Velocity)"
            BotCapitalTier.LEVEL_0_TO_1K ->
                "Level 0 - 1K (Micro & Starter Sizing)" to "Level 0 to 1K (Micro & Starter Sizing)"
            BotCapitalTier.LEVEL_1K_TO_10K ->
                "Level 1K - 10K (Growth Portfolio Mode)" to "Level 1K to 10K (Growth Portfolio Mode)"
            BotCapitalTier.LEVEL_10K_TO_100K ->
                "Level 10K - 100K (Institutional Scale Master)" to "Level 10K to 100K (Institutional Scale Master)"
            BotCapitalTier.ADVANCED_INSTITUTIONAL_TIER -> 
                "Umahiri Mkuu wa Taasisi (High Alpha Core)" to "Advanced Institutional Master Expertise (High Alpha Core)"
        }

        // Operational mode
        val opMode = when (bot.capitalTier) {
            BotCapitalTier.MICRO_CENT_TIER -> "SAFE_RETAIL_DELTA"
            BotCapitalTier.RETAIL_MEDIUM_TIER -> "VOLATILITY_SWING_PROFILE"
            BotCapitalTier.LEVEL_0_TO_1K -> "LEVEL_0_TO_1K_STARTER"
            BotCapitalTier.LEVEL_1K_TO_10K -> "LEVEL_1K_TO_10K_GROWTH"
            BotCapitalTier.LEVEL_10K_TO_100K -> "LEVEL_10K_TO_100K_INSTITUTIONAL"
            BotCapitalTier.ADVANCED_INSTITUTIONAL_TIER -> "Sovereign AI Neural Compounder"
        }

        // Unlocked specialized features representing "umahiri"
        val featuresSw = when (bot.capitalTier) {
            BotCapitalTier.MICRO_CENT_TIER -> listOf(
                "Ulinzi mkali wa Pip 15 wa kuzuia hasara",
                "Kikomo cha hasara ya siku ya $0.50 hadi $2.00",
                "Utekelezaji tulivu bila slippage"
            )
            BotCapitalTier.RETAIL_MEDIUM_TIER -> listOf(
                "Ufuatiliaji wa maeneo ya Volume Profile (POC)",
                "Trailing SL inayohama kwa 0.8% kila pips 10",
                "Kinga ya habari nzito (News Cooldown Engine)"
            )
            BotCapitalTier.LEVEL_0_TO_1K -> listOf(
                "Ulinzi mkali wa mtaji mdogo (0.01 lot micro sizing)",
                "Trailing Stop Loss ya haraka kulinda dola chache",
                "Uteuzi wa masoko yenye ada na spread ndogo (Deriv & Crypto)"
            )
            BotCapitalTier.LEVEL_1K_TO_10K -> listOf(
                "Mgao wa kikosi wa boti nyingi (Multi-market portfolio allocation)",
                "Hedging ya hatari na udhibiti wa drawdown chini ya 4.0%",
                "Kujumuisha uchambuzi wa Order Flow & Volume Profile"
            )
            BotCapitalTier.LEVEL_10K_TO_100K -> listOf(
                "Utekelezaji wa kitaasisi (Kyle Lambda slippage protection)",
                "Kelly Criterion & Fat-Tail VaR 99% risk optimization",
                "Stat-Arb Cointegration na Black-Scholes Greeks Hedging"
            )
            BotCapitalTier.ADVANCED_INSTITUTIONAL_TIER -> listOf(
                "Gridi ya Neva ya pande zote (Neural Grid Routing)",
                "Mgao thabiti wa dynamic hedging (Long/Short arbitrage)",
                "Sovereign Multi-Regime tracking (RSI/ATR cluster optimization)"
            )
        }

        val featuresEn = when (bot.capitalTier) {
            BotCapitalTier.MICRO_CENT_TIER -> listOf(
                "Strict 15-pip fixed stop loss protection",
                "Tight daily loss brake at $0.50 - $2.00",
                "Quiet slippage-free execution"
            )
            BotCapitalTier.RETAIL_MEDIUM_TIER -> listOf(
                "Volume profile POC zone matching",
                "Trailing SL shifting by 0.8% per 10 pips",
                "High-impact macro news cooldown brakes"
            )
            BotCapitalTier.LEVEL_0_TO_1K -> listOf(
                "Micro capital protection (0.01 micro-lot sizing)",
                "Rapid trailing profit locks for micro accounts",
                "Ultra-low spread market routing (Deriv Synthetics & Top Crypto)"
            )
            BotCapitalTier.LEVEL_1K_TO_10K -> listOf(
                "Multi-market fleet diversification and cross-asset risk hedge",
                "Strict max drawdown ceiling capped at 4.0% daily",
                "Integrated Order Flow footprint and Volume Profile execution"
            )
            BotCapitalTier.LEVEL_10K_TO_100K -> listOf(
                "Institutional execution with Kyle Lambda market impact barrier",
                "Fat-tail adjusted Kelly Criterion and Student-t VaR 99% sizing",
                "Nobel stat-arb cointegration & Black-Scholes Greeks volatility delta hedge"
            )
            BotCapitalTier.ADVANCED_INSTITUTIONAL_TIER -> listOf(
                "Multi-market neural routing grid",
                "Dynamic hedging allocation (Long/Short spreads)",
                "Sovereign multi-regime optimization with dynamic RSI clusters"
            )
        }

        // Informative Swahili & English guard descriptions
        val swReason = if (isQualified) {
            "Sifa zote zimekidhiwa. Boti hii inaweza kuendeshwa kwa usalama na mtaji huu."
        } else {
            "Mtaji halisi wa sasa ($${String.format("%.2f", verifiedAccountBalance)}) hautoshi kuendesha boti hii ya kiwango cha ${bot.capitalTier.labelSwahili} kwa usalama. Inahitaji kiwango cha chini cha $${minCap}."
        }

        val enReason = if (isQualified) {
            "All conditions met. This bot is fully cleared for safe execution on current capital."
        } else {
            "Verified account capital ($${String.format("%.2f", verifiedAccountBalance)}) is below the safety threshold required for this ${bot.capitalTier.labelEn}. Requires minimum $${minCap} to prevent margin calls."
        }

        return BotExpertiseReport(
            botId = bot.id,
            botName = bot.name,
            capitalTier = bot.capitalTier,
            minRequiredUsd = minCap,
            maxRecommendedUsd = maxCap,
            recommendedLotSize = recommendedLot,
            recommendedLeverage = bot.recommendedLeverage,
            maxDrawdownLimitPct = bot.maxDrawdownPct,
            isQualified = isQualified,
            operationalMode = opMode,
            unlockedFeaturesSwahili = featuresSw,
            unlockedFeaturesEn = featuresEn,
            expertiseLevelSwahili = levelSw,
            expertiseLevelEn = levelEn,
            customGuardReasonSwahili = swReason,
            customGuardReasonEn = enReason
        )
    }

    /**
     * Backend qualification guard. Enforces that trades are only executed if capital constraints are met.
     */
    fun isExecutionPermittedByCapitalExpertise(bot: BotNode, balance: Double): Boolean {
        return balance >= bot.capitalTier.minCap
    }
}
