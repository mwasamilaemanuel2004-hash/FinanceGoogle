import { Candle } from '../tools/smc_detector';

export interface ChanceEvaluation {
  isAnomalyDetected: boolean;
  anomalyScore: number; // 0.0 - 1.0 (Isolation Forest metric)
  patternSignature: string;
  isTradePermitted: boolean;
  sizingFactor: number; // e.g. 0.5x Kelly
  reasoning: string;
  coolingDownUntil: number; // Timestamp
  recentLossCount: number;
}

export class ChanceHandler {
  private static instance: ChanceHandler;
  private recentLossCount: number = 0;
  private coolingDownUntil: number = 0;
  private chanceTradeHistory: Array<{
    timestamp: number;
    signature: string;
    result: 'WIN' | 'LOSS' | 'PENDING';
    pnlPct: number;
  }> = [];

  private constructor() {}

  public static getInstance(): ChanceHandler {
    if (!ChanceHandler.instance) {
      ChanceHandler.instance = new ChanceHandler();
    }
    return ChanceHandler.instance;
  }

  /**
   * Evaluates anomalous or unstructured price action that is not captured by rigid formulas,
   * without ever violating Hard Risk, Circuit Breakers, or forcing destructive losses.
   */
  public evaluateChanceOpportunity(
    candles: Candle[],
    currentDrawdownPct: number = 0,
    currentVaR95Pct: number = 1.2
  ): ChanceEvaluation {
    const now = Date.now();

    // 1. Check active cooldown
    if (now < this.coolingDownUntil) {
      const remainingMins = Math.ceil((this.coolingDownUntil - now) / 60000);
      return {
        isAnomalyDetected: false,
        anomalyScore: 0,
        patternSignature: 'COOLDOWN_PROTECTION',
        isTradePermitted: false,
        sizingFactor: 0,
        reasoning: `Chance trading temporarily locked for ${remainingMins} min due to 2 consecutive trial losses. Core logic fully preserved.`,
        coolingDownUntil: this.coolingDownUntil,
        recentLossCount: this.recentLossCount
      };
    }

    // 2. Anomaly Detection (Statistical Isolation Forest proxy on candle velocity, spread, and volume outlier)
    const n = candles.length;
    if (n < 20) {
      return {
        isAnomalyDetected: false,
        anomalyScore: 0,
        patternSignature: 'INSUFFICIENT_HISTORY',
        isTradePermitted: false,
        sizingFactor: 0,
        reasoning: 'Insufficient candle data for statistical outlier detection.',
        coolingDownUntil: 0,
        recentLossCount: this.recentLossCount
      };
    }

    const lastCandle = candles[n - 1];
    const prevCandles = candles.slice(-20, -1);
    const avgVol = prevCandles.reduce((acc, c) => acc + c.volume, 0) / prevCandles.length;
    const avgRange = prevCandles.reduce((acc, c) => acc + Math.abs(c.high - c.low), 0) / prevCandles.length;

    const currentRange = Math.abs(lastCandle.high - lastCandle.low);
    const volRatio = lastCandle.volume / (avgVol || 1);
    const rangeRatio = currentRange / (avgRange || 1);

    // Anomaly identified when volume & velocity spike unexpectedly without standard technical setup
    const isAnomaly = volRatio > 2.8 && rangeRatio > 2.2;
    const anomalyScore = Math.min(1.0, (volRatio / 5.0) * 0.5 + (rangeRatio / 5.0) * 0.5);
    const patternSignature = `ANOMALY_VOL_${volRatio.toFixed(1)}X_RANGE_${rangeRatio.toFixed(1)}X`;

    if (!isAnomaly) {
      return {
        isAnomalyDetected: false,
        anomalyScore: Number(anomalyScore.toFixed(3)),
        patternSignature: 'NORMAL_DISTRIBUTION',
        isTradePermitted: false,
        sizingFactor: 0,
        reasoning: 'Market behaving within standard mathematical distribution.',
        coolingDownUntil: 0,
        recentLossCount: this.recentLossCount
      };
    }

    // 3. Strict Risk Filter: Check VaR < 2.0% and MaxDD < 5.0%
    const riskPermitted = currentVaR95Pct < 2.0 && currentDrawdownPct < 5.0 && anomalyScore >= 0.65;

    let reasoning = '';
    let sizing = 0;

    if (riskPermitted) {
      sizing = 0.5; // 0.5x Kelly conservative exploratory sizing
      reasoning = `Unusual market opportunity detected (${patternSignature}, score ${anomalyScore.toFixed(2)}). Risk passed (VaR ${currentVaR95Pct.toFixed(2)}% < 2%, DD ${currentDrawdownPct.toFixed(2)}% < 5%). Sizing clamped to 0.5x Kelly.`;
    } else {
      reasoning = `Anomalous pattern detected but trade BLOCKED by Hard Risk Governor (VaR ${currentVaR95Pct.toFixed(2)}% or DD ${currentDrawdownPct.toFixed(2)}% exceeds safety boundary). Zero loss forced.`;
    }

    return {
      isAnomalyDetected: true,
      anomalyScore: Number(anomalyScore.toFixed(3)),
      patternSignature,
      isTradePermitted: riskPermitted,
      sizingFactor: sizing,
      reasoning,
      coolingDownUntil: 0,
      recentLossCount: this.recentLossCount
    };
  }

  /**
   * Records execution outcome of an exploratory chance trade.
   */
  public recordTradeOutcome(signature: string, isWin: boolean, pnlPct: number) {
    this.chanceTradeHistory.push({
      timestamp: Date.now(),
      signature,
      result: isWin ? 'WIN' : 'LOSS',
      pnlPct
    });

    if (isWin) {
      this.recentLossCount = 0;
    } else {
      this.recentLossCount++;
      if (this.recentLossCount >= 2) {
        // Enforce 1-hour automatic cooldown for exploratory chance execution
        this.coolingDownUntil = Date.now() + 3600000;
      }
    }
  }

  public getTradeHistory() {
    return [...this.chanceTradeHistory];
  }
}

export const chanceHandler = ChanceHandler.getInstance();
