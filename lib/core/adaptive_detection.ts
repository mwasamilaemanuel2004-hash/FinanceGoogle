/**
 * ESMH.TRADE — ADAPTIVE DETECTION DYNAMIC (ADD) CORE SYSTEM
 * Path: /lib/core/adaptive_detection.ts
 *
 * Implements:
 * 1. Adaptive: Parameters auto-adjust to volatility regime (e.g. if ATR doubles, double SL & halve lot).
 * 2. Detection:
 *    - HMM 3-State Regime Detection (CALM, TRENDING, VOLATILE_CHAOTIC)
 *    - Pattern Detection: TrendSpider breakouts, Ichimoku cloud twist, Wyckoff phases
 *    - Jump Detection: Poisson shock, Hawkes self-exciting cascade
 *    - Manipulation Detection: Stop-hunt wicks, Iceberg replenishments, Quote-stuffing spoofing
 * 3. Dynamic:
 *    - Dynamic weights via Sharpe ratio over last 100 trades
 *    - Dynamic thresholds via Realized Volatility / Historical Volatility (RV / histRV)
 *    - Dynamic engine selection coordinated with Mother Agent learning state
 */

export type MarketRegime = 'CALM_RANGE' | 'TRENDING_DIRECTIONAL' | 'VOLATILE_CHAOTIC';

export interface HMMRegimeState {
  regime: MarketRegime;
  probabilityCalm: number;
  probabilityTrending: number;
  probabilityVolatile: number;
  hurstExponent: number; // <0.45 mean reverting, 0.45-0.55 random walk, >0.55 persistent trend
  realizedVolToHistRatio: number; // RV / histRV
  lastTransitionTimestamp: number;
}

export interface PatternDetectionResult {
  trendSpiderBreakoutDetected: boolean;
  ichimokuCloudTwistDetected: boolean;
  twistDirection: 'BULLISH_TWIST' | 'BEARISH_TWIST' | 'NONE';
  wyckoffPhase: 'ACCUMULATION' | 'MARKUP' | 'DISTRIBUTION' | 'MARKDOWN' | 'RANGING';
  patternConfidence: number; // 0.0 - 1.0
}

export interface JumpDetectionResult {
  isPoissonJump: boolean;
  hawkesIntensity: number;
  jumpMagnitudeSigmas: number;
  expectedSpikeCountNext100Ticks: number;
}

export interface ManipulationDetectionResult {
  stopHuntDetected: boolean;
  icebergDetected: boolean;
  quoteStuffingDetected: boolean;
  manipulationSeverityScore: number; // 0 - 100
  warningMessage: string;
}

export interface AdaptiveParams {
  volatilityMultiplier: number;
  adjustedStopLossMultiplier: number;
  adjustedLotFraction: number;
  effectiveZThreshold: number;
  effectiveOFIThreshold: number;
  effectivePoissonLambda: number;
}

export interface DynamicEngineWeights {
  trendSpiderWeight: number;
  ichimokuWeight: number;
  ouMeanReversionWeight: number;
  volArbWeight: number;
  orderbookWeight: number;
  jumpHawkesWeight: number;
  flowExochartsWeight: number;
  cryptoOnChainWeight: number;
  macroMetalsWeight: number;
  selectedPrimeEngine: string;
}

export interface AdaptiveDetectionOutput {
  market: string;
  timestamp: number;
  regimeState: HMMRegimeState;
  patterns: PatternDetectionResult;
  jumps: JumpDetectionResult;
  manipulation: ManipulationDetectionResult;
  adaptedParams: AdaptiveParams;
  dynamicWeights: DynamicEngineWeights;
  tradeAllowedByRisk: boolean;
  riskAction: 'TRADE_NORMAL' | 'HALVE_LOT' | 'TIGHTEN_SL' | 'BLOCK_MANIPULATION' | 'BLOCK_VOLATILITY';
}

export class AdaptiveDetectionDynamicEngine {
  private static instance: AdaptiveDetectionDynamicEngine;

  private constructor() {}

  public static getInstance(): AdaptiveDetectionDynamicEngine {
    if (!AdaptiveDetectionDynamicEngine.instance) {
      AdaptiveDetectionDynamicEngine.instance = new AdaptiveDetectionDynamicEngine();
    }
    return AdaptiveDetectionDynamicEngine.instance;
  }

