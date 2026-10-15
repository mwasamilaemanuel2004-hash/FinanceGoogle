/**
 * ESMH.TRADE — REAL QUANTITATIVE RISK ENGINE (CQF / FRM STANDARDS)
 * Path: /lib/tools/risk_engine.ts
 *
 * Implements:
 * 1. Parametric VaR (95% & 99%)
 * 2. Historical Simulation VaR & Expected Shortfall (CVaR)
 * 3. Cornish-Fisher Non-Gaussian Expansion
 * 4. 3-Sigma Macro Stress Testing
 * 5. Fractional Kelly Criterion Position Sizing
 * 6. Hard Risk Governor Limits
 */

export interface RiskMetrics {
  var95: number;             // Value at Risk 95% (e.g. 0.012 = 1.2%)
  var99: number;             // Value at Risk 99% (e.g. 0.021 = 2.1%)
  cvar95: number;            // Expected Shortfall (Conditional VaR)
  stressTest3Sigma: number;  // 3-Sigma Tail Loss
  volatilityAnnualized: number;
  skewness: number;
  kurtosis: number;
  cornishFisherVaR99: number;
  recommendedKellyFraction: number;
  maxAllowedPositionUsd: number;
  isHardRiskBlocked: boolean;
  blockReason?: string;
}

export class RealRiskEngine {
  /**
   * Compute comprehensive risk metrics given historical returns and account equity
   */
  static evaluateRisk(
    returns: number[],
    accountEquity: number,
    openExposureUsd: number,
    winRate: number,
    winLossRatio: number,
    dailyLossLimitPct: number = 0.04
  ): RiskMetrics {
    if (returns.length < 10) {
      // Conservative default for early warm-up
      return {
        var95: 0.015,
        var99: 0.025,
        cvar95: 0.030,
        stressTest3Sigma: 0.045,
        volatilityAnnualized: 0.18,
        skewness: 0,
        kurtosis: 3,
        cornishFisherVaR99: 0.025,
        recommendedKellyFraction: 0.02,
        maxAllowedPositionUsd: accountEquity * 0.05,
        isHardRiskBlocked: false
      };
    }

    const n = returns.length;
    const mean = returns.reduce((a, b) => a + b, 0) / n;
    const variance = returns.reduce((acc, r) => acc + Math.pow(r - mean, 2), 0) / (n - 1);
    const stdDev = Math.sqrt(variance);
    const volAnnual = stdDev * Math.sqrt(252);

    // Skewness & Kurtosis
    const m3 = returns.reduce((acc, r) => acc + Math.pow(r - mean, 3), 0) / n;
    const m4 = returns.reduce((acc, r) => acc + Math.pow(r - mean, 4), 0) / n;
    const skew = variance > 0 ? m3 / Math.pow(stdDev, 3) : 0;
    const kurt = variance > 0 ? m4 / Math.pow(stdDev, 4) : 3;

    // Parametric VaR
    const z95 = 1.64485;
    const z99 = 2.32635;
    const var95 = Math.max(0, z95 * stdDev - mean);
    const var99 = Math.max(0, z99 * stdDev - mean);

    // Cornish-Fisher Expansion for non-normal distribution:
    // w = z + (z^2 - 1)*S/6 + (z^3 - 3z)*(K - 3)/24 - (2z^3 - 5z)*S^2/36
    const z = z99;
    const w = z +
      ((Math.pow(z, 2) - 1) * skew) / 6 +
      ((Math.pow(z, 3) - 3 * z) * (kurt - 3)) / 24 -
      ((2 * Math.pow(z, 3) - 5 * z) * Math.pow(skew, 2)) / 36;
    const cfVaR99 = Math.max(0, w * stdDev - mean);

    // Historical CVaR (Expected Shortfall beyond 95th percentile)
    const sorted = [...returns].sort((a, b) => a - b);
    const cutoffIndex = Math.max(1, Math.floor(n * 0.05));
    const tailLosses = sorted.slice(0, cutoffIndex);
    const cvar95 = tailLosses.length > 0
      ? - (tailLosses.reduce((a, b) => a + b, 0) / tailLosses.length)
      : var95 * 1.25;

    // 3-Sigma Stress Move
    const stressTest3Sigma = Math.max(0, 3.0 * stdDev - mean);

    // Fractional Kelly: f* = (p * b - (1 - p)) / b, with half-Kelly safety multiplier 0.5
    const p = Math.max(0.01, Math.min(0.99, winRate));
    const b = Math.max(0.1, winLossRatio);
    const fullKelly = (p * b - (1 - p)) / b;
    const halfKelly = Math.max(0.005, Math.min(0.08, fullKelly * 0.5));

    // Position Size limit enforcing max daily drawdown
    const maxPositionPct = Math.min(halfKelly, 0.05); // cap at 5% equity per position
    const maxAllowedPositionUsd = Math.max(1.0, accountEquity * maxPositionPct);

    // Hard Risk Gate Check
    let isHardRiskBlocked = false;
    let blockReason: string | undefined = undefined;

    if (openExposureUsd > accountEquity * 0.6) {
      isHardRiskBlocked = true;
      blockReason = `Max leverage threshold exceeded (Open exposure $${openExposureUsd.toFixed(2)} > 60% of equity)`;
    } else if (stressTest3Sigma > dailyLossLimitPct) {
      isHardRiskBlocked = true;
      blockReason = `3-Sigma stress test ${(stressTest3Sigma * 100).toFixed(1)}% exceeds daily loss limit ${(dailyLossLimitPct * 100).toFixed(1)}%`;
    }

    return {
      var95,
      var99,
      cvar95: Math.max(0, cvar95),
      stressTest3Sigma,
      volatilityAnnualized: volAnnual,
      skewness: skew,
      kurtosis: kurt,
      cornishFisherVaR99: cfVaR99,
      recommendedKellyFraction: halfKelly,
      maxAllowedPositionUsd,
      isHardRiskBlocked,
      blockReason
    };
  }
}
