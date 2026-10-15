export interface Candle {
  timestamp: number;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface SMCAnalysisResult {
  hasBOS: boolean;
  bosDirection: 'BULLISH' | 'BEARISH' | 'NONE';
  bosPrice: number;
  hasCHoCH: boolean;
  chochDirection: 'BULLISH' | 'BEARISH' | 'NONE';
  chochPrice: number;
  fvgGaps: Array<{
    type: 'BULLISH_FVG' | 'BEARISH_FVG';
    top: number;
    bottom: number;
    midpoint: number;
    candleIndex: number;
  }>;
  orderBlocks: Array<{
    type: 'BULLISH_OB' | 'BEARISH_OB';
    high: number;
    low: number;
    mitigated: boolean;
    candleIndex: number;
  }>;
  liquiditySweeps: Array<{
    type: 'BUY_SIDE_SWEEP' | 'SELL_SIDE_SWEEP';
    sweptLevel: number;
    wickSize: number;
    candleIndex: number;
  }>;
  latencyMs: number;
}

export class SMCDetector {
  /**
   * Fast O(n) algorithmic detection of SMC elements: BOS, CHoCH, OB, FVG, Liquidity sweeps.
   * Execution target: < 5ms for 1000 candles.
   */
  public static analyze(candles: Candle[]): SMCAnalysisResult {
    const startTime = performance.now();
    const n = candles.length;
    if (n < 5) {
      return {
        hasBOS: false,
        bosDirection: 'NONE',
        bosPrice: 0,
        hasCHoCH: false,
        chochDirection: 'NONE',
        chochPrice: 0,
        fvgGaps: [],
        orderBlocks: [],
        liquiditySweeps: [],
        latencyMs: 0
      };
    }

    // 1. Swing Highs and Lows (Fractals / ZigZag detection)
    const swingHighs: Array<{ index: number; price: number }> = [];
    const swingLows: Array<{ index: number; price: number }> = [];

    for (let i = 2; i < n - 2; i++) {
      const prev2 = candles[i - 2];
      const prev1 = candles[i - 1];
      const curr = candles[i];
      const next1 = candles[i + 1];
      const next2 = candles[i + 2];

      if (curr.high > prev2.high && curr.high > prev1.high && curr.high > next1.high && curr.high > next2.high) {
        swingHighs.push({ index: i, price: curr.high });
      }
      if (curr.low < prev2.low && curr.low < prev1.low && curr.low < next1.low && curr.low < next2.low) {
        swingLows.push({ index: i, price: curr.low });
      }
    }

    // 2. Break of Structure (BOS) & Change of Character (CHoCH)
    let hasBOS = false;
    let bosDirection: 'BULLISH' | 'BEARISH' | 'NONE' = 'NONE';
    let bosPrice = 0;
    let hasCHoCH = false;
    let chochDirection: 'BULLISH' | 'BEARISH' | 'NONE' = 'NONE';
    let chochPrice = 0;

    const lastCandle = candles[n - 1];
    const prevCandle = candles[n - 2];

    const lastSwingHigh = swingHighs.length > 0 ? swingHighs[swingHighs.length - 1] : null;
    const lastSwingLow = swingLows.length > 0 ? swingLows[swingLows.length - 1] : null;
    const prevSwingHigh = swingHighs.length > 1 ? swingHighs[swingHighs.length - 2] : null;
    const prevSwingLow = swingLows.length > 1 ? swingLows[swingLows.length - 2] : null;

    if (lastSwingHigh && lastCandle.close > lastSwingHigh.price) {
      if (prevSwingLow && prevSwingHigh && lastSwingHigh.price > prevSwingHigh.price) {
        hasBOS = true;
        bosDirection = 'BULLISH';
        bosPrice = lastSwingHigh.price;
      } else {
        hasCHoCH = true;
        chochDirection = 'BULLISH';
        chochPrice = lastSwingHigh.price;
      }
    } else if (lastSwingLow && lastCandle.close < lastSwingLow.price) {
      if (prevSwingHigh && prevSwingLow && lastSwingLow.price < prevSwingLow.price) {
        hasBOS = true;
        bosDirection = 'BEARISH';
        bosPrice = lastSwingLow.price;
      } else {
        hasCHoCH = true;
        chochDirection = 'BEARISH';
        chochPrice = lastSwingLow.price;
      }
    }

    // 3. Fair Value Gaps (FVG)
    // Bullish FVG: Candle[i-2].high < Candle[i].low
    // Bearish FVG: Candle[i-2].low > Candle[i].high
    const fvgGaps: SMCAnalysisResult['fvgGaps'] = [];
    for (let i = n - 1; i >= Math.max(2, n - 20); i--) {
      const c1 = candles[i - 2];
      const c2 = candles[i - 1];
      const c3 = candles[i];

      if (c3.low > c1.high) {
        fvgGaps.push({
          type: 'BULLISH_FVG',
          top: c3.low,
          bottom: c1.high,
          midpoint: (c3.low + c1.high) / 2,
          candleIndex: i
        });
      } else if (c3.high < c1.low) {
        fvgGaps.push({
          type: 'BEARISH_FVG',
          top: c1.low,
          bottom: c3.high,
          midpoint: (c1.low + c3.high) / 2,
          candleIndex: i
        });
      }
    }

    // 4. Order Blocks (OB)
    // Bullish OB: Last down candle before strong impulsive up move (volume > 1.8 * avgVolume)
    // Bearish OB: Last up candle before strong impulsive down move
    const orderBlocks: SMCAnalysisResult['orderBlocks'] = [];
    let avgVolume = 0;
    const lookback = Math.min(30, n);
    for (let i = n - lookback; i < n; i++) {
      avgVolume += candles[i].volume;
    }
    avgVolume /= lookback;

    for (let i = n - 2; i >= Math.max(3, n - 25); i--) {
      const c = candles[i];
      const nextC = candles[i + 1];

      // Bullish OB: c was red, nextC broke high strongly with high volume
      if (c.close < c.open && nextC.close > nextC.open && nextC.volume > avgVolume * 1.5) {
        const isMitigated = lastCandle.low <= c.low;
        orderBlocks.push({
          type: 'BULLISH_OB',
          high: c.high,
          low: c.low,
          mitigated: isMitigated,
          candleIndex: i
        });
      } else if (c.close > c.open && nextC.close < nextC.open && nextC.volume > avgVolume * 1.5) {
        const isMitigated = lastCandle.high >= c.high;
        orderBlocks.push({
          type: 'BEARISH_OB',
          high: c.high,
          low: c.low,
          mitigated: isMitigated,
          candleIndex: i
        });
      }
    }

    // 5. Liquidity Sweeps (Equal highs/lows sweep or stop hunts)
    const liquiditySweeps: SMCAnalysisResult['liquiditySweeps'] = [];
    const atr = Math.abs(candles[n - 1].high - candles[n - 1].low);

    if (lastSwingHigh && lastCandle.high > lastSwingHigh.price && lastCandle.close < lastSwingHigh.price) {
      liquiditySweeps.push({
        type: 'BUY_SIDE_SWEEP',
        sweptLevel: lastSwingHigh.price,
        wickSize: lastCandle.high - lastCandle.close,
        candleIndex: n - 1
      });
    }

    if (lastSwingLow && lastCandle.low < lastSwingLow.price && lastCandle.close > lastSwingLow.price) {
      liquiditySweeps.push({
        type: 'SELL_SIDE_SWEEP',
        sweptLevel: lastSwingLow.price,
        wickSize: lastCandle.close - lastCandle.low,
        candleIndex: n - 1
      });
    }

    const latencyMs = Number((performance.now() - startTime).toFixed(3));
    return {
      hasBOS,
      bosDirection,
      bosPrice,
      hasCHoCH,
      chochDirection,
      chochPrice,
      fvgGaps,
      orderBlocks,
      liquiditySweeps,
      latencyMs
    };
  }
}
