package com.example.engine.lib.quant_core

import kotlin.math.*

data class BayesEvDecision(
    val priorProbability: Double, // P(H) from market regime base rate
    val evidenceLikelihood: Double, // P(E|H) from ensemble models (KNN, RF, XGB)
    val marginalEvidenceProb: Double, // P(E)
    val posteriorProbability: Double, // P(H|E) Bayesian updated win rate
    val expectedValueDollars: Double, // EV = P*win - (1-P)*loss
    val evToRiskRatio: Double,
    val isPositiveEv: Boolean,
    val recommendedKellyFraction: Double,
    val confidenceGrade: String // "GRADE_A_PRIME", "GRADE_B", "MARGINAL", "NEGATIVE_EV"
)

object ExpectedValueAndBayesEngine {

    /**
     * Bayes' Theorem: P(H|E) = (P(E|H) * P(H)) / P(E)
     *
     * @param regimePrior Base win rate in this regime (e.g., 0.54 in Trending, 0.48 in Compression)
     * @param ensembleLikelihood True positive rate of current predictive ensemble
     * @param falsePositiveRate False positive rate of signals in adverse conditions (e.g., 0.35)
     * @param expectedWinReward Dollars gained on win
     * @param expectedLossCost Dollars lost on stop loss
     */
    fun evaluateBayesianTrade(
        regimePrior: Double,
        ensembleLikelihood: Double,
        falsePositiveRate: Double = 0.35,
        expectedWinReward: Double = 2.0, // Risk-Reward ratio 2:1
        expectedLossCost: Double = 1.0
    ): BayesEvDecision {
        val safePrior = regimePrior.coerceIn(0.1, 0.9)
        val safeLikelihood = ensembleLikelihood.coerceIn(0.1, 0.99)
        val safeFp = falsePositiveRate.coerceIn(0.01, 0.8)

        // P(E) = P(E|H)*P(H) + P(E|~H)*P(~H)
        val marginalE = safeLikelihood * safePrior + safeFp * (1.0 - safePrior)

        // P(H|E) = (P(E|H) * P(H)) / P(E)
        val posterior = ((safeLikelihood * safePrior) / marginalE.coerceAtLeast(1e-6)).coerceIn(0.01, 0.99)

        // Mathematical Expected Value (EV):
        // EV = P * WinReward - (1 - P) * LossCost
        val ev = posterior * expectedWinReward - (1.0 - posterior) * expectedLossCost
        val isPositiveEv = ev > 0.05

        val evToRiskRatio = if (expectedLossCost > 0) ev / expectedLossCost else 0.0

        // Kelly fraction f* = (p*b - q) / b where b = WinReward/LossCost, q = 1 - p
        val b = (expectedWinReward / expectedLossCost.coerceAtLeast(1e-6)).coerceAtLeast(0.1)
        val q = 1.0 - posterior
        val rawKelly = (posterior * b - q) / b
        val safeHalfKelly = (rawKelly / 2.0).coerceIn(0.0, 0.25)

        val confidenceGrade = when {
            posterior >= 0.70 && isPositiveEv -> "GRADE_A_PRIME"
            posterior >= 0.58 && isPositiveEv -> "GRADE_B"
            isPositiveEv -> "MARGINAL"
            else -> "NEGATIVE_EV"
        }

        return BayesEvDecision(
            priorProbability = safePrior,
            evidenceLikelihood = safeLikelihood,
            marginalEvidenceProb = marginalE,
            posteriorProbability = posterior,
            expectedValueDollars = ev,
            evToRiskRatio = evToRiskRatio,
            isPositiveEv = isPositiveEv,
            recommendedKellyFraction = if (isPositiveEv) safeHalfKelly else 0.0,
            confidenceGrade = confidenceGrade
        )
    }
}
