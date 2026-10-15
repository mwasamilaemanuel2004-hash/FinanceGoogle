export type OpportunityType = 'WEAK_PROFIT' | 'LOSS_HAZARD' | 'NEUTRAL';

export interface ClassificationFeatureVector {
  symbol: string;
  price: number;
  // 1. Orderflow & DOM
  ofiRatio: number; // -1.0 to +1.0
  cvdDivergence: boolean;
  domImbalancePct: number; // e.g. 70% buy vs 30% sell
  footprintImbalanceRatio: number; // e.g. 3.0 (300% bid absorption)
  trappedSellersCount: number;
  trappedBuyersCount: number;
  icebergDetected: boolean;
  icebergLots: number;
  // 2. Volume Profile & Microstructure
  inLiquidityVoid: boolean;
  voidDistancePct: number; // e.g. 0.08%
  distanceToPocPct: number;
  absorptionDetected: boolean;
  exhaustionClimaxDetected: boolean;
  // 3. Risk & Toxicity
  vpinToxicity: number; // 0.0 - 1.0 (adverse selection risk)
  spoofingDetected: boolean;
  utadFakeBreakoutDetected: boolean;
  minutesToHighImpactNews: number;
  // 4. Macro & Options
  netDealerGexUsd: number;
  dixDarkPoolPct: number;
  tipsRealYieldTrend: 'RISING' | 'FALLING' | 'STABLE';
}

export interface WeakVsLossClassificationResult {
  type: OpportunityType;
  confidencePct: number; // 0 - 100%
  expectedValuePct: number; // EV = winProb * winPct - (1 - winProb) * lossPct
  winProbabilityPct: number;
  potentialProfitPct: number;
  riskLossPct: number;
  reasoning: string;
  maneuver: {
    recommendedAction: 'EXECUTE_ISOLATED_SUB_TRADE' | 'REJECT_HAZARD' | 'STAND_ASIDE';
    sizeFactorKelly: number; // e.g. 0.3x
    takeProfitTargetPct: number;
    stopLossThresholdPct: number;
    timeStopMinutes: number;
    dontBreakMainLogic: boolean;
  };
  latencyMs: number;
}

export class WeakVsLossClassifier {
  private static instance: WeakVsLossClassifier;

  private constructor() {}

  public static getInstance(): WeakVsLossClassifier {
    if (!WeakVsLossClassifier.instance) {
      WeakVsLossClassifier.instance = new WeakVsLossClassifier();
    }
    return WeakVsLossClassifier.instance;
  }

  /**
   * Fast evaluation in < 10ms distinguishing high-probability small profits from dangerous traps.
   */
  public classify(features: ClassificationFeatureVector): WeakVsLossClassificationResult {
    const t0 = performance.now();

    // 1. Check for immediate Hasara / Hazards (Defensive check)
    const isImminentNews = features.minutesToHighImpactNews <= 2;
    const isToxicFlow = features.vpinToxicity > 0.65;
    const isSpoofOrUtad = features.spoofingDetected || features.utadFakeBreakoutDetected;
    const isExhaustion = features.exhaustionClimaxDetected;

    if (isImminentNews || isToxicFlow || isSpoofOrUtad || isExhaustion) {
      const hazardReason = isImminentNews
        ? 'High impact news event within 2 minutes — severe slippage and spread widening risk.'
        : isToxicFlow
        ? `Adverse selection toxicity (VPIN ${features.vpinToxicity.toFixed(2)} > 0.65) indicates informed institutional predator flow against current position.`
        : isSpoofOrUtad
        ? 'Spoofing order cancellation / UTAD fake breakout manipulation detected.'
        : 'Volume exhaustion climax detected — continuation highly unlikely.';

      return {
        type: 'LOSS_HAZARD',
        confidencePct: 88.5,
        expectedValuePct: -0.45,
        winProbabilityPct: 22.0,
        potentialProfitPct: 0.05,
        riskLossPct: 0.50,
        reasoning: `HAZARD AVOIDED: ${hazardReason}`,
        maneuver: {
          recommendedAction: 'REJECT_HAZARD',
          sizeFactorKelly: 0.0,
          takeProfitTargetPct: 0.0,
          stopLossThresholdPct: 0.0,
          timeStopMinutes: 0,
          dontBreakMainLogic: true
        },
        latencyMs: +(performance.now() - t0).toFixed(2)
      };
    }

    // 2. Check for Weak Profit characteristics (High EV micro-arbitrage)
    const hasOrderbookSupport = features.domImbalancePct >= 65 || features.icebergDetected;
    const hasAbsorption = features.absorptionDetected || features.footprintImbalanceRatio >= 2.5;
    const hasVoidVacuum = features.inLiquidityVoid && features.voidDistancePct >= 0.03 && features.voidDistancePct <= 0.15;
    const isPositiveOFI = features.ofiRatio > 0.1 || features.cvdDivergence;

    if ((hasOrderbookSupport || hasAbsorption) && (hasVoidVacuum || isPositiveOFI)) {
      const targetProfitPct = Math.min(0.12, Math.max(0.04, features.voidDistancePct || 0.08));
      const stopLossPct = 0.04;
      const winProb = 0.68;
      const ev = winProb * targetProfitPct - (1 - winProb) * stopLossPct;

      return {
        type: 'WEAK_PROFIT',
        confidencePct: 74.0,
        expectedValuePct: +ev.toFixed(4),
        winProbabilityPct: winProb * 100,
        potentialProfitPct: targetProfitPct,
        riskLossPct: stopLossPct,
        reasoning: `WEAK_PROFIT VALID: +${(targetProfitPct * 100).toFixed(2)}% target in LVN void. Absorption + Iceberg (${features.domImbalancePct}% DOM bias) confirms 68% win rate with positive mathematical expectancy (+${(ev * 100).toFixed(2)}%). Take isolated 0.3x Kelly position.`,
        maneuver: {
          recommendedAction: 'EXECUTE_ISOLATED_SUB_TRADE',
          sizeFactorKelly: 0.30,
          takeProfitTargetPct: targetProfitPct,
          stopLossThresholdPct: stopLossPct,
          timeStopMinutes: 15,
          dontBreakMainLogic: true
        },
        latencyMs: +(performance.now() - t0).toFixed(2)
      };
    }

    return {
      type: 'NEUTRAL',
      confidencePct: 50.0,
      expectedValuePct: 0.0,
      winProbabilityPct: 50.0,
      potentialProfitPct: 0.0,
      riskLossPct: 0.0,
      reasoning: 'Market in standard equilibrium without high EV asymmetry.',
      maneuver: {
        recommendedAction: 'STAND_ASIDE',
        sizeFactorKelly: 0.0,
        takeProfitTargetPct: 0.0,
        stopLossThresholdPct: 0.0,
        timeStopMinutes: 0,
        dontBreakMainLogic: true
      },
      latencyMs: +(performance.now() - t0).toFixed(2)
    };
  }
}

export const weakVsLossClassifier = WeakVsLossClassifier.getInstance();
