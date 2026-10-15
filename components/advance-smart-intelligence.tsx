import React from 'react';

export interface AdvanceSmartIntelligenceProps {
  symbol: string;
  decision: 'BUY' | 'SELL' | 'HOLD';
  confidencePct: number;
  latencyMs: number;
  expectedReturnPct: number;
  expectedRiskPct: number;
  circuitBreakerActive: boolean;
  marketRegime: string;
  gbmScore: number;
  lstmScore: number;
  transformerScore: number;
  metaProb: number;
  hmmTransition: {
    calmPct: number;
    trendingPct: number;
    volatilePct: number;
    confidencePct: number;
  };
  macroSentimentScore: number;
  forecasts: Array<{
    timeframe: string;
    direction: 'BULLISH' | 'BEARISH' | 'NEUTRAL';
    confidence: number;
    support: number;
    resistance: number;
  }>;
  realToolsMetrics: {
    rsi: number;
    macdLine: number;
    orderbookOfi: number;
    microPrice: number;
    var95Pct: number;
    kellyFractionPct: number;
  };
}

export const AdvanceSmartIntelligenceCard: React.FC<AdvanceSmartIntelligenceProps> = ({
  symbol,
  decision,
  confidencePct,
  latencyMs,
  expectedReturnPct,
  expectedRiskPct,
  circuitBreakerActive,
  marketRegime,
  gbmScore,
  lstmScore,
  transformerScore,
  metaProb,
  hmmTransition,
  macroSentimentScore,
  forecasts,
  realToolsMetrics
}) => {
  const isBuy = decision === 'BUY';
  const isSell = decision === 'SELL';
  const decisionColor = isBuy ? '#22c55e' : isSell ? '#ef4444' : '#38bdf8';

  return (
    <div style={{
      background: 'rgba(15, 23, 42, 0.90)',
      border: `1px solid ${decisionColor}40`,
      borderRadius: '14px',
      padding: '18px',
      color: '#f8fafc',
      fontFamily: 'system-ui, -apple-system, sans-serif',
      boxShadow: '0 8px 30px rgba(0, 0, 0, 0.45)',
      display: 'flex',
      flexDirection: 'column',
      gap: '14px'
    }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontSize: '18px', fontWeight: 800 }}>{symbol}</span>
            <span style={{
              background: `${decisionColor}20`,
              color: decisionColor,
              border: `1px solid ${decisionColor}50`,
              padding: '2px 8px',
              borderRadius: '6px',
              fontWeight: 800,
              fontSize: '11px'
            }}>
              {decision}
            </span>
          </div>
          <div style={{ fontSize: '11px', color: '#94a3b8' }}>
            Advance Smart Engine • 200 Features Stacking Ensemble
          </div>
        </div>

        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '16px', fontWeight: 800, color: '#f59e0b' }}>
            {confidencePct}% CONF
          </div>
          <div style={{ fontSize: '11px', color: '#22c55e', fontWeight: 700 }}>
            Latency: {latencyMs}ms p95
          </div>
        </div>
      </div>

      {/* Primary KPI Row */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(4, 1fr)',
        gap: '8px',
        background: 'rgba(30, 41, 59, 0.6)',
        padding: '10px',
        borderRadius: '8px'
      }}>
        <div>
          <div style={{ fontSize: '10px', color: '#94a3b8' }}>RETURN EXP.</div>
          <div style={{ fontSize: '13px', fontWeight: 700, color: '#22c55e' }}>+{expectedReturnPct}%</div>
        </div>
        <div>
          <div style={{ fontSize: '10px', color: '#94a3b8' }}>RISK EXP.</div>
          <div style={{ fontSize: '13px', fontWeight: 700, color: '#ef4444' }}>-{expectedRiskPct}%</div>
        </div>
        <div>
          <div style={{ fontSize: '10px', color: '#94a3b8' }}>REGIME</div>
          <div style={{ fontSize: '12px', fontWeight: 700, color: '#38bdf8' }}>{marketRegime}</div>
        </div>
        <div>
          <div style={{ fontSize: '10px', color: '#94a3b8' }}>CIRCUIT BREAKER</div>
          <div style={{ fontSize: '12px', fontWeight: 700, color: circuitBreakerActive ? '#ef4444' : '#22c55e' }}>
            {circuitBreakerActive ? 'BLOCKED (<55%)' : 'ARMED (OK)'}
          </div>
        </div>
      </div>

      {/* Stacking Ensemble Breakdown */}
      <div style={{
        background: 'rgba(30, 41, 59, 0.4)',
        border: '1px solid rgba(148, 163, 184, 0.15)',
        padding: '10px',
        borderRadius: '8px'
      }}>
        <div style={{ fontSize: '11px', fontWeight: 700, color: '#38bdf8', marginBottom: '6px' }}>
          STACKING ENSEMBLE SUB-MODELS (WEIGHTED 40/35/25)
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '6px', fontSize: '11px' }}>
          <div>LightGBM: <strong>{gbmScore.toFixed(3)}</strong></div>
          <div>LSTM (128): <strong>{lstmScore.toFixed(3)}</strong></div>
          <div>Transformer: <strong>{transformerScore.toFixed(3)}</strong></div>
          <div>Meta Prob: <strong style={{ color: '#f59e0b' }}>{metaProb.toFixed(3)}</strong></div>
        </div>
      </div>

      {/* Advance Intelligence HMM & Forecasts */}
      <div style={{
        background: 'rgba(30, 41, 59, 0.4)',
        border: '1px solid rgba(148, 163, 184, 0.15)',
        padding: '10px',
        borderRadius: '8px'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
          <span style={{ fontSize: '11px', fontWeight: 700, color: '#c084fc' }}>
            HMM REGIME TRANSITION & FORECAST PIPELINE
          </span>
          <span style={{ fontSize: '10px', color: '#f59e0b', fontWeight: 700 }}>
            Macro Sentiment: {macroSentimentScore.toFixed(2)}
          </span>
        </div>
        <div style={{ fontSize: '11px', color: '#cbd5e1', marginBottom: '8px' }}>
          Calm: {hmmTransition.calmPct}% • Trending: {hmmTransition.trendingPct}% • Volatile: {hmmTransition.volatilePct}% (Conf: {hmmTransition.confidencePct}%)
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          {forecasts.map((fc, i) => (
            <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '10px', color: '#94a3b8' }}>
              <span style={{ fontWeight: 700, color: '#38bdf8' }}>{fc.timeframe}</span>
              <span style={{ fontWeight: 700, color: fc.direction === 'BULLISH' ? '#22c55e' : '#ef4444' }}>
                {fc.direction} ({fc.confidence}%)
              </span>
              <span>S: {fc.support} / R: {fc.resistance}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Real Tools Benchmark */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 1fr)',
        gap: '6px',
        fontSize: '10px',
        color: '#94a3b8'
      }}>
        <div>TA-Lib RSI: <strong style={{ color: '#f8fafc' }}>{realToolsMetrics.rsi.toFixed(1)}</strong></div>
        <div>Orderbook OFI: <strong style={{ color: '#38bdf8' }}>{realToolsMetrics.orderbookOfi.toFixed(3)}</strong></div>
        <div>Kelly Sizing: <strong style={{ color: '#22c55e' }}>{realToolsMetrics.kellyFractionPct.toFixed(1)}%</strong></div>
      </div>
    </div>
  );
};
