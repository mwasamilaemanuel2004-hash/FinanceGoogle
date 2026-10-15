/**
 * ESMH.TRADE — GROUP 3: BOOM & CRASH & JUMP QUANT ENGINES (CQF / FRM / CFA / ADD MERGED)
 * Path: /lib/quant_core/boom_crash_engines.ts
 *
 * Covers: Boom 500/1000, Crash 500/1000, Jump 10-100, Range Break 100/200.
 */

import { JumpProbabilityEngine, JumpProbabilityEvaluation } from '../engines/jump_probability';
import { StopHuntEngine, StopHuntEvaluation } from '../engines/stop_hunt';
import { IchimokuCompleteEngine, IchimokuEvaluation } from '../engines/ichimoku_complete';

export interface BoomCrashGroupEvaluation {
  symbol: string;
  jumpEval: JumpProbabilityEvaluation;
  stopHuntEval: StopHuntEvaluation;
  ichimokuEval: IchimokuEvaluation;
  combinedSignal: 'STRONG_LONG_ANTICIPATION' | 'STRONG_SHORT_FADE' | 'QUARANTINE_WAIT';
  adaptiveLotSize: number;
  overallScore: number;
}

export class BoomCrashEngines {
  public static evaluateMarket(
    symbol: string = 'BOOM1000',
    currentPrice: number = 8520.0,
    ticksSinceLastSpike: number = 240
  ): BoomCrashGroupEvaluation {
    const jump = JumpProbabilityEngine.evaluate(symbol, currentPrice, ticksSinceLastSpike);
    const stopHunt = StopHuntEngine.evaluate(symbol, currentPrice, currentPrice * 1.002, currentPrice * 0.998, 1.8);
    const ichimoku = IchimokuCompleteEngine.evaluate(symbol, currentPrice, 15.0);

    let combinedSignal: 'STRONG_LONG_ANTICIPATION' | 'STRONG_SHORT_FADE' | 'QUARANTINE_WAIT' = 'STRONG_LONG_ANTICIPATION';
    if (jump.signal === 'WAIT_AFTER_SPIKE') {
      combinedSignal = 'QUARANTINE_WAIT';
    } else if (jump.signal === 'ANTICIPATE_SPIKE_LONG') {
      combinedSignal = 'STRONG_LONG_ANTICIPATION';
    } else {
      combinedSignal = 'STRONG_SHORT_FADE';
    }

    return {
      symbol: symbol.toUpperCase(),
      jumpEval: jump,
      stopHuntEval: stopHunt,
      ichimokuEval: ichimoku,
      combinedSignal,
      adaptiveLotSize: jump.frm.adaptiveLotSize,
      overallScore: Number(((jump.cfa.winRatePct * 0.6) + (ichimoku.cqf.ichimokuScore * 0.4)).toFixed(1)),
    };
  }
}
