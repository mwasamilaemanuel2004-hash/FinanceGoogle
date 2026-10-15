import { selfModel } from '../consciousness/self_model';
import { generateVoiceOutput, VoiceMode } from '../consciousness/voice';
import { metaCognition } from '../consciousness/meta_cognition';
import { adaptiveDetection, AdaptiveDetectionOutput } from '../core/adaptive_detection';
import { continuousLearning } from '../learning/continuous_learning';

export interface EngineSelectionDecision {
  market: string;
  assetGroup: string;
  primeEngine: string;
  secondaryEngine: string;
  primeEngineWeightPct: number;
  secondaryEngineWeightPct: number;
  regimeDetected: string;
  adaptiveLotDiscount: number;
  reasoning: string;
}

export interface MotherDecision {
  market: string;
  action: 'BUY' | 'SELL' | 'WAIT';
  lot: number;
  humanFeeling: string;
  beyondConfidencePct: number;
  decisionNarrative: string;
  selfAwarenessTimestamp: string;
  engineSelection?: EngineSelectionDecision;
}

export class MotherAgent {
  private static instance: MotherAgent;
  private voiceMode: VoiceMode = 'HYBRID';

  private constructor() {}

  public static getInstance(): MotherAgent {
    if (!MotherAgent.instance) {
      MotherAgent.instance = new MotherAgent();
    }
    return MotherAgent.instance;
  }

  public setVoiceMode(mode: VoiceMode) {
    this.voiceMode = mode;
  }

  /**
   * Group 10: Dynamic Routing with Quant + ADD (Adaptive Detection Dynamic)
   */
  public selectEngineForMarket(
    market: string,
    currentPrice: number = 100,
    atr: number = 1.0,
    avgAtr: number = 1.0,
    recentPrices: number[] = [100, 100.5, 101, 101.2, 100.8]
  ): EngineSelectionDecision {
    const sym = market.toUpperCase();
    const learning = continuousLearning.getLearningState(sym);
    const add = adaptiveDetection.evaluate(sym, currentPrice, atr, avgAtr, recentPrices);

    let assetGroup = 'FOREX';
    let primeEngine = 'TrendSpider + Universal Ichimoku (9, 26, 52)';
    let secondaryEngine = 'Forex Orderbook OFI + Spread Filter';
    let primeWeight = 60;
    let secWeight = 40;

    if (sym.includes('BOOM') || sym.includes('CRASH') || sym.includes('JUMP') || sym.includes('RANGE')) {
      assetGroup = 'BOOM_CRASH_GROUP';
      primeEngine = 'Poisson Jump + Hawkes Point Process';
      secondaryEngine = 'Stop-Hunt Sweep + Fast Ichimoku (3, 10, 20)';
      primeWeight = 65;
      secWeight = 35;
    } else if (sym.startsWith('V') || sym.includes('VOLATILITY')) {
      assetGroup = 'VOLATILITY_GROUP';
      if (add.regimeState.regime === 'CALM_RANGE') {
        primeEngine = 'Ornstein-Uhlenbeck Mean Reversion MLE';
        secondaryEngine = 'V75 Tick Orderbook + Volatility Cone';
        primeWeight = 70;
        secWeight = 30;
      } else {
        primeEngine = 'Volatility Dispersion Arbitrage + Fast Ichimoku (5, 15, 30)';
        secondaryEngine = 'Ornstein-Uhlenbeck Mean Reversion';
        primeWeight = 55;
        secWeight = 45;
      }
    } else if (sym.includes('BTC') || sym.includes('ETH') || sym.includes('SOL') || sym.includes('BNB') || sym.includes('XRP')) {
      assetGroup = 'CRYPTO_GROUP';
      primeEngine = 'Merton Jump-Diffusion + On-Chain Flow';
      secondaryEngine = 'Funding Rate Fade + Coinglass Liquidation Clusters';
      primeWeight = 60;
      secWeight = 40;
    } else if (sym.includes('XAU') || sym.includes('XAG') || sym.includes('GOLD') || sym.includes('SILVER') || sym.includes('OIL')) {
      assetGroup = 'COMMODITIES_GROUP';
      primeEngine = 'Real Rates (TIPS) + DXY Inversion';
      secondaryEngine = 'Thick Cloud Ichimoku (2x cloud buffer) + Gibson-Schwartz';
      primeWeight = 65;
      secWeight = 35;
    } else if (sym.includes('AAPL') || sym.includes('NVDA') || sym.includes('MSFT') || sym.includes('TSLA')) {
      assetGroup = 'STOCK_GROUP';
      primeEngine = 'Exocharts Polygon Options Sweeps + FINRA Dark Pool';
      secondaryEngine = 'Level 2 Heatmap Iceberg + VWAP Bands';
      primeWeight = 65;
      secWeight = 35;
    } else if (sym.includes('US500') || sym.includes('NAS100') || sym.includes('US30') || sym.includes('GER40')) {
      assetGroup = 'INDICES_GROUP';
      primeEngine = 'Advance-Decline TRIN + Yield Curve 10Y-2Y Spread';
      secondaryEngine = 'CAPM Factor Model + Ichimoku Cloud';
      primeWeight = 60;
      secWeight = 40;
    }

    // Adaptive adjustment:
    // If HMM detects trending -> increase TrendSpider/Ichimoku weight
    if (add.regimeState.regime === 'TRENDING_DIRECTIONAL') {
      primeWeight = Math.min(85, primeWeight + 10);
      secWeight = Math.max(15, secWeight - 10);
    } else if (add.regimeState.regime === 'VOLATILE_CHAOTIC') {
      primeWeight = 50;
      secWeight = 50;
    }

    const adaptiveDiscount = add.adaptedParams.adjustedLotFraction;

    return {
      market: sym,
      assetGroup,
      primeEngine,
      secondaryEngine,
      primeEngineWeightPct: primeWeight,
      secondaryEngineWeightPct: secWeight,
      regimeDetected: add.regimeState.regime,
      adaptiveLotDiscount: adaptiveDiscount,
      reasoning: `Selected ${primeEngine} (${primeWeight}%) + ${secondaryEngine} (${secWeight}%) based on ${add.regimeState.regime} regime, Hurst ${add.regimeState.hurstExponent}, rolling Sharpe ${learning?.sharpe_last_100 || 1.35}.`,
    };
  }

