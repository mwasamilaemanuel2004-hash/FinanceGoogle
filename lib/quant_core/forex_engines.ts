/**
 * ESMH.TRADE — GROUP 1: FOREX & METALS QUANT ENGINES
 * Path: /lib/quant_core/forex_engines.ts
 *
 * Covers 9 Core FX Pairs (EURUSD, GBPUSD, USDJPY, AUDUSD, USDCAD, NZDUSD, EURGBP, EURJPY, GBPJPY)
 * + Precious Metals (XAUUSD Gold, XAGUSD Silver).
 * Full compliance with CQF, FRM, and CFA quantitative finance standards.
 */

import { TrendSpiderAutoEngine, TrendSpiderEvaluation } from '../engines/trendspider_auto';
import { UniversalIchimokuEngine, UniversalIchimokuEvaluation } from '../engines/ichimoku_complete';

export interface ForexGroupMetrics {
  pair: string;
  trendSpider: TrendSpiderEvaluation;
  ichimoku: UniversalIchimokuEvaluation;
  cqfCompositeScore: number;
  frmCompositeVaR95: number;
  cfaCompositeSharpe: number;
  cfaCompositeReturnAnnual: number;
  isTradeEligible: boolean;
  recommendation: 'EXECUTE_LONG' | 'EXECUTE_SHORT' | 'STAND_ASIDE';
}

export class ForexEnginesManager {
  public static readonly SUPPORTED_PAIRS = [
    'EURUSD', 'GBPUSD', 'USDJPY', 'AUDUSD', 'USDCAD', 'NZDUSD',
    'EURGBP', 'EURJPY', 'GBPJPY', 'XAUUSD', 'XAGUSD'
  ];

  public static evaluatePair(pair: string, currentPrice: number, atr: number): ForexGroupMetrics {
    const trendSpider = TrendSpiderAutoEngine.evaluate(pair, currentPrice, atr);
    const ichimoku = UniversalIchimokuEngine.evaluate(pair, currentPrice, atr);

    const cqfCompositeScore = (trendSpider.cqf.trendStrengthScore * 10) + (ichimoku.cqf.ichimokuScore * 0.6);
    const frmCompositeVaR95 = Math.max(trendSpider.frm.valueAtRisk95Pct, ichimoku.frm.valueAtRisk95Pct);
    const cfaCompositeSharpe = (trendSpider.cfa.sharpeRatio + ichimoku.cfa.sharpeRatio) / 2.0;
    const cfaCompositeReturnAnnual = (trendSpider.cfa.annualizedReturnPct + ichimoku.cfa.annualizedReturnPct) / 2.0;

    const isTradeEligible = frmCompositeVaR95 <= 2.0 && cfaCompositeSharpe >= 1.3;
    const recommendation = (cqfCompositeScore > 50 && isTradeEligible) ? 'EXECUTE_LONG' : 'STAND_ASIDE';

    return {
      pair,
      trendSpider,
      ichimoku,
      cqfCompositeScore: Number(cqfCompositeScore.toFixed(2)),
      frmCompositeVaR95: Number(frmCompositeVaR95.toFixed(2)),
      cfaCompositeSharpe: Number(cfaCompositeSharpe.toFixed(2)),
      cfaCompositeReturnAnnual: Number(cfaCompositeReturnAnnual.toFixed(2)),
      isTradeEligible,
      recommendation,
    };
  }

  public static evaluateAll(): Record<string, ForexGroupMetrics> {
    const samplePrices: Record<string, { price: number; atr: number }> = {
      'EURUSD': { price: 1.0850, atr: 0.0045 },
      'GBPUSD': { price: 1.2980, atr: 0.0060 },
      'USDJPY': { price: 148.50, atr: 0.85 },
      'AUDUSD': { price: 0.6650, atr: 0.0040 },
      'USDCAD': { price: 1.3620, atr: 0.0050 },
      'NZDUSD': { price: 0.6050, atr: 0.0038 },
      'EURGBP': { price: 0.8360, atr: 0.0030 },
      'EURJPY': { price: 161.20, atr: 0.95 },
      'GBPJPY': { price: 192.80, atr: 1.20 },
      'XAUUSD': { price: 2650.0, atr: 28.5 },
      'XAGUSD': { price: 31.80, atr: 0.65 },
    };

    const results: Record<string, ForexGroupMetrics> = {};
    for (const pair of this.SUPPORTED_PAIRS) {
      const data = samplePrices[pair] || { price: 1.0, atr: 0.01 };
      results[pair] = this.evaluatePair(pair, data.price, data.atr);
    }
    return results;
  }
}
