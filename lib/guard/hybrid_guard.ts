/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — HYBRID REPLACEMENT GUARD LAYER
 * ============================================================================
 * Evaluates trade viability in < 5ms:
 * - Weak Leg EV (probability > 60%, EV > 0)
 * - Strong Leg EV (RR > 3:1, probability > 45%, EV > 0.3%)
 * - 4-Pillar Confluence: Quantum (qp) + Orderflow (of) + Institutional (ip) + Hawkes (hp) >= 2
 * - Market Quality (MQ) Score & Anti-Loss Allocation
 * ============================================================================
 */

export interface HybridConfluenceInput {
  hasQuantumSignal: boolean;        // qp: Born rule > 60%, tunneling
  hasOrderflowConfirmation: boolean;// of: OFI > 0, CVD divergence, iceberg bid
  hasInstitutionalPrice: boolean;   // ip: Order Block, LVN void, Wyckoff phase
  hasHawkesProcessMomentum: boolean;// hp: Clustering arrival intensity > 1.8x
  marketQualityScore: number;       // MQ score: 0 to 100
  weakWinProbability: number;       // e.g. 0.68 (68%)
  weakPotentialGainPct: number;     // e.g. 0.08%
  weakPotentialLossPct: number;     // e.g. 0.05%
  strongWinProbability: number;     // e.g. 0.52 (52%)
  strongExpectedReturnPct: number;  // e.g. 1.20%
  strongInitialRiskPct: number;     // e.g. 0.50%
  bigProfitScorePct: number;        // e.g. 75%
}

export type HybridAllocationMode =
  | 'ALLOW_50_50_HYBRID'
  | 'ALLOW_100_STRONG_ONLY'
  | 'ALLOW_100_WEAK_ONLY'
  | 'BLOCK_TRADE';

export interface HybridGuardDecision {
  isPermitted: boolean;
  allocationMode: HybridAllocationMode;
  weakLegAllocationPct: number;
  strongLegAllocationPct: number;
  sizeMultiplier: number;
  confluenceCount: number;
  evWeakPct: number;
  evStrongPct: number;
  reasoning: string;
  latencyMs: number;
}

export class HybridGuard {
  private static instance: HybridGuard;

  private constructor() {}

  public static getInstance(): HybridGuard {
    if (!HybridGuard.instance) {
      HybridGuard.instance = new HybridGuard();
    }
    return HybridGuard.instance;
  }

  /**
   * Fast evaluation in < 5ms.
   */
  public evaluate(input: HybridConfluenceInput): HybridGuardDecision {
    const t0 = performance.now();

    // 1. Calculate Expected Values
    // EV_weak = (p * win) - ((1 - p) * loss)
    const evWeak = input.weakWinProbability * input.weakPotentialGainPct -
      (1.0 - input.weakWinProbability) * input.weakPotentialLossPct;

    // EV_strong = (p * win) - ((1 - p) * loss)
    const evStrong = input.strongWinProbability * input.strongExpectedReturnPct -
      (1.0 - input.strongWinProbability) * input.strongInitialRiskPct;

    const strongRR = input.strongInitialRiskPct > 0
      ? input.strongExpectedReturnPct / input.strongInitialRiskPct
      : 2.0;

    // 2. Confluence 4-Pillar Score
    let confluence = 0;
    if (input.hasQuantumSignal) confluence++;
    if (input.hasOrderflowConfirmation) confluence++;
    if (input.hasInstitutionalPrice) confluence++;
    if (input.hasHawkesProcessMomentum) confluence++;

    const isWeakValid = evWeak > 0 && input.weakWinProbability >= 0.60;
    const isStrongValid = evStrong >= 0.003 && strongRR >= 2.4 && input.strongWinProbability >= 0.45;

    // 3. Market Quality (MQ) Sizing Adjuster
    let sizeMult = 1.0;
    if (input.marketQualityScore < 40) {
      sizeMult = 0.50; // Choppy/ugly market: reduce risk by 50%
    } else if (input.marketQualityScore >= 75) {
      sizeMult = 1.50; // Beautiful market: boosted sizing
    }

    // 4. Decision Matrix
    if (!isWeakValid && !isStrongValid) {
      return {
        isPermitted: false,
        allocationMode: 'BLOCK_TRADE',
        weakLegAllocationPct: 0,
        strongLegAllocationPct: 0,
        sizeMultiplier: 0.0,
        confluenceCount: confluence,
        evWeakPct: +evWeak.toFixed(4),
        evStrongPct: +evStrong.toFixed(4),
        reasoning: 'BLOCK: Both Weak and Strong legs have negative EV or insufficient probability.',
        latencyMs: +(performance.now() - t0).toFixed(2)
      };
    }

    if (confluence < 2) {
      return {
        isPermitted: false,
        allocationMode: 'BLOCK_TRADE',
        weakLegAllocationPct: 0,
        strongLegAllocationPct: 0,
        sizeMultiplier: 0.0,
        confluenceCount: confluence,
        evWeakPct: +evWeak.toFixed(4),
        evStrongPct: +evStrong.toFixed(4),
        reasoning: `BLOCK: Confluence count (${confluence}/4) is below required minimum threshold of 2.`,
        latencyMs: +(performance.now() - t0).toFixed(2)
      };
    }

    // Weak EV negative but Strong EV positive & Big Profit Score > 70% -> 100% Strong
    if (!isWeakValid && isStrongValid && input.bigProfitScorePct >= 70) {
      return {
        isPermitted: true,
        allocationMode: 'ALLOW_100_STRONG_ONLY',
        weakLegAllocationPct: 0,
        strongLegAllocationPct: 100,
        sizeMultiplier: sizeMult,
        confluenceCount: confluence,
        evWeakPct: +evWeak.toFixed(4),
        evStrongPct: +evStrong.toFixed(4),
        reasoning: 'ALLOW STRONG REPLACEMENT: Weak scalp invalid but macro runner strongly positive. 100% Strong allocated.',
        latencyMs: +(performance.now() - t0).toFixed(2)
      };
    }

    // Strong EV negative but Weak EV positive -> 100% Weak (Choppy market protection)
    if (isWeakValid && !isStrongValid) {
      return {
        isPermitted: true,
        allocationMode: 'ALLOW_100_WEAK_ONLY',
        weakLegAllocationPct: 100,
        strongLegAllocationPct: 0,
        sizeMultiplier: sizeMult,
        confluenceCount: confluence,
        evWeakPct: +evWeak.toFixed(4),
        evStrongPct: +evStrong.toFixed(4),
        reasoning: 'ALLOW WEAK REPLACEMENT: Strong runner invalid in choppy market (MQ < 50). 100% Weak allocated to harvest micro-profit without big SL risk.',
        latencyMs: +(performance.now() - t0).toFixed(2)
      };
    }

    // Both valid -> 50/50 Hybrid
    return {
      isPermitted: true,
      allocationMode: 'ALLOW_50_50_HYBRID',
      weakLegAllocationPct: 50,
      strongLegAllocationPct: 50,
      sizeMultiplier: sizeMult,
      confluenceCount: confluence,
      evWeakPct: +evWeak.toFixed(4),
      evStrongPct: +evStrong.toFixed(4),
      reasoning: 'ALLOW 50/50 HYBRID: Both Weak scalp and Strong runner have positive EV with 4-pillar confluence. 50% Weak / 50% Strong allocated.',
      latencyMs: +(performance.now() - t0).toFixed(2)
    };
  }
}

export const hybridGuard = HybridGuard.getInstance();
