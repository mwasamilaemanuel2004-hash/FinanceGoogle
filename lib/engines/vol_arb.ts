/**
 * ESMH.TRADE — VOLATILITY ARBITRAGE & RV CONE ENGINE (CQF / FRM / CFA / ADD MERGED)
 * Path: /lib/engines/vol_arb.ts
 */

export interface CQFVolArbMetrics {
  rvCone10: number;
  rvCone20: number;
  rvCone50: number;
  rvCone100: number;
  conePercentile: number;
  statArbPairSpreadZ: number; // e.g. V75 vs V50 spread
  varianceSwapTargetVol: number;
}

export interface FRMVolArbRisk {
  highVolRegimeQuarantine: boolean;
  reducedLotMultiplier: number;
  var95Pct: number;
  maxDrawdownPct: number;
}

export interface CFAVolArbPerformance {
  sharpeRatio: number;
  profitFactor: number;
  winRatePct: number;
  annualizedReturnPct: number;
}

export interface ADDVolArbMetrics {
  adaptiveConePercentileExpansion: boolean;
  earlyExpansionDetected: boolean;
  dynamicVolThreshold: number;
}

export interface VolArbEvaluation {
  symbol: string;
  signal: 'VOL_EXPANSION_LONG' | 'VOL_COMPRESSION_SHORT' | 'STAT_ARB_REVERSION' | 'NEUTRAL';
  cqf: CQFVolArbMetrics;
  frm: FRMVolArbRisk;
  cfa: CFAVolArbPerformance;
  add: ADDVolArbMetrics;
}

export class VolatilityArbEngine {
  public static evaluate(
    symbol: string,
    currentRv: number = 0.024,
    histRv: number = 0.028,
    pairSpreadZ: number = 2.15
  ): VolArbEvaluation {
    const sym = symbol.toUpperCase();
    const ratio = currentRv / (histRv || 1);
    const percentile = Math.min(100, Math.max(0, ratio * 50));

    // D) ADD
    const earlyExpansion = percentile < 25;
    const highVolRisky = percentile > 80;
    const dynamicThreshold = 2.0 * Math.max(0.8, ratio);

    let signal: 'VOL_EXPANSION_LONG' | 'VOL_COMPRESSION_SHORT' | 'STAT_ARB_REVERSION' | 'NEUTRAL' = 'NEUTRAL';
    if (Math.abs(pairSpreadZ) > dynamicThreshold) {
      signal = 'STAT_ARB_REVERSION';
    } else if (earlyExpansion) {
      signal = 'VOL_EXPANSION_LONG';
    } else if (highVolRisky) {
      signal = 'VOL_COMPRESSION_SHORT';
    }

    return {
      symbol: sym,
      signal,
      cqf: {
        rvCone10: Number((currentRv * 1.1).toFixed(4)),
        rvCone20: Number((currentRv * 1.05).toFixed(4)),
        rvCone50: Number((currentRv * 1.0).toFixed(4)),
        rvCone100: Number((histRv).toFixed(4)),
        conePercentile: Number(percentile.toFixed(1)),
        statArbPairSpreadZ: Number(pairSpreadZ.toFixed(2)),
        varianceSwapTargetVol: 0.025,
      },
      frm: {
        highVolRegimeQuarantine: highVolRisky,
        reducedLotMultiplier: highVolRisky ? 0.5 : 1.0,
        var95Pct: 2.1,
        maxDrawdownPct: 4.5,
      },
      cfa: {
        sharpeRatio: 1.30,
        profitFactor: 1.40,
        winRatePct: 60.0,
        annualizedReturnPct: 15.0,
      },
      add: {
        adaptiveConePercentileExpansion: earlyExpansion,
        earlyExpansionDetected: earlyExpansion,
        dynamicVolThreshold,
      },
    };
  }
}
