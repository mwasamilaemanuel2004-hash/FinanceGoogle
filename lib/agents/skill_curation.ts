/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — SKILL CURATION (LAYER 2 OF SAFE LEARNING)
 * ============================================================================
 * Every autonomously invented or modified skill MUST pass 9 rigorous tests:
 * 1. Walk-Forward OOS Sharpe >= 1.0
 * 2. PBO (Probability of Backtest Overfitting) < 30%
 * 3. DSR (Deflated Sharpe Ratio) > 0
 * 4. CSCV (Combinatorial Symmetric Cross Validation) Win > 60%
 * 5. Minimum 100 trades sample
 * 6. Expectancy (EV) > 0.03% net of fees
 * 7. Max consecutive losses < 8
 * 8. Correlation with existing skills < 0.70 (deduplication via RAG)
 * 9. Deriv digits specific: Z-Score > 2.0 over 1000 ticks
 */

import { RagEngine } from '../rag/rag_engine';

export interface ProposedSkill {
  id: string;
  name: string;
  market: string;
  category: string;
  keywords: string;
  sampleTradesCount: number;
  trainSharpe: number;
  walkForwardOosSharpe: number;
  pboPct: number;
  dsrValue: number;
  cscvWinRatePct: number;
  netExpectancyPct: number;
  maxConsecutiveLosses: number;
  digitBiasZScore?: number;
}

export interface CurationTestResult {
  testNumber: number;
  testName: string;
  passed: boolean;
  actualValue: string;
  requiredCriteria: string;
}

export interface CurationEvaluation {
  skillId: string;
  skillName: string;
  passedAllTests: boolean;
  testResults: CurationTestResult[];
  status: 'REJECTED_CURATION' | 'PENDING_HUMAN_VETO';
  vetoExpiresAt?: string;
  failureReason?: string;
}

export class SkillCurationEngine {
  private ragEngine: RagEngine;

  constructor() {
    this.ragEngine = RagEngine.getInstance();
  }

  public evaluateSkill(skill: ProposedSkill): CurationEvaluation {
    const results: CurationTestResult[] = [];

    // 1. Walk-Forward OOS Sharpe >= 1.0
    const passOos = skill.walkForwardOosSharpe >= 1.0;
    results.push({
      testNumber: 1,
      testName: 'Walk-Forward Out-Of-Sample Sharpe',
      passed: passOos,
      actualValue: skill.walkForwardOosSharpe.toFixed(2),
      requiredCriteria: '>= 1.0'
    });

    // 2. PBO < 30%
    const passPbo = skill.pboPct < 30.0;
    results.push({
      testNumber: 2,
      testName: 'Probability of Backtest Overfitting (PBO)',
      passed: passPbo,
      actualValue: `${skill.pboPct.toFixed(1)}%`,
      requiredCriteria: '< 30.0%'
    });

    // 3. DSR > 0
    const passDsr = skill.dsrValue > 0.0;
    results.push({
      testNumber: 3,
      testName: 'Deflated Sharpe Ratio (DSR)',
      passed: passDsr,
      actualValue: skill.dsrValue.toFixed(3),
      requiredCriteria: '> 0.0'
    });

    // 4. CSCV Win Rate > 60%
    const passCscv = skill.cscvWinRatePct >= 60.0;
    results.push({
      testNumber: 4,
      testName: 'CSCV Symmetric Cross-Validation Win Folds',
      passed: passCscv,
      actualValue: `${skill.cscvWinRatePct.toFixed(1)}%`,
      requiredCriteria: '>= 60.0%'
    });

    // 5. Minimum 100 Trades Sample
    const passSample = skill.sampleTradesCount >= 100;
    results.push({
      testNumber: 5,
      testName: 'Minimum Trades Sample Size',
      passed: passSample,
      actualValue: `${skill.sampleTradesCount} trades`,
      requiredCriteria: '>= 100 trades'
    });

    // 6. Net Expectancy EV > 0.03%
    const passEv = skill.netExpectancyPct > 0.03;
    results.push({
      testNumber: 6,
      testName: 'Net Expectancy (EV) per Trade',
      passed: passEv,
      actualValue: `+${(skill.netExpectancyPct * 100).toFixed(2)}%`,
      requiredCriteria: '> +0.03%'
    });

    // 7. Max Consecutive Losses < 8
    const passLossStreak = skill.maxConsecutiveLosses < 8;
    results.push({
      testNumber: 7,
      testName: 'Max Consecutive Loss Sequence',
      passed: passLossStreak,
      actualValue: `${skill.maxConsecutiveLosses} losses`,
      requiredCriteria: '< 8 losses'
    });

    // 8. Correlation with Existing Skills < 0.70 via RAG
    const originalityCheck = this.ragEngine.verifySkillOriginality(skill.keywords);
    const passOriginality = originalityCheck.isOriginal && originalityCheck.correlation < 0.70;
    results.push({
      testNumber: 8,
      testName: 'RAG Knowledge Deduplication & Originality',
      passed: passOriginality,
      actualValue: `Max correlation: ${(originalityCheck.correlation * 100).toFixed(1)}%`,
      requiredCriteria: '< 70.0% correlation'
    });

    // 9. Deriv Digits Z-Score > 2.0 (if applicable)
    let passDigits = true;
    if (skill.digitBiasZScore !== undefined) {
      passDigits = skill.digitBiasZScore >= 2.0;
      results.push({
        testNumber: 9,
        testName: 'Deriv Digits Statistical Significance (Z-Score)',
        passed: passDigits,
        actualValue: `Z = ${skill.digitBiasZScore.toFixed(2)}`,
        requiredCriteria: 'Z >= 2.0'
      });
    } else {
      results.push({
        testNumber: 9,
        testName: 'Market Type Specific Significance',
        passed: true,
        actualValue: 'N/A (Non-digit asset)',
        requiredCriteria: 'Satisfied'
      });
    }

    const passedAllTests = results.every(r => r.passed);
    const vetoExpiry = new Date(Date.now() + 24 * 3600 * 1000).toISOString();

    return {
      skillId: skill.id,
      skillName: skill.name,
      passedAllTests,
      testResults: results,
      status: passedAllTests ? 'PENDING_HUMAN_VETO' : 'REJECTED_CURATION',
      vetoExpiresAt: passedAllTests ? vetoExpiry : undefined,
      failureReason: passedAllTests ? undefined : results.filter(r => !r.passed).map(r => r.testName).join(', ')
    };
  }
}
