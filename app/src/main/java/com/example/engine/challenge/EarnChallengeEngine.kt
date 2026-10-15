package com.example.engine.challenge

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Earn Challenge Tier Definition:
 * Level 1: $10 (or $0-$10) -> $1,000
 * Level 2: $1,000 -> $10,000
 * Level 3: $10,000 -> $100,000
 */
enum class ChallengeLevel(
    val levelNumber: Int,
    val titleEn: String,
    val titleSw: String,
    val startCapitalDefault: Double,
    val targetCapital: Double,
    val maxDrawdownAllowedPct: Double,
    val dailyLossLimitPct: Double,
    val recommendedLotFormula: String,
    val riskMultiplier: Double
) {
    LEVEL_1(
        levelNumber = 1,
        titleEn = "Micro to Starter ($10 to $1,000)",
        titleSw = "Ngazi ya 1: Mtaji Mdogo Hadi $1,000",
        startCapitalDefault = 10.0,
        targetCapital = 1000.0,
        maxDrawdownAllowedPct = 12.0,
        dailyLossLimitPct = 4.0,
        recommendedLotFormula = "0.01 - 0.05 lot micro",
        riskMultiplier = 1.0
    ),
    LEVEL_2(
        levelNumber = 2,
        titleEn = "Starter to Growth ($1,000 to $10,000)",
        titleSw = "Ngazi ya 2: Ukuaji wa Kati Hadi $10,000",
        startCapitalDefault = 1000.0,
        targetCapital = 10000.0,
        maxDrawdownAllowedPct = 10.0,
        dailyLossLimitPct = 3.0,
        recommendedLotFormula = "0.10 - 0.30 lot medium",
        riskMultiplier = 0.8
    ),
    LEVEL_3(
        levelNumber = 3,
        titleEn = "Growth to Master ($10,000 to $100,000)",
        titleSw = "Ngazi ya 3: Mtaalamu Mkuu Hadi $100,000",
        startCapitalDefault = 10000.0,
        targetCapital = 100000.0,
        maxDrawdownAllowedPct = 8.0,
        dailyLossLimitPct = 2.5,
        recommendedLotFormula = "0.50 - 2.00 lot institutional",
        riskMultiplier = 0.6
    )
}

enum class ChallengeTimeframeTarget(val labelEn: String, val labelSw: String, val days: Int) {
    DAYS_7("7 Days Sprint", "Mbio za Siku 7", 7),
    DAYS_14("14 Days Fast", "Siku 14 Kasi", 14),
    DAYS_30("30 Days Standard", "Mwezi 1 (Siku 30)", 30),
    DAYS_60("60 Days Balanced", "Miezi 2 (Siku 60)", 60),
    DAYS_90("90 Days Conservative", "Miezi 3 (Siku 90)", 90)
}

enum class ChallengeStatus {
    ACTIVE,
    PAUSED,
    TARGET_REACHED,
    FAILED_DRAWDOWN,
    COMPLETED
}

data class ChallengeMilestone(
    val milestonePct: Double, // 25%, 50%, 75%, 100%
    val reached: Boolean = false,
    val reachedTimestamp: Long = 0L,
    val equityAtMilestone: Double = 0.0
)

data class EarnChallengePlan(
    val id: String = UUID.randomUUID().toString(),
    val level: ChallengeLevel,
    val timeframeTarget: ChallengeTimeframeTarget,
    val initialCapital: Double,
    var currentEquity: Double,
    val targetCapital: Double,
    val startTime: Long = System.currentTimeMillis(),
    val deadlineTime: Long,
    var status: ChallengeStatus = ChallengeStatus.ACTIVE,
    var peakEquity: Double,
    var maxDrawdownExperiencedPct: Double = 0.0,
    var totalTradesExecuted: Int = 0,
    var winTradesCount: Int = 0,
    var lossTradesCount: Int = 0,
    var totalPnlUsd: Double = 0.0,
    var milestones: List<ChallengeMilestone> = listOf(
        ChallengeMilestone(25.0),
        ChallengeMilestone(50.0),
        ChallengeMilestone(75.0),
        ChallengeMilestone(100.0)
    )
) {
    val progressPct: Double
        get() {
            val totalSpan = (targetCapital - initialCapital).coerceAtLeast(1.0)
            val currentSpan = (currentEquity - initialCapital).coerceAtLeast(0.0)
            return ((currentSpan / totalSpan) * 100.0).coerceIn(0.0, 100.0)
        }

    val remainingDays: Int
        get() {
            val diffMs = deadlineTime - System.currentTimeMillis()
            return ((diffMs / (1000L * 60 * 60 * 24)).coerceAtLeast(0L)).toInt()
        }
}

