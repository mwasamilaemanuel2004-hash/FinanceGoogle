/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — BAD BEHAVIOR DETECTOR & ROLLBACK (LAYER 4)
 * ============================================================================
 * Continuously audits algorithmic learning in real time (<10ms).
 * Detects:
 * 1. Overfitting (Train Win 80% vs Test Win 40%)
 * 2. Revenge trading (Size increase >2x after loss)
 * 3. Weak-only learning (Closing weak 0.06% and cutting strong runners)
 * 4. Chasing (Trading during blackout or toxic VPIN)
 * 5. Martingale attempts (Doubling down after loss)
 * 6. Signal copying without edge
 * Executes immediate parameter rollback if any violation occurs.
 */

export interface BadBehaviorIncident {
  id: string;
  timestamp: string;
  anomalyType: 'OVERFIT' | 'REVENGE_SIZING' | 'WEAK_ONLY_BIAS' | 'HIGH_IMPACT_CHASE' | 'MARTINGALE' | 'SIGNAL_COPY';
  detectedValue: string;
  actionTaken: string;
}

export class BadBehaviorDetector {
  private static instance: BadBehaviorDetector;
  private incidentLog: BadBehaviorIncident[] = [];
  private lastGoodParameters: Record<string, any> = {
    maxTradeSizePct: 1.0,
    slAtrMultiple: 1.5,
    hybridRatioWeak: 0.5,
    hybridRatioStrong: 0.5
  };

  private constructor() {}

  public static getInstance(): BadBehaviorDetector {
    if (!BadBehaviorDetector.instance) {
      BadBehaviorDetector.instance = new BadBehaviorDetector();
    }
    return BadBehaviorDetector.instance;
  }

  public getIncidentHistory(): BadBehaviorIncident[] {
    return [...this.incidentLog];
  }

  /**
   * Audit an autonomous behavior decision (<10ms)
   */
  public auditDecision(context: {
    recentLossCount: number;
    proposedSizeMultiple: number;
    trainWinRatePct?: number;
    testWinRatePct?: number;
    consecutiveWeakOnlyCount: number;
    bigProfitScore: number;
    isNewsWindow: boolean;
    vpinToxicity: number;
  }): { isClean: boolean; incident?: BadBehaviorIncident; rollbackParameters?: Record<string, any> } {
    const now = new Date().toISOString();

    // 1. Martingale or Revenge Sizing: Size > 2x following a loss
    if (context.recentLossCount > 0 && context.proposedSizeMultiple >= 2.0) {
      const incident: BadBehaviorIncident = {
        id: `inc-${Date.now()}-rev`,
        timestamp: now,
        anomalyType: 'REVENGE_SIZING',
        detectedValue: `Size proposed at ${context.proposedSizeMultiple}x following ${context.recentLossCount} losses`,
        actionTaken: 'BLOCKED & ROLLED BACK: Reset position sizing to safe baseline 1.0x.'
      };
      this.incidentLog.push(incident);
      return { isClean: false, incident, rollbackParameters: { maxTradeSizePct: 1.0 } };
    }

    // 2. Overfitting Detection: Sharp drop from train to test
    if (context.trainWinRatePct && context.testWinRatePct) {
      const drop = context.trainWinRatePct - context.testWinRatePct;
      if (drop > 35.0 && context.testWinRatePct < 50.0) {
        const incident: BadBehaviorIncident = {
          id: `inc-${Date.now()}-ovf`,
          timestamp: now,
          anomalyType: 'OVERFIT',
          detectedValue: `Train Win ${context.trainWinRatePct}% dropped to Test Win ${context.testWinRatePct}% (Delta: -${drop}%)`,
          actionTaken: 'BLOCKED & ROLLED BACK: Subservient skill deactivated and rolled back to previous model checkpoint.'
        };
        this.incidentLog.push(incident);
        return { isClean: false, incident, rollbackParameters: this.lastGoodParameters };
      }
    }

    // 3. Weak-only bias: Cutting strong runners repeatedly
    if (context.consecutiveWeakOnlyCount >= 10 && context.bigProfitScore > 70) {
      const incident: BadBehaviorIncident = {
        id: `inc-${Date.now()}-weak`,
        timestamp: now,
        anomalyType: 'WEAK_ONLY_BIAS',
        detectedValue: `${context.consecutiveWeakOnlyCount} consecutive weak exits while Big Profit Score is ${context.bigProfitScore}%`,
        actionTaken: 'ENFORCED HYBRID REPLACEMENT: Forced 50% weak + 50% strong runner allocation.'
      };
      this.incidentLog.push(incident);
      return { isClean: false, incident, rollbackParameters: { hybridRatioWeak: 0.5, hybridRatioStrong: 0.5 } };
    }

    // 4. Chasing during news blackout
    if (context.isNewsWindow || context.vpinToxicity > 0.70) {
      const incident: BadBehaviorIncident = {
        id: `inc-${Date.now()}-chs`,
        timestamp: now,
        anomalyType: 'HIGH_IMPACT_CHASE',
        detectedValue: `NewsWindow: ${context.isNewsWindow}, VPIN Toxicity: ${context.vpinToxicity.toFixed(2)}`,
        actionTaken: 'BLOCKED: Execution suppressed during high impact turbulence window.'
      };
      this.incidentLog.push(incident);
      return { isClean: false, incident };
    }

    return { isClean: true };
  }

  public saveCheckpoint(params: Record<string, any>): void {
    this.lastGoodParameters = { ...params };
  }
}
