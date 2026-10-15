import { WeakVsLossClassificationResult } from '../cognition/weak_vs_loss_classifier';

export interface ExpectedValueAudit {
  grossWinPipsOrPct: number;
  grossLossPipsOrPct: number;
  winRate: number;
  mathematicalExpectancy: number; // E = (winRate * win) - ((1 - winRate) * loss)
  isApprovedForExecution: boolean;
  maxAllowableSizingFraction: number; // Bound by Kelly Criterion
}

export class ProfitLossDifferentiator {
  private static instance: ProfitLossDifferentiator;

  private constructor() {}

  public static getInstance(): ProfitLossDifferentiator {
    if (!ProfitLossDifferentiator.instance) {
      ProfitLossDifferentiator.instance = new ProfitLossDifferentiator();
    }
    return ProfitLossDifferentiator.instance;
  }

  /**
   * Mathematically evaluates whether a proposed micro trade is a genuine high-Sharpe
   * weak profit or a disguised hazard that leads to cumulative losses.
   */
  public evaluateExpectancy(
    potentialGainPct: number,
    potentialLossPct: number,
    historicalWinRatePct: number,
    currentVpin: number
  ): ExpectedValueAudit {
    const p = Math.max(0.01, Math.min(0.99, historicalWinRatePct / 100));
    const win = Math.abs(potentialGainPct);
    const loss = Math.abs(potentialLossPct);

    // VPIN toxicity haircut: If VPIN is elevated, penalize win rate
    const vpinPenalty = currentVpin > 0.4 ? (currentVpin - 0.4) * 0.3 : 0.0;
    const adjustedWinRate = Math.max(0.1, p - vpinPenalty);

    const ev = adjustedWinRate * win - (1.0 - adjustedWinRate) * loss;

    // Continuous Kelly: f* = (b*p - q) / b where b = win / loss
    const b = loss > 0 ? win / loss : 1.0;
    const q = 1.0 - adjustedWinRate;
    const rawKelly = b > 0 ? (b * adjustedWinRate - q) / b : 0.0;
    const clampedKelly = Math.max(0.0, Math.min(0.02, rawKelly * 0.25)); // Max 2% capital per trade

    const approved = ev > 0.0002 && adjustedWinRate >= 0.58 && currentVpin < 0.60;

    return {
      grossWinPipsOrPct: win,
      grossLossPipsOrPct: loss,
      winRate: adjustedWinRate,
      mathematicalExpectancy: ev,
      isApprovedForExecution: approved,
      maxAllowableSizingFraction: approved ? clampedKelly : 0.0
    };
  }
}

export const profitLossDifferentiator = ProfitLossDifferentiator.getInstance();