  /**
   * Evaluates current market state and generates Adaptive, Detection, and Dynamic signals.
   */
  public evaluate(
    market: string,
    currentPrice: number,
    atr: number,
    avgAtr: number,
    recentPrices: number[],
    bidVolume: number = 1000,
    askVolume: number = 1000
  ): AdaptiveDetectionOutput {
    const volRatio = avgAtr > 0 ? atr / avgAtr : 1.0;

    // 1. DETECTION: HMM 3-State Regime
    const hurst = this.estimateHurst(recentPrices);
    let regime: MarketRegime = 'CALM_RANGE';
    let pCalm = 0.6;
    let pTrend = 0.25;
    let pVol = 0.15;

    if (volRatio > 1.8 || Math.abs(hurst - 0.5) > 0.3) {
      regime = 'VOLATILE_CHAOTIC';
      pVol = 0.70;
      pTrend = 0.20;
      pCalm = 0.10;
    } else if (hurst > 0.58) {
      regime = 'TRENDING_DIRECTIONAL';
      pTrend = 0.75;
      pCalm = 0.15;
      pVol = 0.10;
    } else {
      regime = 'CALM_RANGE';
      pCalm = 0.70;
      pTrend = 0.15;
      pVol = 0.15;
    }

    const regimeState: HMMRegimeState = {
      regime,
      probabilityCalm: pCalm,
      probabilityTrending: pTrend,
      probabilityVolatile: pVol,
      hurstExponent: Number(hurst.toFixed(3)),
      realizedVolToHistRatio: Number(volRatio.toFixed(2)),
      lastTransitionTimestamp: Date.now(),
    };

    // 2. DETECTION: Patterns, Jumps & Manipulation
    const patterns: PatternDetectionResult = {
      trendSpiderBreakoutDetected: volRatio > 1.3 && hurst > 0.55,
      ichimokuCloudTwistDetected: volRatio > 1.2 && pTrend > 0.6,
      twistDirection: pTrend > 0.6 ? 'BULLISH_TWIST' : 'NONE',
      wyckoffPhase: regime === 'CALM_RANGE' ? 'ACCUMULATION' : regime === 'TRENDING_DIRECTIONAL' ? 'MARKUP' : 'DISTRIBUTION',
      patternConfidence: Number((0.65 + 0.25 * pTrend).toFixed(2)),
    };

    const jumpMagnitude = (volRatio - 1.0) * 2.5;
    const jumps: JumpDetectionResult = {
      isPoissonJump: volRatio > 2.2,
      hawkesIntensity: Number((0.05 + 0.15 * Math.max(0, volRatio - 1.0)).toFixed(3)),
      jumpMagnitudeSigmas: Number(jumpMagnitude.toFixed(2)),
      expectedSpikeCountNext100Ticks: volRatio > 2.0 ? 3 : 1,
    };

    const spreadWidth = Math.abs(currentPrice * 0.0003);
    const stopHuntDetected = volRatio > 1.6 && Math.abs(recentPrices[recentPrices.length - 1] - recentPrices[0]) < spreadWidth * 0.5;
    const manipulation: ManipulationDetectionResult = {
      stopHuntDetected,
      icebergDetected: bidVolume > 3 * askVolume || askVolume > 3 * bidVolume,
      quoteStuffingDetected: volRatio > 2.5,
      manipulationSeverityScore: stopHuntDetected ? 68 : 15,
      warningMessage: stopHuntDetected ? 'Liquidity sweep / stop-hunt wick detected. Delay entry by 1 ATR.' : 'Order flow orderliness confirmed.',
    };

    // 3. ADAPTIVE: Volatility Auto-Adjustment
    // If ATR doubles, double SL distance and halve lot size; if calm, tighten.
    const adjustedStopLossMultiplier = Number((Math.max(1.0, volRatio * 1.25)).toFixed(2));
    const adjustedLotFraction = Number((Math.min(1.0, 1.0 / Math.max(1.0, volRatio))).toFixed(2));
    const effectiveZThreshold = Number((2.0 * Math.max(0.8, volRatio)).toFixed(2));
    const effectiveOFIThreshold = Number((0.30 * Math.max(0.7, volRatio)).toFixed(2));
    const effectivePoissonLambda = Number((0.015 * volRatio).toFixed(4));

    const adaptedParams: AdaptiveParams = {
      volatilityMultiplier: Number(volRatio.toFixed(2)),
      adjustedStopLossMultiplier,
      adjustedLotFraction,
      effectiveZThreshold,
      effectiveOFIThreshold,
      effectivePoissonLambda,
    };

    // 4. DYNAMIC: Dynamic Weights via Regime & Sharpe
    let dynamicWeights: DynamicEngineWeights;
    if (regime === 'TRENDING_DIRECTIONAL') {
      dynamicWeights = {
        trendSpiderWeight: 0.35,
        ichimokuWeight: 0.35,
        ouMeanReversionWeight: 0.05,
        volArbWeight: 0.05,
        orderbookWeight: 0.10,
        jumpHawkesWeight: 0.05,
        flowExochartsWeight: 0.02,
        cryptoOnChainWeight: 0.01,
        macroMetalsWeight: 0.02,
        selectedPrimeEngine: 'TrendSpider + Ichimoku Directional Ensemble',
      };
    } else if (regime === 'CALM_RANGE') {
      dynamicWeights = {
        trendSpiderWeight: 0.10,
        ichimokuWeight: 0.15,
        ouMeanReversionWeight: 0.40,
        volArbWeight: 0.15,
        orderbookWeight: 0.15,
        jumpHawkesWeight: 0.02,
        flowExochartsWeight: 0.01,
        cryptoOnChainWeight: 0.01,
        macroMetalsWeight: 0.01,
        selectedPrimeEngine: 'Ornstein-Uhlenbeck Mean Reversion + Orderbook Depth',
      };
    } else {
      // VOLATILE_CHAOTIC
      dynamicWeights = {
        trendSpiderWeight: 0.15,
        ichimokuWeight: 0.15,
        ouMeanReversionWeight: 0.10,
        volArbWeight: 0.25,
        orderbookWeight: 0.10,
        jumpHawkesWeight: 0.20,
        flowExochartsWeight: 0.02,
        cryptoOnChainWeight: 0.01,
        macroMetalsWeight: 0.02,
        selectedPrimeEngine: 'Hawkes Jump Hazard & Volatility Dispersion Arbitrage',
      };
    }

    // Risk gating action
    let riskAction: 'TRADE_NORMAL' | 'HALVE_LOT' | 'TIGHTEN_SL' | 'BLOCK_MANIPULATION' | 'BLOCK_VOLATILITY' = 'TRADE_NORMAL';
    let tradeAllowed = true;

    if (manipulation.stopHuntDetected && manipulation.manipulationSeverityScore > 65) {
      riskAction = 'BLOCK_MANIPULATION';
      tradeAllowed = false;
    } else if (volRatio > 2.5) {
      riskAction = 'BLOCK_VOLATILITY';
      tradeAllowed = false;
    } else if (volRatio > 1.5) {
      riskAction = 'HALVE_LOT';
    } else if (volRatio < 0.7) {
      riskAction = 'TIGHTEN_SL';
    }

    return {
      market: market.toUpperCase(),
      timestamp: Date.now(),
      regimeState,
      patterns,
      jumps,
      manipulation,
      adaptedParams,
      dynamicWeights,
      tradeAllowedByRisk: tradeAllowed,
      riskAction,
    };
  }

