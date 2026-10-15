import { Candle } from '../tools/smc_detector';

export type WyckoffMajorPhase = 'ACCUMULATION' | 'MARKUP' | 'DISTRIBUTION' | 'MARKDOWN' | 'MANIPULATION';

export type WyckoffSubPhase =
  | 'PHASE_A_STOPPING'
  | 'PHASE_B_BUILDING_CAUSE'
  | 'PHASE_C_SPRING'
  | 'PHASE_C_TEST'
  | 'PHASE_D_SOS_LPS'
  | 'PHASE_E_MARKUP'
  | 'PHASE_A_BUYING_CLIMAX'
  | 'PHASE_B_DISTRIBUTION_CAUSE'
  | 'PHASE_C_UTAD'
  | 'PHASE_D_SOW'
  | 'PHASE_E_MARKDOWN'
  | 'LIQUIDITY_GRAB_MANIPULATION';

export interface WyckoffDetectionResult {
  majorPhase: WyckoffMajorPhase;
  subPhase: WyckoffSubPhase;
  confidence: number;
  springDetected: boolean;
  springPrice: number;
  utadDetected: boolean;
  utadPrice: number;
  signOfStrengthDetected: boolean;
  signOfWeaknessDetected: boolean;
  manipulationProbability: number;
  expectedNextTrend: 'MARKUP' | 'MARKDOWN' | 'RANGING';
  daysInPhase: number;
  reasoning: string;
  latencyMs: number;
}

