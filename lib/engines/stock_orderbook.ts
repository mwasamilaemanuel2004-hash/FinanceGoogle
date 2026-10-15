/**
 * ESMH.TRADE — STOCK ORDERBOOK & LEVEL 2 HEATMAP ENGINE (CQF / FRM / CFA / ADD MERGED)
 * Path: /lib/engines/stock_orderbook.ts
 */

export interface StockOrderbookEvaluation {
  symbol: string;
  level2Imbalance: number;
  icebergDetected: boolean;
  quoteStuffingDetected: boolean;
  marketManipulationScore: number; // 0 to 100
  signal: 'L2_BUY_IMBALANCE' | 'L2_SELL_IMBALANCE' | 'MANIPULATION_DETECTED_WAIT' | 'BALANCED';
}

export class StockOrderbookEngine {
  public static evaluate(
    symbol: string = 'NVDA',
    bidDepth: number = 85000,
    askDepth: number = 42000,
    quoteUpdateFreqMs: number = 12
  ): StockOrderbookEvaluation {
    const sym = symbol.toUpperCase();
    const ratio = (bidDepth - askDepth) / (bidDepth + askDepth || 1);
    const quoteStuffing = quoteUpdateFreqMs < 20 && Math.abs(ratio) < 0.1;
    const iceberg = bidDepth > 2.5 * askDepth;

    let signal: 'L2_BUY_IMBALANCE' | 'L2_SELL_IMBALANCE' | 'MANIPULATION_DETECTED_WAIT' | 'BALANCED' = 'BALANCED';
    if (quoteStuffing) {
      signal = 'MANIPULATION_DETECTED_WAIT';
    } else if (ratio > 0.3) {
      signal = 'L2_BUY_IMBALANCE';
    } else if (ratio < -0.3) {
      signal = 'L2_SELL_IMBALANCE';
    }

    return {
      symbol: sym,
      level2Imbalance: Number(ratio.toFixed(2)),
      icebergDetected: iceberg,
      quoteStuffingDetected: quoteStuffing,
      marketManipulationScore: quoteStuffing ? 85 : iceberg ? 45 : 12,
      signal,
    };
  }
}