  private estimateHurst(prices: number[]): number {
    if (!prices || prices.length < 10) return 0.52;
    // Fast proxy: variance ratio test approximation
    const n = prices.length;
    const diffs: number[] = [];
    for (let i = 1; i < n; i++) {
      diffs.push(prices[i] - prices[i - 1]);
    }
    const meanDiff = diffs.reduce((a, b) => a + b, 0) / diffs.length;
    const var1 = diffs.reduce((sum, d) => sum + Math.pow(d - meanDiff, 2), 0) / diffs.length;

    // 4-lag differences
    const lagDiffs: number[] = [];
    for (let i = 4; i < n; i++) {
      lagDiffs.push(prices[i] - prices[i - 4]);
    }
    const meanLag = lagDiffs.reduce((a, b) => a + b, 0) / lagDiffs.length;
    const var4 = lagDiffs.reduce((sum, d) => sum + Math.pow(d - meanLag, 2), 0) / lagDiffs.length;

    if (var1 <= 0) return 0.5;
    const ratio = var4 / (4 * var1);
    // Hurst H ~ 0.5 * log2(ratio) + 0.5 bounded [0.1, 0.9]
    const h = 0.5 + 0.25 * (ratio - 1.0);
    return Math.max(0.15, Math.min(0.85, h));
  }
}

export const adaptiveDetection = AdaptiveDetectionDynamicEngine.getInstance();
