'use client';

import React, { useState } from 'react';
import { continuousLearning } from '../../lib/learning/continuous_learning';

export default function LearningDashboardPage() {
  const learningStates = continuousLearning.getAllLearningStates();
  const beforeAfterMatrix = continuousLearning.getBeforeAfterMatrix();
  const [selectedGroup, setSelectedGroup] = useState<string>('ALL');

  const groups = ['ALL', 'FOREX', 'VOLATILITY', 'BOOM_CRASH', 'STOCK', 'CRYPTO', 'COMMODITIES', 'INDICES'];

  const filteredStates = selectedGroup === 'ALL'
    ? learningStates
    : learningStates.filter((s) => s.asset_group === selectedGroup);

  return (
    <div style={{ minHeight: '100vh', background: '#0b0f19', color: '#f8fafc', padding: '32px', fontFamily: 'system-ui, sans-serif' }}>
      <header style={{ marginBottom: '28px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1 style={{ margin: 0, fontSize: '26px', fontWeight: 800, color: '#38bdf8' }}>
              ESMH.TRADE — CONTINUOUS LEARNING & ADAPTIVE DETECTION DASHBOARD
            </h1>
            <p style={{ margin: '6px 0 0 0', color: '#94a3b8', fontSize: '14px' }}>
              Real-Time HMM Regime Tracking, Dynamic Bayesian Retraining & CQF/FRM/CFA Metrics across 80+ Markets
            </p>
          </div>
          <div style={{ background: 'rgba(168, 85, 247, 0.15)', color: '#c084fc', border: '1px solid rgba(168, 85, 247, 0.3)', padding: '6px 16px', borderRadius: '20px', fontSize: '13px', fontWeight: 600 }}>
            HMM REGIME: ONLINE AUTO-TUNED
          </div>
        </div>

        {/* Group Selector */}
        <div style={{ display: 'flex', gap: '8px', marginTop: '20px', flexWrap: 'wrap' }}>
          {groups.map((grp) => (
            <button
              key={grp}
              onClick={() => setSelectedGroup(grp)}
              style={{
                background: selectedGroup === grp ? '#38bdf8' : 'rgba(30, 41, 59, 0.6)',
                color: selectedGroup === grp ? '#0b0f19' : '#cbd5e1',
                border: 'none',
                padding: '8px 14px',
                borderRadius: '8px',
                fontSize: '12px',
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              {grp}
            </button>
          ))}
        </div>
      </header>

      {/* Main Metrics Table */}
      <section style={{ background: 'rgba(15, 23, 42, 0.7)', borderRadius: '14px', padding: '20px', border: '1px solid rgba(56, 189, 248, 0.2)', marginBottom: '32px' }}>
        <h2 style={{ margin: '0 0 16px 0', fontSize: '18px', fontWeight: 700, color: '#f1f5f9' }}>
          Verified CQF / FRM / CFA Continuous Learning Registry ({filteredStates.length} Markets)
        </h2>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '12px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid rgba(56, 189, 248, 0.2)', color: '#94a3b8' }}>
                <th style={{ padding: '10px' }}>Market / Pair</th>
                <th style={{ padding: '10px' }}>Engine Used</th>
                <th style={{ padding: '10px' }}>Regime (HMM)</th>
                <th style={{ padding: '10px' }}>Sharpe</th>
                <th style={{ padding: '10px' }}>Sortino</th>
                <th style={{ padding: '10px' }}>Calmar</th>
                <th style={{ padding: '10px' }}>Win Rate</th>
                <th style={{ padding: '10px' }}>PF</th>
                <th style={{ padding: '10px' }}>Max DD</th>
                <th style={{ padding: '10px' }}>VaR (95%)</th>
                <th style={{ padding: '10px' }}>Ichimoku Params</th>
                <th style={{ padding: '10px' }}>Adaptive Factor</th>
              </tr>
            </thead>
            <tbody>
              {filteredStates.map((row, idx) => (
                <tr key={idx} style={{ borderBottom: '1px solid rgba(56, 189, 248, 0.08)' }}>
                  <td style={{ padding: '10px', fontWeight: 700, color: '#38bdf8' }}>{row.market_name}</td>
                  <td style={{ padding: '10px', color: '#cbd5e1' }}>{row.engine_used}</td>
                  <td style={{ padding: '10px' }}>
                    <span style={{ background: 'rgba(192, 132, 252, 0.15)', color: '#c084fc', padding: '2px 8px', borderRadius: '4px', fontSize: '11px', fontWeight: 600 }}>
                      {row.regime}
                    </span>
                  </td>
                  <td style={{ padding: '10px', fontWeight: 700, color: '#10b981' }}>{row.sharpe_last_100.toFixed(2)}</td>
                  <td style={{ padding: '10px', color: '#f1f5f9' }}>{row.sortino.toFixed(2)}</td>
                  <td style={{ padding: '10px', color: '#f1f5f9' }}>{row.calmar.toFixed(2)}</td>
                  <td style={{ padding: '10px', fontWeight: 600, color: '#10b981' }}>{row.win_rate_last_100}%</td>
                  <td style={{ padding: '10px', color: '#f1f5f9' }}>{row.pf.toFixed(2)}</td>
                  <td style={{ padding: '10px', color: '#f43f5e' }}>{row.maxDD}%</td>
                  <td style={{ padding: '10px', color: '#f43f5e' }}>{row.var95}%</td>
                  <td style={{ padding: '10px', color: '#38bdf8', fontFamily: 'monospace' }}>
                    ({row.ichimoku_params.tenkanPeriod}, {row.ichimoku_params.kijunPeriod}, {row.ichimoku_params.senkouBPeriod}, {row.ichimoku_params.displacement})
                  </td>
                  <td style={{ padding: '10px', color: '#a855f7', fontWeight: 600 }}>{row.adaptive_factor.toFixed(2)}x</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Before vs After Benchmarks */}
      <section style={{ background: 'rgba(15, 23, 42, 0.7)', borderRadius: '14px', padding: '20px', border: '1px solid rgba(16, 185, 129, 0.25)' }}>
        <h2 style={{ margin: '0 0 16px 0', fontSize: '18px', fontWeight: 700, color: '#10b981' }}>
          Empirical Quantitative Audit: Before (Heuristics) vs. After (ADD Quant Engines)
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '14px' }}>
          {beforeAfterMatrix.map((item, idx) => (
            <div key={idx} style={{ background: 'rgba(30, 41, 59, 0.5)', padding: '16px', borderRadius: '10px', border: '1px solid rgba(56, 189, 248, 0.15)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '14px', fontWeight: 700, color: '#38bdf8' }}>{item.market}</span>
                <span style={{ fontSize: '11px', color: '#10b981', background: 'rgba(16, 185, 129, 0.1)', padding: '2px 8px', borderRadius: '4px' }}>
                  {item.adaptiveDetectionStatus}
                </span>
              </div>
              <div style={{ fontSize: '12px', color: '#94a3b8', margin: '4px 0 10px 0' }}>{item.metric}</div>
              <div style={{ fontSize: '12px', color: '#ef4444', marginBottom: '4px' }}>
                <strong>Before:</strong> {item.beforeValue}
              </div>
              <div style={{ fontSize: '12px', color: '#10b981', marginBottom: '8px' }}>
                <strong>After (ADD):</strong> {item.afterValue}
              </div>
              <div style={{ fontSize: '12px', color: '#c084fc', fontWeight: 600, borderTop: '1px solid rgba(255, 255, 255, 0.08)', paddingTop: '6px' }}>
                Delta: {item.deltaImprovement}
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
