/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — REPLACEMENT LOGIC HANDLER
 * ============================================================================
 * Handles state transitions and risk-replacement actions:
 * - On Weak TP Hit: Moves Strong Leg Stop Loss to Break-Even (Risk-Free Replacement)
 * - On Weak SL Hit: Preserves Strong Runner to compensate small loss
 * - Trailing logic for Strong Leg (ATR 2x, Chandelier, BOS, CVD, GEX)
 * ============================================================================
 */

export interface HybridLegState {
  legType: 'WEAK' | 'STRONG';
  allocatedSizeLots: number;
  entryPrice: number;
  currentPrice: number;
  takeProfitPrice?: number;
  stopLossPrice: number;
  status: 'PENDING' | 'RUNNING' | 'RISK_FREE' | 'CLOSED_PROFIT' | 'CLOSED_SL' | 'CLOSED_BREAKEVEN' | 'CLOSED_TIMESTOP';
  realizedPnlUsd: number;
  returnPct: number;
}

export interface HybridTradeSession {
  tradeId: string;
  symbol: string;
  entryPrice: number;
  entryTimestamp: number;
  marketQualityScore: number;
  weakLeg: HybridLegState;
  strongLeg: HybridLegState;
  netRealizedPnlUsd: number;
  netRealizedReturnPct: number;
  isFullyClosed: boolean;
  replacementActive: boolean;
  replacementLog: string[];
}

export class ReplacementLogicHandler {
  private static instance: ReplacementLogicHandler;

  private constructor() {}

  public static getInstance(): ReplacementLogicHandler {
    if (!ReplacementLogicHandler.instance) {
      ReplacementLogicHandler.instance = new ReplacementLogicHandler();
    }
    return ReplacementLogicHandler.instance;
  }

  /**
   * Action 1: On Weak Leg Take Profit Hit
   * Lock weak leg profit and IMMEDIATELY move Strong Leg Stop-Loss to Entry Price (Break-Even).
   * REPLACEMENT: The risk of the entire trade is now ZERO (Risk-Free Runner).
   */
  public onWeakTPHit(trade: HybridTradeSession, exitPrice: number): void {
    trade.weakLeg.currentPrice = exitPrice;
    trade.weakLeg.status = 'CLOSED_PROFIT';
    const weakGainPct = ((exitPrice - trade.entryPrice) / trade.entryPrice) * 100;
    trade.weakLeg.returnPct = +weakGainPct.toFixed(4);
    // 50% size * gain
    const netContributionPct = +(weakGainPct * 0.50).toFixed(4);
    trade.weakLeg.realizedPnlUsd = +(trade.weakLeg.allocatedSizeLots * (exitPrice - trade.entryPrice)).toFixed(2);

    // REPLACEMENT ACTION: Move strong leg SL to Entry Price
    trade.strongLeg.stopLossPrice = trade.entryPrice;
    trade.strongLeg.status = 'RISK_FREE';
    trade.replacementActive = true;

    const logEntry = `REPLACEMENT: Weak TP hit at ${exitPrice.toFixed(2)} (+${weakGainPct.toFixed(3)}%, net +${netContributionPct.toFixed(3)}%). Strong Leg SL moved to Break-Even (${trade.entryPrice.toFixed(2)}). Strong Leg is now 100% RISK-FREE.`;
    trade.replacementLog.push(logEntry);
  }

  /**
   * Action 2: On Weak Leg Stop-Loss Hit
   * Small loss recorded (-0.025% net). Strong leg runner remains open to compensate.
   */
  public onWeakSLHit(trade: HybridTradeSession, exitPrice: number): void {
    trade.weakLeg.currentPrice = exitPrice;
    trade.weakLeg.status = 'CLOSED_SL';
    const weakLossPct = ((exitPrice - trade.entryPrice) / trade.entryPrice) * 100;
    trade.weakLeg.returnPct = +weakLossPct.toFixed(4);
    trade.weakLeg.realizedPnlUsd = +(trade.weakLeg.allocatedSizeLots * (exitPrice - trade.entryPrice)).toFixed(2);

    const logEntry = `WEAK SL HIT at ${exitPrice.toFixed(2)} (${weakLossPct.toFixed(3)}%, net ${(weakLossPct * 0.50).toFixed(3)}%). Strong Leg remains active to compensate.`;
    trade.replacementLog.push(logEntry);
  }

