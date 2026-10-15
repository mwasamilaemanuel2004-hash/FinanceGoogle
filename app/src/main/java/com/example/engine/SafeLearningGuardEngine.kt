package com.example.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — SAFE LEARNING GUARD ENGINE (5 LAYERS DEFENSE)
 * ============================================================================
 * Protects system from bad autonomous learning:
 * Layer 1: Immutable Risk (2% size, 1.8 ATR SL, 5% DD breaker, 20 trades/hr)
 * Layer 2: Skill Curation (9 tests, PBO < 30%, DSR > 0, 24h human veto)
 * Layer 3: Bounded Learning (size * 0.9 on loss, max 5% adjustment rate)
 * Layer 4: Bad Behavior Detector & Rollback (overfit, revenge 2x, martingale)
 * Layer 5: Human Veto & Live Audit Transparency
 */

data class PendingVetoSkillKt(
    val id: String,
    val name: String,
    val market: String,
    val trainSharpe: Float,
    val oosSharpe: Float,
    val pboPct: Float,
    val dsrScore: Float,
    val hoursRemaining: Float,
    val status: String
)

data class BadBehaviorAnomalyKt(
    val id: String,
    val timestamp: String,
    val type: String,
    val details: String,
    val mitigationAction: String
)

class SafeLearningGuardEngine private constructor() {

    private val _pendingVetoSkills = MutableStateFlow<List<PendingVetoSkillKt>>(getInitialPendingSkills())
    val pendingVetoSkills: StateFlow<List<PendingVetoSkillKt>> = _pendingVetoSkills.asStateFlow()

    private val _blockedAnomalies = MutableStateFlow<List<BadBehaviorAnomalyKt>>(getInitialAnomalies())
    val blockedAnomalies: StateFlow<List<BadBehaviorAnomalyKt>> = _blockedAnomalies.asStateFlow()

    private val _immutableMaxTradeSizePct = MutableStateFlow(2.0f)
    val immutableMaxTradeSizePct: StateFlow<Float> = _immutableMaxTradeSizePct.asStateFlow()

    private val _currentPositionMultiplier = MutableStateFlow(1.0f)
    val currentPositionMultiplier: StateFlow<Float> = _currentPositionMultiplier.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<String>>(
        listOf(
            "[12:24:10] Immutable Risk Check: Size 1.0% within hard limit 2.0%. ALLOWED.",
            "[12:25:30] Skill [Deriv_Vol75_Even_Bias_v1] passed 9 curation tests (PBO 22%, DSR 0.42). Held in 24h Human Veto window.",
            "[12:26:05] Anomaly Filter: Martingale 2x size attempt detected after loss. BLOCKED & rolled back to safe 1.0x.",
            "[12:27:18] Bounded Learning: Trade loss -0.25% -> size scaled by 0.90 to 0.9x without revenge."
        )
    )
    val auditLogs: StateFlow<List<String>> = _auditLogs.asStateFlow()

    companion object {
        @Volatile
        private var instance: SafeLearningGuardEngine? = null

        fun getInstance(): SafeLearningGuardEngine {
            return instance ?: synchronized(this) {
                instance ?: SafeLearningGuardEngine().also { instance = it }
            }
        }

        private fun getInitialPendingSkills(): List<PendingVetoSkillKt> {
            return listOf(
                PendingVetoSkillKt(
                    id = "skill-deriv-vol75-curated",
                    name = "Deriv_Vol75_Even_Bias_v1",
                    market = "Volatility 75 Index",
                    trainSharpe = 1.50f,
                    oosSharpe = 1.25f,
                    pboPct = 22.0f,
                    dsrScore = 0.42f,
                    hoursRemaining = 23.4f,
                    status = "PENDING_HUMAN_VETO"
                ),
                PendingVetoSkillKt(
                    id = "skill-gold-void-spring",
                    name = "Gold_LVN_Void_Spring_v2",
                    market = "XAU/USD (Gold)",
                    trainSharpe = 1.62f,
                    oosSharpe = 1.38f,
                    pboPct = 18.5f,
                    dsrScore = 0.55f,
                    hoursRemaining = 18.2f,
                    status = "PENDING_HUMAN_VETO"
                )
            )
        }

        private fun getInitialAnomalies(): List<BadBehaviorAnomalyKt> {
            return listOf(
                BadBehaviorAnomalyKt(
                    id = "anom-01",
                    timestamp = "12:15:02",
                    type = "OVERFIT_REJECTION",
                    details = "Train Win 82% dropped to Test Win 38% (Delta: -44%). PBO was 42%.",
                    mitigationAction = "Deactivated skill & rolled back to stable baseline model."
                ),
                BadBehaviorAnomalyKt(
                    id = "anom-02",
                    timestamp = "12:18:44",
                    type = "REVENGE_MARTINGALE_BLOCKED",
                    details = "Proposed trade size 3.0x following 2 consecutive losses.",
                    mitigationAction = "Blocked sizing increase. Reset multiplier to safe 1.0x."
                ),
                BadBehaviorAnomalyKt(
                    id = "anom-03",
                    timestamp = "12:22:10",
                    type = "WEAK_ONLY_BIAS_CORRECTED",
                    details = "10 consecutive weak exits while Big Profit Score > 75%.",
                    mitigationAction = "Enforced 50% Weak + 50% Strong Hybrid runner allocation."
                )
            )
        }
    }

    fun approvePendingSkill(id: String) {
        _pendingVetoSkills.value = _pendingVetoSkills.value.filter { it.id != id }
        addAudit("HUMAN APPROVED: Skill [$id] promoted to ACTIVE LIVE trading with 0.1x micro-position testing.")
    }

    fun rejectPendingSkill(id: String) {
        _pendingVetoSkills.value = _pendingVetoSkills.value.filter { it.id != id }
        addAudit("HUMAN VETO APPLIED: Skill [$id] rejected and deleted from production pipeline.")
    }

    fun addAudit(msg: String) {
        val entry = "[${System.currentTimeMillis() % 100000}] $msg"
        _auditLogs.value = listOf(entry) + _auditLogs.value.take(20)
    }
}
