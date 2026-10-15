/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — QUANT COGNITION REASONING CHAIN
 * ============================================================================
 * Chain-of-thought architecture modeling a senior human quantitative trader:
 * Step 1: Perceive (Macro, Inflow, DOM, Volume Profile, GEX, Rates)
 * Step 2: Recognize Pattern (Historical vector embedding match)
 * Step 3: Differentiate (Weak Profit Opportunity vs Hazard / Loss)
 * Step 4: Decide Maneuver (Main logic isolation, small sizing, risk bounds)
 * Step 5: Learn & Evolve (Walk-forward performance adaptation)
 * ============================================================================
 */

export interface MarketPerceptionInput {
  symbol: string;
  price: number;
  dxyIndex?: number;
  dxyChangePct?: number;
  tipsYieldPct?: number;
  ofiRatio?: number;
  cvdDeltaUsd?: number;
  domBidWallPrice?: number;
  domBidWallLots?: number;
  domAskWallPrice?: number;
  domAskWallLots?: number;
  pocPrice?: number;
  lvnVoidLow?: number;
  lvnVoidHigh?: number;
  netDealerGexUsd?: number;
  putWallPrice?: number;
  callWallPrice?: number;
  highImpactNewsMinutesAway?: number;
  vpinToxicity?: number;
  spoofingDetected?: boolean;
}

export interface HistoricalPatternMatch {
  patternId: string;
  dateReference: string;
  similarityScore: number; // 0.0 - 1.0 (Cosine similarity)
  historicalOutcomeReturnPct: number;
  historicalWinRatePct: number;
  description: string;
}

export interface ReasoningStep {
  stepNumber: 1 | 2 | 3 | 4 | 5;
  stepName: 'Perceive' | 'Recognize Pattern' | 'Differentiate' | 'Decide Maneuver' | 'Learn & Evolve';
  thoughtNarrative: string;
  inferences: Record<string, string | number | boolean>;
  latencyMs: number;
}

export interface FullChainOfThought {
  timestamp: number;
  symbol: string;
  steps: ReasoningStep[];
  conclusion: {
    primaryMacroBias: 'BUY' | 'SELL' | 'NEUTRAL';
    weakProfitSubOpportunity: boolean;
    weakProfitAction: 'TAKE' | 'AVOID';
    subTradeSignal?: 'BUY' | 'SELL';
    subTradeExpectedReturnPct: number;
    subTradeConfidencePct: number;
    subTradeKellyFactor: number;
    targetTakeProfit: number;
    stopLoss: number;
    timeStopMinutes: number;
    isolationVerified: boolean;
  };
  totalLatencyMs: number;
}