  /**
   * Action 3: Trail Strong Leg using ATR 2x and Structural Protections
   */
  public evaluateStrongTrailing(
    trade: HybridTradeSession,
    currentPrice: number,
    atr2x: number,
    marketState: {
      adx: number;
      hasBosConfirmation: boolean;
      hasCvdSupport: boolean;
      hasExhaustionClimax: boolean;
      hasUtadOrSpoof: boolean;
      vpinToxicity: number;
      marketQualityScore: number;
    }
  ): boolean {
    if (trade.strongLeg.status === 'CLOSED_PROFIT' || trade.strongLeg.status === 'CLOSED_SL' || trade.strongLeg.status === 'CLOSED_BREAKEVEN') {
      return false;
    }

    trade.strongLeg.currentPrice = currentPrice;

    // Check if stopped out by Break-Even or Trailing SL
    if (currentPrice <= trade.strongLeg.stopLossPrice) {
      const isBreakeven = Math.abs(trade.strongLeg.stopLossPrice - trade.entryPrice) < 0.01;
      trade.strongLeg.status = isBreakeven ? 'CLOSED_BREAKEVEN' : (trade.strongLeg.stopLossPrice > trade.entryPrice ? 'CLOSED_PROFIT' : 'CLOSED_SL');
      const strongReturnPct = ((currentPrice - trade.entryPrice) / trade.entryPrice) * 100;
      trade.strongLeg.returnPct = +strongReturnPct.toFixed(4);
      trade.strongLeg.realizedPnlUsd = +(trade.strongLeg.allocatedSizeLots * (currentPrice - trade.entryPrice)).toFixed(2);
      trade.replacementLog.push(`Strong Leg closed at ${currentPrice.toFixed(2)} (${trade.strongLeg.status}). Return: ${strongReturnPct.toFixed(3)}%.`);
      this.reconcileSession(trade);
      return true;
    }

    // Defensive Exit Condition for Strong Leg
    const isExhaustion = marketState.hasExhaustionClimax;
    const isManipulation = marketState.hasUtadOrSpoof;
    const isToxic = marketState.vpinToxicity > 0.70;
    const isChoppyCollapse = marketState.marketQualityScore < 40;

    if (isExhaustion || isManipulation || isToxic || isChoppyCollapse) {
      trade.strongLeg.status = currentPrice >= trade.entryPrice ? 'CLOSED_PROFIT' : 'CLOSED_SL';
      const strongReturnPct = ((currentPrice - trade.entryPrice) / trade.entryPrice) * 100;
      trade.strongLeg.returnPct = +strongReturnPct.toFixed(4);
      trade.strongLeg.realizedPnlUsd = +(trade.strongLeg.allocatedSizeLots * (currentPrice - trade.entryPrice)).toFixed(2);
      trade.replacementLog.push(`DEFENSIVE EXIT: Strong leg closed at ${currentPrice.toFixed(2)} due to market decay/VPIN. Return: ${strongReturnPct.toFixed(3)}%.`);
      this.reconcileSession(trade);
      return true;
    }

    // Trailing logic: Advance SL using Trailing ATR 2x
    const candidateSl = currentPrice - atr2x;
    if (candidateSl > trade.strongLeg.stopLossPrice) {
      trade.strongLeg.stopLossPrice = +candidateSl.toFixed(2);
      trade.replacementLog.push(`Trailing SL advanced to ${candidateSl.toFixed(2)} (ATR 2x trailing).`);
    }

    return false;
  }

  public reconcileSession(trade: HybridTradeSession): void {
    const weakPnl = trade.weakLeg.realizedPnlUsd;
    const strongPnl = trade.strongLeg.realizedPnlUsd;
    trade.netRealizedPnlUsd = +(weakPnl + strongPnl).toFixed(2);
    // Net return % is weighted sum of the two 50% legs
    trade.netRealizedReturnPct = +(trade.weakLeg.returnPct * 0.50 + trade.strongLeg.returnPct * 0.50).toFixed(4);
    trade.isFullyClosed = (trade.weakLeg.status.startsWith('CLOSED') && trade.strongLeg.status.startsWith('CLOSED'));
  }
}

export const replacementLogicHandler = ReplacementLogicHandler.getInstance();
