export interface WeakProfitSubTrade {
  subTradeId: string;
  symbol: string;
  side: 'BUY' | 'SELL';
  parentMacroLogic: 'BUY' | 'SELL' | 'NEUTRAL';
  entryPrice: number;
  takeProfitPrice: number;
  stopLossPrice: number;
  timeStopMinutes: number;
  entryTimestamp: number;
  expiresTimestamp: number;
  sizeUnits: number;
  allocatedCapitalUsd: number;
  isolatedPnlUsd: number;
  status: 'ACTIVE' | 'CLOSED_TAKE_PROFIT' | 'CLOSED_STOP_LOSS' | 'CLOSED_TIME_STOP';
  reasoning: string;
}

export class WeakProfitExecutor {
  private static instance: WeakProfitExecutor;
  private activeSubTrades: Map<string, WeakProfitSubTrade> = new Map();
  private tradeHistory: WeakProfitSubTrade[] = [];

  private constructor() {}

  public static getInstance(): WeakProfitExecutor {
    if (!WeakProfitExecutor.instance) {
      WeakProfitExecutor.instance = new WeakProfitExecutor();
    }
    return WeakProfitExecutor.instance;
  }

  /**
   * Spawns an isolated sub-trade for a micro weak-profit opportunity.
   * Guarantees that the parent macro logic (e.g. Trend SELL) remains completely unaltered.
   */
  public executeIsolatedSubTrade(params: {
    symbol: string;
    side: 'BUY' | 'SELL';
    parentMacroLogic: 'BUY' | 'SELL' | 'NEUTRAL';
    currentPrice: number;
    takeProfitPct: number;
    stopLossPct: number;
    timeStopMinutes?: number;
    availableCapitalUsd: number;
    kellyFactor?: number;
    reasoning?: string;
  }): WeakProfitSubTrade {
    const timeStopMins = params.timeStopMinutes || 15;
    const now = Date.now();
    const expires = now + timeStopMins * 60 * 1000;
    const factor = params.kellyFactor || 0.3; // 0.3x Kelly
    const subTradeCapital = Math.min(params.availableCapitalUsd * 0.015, params.availableCapitalUsd * factor * 0.02);

    const price = params.currentPrice;
    const tpPrice = params.side === 'BUY'
      ? price * (1 + params.takeProfitPct / 100)
      : price * (1 - params.takeProfitPct / 100);
    const slPrice = params.side === 'BUY'
      ? price * (1 - params.stopLossPct / 100)
      : price * (1 + params.stopLossPct / 100);

    const units = subTradeCapital / price;

    const subTrade: WeakProfitSubTrade = {
      subTradeId: `WP_${params.symbol.replace('/', '')}_${Date.now()}`,
      symbol: params.symbol,
      side: params.side,
      parentMacroLogic: params.parentMacroLogic,
      entryPrice: price,
      takeProfitPrice: +tpPrice.toFixed(4),
      stopLossPrice: +slPrice.toFixed(4),
      timeStopMinutes: timeStopMins,
      entryTimestamp: now,
      expiresTimestamp: expires,
      sizeUnits: +units.toFixed(4),
      allocatedCapitalUsd: +subTradeCapital.toFixed(2),
      isolatedPnlUsd: 0.0,
      status: 'ACTIVE',
      reasoning: params.reasoning || 'Isolated micro weak-profit scalp without breaking macro logic.'
    };

    this.activeSubTrades.set(subTrade.subTradeId, subTrade);
    return subTrade;
  }

  /**
   * Evaluates active sub-trades on each incoming tick.
   */
  public updateOnTick(symbol: string, currentPrice: number): WeakProfitSubTrade[] {
    const closed: WeakProfitSubTrade[] = [];
    const now = Date.now();

    for (const [id, trade] of this.activeSubTrades.entries()) {
      if (trade.symbol !== symbol) continue;

      let isClosed = false;

      // Check Take Profit
      if ((trade.side === 'BUY' && currentPrice >= trade.takeProfitPrice) ||
          (trade.side === 'SELL' && currentPrice <= trade.takeProfitPrice)) {
        trade.status = 'CLOSED_TAKE_PROFIT';
        const pnl = Math.abs(trade.takeProfitPrice - trade.entryPrice) * trade.sizeUnits;
        trade.isolatedPnlUsd = +pnl.toFixed(2);
        isClosed = true;
      }
      // Check Stop Loss
      else if ((trade.side === 'BUY' && currentPrice <= trade.stopLossPrice) ||
               (trade.side === 'SELL' && currentPrice >= trade.stopLossPrice)) {
        trade.status = 'CLOSED_STOP_LOSS';
        const loss = -Math.abs(trade.entryPrice - trade.stopLossPrice) * trade.sizeUnits;
        trade.isolatedPnlUsd = +loss.toFixed(2);
        isClosed = true;
      }
      // Check Time Stop
      else if (now >= trade.expiresTimestamp) {
        trade.status = 'CLOSED_TIME_STOP';
        const pnl = (trade.side === 'BUY' ? (currentPrice - trade.entryPrice) : (trade.entryPrice - currentPrice)) * trade.sizeUnits;
        trade.isolatedPnlUsd = +pnl.toFixed(2);
        isClosed = true;
      }

      if (isClosed) {
        this.activeSubTrades.delete(id);
        this.tradeHistory.push(trade);
        closed.push(trade);
      }
    }

    return closed;
  }

  public getActiveSubTrades(): WeakProfitSubTrade[] {
    return Array.from(this.activeSubTrades.values());
  }

  public getHistory(): WeakProfitSubTrade[] {
    return [...this.tradeHistory];
  }
}

export const weakProfitExecutor = WeakProfitExecutor.getInstance();
