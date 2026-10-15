export interface EmotionalMetrics {
  marketFearGreedIndex: number;  // 0 to 100
  internalFear: number;          // 0 to 100 derived from current drawdown
  internalGreed: number;         // 0 to 100 derived from win streaks
  confidenceLevel: number;       // 0 to 100
  patienceLevel: number;         // 0 to 100
  lotAdjustmentMultiplier: number; // e.g. 0.5 to 1.0
}

export function computeEmotionalDynamics(drawdownPct: number, consecutiveWins: number): EmotionalMetrics {
  // If DD > 3%, fear rises; if consecutive wins > 3, greed tends to rise but is clamped
  const internalFear = Math.min(100, Math.max(10, drawdownPct * 15));
  const rawGreed = Math.min(100, consecutiveWins * 12);
  const internalGreed = internalFear > 40 ? Math.max(10, rawGreed - 30) : rawGreed;
  
  // High fear dampens lot size by 50% for capital safety
  let lotAdjustmentMultiplier = 1.0;
  if (internalFear > 50) {
    lotAdjustmentMultiplier = 0.5;
  } else if (internalGreed > 70) {
    // Greed is checked by enforcing fractional Kelly
    lotAdjustmentMultiplier = 0.75;
  }

  return {
    marketFearGreedIndex: 52, // Neutral-greed
    internalFear,
    internalGreed,
    confidenceLevel: 82,
    patienceLevel: 94,
    lotAdjustmentMultiplier,
  };
}