export class WyckoffDetector {
  /**
   * Evaluates Wyckoff structural phases A through E & manipulation traps in < 15ms.
   */
  public static analyze(
    candles: Candle[],
    cvdDelta: number = 0,
    dixScore: number = 0.5
  ): WyckoffDetectionResult {
    const t0 = performance.now();
    const n = candles.length;
    if (n < 20) {
      return {
        majorPhase: 'ACCUMULATION',
        subPhase: 'PHASE_B_BUILDING_CAUSE',
        confidence: 50,
        springDetected: false,
        springPrice: 0,
        utadDetected: false,
        utadPrice: 0,
        signOfStrengthDetected: false,
        signOfWeaknessDetected: false,
        manipulationProbability: 0.1,
        expectedNextTrend: 'RANGING',
        daysInPhase: 5,
        reasoning: 'Insufficient historical bars for Wyckoff cycle determination.',
        latencyMs: 0
      };
    }

    const lastCandle = candles[n - 1];
    const recentCandles = candles.slice(-50);
    const rangeHigh = Math.max(...recentCandles.map(c => c.high));
    const rangeLow = Math.min(...recentCandles.map(c => c.low));
    const midPoint = (rangeHigh + rangeLow) / 2;
    const atr = recentCandles.slice(-14).reduce((acc, c) => acc + (c.high - c.low), 0) / 14;

    // Detect Spring: Candle dips below support rangeLow with a long lower wick (> 1.5 ATR) and immediate reclaim
    const lowerWick = Math.min(lastCandle.open, lastCandle.close) - lastCandle.low;
    const isSpring = lastCandle.low < rangeLow && lastCandle.close >= rangeLow * 0.998 && lowerWick > atr * 1.5;

    // Detect UTAD (Upthrust After Distribution): Candle breaks above rangeHigh with upper wick and immediate drop
    const upperWick = lastCandle.high - Math.max(lastCandle.open, lastCandle.close);
    const isUTAD = lastCandle.high > rangeHigh && lastCandle.close <= rangeHigh * 1.002 && upperWick > atr * 1.5;

    // Sign of Strength (SOS): Strong wide spread up candle with volume > 2x average
    const avgVol = recentCandles.reduce((a, b) => a + b.volume, 0) / recentCandles.length;
    const isSOS = lastCandle.close > lastCandle.open && (lastCandle.close - lastCandle.open) > atr * 1.8 && lastCandle.volume > avgVol * 1.8;

    // Sign of Weakness (SOW): Strong wide spread down candle with heavy sell volume
    const isSOW = lastCandle.close < lastCandle.open && (lastCandle.open - lastCandle.close) > atr * 1.8 && lastCandle.volume > avgVol * 1.8;

    let majorPhase: WyckoffMajorPhase = 'ACCUMULATION';
    let subPhase: WyckoffSubPhase = 'PHASE_B_BUILDING_CAUSE';
    let confidence = 70;
    let expectedNext: 'MARKUP' | 'MARKDOWN' | 'RANGING' = 'MARKUP';
    let manipulationProb = 0.25;
    let reasoning = '';

    // Phase evaluation
    if (isSpring) {
      majorPhase = 'MANIPULATION';
      subPhase = 'PHASE_C_SPRING';
      confidence = 88;
      manipulationProb = 0.92;
      expectedNext = 'MARKUP';
      reasoning = `WYCKOFF SPRING (Phase C Manipulation): Retail panic trap below ${rangeLow.toFixed(2)}. Lower wick ${lowerWick.toFixed(2)} absorbed by smart money. Imminent markup phase expected.`;
    } else if (isUTAD) {
      majorPhase = 'MANIPULATION';
      subPhase = 'PHASE_C_UTAD';
      confidence = 89;
      manipulationProb = 0.94;
      expectedNext = 'MARKDOWN';
      reasoning = `WYCKOFF UTAD (Phase C Upthrust Manipulation): Retail breakout trap above ${rangeHigh.toFixed(2)}. Smart money offloaded inventory into retail FOMO. Imminent markdown phase expected.`;
    } else if (isSOS) {
      majorPhase = 'ACCUMULATION';
      subPhase = 'PHASE_D_SOS_LPS';
      confidence = 84;
      manipulationProb = 0.20;
      expectedNext = 'MARKUP';
      reasoning = `Sign of Strength (SOS) confirmed. Volume expansion ${(lastCandle.volume / avgVol).toFixed(1)}x average. Transitioning to Phase E Markup.`;
    } else if (isSOW) {
      majorPhase = 'DISTRIBUTION';
      subPhase = 'PHASE_D_SOW';
      confidence = 84;
      manipulationProb = 0.20;
      expectedNext = 'MARKDOWN';
      reasoning = `Sign of Weakness (SOW) confirmed. Heavy institutional distribution breaking support. Transitioning to Phase E Markdown.`;
    } else if (lastCandle.close > rangeHigh) {
      majorPhase = 'MARKUP';
      subPhase = 'PHASE_E_MARKUP';
      confidence = 80;
      expectedNext = 'MARKUP';
      reasoning = `Phase E Markup: Confirmed sustained markup trend above historical distribution range.`;
    } else if (lastCandle.close < rangeLow) {
      majorPhase = 'MARKDOWN';
      subPhase = 'PHASE_E_MARKDOWN';
      confidence = 80;
      expectedNext = 'MARKDOWN';
      reasoning = `Phase E Markdown: Price trending downward following institutional distribution completion.`;
    } else {
      // Intrarange diagnosis based on CVD and DIX
      if (cvdDelta > 0 && dixScore > 0.52) {
        majorPhase = 'ACCUMULATION';
        subPhase = 'PHASE_B_BUILDING_CAUSE';
        confidence = 75;
        expectedNext = 'MARKUP';
        reasoning = `Accumulation Phase B: CVD rising (+$${(cvdDelta / 1e6).toFixed(2)}M) while price remains flat. Dark Pool DIX ${dixScore.toFixed(2)} indicates hidden absorption.`;
      } else if (cvdDelta < 0 && dixScore < 0.45) {
        majorPhase = 'DISTRIBUTION';
        subPhase = 'PHASE_B_DISTRIBUTION_CAUSE';
        confidence = 75;
        expectedNext = 'MARKDOWN';
        reasoning = `Distribution Phase B: CVD declining (-$${Math.abs(cvdDelta / 1e6).toFixed(2)}M) while price trades near upper channel. Institutional offloading.`;
      } else {
        majorPhase = 'ACCUMULATION';
        subPhase = 'PHASE_A_STOPPING';
        confidence = 65;
        expectedNext = 'RANGING';
        reasoning = `Preliminary balance: Market moving within consolidation bounds [${rangeLow.toFixed(2)} - ${rangeHigh.toFixed(2)}].`;
      }
    }

    const latencyMs = Number((performance.now() - t0).toFixed(3));
    return {
      majorPhase,
      subPhase,
      confidence,
      springDetected: isSpring,
      springPrice: isSpring ? lastCandle.low : 0,
      utadDetected: isUTAD,
      utadPrice: isUTAD ? lastCandle.high : 0,
      signOfStrengthDetected: isSOS,
      signOfWeaknessDetected: isSOW,
      manipulationProbability: Number(manipulationProb.toFixed(2)),
      expectedNextTrend: expectedNext,
      daysInPhase: Math.min(45, Math.max(3, Math.floor(n / 20))),
      reasoning,
      latencyMs
    };
  }
}
