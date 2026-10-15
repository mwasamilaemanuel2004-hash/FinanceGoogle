/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — SAFE LEARNING GUARD (5 LAYERS TOTAL SECURITY)
 * ============================================================================
 * Unifies:
 * Layer 1: Immutable Risk Guard
 * Layer 2: Skill Curation (9 Tests + 24h Human Veto)
 * Layer 3: Bounded Learning (size * 0.9, max 5% rate)
 * Layer 4: Bad Behavior Detector & Automated Rollback
 * Layer 5: Human Veto & Real-Time Audit Transparency
 */

import { ImmutableRiskGuard } from '../guard/immutable_risk';
import { SkillCurationEngine, ProposedSkill, CurationEvaluation } from '../agents/skill_curation';
import { BoundedLearningEngine } from './bounded_learning';
import { BadBehaviorDetector, BadBehaviorIncident } from '../guard/bad_behavior_detector';

export interface HumanVetoPendingSkill {
  id: string;
  name: string;
  market: string;
  curationReport: CurationEvaluation;
  timeRemainingHours: number;
}

export class SafeLearningGuard {
  private static instance: SafeLearningGuard;
  private curationEngine: SkillCurationEngine;
  private boundedLearning: BoundedLearningEngine;
  private badBehaviorDetector: BadBehaviorDetector;
  private pendingVetoSkills: Map<string, HumanVetoPendingSkill> = new Map();
  private auditLog: string[] = [];

  private constructor() {
    this.curationEngine = new SkillCurationEngine();
    this.boundedLearning = BoundedLearningEngine.getInstance();
    this.badBehaviorDetector = BadBehaviorDetector.getInstance();
    this.seedDefaultPendingSkill();
  }

  private seedDefaultPendingSkill(): void {
    const demoSkill: ProposedSkill = {
      id: 'skill-deriv-vol75-curated-01',
      name: 'Deriv_Vol75_Even_Bias_v1',
      market: 'Volatility 75 Index',
      category: 'DERIV_SYNTHETIC',
      keywords: 'Volatility 75 even odd digit bias tunneling deriv synthetic',
      sampleTradesCount: 1000,
      trainSharpe: 1.5,
      walkForwardOosSharpe: 1.25,
      pboPct: 22.0,
      dsrValue: 0.42,
      cscvWinRatePct: 70.0,
      netExpectancyPct: 0.08,
      maxConsecutiveLosses: 4,
      digitBiasZScore: 2.3
    };
    const evalRes = this.curationEngine.evaluateSkill(demoSkill);
    if (evalRes.passedAllTests) {
      this.pendingVetoSkills.set(demoSkill.id, {
        id: demoSkill.id,
        name: demoSkill.name,
        market: demoSkill.market,
        curationReport: evalRes,
        timeRemainingHours: 23.5
      });
    }
  }

  public static getInstance(): SafeLearningGuard {
    if (!SafeLearningGuard.instance) {
      SafeLearningGuard.instance = new SafeLearningGuard();
    }
    return SafeLearningGuard.instance;
  }

  /**
   * Layer 1: Check proposed trade parameters against immutable boundaries
   */
  public checkImmutableRisk(params: any): { allowed: boolean; reason?: string } {
    return ImmutableRiskGuard.validateProposedParameters(params);
  }

  /**
   * Layer 2: Vet a newly invented skill through 9 curation tests
   */
  public vetProposedSkill(skill: ProposedSkill): CurationEvaluation {
    const evalResult = this.curationEngine.evaluateSkill(skill);
    if (evalResult.passedAllTests) {
      this.pendingVetoSkills.set(skill.id, {
        id: skill.id,
        name: skill.name,
        market: skill.market,
        curationReport: evalResult,
        timeRemainingHours: 24.0
      });
      this.logAudit(`Skill [${skill.name}] PASSED all 9 Curation Tests -> PENDING_HUMAN_VETO (24h Window).`);
    } else {
      this.logAudit(`Skill [${skill.name}] REJECTED by Curation Engine: ${evalResult.failureReason}`);
    }
    return evalResult;
  }

  /**
   * Layer 3: Handle trade outcome with bounded learning
   */
  public recordTradeOutcome(outcome: any) {
    const res = this.boundedLearning.onTradeOutcome(outcome);
    this.logAudit(res.actionTaken);
    return res;
  }

  /**
   * Layer 4: Audit autonomous behavior for bad traits
   */
  public auditAutonomousBehavior(context: any): { isClean: boolean; incident?: BadBehaviorIncident } {
    const res = this.badBehaviorDetector.auditDecision(context);
    if (!res.isClean && res.incident) {
      this.logAudit(`[ANOMALY BLOCKED]: ${res.incident.anomalyType} — ${res.incident.detectedValue}`);
    }
    return res;
  }

  /**
   * Layer 5: Human Veto Actions
   */
  public approveSkill(skillId: string): boolean {
    if (this.pendingVetoSkills.has(skillId)) {
      const item = this.pendingVetoSkills.get(skillId)!;
      this.pendingVetoSkills.delete(skillId);
      this.logAudit(`HUMAN APPROVED: Skill [${item.name}] promoted to LIVE ACTIVE with test size 0.1x.`);
      return true;
    }
    return false;
  }

  public rejectSkill(skillId: string): boolean {
    if (this.pendingVetoSkills.has(skillId)) {
      const item = this.pendingVetoSkills.get(skillId)!;
      this.pendingVetoSkills.delete(skillId);
      this.logAudit(`HUMAN VETO APPLIED: Skill [${item.name}] deleted from deployment pipeline.`);
      return true;
    }
    return false;
  }

  public getPendingVetoSkills(): HumanVetoPendingSkill[] {
    return Array.from(this.pendingVetoSkills.values());
  }

  public getAuditHistory(): string[] {
    return [...this.auditLog];
  }

  private logAudit(msg: string): void {
    const entry = `[${new Date().toISOString().substring(11, 19)}] ${msg}`;
    this.auditLog.unshift(entry);
    if (this.auditLog.length > 50) this.auditLog.pop();
  }
}
