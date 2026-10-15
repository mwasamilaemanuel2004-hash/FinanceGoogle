import React from 'react';

export interface EngineCardProps {
  engineName: string;
  category: string;
  pricingModel: string;
  signalFormula: string;
  var95: number;
  maxDrawdown: number;
  sharpe: number;
  sortino: number;
  calmar: number;
  profitFactor: number;
  winRate: number;
  annualizedReturn: number;
  adaptiveFactor: number;
  regimeDetected: string;
  dynamicWeightPct: number;
  detectionFlags: string[];
}

export const EngineCard: React.FC<EngineCardProps> = ({
  engineName,
  category,
  pricingModel,
  signalFormula,
  var95,
  maxDrawdown,
  sharpe,
  sortino,
  calmar,
  profitFactor,
  winRate,
  annualizedReturn,
  adaptiveFactor,
  regimeDetected,
  dynamicWeightPct,
  detectionFlags,
}) => {
  return (
    <div style={{
      background: 'rgba(15, 23, 42, 0.85)',
      border: '1px solid rgba(56, 189, 248, 0.25)',
      borderRadius: '12px',
      padding: '16px',
      color: '#f8fafc',
      fontFamily: 'system-ui, -apple-system, sans-serif',
      boxShadow: '0 4px 16px rgba(0, 0, 0, 0.4)',
    }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
        <div>
          <span style={{ fontSize: '11px', color: '#38bdf8', textTransform: 'uppercase', letterSpacing: '0.08em', fontWeight: 700 }}>
            {category}
          </span>
          <h3 style={{ margin: '4px 0 0 0', fontSize: '16px', fontWeight: 600 }}>{engineName}</h3>
        </div>
        <div style={{
          background: 'rgba(56, 189, 248, 0.15)',
          color: '#38bdf8',
          border: '1px solid rgba(56, 189, 248, 0.3)',
          padding: '4px 10px',
          borderRadius: '20px',
          fontSize: '12px',
          fontWeight: 600,
        }}>
          Weight: {dynamicWeightPct}%
        </div>
      </div>

      {/* Grid: CQF, FRM, CFA, ADD */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '10px' }}>
        {/* CQF */}
        <div style={{ background: 'rgba(30, 41, 59, 0.6)', padding: '10px', borderRadius: '8px', borderLeft: '3px solid #38bdf8' }}>
          <div style={{ fontSize: '11px', color: '#94a3b8', fontWeight: 600 }}>A) CQF QUANT CORE</div>
          <div style={{ fontSize: '12px', color: '#f1f5f9', marginTop: '4px' }}><strong>Model:</strong> {pricingModel}</div>
          <div style={{ fontSize: '11px', color: '#cbd5e1', marginTop: '2px', fontFamily: 'monospace' }}><strong>Formula:</strong> {signalFormula}</div>
        </div>

        {/* FRM */}
        <div style={{ background: 'rgba(30, 41, 59, 0.6)', padding: '10px', borderRadius: '8px', borderLeft: '3px solid #f43f5e' }}>
          <div style={{ fontSize: '11px', color: '#94a3b8', fontWeight: 600 }}>B) FRM RISK CONTROL</div>
          <div style={{ fontSize: '12px', color: '#f1f5f9', marginTop: '4px' }}><strong>VaR (95%):</strong> {var95}%</div>
          <div style={{ fontSize: '12px', color: '#f1f5f9', marginTop: '2px' }}><strong>Max DD Cap:</strong> {maxDrawdown}%</div>
        </div>

        {/* CFA */}
        <div style={{ background: 'rgba(30, 41, 59, 0.6)', padding: '10px', borderRadius: '8px', borderLeft: '3px solid #10b981' }}>
          <div style={{ fontSize: '11px', color: '#94a3b8', fontWeight: 600 }}>C) CFA PERFORMANCE</div>
          <div style={{ fontSize: '12px', color: '#f1f5f9', marginTop: '4px' }}>
            <strong>Sharpe:</strong> {sharpe.toFixed(2)} | <strong>Sortino:</strong> {sortino.toFixed(2)} | <strong>Calmar:</strong> {calmar.toFixed(2)}
          </div>
          <div style={{ fontSize: '12px', color: '#10b981', marginTop: '2px', fontWeight: 600 }}>
            Win: {winRate}% | PF: {profitFactor.toFixed(2)} | Ret: +{annualizedReturn}%
          </div>
        </div>

        {/* ADD */}
        <div style={{ background: 'rgba(30, 41, 59, 0.6)', padding: '10px', borderRadius: '8px', borderLeft: '3px solid #a855f7' }}>
          <div style={{ fontSize: '11px', color: '#94a3b8', fontWeight: 600 }}>D) ADD ADAPTIVE DYNAMIC</div>
          <div style={{ fontSize: '12px', color: '#f1f5f9', marginTop: '4px' }}>
            <strong>Regime:</strong> <span style={{ color: '#c084fc', fontWeight: 600 }}>{regimeDetected}</span>
          </div>
          <div style={{ fontSize: '12px', color: '#f1f5f9', marginTop: '2px' }}>
            <strong>Adaptive Vol Factor:</strong> {adaptiveFactor.toFixed(2)}x
          </div>
        </div>
      </div>

      {/* Detection Flags */}
      <div style={{ marginTop: '12px', display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
        {detectionFlags.map((flag, idx) => (
          <span key={idx} style={{
            fontSize: '10px',
            background: 'rgba(168, 85, 247, 0.15)',
            color: '#d8b4fe',
            border: '1px solid rgba(168, 85, 247, 0.3)',
            borderRadius: '4px',
            padding: '2px 6px',
            fontWeight: 500,
          }}>
            {flag}
          </span>
        ))}
      </div>
    </div>
  );
};
