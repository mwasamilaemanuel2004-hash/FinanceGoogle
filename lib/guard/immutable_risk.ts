/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — IMMUTABLE RISK RULES (LAYER 1 OF SAFE LEARNING)
 * ============================================================================
 * Hardcoded risk guardrails that CANNOT be overwritten, bypassed, or learned
 * around by any autonomous agent, machine learning model, or skill inventor.
 */

export interface ImmutableRiskConfig {
  MAX_SIZE_PERCENT: number;          // Hard limit 2% per trade
  HARD_SL_ATR_MULTIPLE: number;      // Must be <= 1.8 * ATR
  MAX_DD_PERCENT: number;            // 5% circuit breaker
  MAX_TRADES_PER_HOUR: number;       // 20 trades max
  NFP_CPI_BLACKOUT_MINUTES: number;  // 2 min pre/post blackout
  MAX_VPIN_TOXICITY: number;         // > 0.70 toxic flow blocked
}

export const IMMUTABLE_RISK_CONSTANTS: ImmutableRiskConfig = {
  MAX_SIZE_PERCENT: 2.0,
  HARD_SL_ATR_MULTIPLE: 1.8,
  MAX_DD_PERCENT: 5.0,
  MAX_TRADES_PER_HOUR: 20,
  NFP_CPI_BLACKOUT_MINUTES: 2,
  MAX_VPIN_TOXICITY: 0.70
};

export class ImmutableRiskGuard {
  /**
   * Validates proposed trade or parameter updates against Immutable Risk
   */
  public static validateProposedParameters(params: {
    proposedSizePct: number;
    hasStopLoss: boolean;
    slAtrMultiple?: number;
    currentHourTradeCount: number;
    isHighImpactNewsWindow: boolean;
    vpinToxicity: number;
    currentDrawdownPct: number;
  }): { allowed: boolean; violationReason?: string } {
    if (params.proposedSizePct > IMMUTABLE_RISK_CONSTANTS.MAX_SIZE_PERCENT) {
      return {
        allowed: false,
        violationReason: `IMMUTABLE_RISK_VIOLATION: Proposed size ${params.proposedSizePct}% exceeds hardcoded limit ${IMMUTABLE_RISK_CONSTANTS.MAX_SIZE_PERCENT}%. Learning attempt blocked.`
      };
    }

    if (!params.hasStopLoss) {
      return {
        allowed: false,
        violationReason: `IMMUTABLE_RISK_VIOLATION: Trade lacks required Stop Loss. Hard stop loss cannot be learned away.`
      };
    }

    if (params.slAtrMultiple && params.slAtrMultiple > IMMUTABLE_RISK_CONSTANTS.HARD_SL_ATR_MULTIPLE) {
      return {
        allowed: false,
        violationReason: `IMMUTABLE_RISK_VIOLATION: Stop Loss distance ${params.slAtrMultiple} ATR exceeds max allowable ${IMMUTABLE_RISK_CONSTANTS.HARD_SL_ATR_MULTIPLE} ATR.`
      };
    }

    if (params.currentHourTradeCount >= IMMUTABLE_RISK_CONSTANTS.MAX_TRADES_PER_HOUR) {
      return {
        allowed: false,
        violationReason: `IMMUTABLE_RISK_VIOLATION: Hourly trade count ${params.currentHourTradeCount} reached maximum threshold ${IMMUTABLE_RISK_CONSTANTS.MAX_TRADES_PER_HOUR}. Overtrading blocked.`
      };
    }

    if (params.isHighImpactNewsWindow) {
      return {
        allowed: false,
        violationReason: `IMMUTABLE_RISK_VIOLATION: Inside NFP/CPI ${IMMUTABLE_RISK_CONSTANTS.NFP_CPI_BLACKOUT_MINUTES}-minute blackout window. High impact volatility chasing blocked.`
      };
    }

    if (params.vpinToxicity > IMMUTABLE_RISK_CONSTANTS.MAX_VPIN_TOXICITY) {
      return {
        allowed: false,
        violationReason: `IMMUTABLE_RISK_VIOLATION: VPIN toxicity ${params.vpinToxicity} exceeds threshold ${IMMUTABLE_RISK_CONSTANTS.MAX_VPIN_TOXICITY}. Toxic adverse selection blocked.`
      };
    }

    if (params.currentDrawdownPct >= IMMUTABLE_RISK_CONSTANTS.MAX_DD_PERCENT) {
      return {
        allowed: false,
        violationReason: `IMMUTABLE_RISK_VIOLATION: Current drawdown ${params.currentDrawdownPct}% reached circuit breaker ${IMMUTABLE_RISK_CONSTANTS.MAX_DD_PERCENT}%. Position sizing halved.`
      };
    }

    return { allowed: true };
  }
}