  public evaluateTradeOpportunity(
    market: string,
    edgePct: number,
    calculatedLot: number,
    currentPrice: number = 100,
    atr: number = 1.0,
    avgAtr: number = 1.0
  ): MotherDecision {
    // 1. Run Consciousness Self Check
    const consciousness = selfModel.selfCheck();

    // 2. Select Engine with Dynamic ADD Routing
    const engineSelection = this.selectEngineForMarket(market, currentPrice, atr, avgAtr);

    // Apply adaptive lot sizing
    const finalLot = Number((calculatedLot * engineSelection.adaptiveLotDiscount).toFixed(3));

    // 3. Meta-Cognitive Validation
    if (edgePct < 60) {
      metaCognition.recordMetaThought(
        `Edge on ${market} is only ${edgePct}%.`,
        'As a disciplined human trader, taking low-probability setups violates long-term expectancy.',
        0.8,
        'PAUSE'
      );

      return {
        market,
        action: 'WAIT',
        lot: 0,
        humanFeeling: 'Patiently waiting for higher quality market structure.',
        beyondConfidencePct: edgePct,
        decisionNarrative: `I see ${market}, but quant consensus shows only ${edgePct}%. Under ${engineSelection.regimeDetected} regime, master trader patience dictates waiting.`,
        selfAwarenessTimestamp: consciousness.lastSelfCheck,
        engineSelection,
      };
    }

    // 4. Positive Edge: Generate voice and take trade
    const voice = generateVoiceOutput(this.voiceMode, market, edgePct, finalLot);

    return {
      market,
      action: 'BUY',
      lot: finalLot,
      humanFeeling: `Confident alignment in ${engineSelection.regimeDetected} regime. Adaptive lot factor: ${engineSelection.adaptiveLotDiscount}.`,
      beyondConfidencePct: edgePct,
      decisionNarrative: `${voice.text} [Routed to ${engineSelection.primeEngine}]`,
      selfAwarenessTimestamp: consciousness.lastSelfCheck,
      engineSelection,
    };
  }
}

export const motherAgent = MotherAgent.getInstance();
