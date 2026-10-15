/**
 * ESMH.TRADE — STOP-HUNT & LIQUIDITY SWEEP ENGINE (CQF / FRM / CFA / ADD MERGED)
 * Path: /lib/engines/stop_hunt.ts
 */

export interface StopHuntEvaluation {
  symbol: string;
  sweepDetected: boolean;
  wickReversalConfidence: number; // 0 to 100
  fadeSweepSignal: 'BUY_FADE_SWEEP' | 'SELL_FADE_SWEEP' | 'NO_SWEEP';
  adaptiveStopLossPrice: number;
}

export class StopHuntEngine {
  public static evaluate(
    symbol: string,
    currentPrice: number,
    highWick: number,
    lowWick: number,
    volumeSurgeRatio: number = 1.8
  ): StopHuntEvaluation {
    const sym = symbol.toUpperCase();
    const upperSweep = highWick > currentPrice * 1.002 && volumeSurgeRatio > 1.5;
    const lowerSweep = lowWick < currentPrice * 0.998 && volumeSurgeRatio > 1.5;

    let signal: 'BUY_FADE_SWEEP' | 'SELL_FADE_SWEEP' | 'NO_SWEEP' = 'NO_SWEEP';
    if (lowerSweep) {
      signal = 'BUY_FADE_SWEEP';
    } else if (upperSweep) {
      signal = 'SELL_FADE_SWEEP';
    }

    return {
      symbol: sym,
      sweepDetected: upperSweep || lowerSweep,
      wickReversalConfidence: (upperSweep || lowerSweep) ? 82 : 20,
      fadeSweepSignal: signal,
      adaptiveStopLossPrice: lowerSweep ? lowWick * 0.999 : highWick * 1.001,
    };
  }
}
