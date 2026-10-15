/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — ANTI-COPY GUARD (NO-COPY ENFORCER)
 * ============================================================================
 * Strictly blocks external copy signals, telegram alerts, or mirror trading
 * unless vetted by autonomous qp+of+ip+hp confluence (>= 2/4) and positive EV.
 * Prevents copy loss.
 * ============================================================================
 */

import { SelfLearningNoCopyEngine } from '../cognition/self_learning_no_copy';

export interface CopyGuardVerdict {
  isCopyAttempt: boolean;
  isBlocked: boolean;
  confluenceScore: number; // 0 to 4
  expectedValuePct: number;
  reason: string;
  timestamp: number;
}

export class NoCopyGuard {
  private static instance: NoCopyGuard;

  private constructor() {}

  public static getInstance(): NoCopyGuard {
    if (!NoCopyGuard.instance) {
      NoCopyGuard.instance = new NoCopyGuard();
    }
    return NoCopyGuard.instance;
  }

  /**
   * Evaluates external trade attempt. If copy signal fails self confluence or has EV <= 0, BLOCKS it.
   */
  public evaluateSignal(
    source: 'EXTERNAL_SIGNAL' | 'TELEGRAM_COPY' | 'AUTONOMOUS_SELF_MODEL',
    confluenceCount: number, // 0 - 4
    expectedValuePct: number
  ): CopyGuardVerdict {
    if (source === 'AUTONOMOUS_SELF_MODEL') {
      return {
        isCopyAttempt: false,
        isBlocked: false,
        confluenceScore: confluenceCount,
        expectedValuePct,
        reason: 'Autonomous self-learned model verified. Copy guard pass.',
        timestamp: Date.now()
      };
    }

    // External copy signal detected
    if (confluenceCount < 2 || expectedValuePct <= 0) {
      SelfLearningNoCopyEngine.getInstance().registerRejectedCopySignal();
      return {
        isCopyAttempt: true,
        isBlocked: true,
        confluenceScore: confluenceCount,
        expectedValuePct,
        reason: `ANTI-COPY GUARD TRIGGERED: External signal rejected. Self-confluence is ${confluenceCount}/4 (< 2/4) or EV is ${expectedValuePct}% (<= 0). Copying external signals prohibited.`,
        timestamp: Date.now()
      };
    }

    return {
      isCopyAttempt: true,
      isBlocked: false,
      confluenceScore: confluenceCount,
      expectedValuePct,
      reason: 'External signal matches high self-learned confluence (>= 2/4) and positive EV.',
      timestamp: Date.now()
    };
  }
}
