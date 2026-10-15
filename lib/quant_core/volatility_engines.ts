/**
 * ESMH.TRADE — GROUP 2: VOLATILITY INDICES ENGINES (CQF / FRM / CFA / ADD MERGED)
 * Path: /lib/quant_core/volatility_engines.ts
 *
 * Covers: V10, V25, V50, V75, V100, V10(1s), V100(1s).
 */

import { OrnsteinUhlenbeckEngine, OrnsteinUhlenbeckEvaluation } from '../engines/ou_mean_reversion';
import { VolatilityArbEngine, VolArbEvaluation } from '../engines/vol_arb';
import { V75OrderbookEngine, V75OrderbookEvaluation } from '../engines/v75_orderbook';
import { IchimokuCompleteEngine, IchimokuEvaluation } from '../engines/ichimoku_complete';

export interface VolatilityGroupEvaluation {
  symbol: string;
  ouEval: OrnsteinUhlenbeckEvaluation;
  volArbEval: VolArbEvaluation;
  v75OrderbookEval?: V75OrderbookEvaluation;
  ichimokuEval: IchimokuEvaluation;
  primeEngineDecision: string;
  ensembleSignal: 'STRONG_BUY' | 'STRONG_SELL' | 'MEAN_REVERT_EXIT' | 'WAIT';
  adaptiveZScore: number;
}

export class VolatilityEngines {
  public static evaluateMarket(
    symbol: string = 'V75',
    currentPrice: number = 425100.0,
    realizedVolRatio: number = 1.05
  ): VolatilityGroupEvaluation {
    const ou = OrnsteinUhlenbeckEngine.evaluate(symbol, currentPrice, realizedVolRatio);
    const volArb = VolatilityArbEngine.evaluate(symbol, 0.024, 0.026);
    const ichi = IchimokuCompleteEngine.evaluate(symbol, currentPrice, 350.0);
    const v75Book = symbol.toUpperCase().includes('75') ? V75OrderbookEngine.evaluate(symbol, currentPrice) : undefined;

    let ensembleSignal: 'STRONG_BUY' | 'STRONG_SELL' | 'MEAN_REVERT_EXIT' | 'WAIT' = 'WAIT';
    if (ou.signal === 'STRONG_BUY_REVERSION') {
      ensembleSignal = 'STRONG_BUY';
    } else if (ou.signal === 'STRONG_SELL_REVERSION') {
      ensembleSignal = 'STRONG_SELL';
    } else if (ou.signal === 'EXIT_TP_HALF_LIFE') {
      ensembleSignal = 'MEAN_REVERT_EXIT';
    }

    return {
      symbol: symbol.toUpperCase(),
      ouEval: ou,
      volArbEval: volArb,
      v75OrderbookEval: v75Book,
      ichimokuEval: ichi,
      primeEngineDecision: ou.add.regimeState === 'STATIONARY_REVERTING' ? 'OU Mean Reversion' : 'Vol Arb Dispersion',
      ensembleSignal,
      adaptiveZScore: ou.add.adaptiveZThreshold,
    };
  }
}
