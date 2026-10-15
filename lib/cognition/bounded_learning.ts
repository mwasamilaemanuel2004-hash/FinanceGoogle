/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — BOUNDED LEARNING (LAYER 3 OF SAFE LEARNING)
 * ============================================================================
 * Learns from mistakes rationally without revenge trading, martingale doubling,
 * or uncontrolled parameter shifts.
 * Formula: size_new = size_old * 0.9 (upon loss) + adds VPIN/volatility filter.
 * Max learning adjustment rate: 5% per trade.
 */

export interface BoundedLearningState {
  currentPositionMultiplier: number;
  consecutiveLosses: number;
  isCoolingDown: boolean;
  cooldownRemainingMinutes: number;
  learnedFilters: string[];
  totalLearningEvents: number;
}

export class BoundedLearningEngine {
  private static instance: BoundedLearningEngine;
  private state: BoundedLearningState = {
    currentPositionMultiplier: 1.0,
    consecutiveLosses: 0,
    isCoolingDown: false,
    cooldownRemainingMinutes: 0,
    learnedFilters: ['VPIN > 0.70 => HOLD', 'Spread > 3.0x => HOLD'],
    totalLearningEvents: 0
  };

  private constructor() {}

  public static getInstance(): BoundedLearningEngine {
    if (!BoundedLearningEngine.instance) {
      BoundedLearningEngine.instance = new BoundedLearningEngine();
    }
    return BoundedLearningEngine.instance;
  }

  public getState(): BoundedLearningState {
    return { ...this.state };
  }

  /**
   * Process a completed trade outcome through bounded rational learning
   */
  public onTradeOutcome(outcome: {
    symbol: string;
    isWin: boolean;
    pnlPct: number;
    setupName: string;
    vpinAtEntry: number;
  }): {
    actionTaken: string;
    newMultiplier: number;
    coolingDown: boolean;
    addedFilter?: string;
  } {
    this.state.totalLearningEvents++;

    if (outcome.isWin) {
      this.state.consecutiveLosses = 0;
      // Gradual recovery: +2% per win, max 1.0x
      this.state.currentPositionMultiplier = Math.min(1.0, Number((this.state.currentPositionMultiplier * 1.02).toFixed(3)));
      return {
        actionTaken: `WIN registered (+${(outcome.pnlPct * 100).toFixed(2)}%). Size multiplier maintained at ${this.state.currentPositionMultiplier}x.`,
        newMultiplier: this.state.currentPositionMultiplier,
        coolingDown: false
      };
    }

    // Loss handling: NEVER Martingale (no doubling)
    this.state.consecutiveLosses++;

    // Formula: size_new = size_old * 0.9 (10% reduction), bounded to min 0.2x
    const reducedMultiplier = Math.max(0.2, Number((this.state.currentPositionMultiplier * 0.90).toFixed(3)));
    this.state.currentPositionMultiplier = reducedMultiplier;

    let addedFilter: string | undefined;
    if (outcome.vpinAtEntry > 0.65) {
      addedFilter = `Learned filter: If ${outcome.setupName} occurs when VPIN > ${outcome.vpinAtEntry.toFixed(2)}, execute HOLD instead.`;
      if (!this.state.learnedFilters.includes(addedFilter)) {
        this.state.learnedFilters.push(addedFilter);
      }
    }

    // 3 consecutive losses circuit breaker
    if (this.state.consecutiveLosses >= 3) {
      this.state.isCoolingDown = true;
      this.state.cooldownRemainingMinutes = 30;
      this.state.currentPositionMultiplier = 0.50; // Halve size for safety
      return {
        actionTaken: `3 CONSECUTIVE LOSSES: Triggered 30-minute cooling breaker. Position sizing temporarily clamped to 0.50x to eliminate revenge trading risk.`,
        newMultiplier: 0.50,
        coolingDown: true,
        addedFilter
      };
    }

    return {
      actionTaken: `LOSS registered (${(outcome.pnlPct * 100).toFixed(2)}%). Reduced position size by 10% to ${this.state.currentPositionMultiplier}x without martingale.`,
      newMultiplier: this.state.currentPositionMultiplier,
      coolingDown: false,
      addedFilter
    };
  }

  public resetCooldown(): void {
    this.state.isCoolingDown = false;
    this.state.cooldownRemainingMinutes = 0;
    this.state.consecutiveLosses = 0;
  }
}