object EarnChallengeEngine {

    private val _currentChallenge = MutableStateFlow<EarnChallengePlan?>(
        EarnChallengePlan(
            level = ChallengeLevel.LEVEL_1,
            timeframeTarget = ChallengeTimeframeTarget.DAYS_30,
            initialCapital = 10.0,
            currentEquity = 10.0,
            targetCapital = 1000.0,
            peakEquity = 10.0,
            deadlineTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L)
        )
    )
    val currentChallenge: StateFlow<EarnChallengePlan?> = _currentChallenge.asStateFlow()

    private val _challengeHistory = MutableStateFlow<List<EarnChallengePlan>>(emptyList())
    val challengeHistory: StateFlow<List<EarnChallengePlan>> = _challengeHistory.asStateFlow()

    fun startChallenge(
        level: ChallengeLevel,
        startCapital: Double,
        timeframe: ChallengeTimeframeTarget
    ): EarnChallengePlan {
        val safeStart = if (startCapital > 0.0) startCapital else level.startCapitalDefault
        val deadline = System.currentTimeMillis() + (timeframe.days.toLong() * 24 * 60 * 60 * 1000L)
        val plan = EarnChallengePlan(
            level = level,
            timeframeTarget = timeframe,
            initialCapital = safeStart,
            currentEquity = safeStart,
            targetCapital = level.targetCapital,
            peakEquity = safeStart,
            deadlineTime = deadline
        )
        _currentChallenge.value = plan
        return plan
    }

    fun recordTradeOutcome(pnlUsd: Double, isWin: Boolean) {
        val plan = _currentChallenge.value ?: return
        if (plan.status != ChallengeStatus.ACTIVE) return

        val newEquity = (plan.currentEquity + pnlUsd).coerceAtLeast(0.1)
        val newPeak = maxOf(plan.peakEquity, newEquity)
        val currentDd = if (newPeak > 0) ((newPeak - newEquity) / newPeak) * 100.0 else 0.0
        val maxDd = maxOf(plan.maxDrawdownExperiencedPct, currentDd)

        plan.currentEquity = newEquity
        plan.peakEquity = newPeak
        plan.maxDrawdownExperiencedPct = maxDd
        plan.totalTradesExecuted += 1
        plan.totalPnlUsd += pnlUsd
        if (isWin) plan.winTradesCount += 1 else plan.lossTradesCount += 1

        // Check Drawdown Violation
        if (maxDd >= plan.level.maxDrawdownAllowedPct) {
            plan.status = ChallengeStatus.FAILED_DRAWDOWN
        } else if (newEquity >= plan.targetCapital) {
            plan.status = ChallengeStatus.TARGET_REACHED
        }

        // Update milestones
        val updatedMilestones = plan.milestones.map { m ->
            val requiredEq = plan.initialCapital + ((plan.targetCapital - plan.initialCapital) * (m.milestonePct / 100.0))
            if (!m.reached && newEquity >= requiredEq) {
                m.copy(reached = true, reachedTimestamp = System.currentTimeMillis(), equityAtMilestone = newEquity)
            } else {
                m
            }
        }
        plan.milestones = updatedMilestones

        _currentChallenge.value = plan.copy()
    }

    fun resetOrCancelChallenge() {
        val cur = _currentChallenge.value
        if (cur != null) {
            _challengeHistory.value = listOf(cur) + _challengeHistory.value.take(19)
        }
        _currentChallenge.value = null
    }
}
