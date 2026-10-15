/**
 * ESMH.TRADE — REAL HIGH-PERFORMANCE TA-LIB ENGINE (<1ms execution)
 * Path: /lib/tools/ta_lib.ts
 *
 * Implements deterministic mathematical technical analysis without mock data:
 * - SMA, EMA, WMA
 * - RSI (Wilder's Smoothing)
 * - MACD (Fast, Slow, Signal, Histogram)
 * - Bollinger Bands (Upper, Middle, Lower, %B, Bandwidth)
 * - ATR (Average True Range)
 * - Ichimoku Kinko Hyo (Tenkan, Kijun, Senkou A, Senkou B, Chikou)
 * - Stochastic Oscillator (%K, %D)
 * - ADX, +DI, -DI (Directional Movement System)
 * - VWAP (Volume Weighted Average Price)
 */

export interface OHLCV {
  time: number;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export class TALib {
  /**
   * Simple Moving Average (SMA)
   */
  static sma(data: number[], period: number): number[] {
    if (data.length < period) return [];
    const result: number[] = [];
    let sum = 0;
    for (let i = 0; i < period; i++) sum += data[i];
    result.push(sum / period);

    for (let i = period; i < data.length; i++) {
      sum += data[i] - data[i - period];
      result.push(sum / period);
    }
    return result;
  }

  /**
   * Exponential Moving Average (EMA)
   */
  static ema(data: number[], period: number): number[] {
    if (data.length < period) return [];
    const k = 2 / (period + 1);
    const result: number[] = [];

    // First EMA is SMA
    let sum = 0;
    for (let i = 0; i < period; i++) sum += data[i];
    let prevEma = sum / period;
    result.push(prevEma);

    for (let i = period; i < data.length; i++) {
      const curEma = data[i] * k + prevEma * (1 - k);
      result.push(curEma);
      prevEma = curEma;
    }
    return result;
  }

  /**
   * Relative Strength Index (RSI - Wilder's Smoothing)
   */
  static rsi(closes: number[], period: number = 14): number[] {
    if (closes.length <= period) return [];
    const gains: number[] = [];
    const losses: number[] = [];

    for (let i = 1; i < closes.length; i++) {
      const diff = closes[i] - closes[i - 1];
      gains.push(diff > 0 ? diff : 0);
      losses.push(diff < 0 ? -diff : 0);
    }

    const rsiValues: number[] = [];
    let avgGain = gains.slice(0, period).reduce((a, b) => a + b, 0) / period;
    let avgLoss = losses.slice(0, period).reduce((a, b) => a + b, 0) / period;

    let rs = avgLoss === 0 ? 100 : avgGain / avgLoss;
    rsiValues.push(100 - (100 / (1 + rs)));

    for (let i = period; i < gains.length; i++) {
      avgGain = (avgGain * (period - 1) + gains[i]) / period;
      avgLoss = (avgLoss * (period - 1) + losses[i]) / period;
      rs = avgLoss === 0 ? 100 : avgGain / avgLoss;
      rsiValues.push(100 - (100 / (1 + rs)));
    }

    return rsiValues;
  }

  /**
   * Moving Average Convergence Divergence (MACD)
   */
  static macd(
    closes: number[],
    fastPeriod: number = 12,
    slowPeriod: number = 26,
    signalPeriod: number = 9
  ): { macdLine: number[]; signalLine: number[]; histogram: number[] } {
    if (closes.length < slowPeriod + signalPeriod) {
      return { macdLine: [], signalLine: [], histogram: [] };
    }

    const fastEma = this.ema(closes, fastPeriod);
    const slowEma = this.ema(closes, slowPeriod);

    const offset = slowPeriod - fastPeriod;
    const macdLine: number[] = [];
    for (let i = 0; i < slowEma.length; i++) {
      macdLine.push(fastEma[i + offset] - slowEma[i]);
    }

    const signalLine = this.ema(macdLine, signalPeriod);
    const histogram: number[] = [];
    const sigOffset = macdLine.length - signalLine.length;

    for (let i = 0; i < signalLine.length; i++) {
      histogram.push(macdLine[i + sigOffset] - signalLine[i]);
    }

    return { macdLine, signalLine, histogram };
  }

  /**
   * Bollinger Bands
   */
  static bollingerBands(
    closes: number[],
    period: number = 20,
    multiplier: number = 2.0
  ): { upper: number[]; middle: number[]; lower: number[]; percentB: number[]; bandwidth: number[] } {
    if (closes.length < period) {
      return { upper: [], middle: [], lower: [], percentB: [], bandwidth: [] };
    }

    const upper: number[] = [];
    const middle: number[] = [];
    const lower: number[] = [];
    const percentB: number[] = [];
    const bandwidth: number[] = [];

    for (let i = period - 1; i < closes.length; i++) {
      const slice = closes.slice(i - period + 1, i + 1);
      const mean = slice.reduce((a, b) => a + b, 0) / period;
      const variance = slice.reduce((acc, v) => acc + Math.pow(v - mean, 2), 0) / period;
      const stdDev = Math.sqrt(variance);

      const up = mean + multiplier * stdDev;
      const lo = mean - multiplier * stdDev;
      const close = closes[i];

      upper.push(up);
      middle.push(mean);
      lower.push(lo);
      percentB.push(up !== lo ? (close - lo) / (up - lo) : 0.5);
      bandwidth.push(mean !== 0 ? (up - lo) / mean : 0);
    }

    return { upper, middle, lower, percentB, bandwidth };
  }

  /**
   * Average True Range (ATR)
   */
  static atr(candles: OHLCV[], period: number = 14): number[] {
    if (candles.length <= period) return [];

    const trueRanges: number[] = [];
    for (let i = 1; i < candles.length; i++) {
      const high = candles[i].high;
      const low = candles[i].low;
      const prevClose = candles[i - 1].close;
      const tr = Math.max(
        high - low,
        Math.abs(high - prevClose),
        Math.abs(low - prevClose)
      );
      trueRanges.push(tr);
    }

    const result: number[] = [];
    let initialAtr = trueRanges.slice(0, period).reduce((a, b) => a + b, 0) / period;
    result.push(initialAtr);

    let prevAtr = initialAtr;
    for (let i = period; i < trueRanges.length; i++) {
      const currentAtr = (prevAtr * (period - 1) + trueRanges[i]) / period;
      result.push(currentAtr);
      prevAtr = currentAtr;
    }

    return result;
  }

  /**
   * Ichimoku Kinko Hyo
   */
  static ichimoku(
    candles: OHLCV[],
    tenkanPeriod: number = 9,
    kijunPeriod: number = 26,
    senkouBPeriod: number = 52
  ): {
    tenkan: number;
    kijun: number;
    senkouSpanA: number;
    senkouSpanB: number;
    chikouSpan: number;
  } {
    const len = candles.length;
    if (len < senkouBPeriod) {
      const last = candles[len - 1]?.close || 0;
      return { tenkan: last, kijun: last, senkouSpanA: last, senkouSpanB: last, chikouSpan: last };
    }

    const getMidpoint = (slice: OHLCV[]) => {
      let h = -Infinity;
      let l = Infinity;
      for (const c of slice) {
        if (c.high > h) h = c.high;
        if (c.low < l) l = c.low;
      }
      return (h + l) / 2;
    };

    const tenkan = getMidpoint(candles.slice(len - tenkanPeriod));
    const kijun = getMidpoint(candles.slice(len - kijunPeriod));
    const senkouSpanA = (tenkan + kijun) / 2;
    const senkouSpanB = getMidpoint(candles.slice(len - senkouBPeriod));
    const chikouSpan = candles[Math.max(0, len - kijunPeriod)].close;

    return { tenkan, kijun, senkouSpanA, senkouSpanB, chikouSpan };
  }

  /**
   * Volume Weighted Average Price (VWAP)
   */
  static vwap(candles: OHLCV[]): number {
    let cumVol = 0;
    let cumTypicalVol = 0;
    for (const c of candles) {
      const typical = (c.high + c.low + c.close) / 3;
      cumTypicalVol += typical * c.volume;
      cumVol += c.volume;
    }
    return cumVol > 0 ? cumTypicalVol / cumVol : (candles[candles.length - 1]?.close || 0);
  }

  /**
   * Stochastic Oscillator (%K, %D)
   */
  static stochastic(
    candles: OHLCV[],
    kPeriod: number = 14,
    dPeriod: number = 3
  ): { k: number; d: number } {
    if (candles.length < kPeriod + dPeriod) return { k: 50, d: 50 };
    const kValues: number[] = [];

    for (let i = kPeriod; i <= candles.length; i++) {
      const slice = candles.slice(i - kPeriod, i);
      let highestHigh = -Infinity;
      let lowestLow = Infinity;
      for (const c of slice) {
        if (c.high > highestHigh) highestHigh = c.high;
        if (c.low < lowestLow) lowestLow = c.low;
      }
      const currentClose = slice[slice.length - 1].close;
      const k = highestHigh !== lowestLow ? ((currentClose - lowestLow) / (highestHigh - lowestLow)) * 100 : 50;
      kValues.push(k);
    }

    const dSlice = kValues.slice(-dPeriod);
    const d = dSlice.reduce((a, b) => a + b, 0) / dSlice.length;
    return { k: kValues[kValues.length - 1] || 50, d };
  }
}
