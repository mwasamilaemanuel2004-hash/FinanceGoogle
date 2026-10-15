'use client';

import React, { useState } from 'react';
import { IchimokuCompleteEngine } from '../../lib/engines/ichimoku_complete';
import { adaptiveDetection } from '../../lib/core/adaptive_detection';

export default function MathLabPage() {
  const [selectedMarket, setSelectedMarket] = useState<string>('EURUSD');
  const [priceInput, setPriceInput] = useState<number>(1.0850);

  const ichiEval = IchimokuCompleteEngine.evaluate(selectedMarket, priceInput, 0.0015);
  const addEval = adaptiveDetection.evaluate(selectedMarket, priceInput, 0.0015, 0.0015, [priceInput * 0.999, priceInput * 1.001, priceInput]);

  const testMarkets = ['EURUSD', 'GBPJPY', 'XAUUSD', 'V75', 'BOOM1000', 'BTC', 'AAPL', 'US500'];

  return (
    <div style={{ minHeight: '100vh', background: '#0b0f19', color: '#f8fafc', padding: '32px', fontFamily: 'system-ui, sans-serif' }}>
      <header style={{ marginBottom: '24px' }}>
        <h1 style={{ margin: 0, fontSize: '26px', fontWeight: 800, color: '#38bdf8' }}>
          ESMH.TRADE — QUANT MATH LAB (ICHIMOKU UNIVERSAL & HMM REGIMES)
        </h1>
        <p style={{ margin: '6px 0 0 0', color: '#94a3b8', fontSize: '14px' }}>
          Real-Time Mathematical Curve Decomposition & Adaptive Parameter Calibration across Any Asset Class
        </p>

        {/* Market Quick Selector */}
        <div style={{ display: 'flex', gap: '8px', marginTop: '16px', flexWrap: 'wrap' }}>
          {testMarkets.map((m) => (
            <button
              key={m}
              onClick={() => {
                setSelectedMarket(m);
                if (m === 'V75') setPriceInput(425000);
                else if (m === 'BOOM1000') setPriceInput(8500);
                else if (m === 'BTC') setPriceInput(68000);
                else if (m === 'XAUUSD') setPriceInput(2650);
                else if (m === 'AAPL') setPriceInput(225);
                else setPriceInput(1.085);
              }}
              style={{
                background: selectedMarket === m ? '#38bdf8' : 'rgba(30, 41, 59, 0.6)',
                color: selectedMarket === m ? '#0b0f19' : '#cbd5e1',
                border: 'none',
                padding: '6px 14px',
                borderRadius: '6px',
                fontSize: '12px',
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              {m}
            </button>
          ))}
        </div>
      </header>

      {/* Grid Overview */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
        {/* Ichimoku Universal Curve Metrics */}
        <div style={{ background: 'rgba(15, 23, 42, 0.8)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(56, 189, 248, 0.25)' }}>
          <h2 style={{ margin: '0 0 12px 0', fontSize: '16px', color: '#38bdf8', fontWeight: 700 }}>
            Universal Ichimoku Kinko Hyo Calibration
          </h2>
          <div style={{ fontSize: '13px', color: '#cbd5e1', lineHeight: '1.8' }}>
            <div><strong>Tenkan Period:</strong> {ichiEval.params.tenkanPeriod} | Value: {ichiEval.cqf.tenkanValue}</div>
            <div><strong>Kijun Period:</strong> {ichiEval.params.kijunPeriod} | Value: {ichiEval.cqf.kijunValue}</div>
            <div><strong>Senkou Span A:</strong> {ichiEval.cqf.senkouSpanA}</div>
            <div><strong>Senkou Span B:</strong> {ichiEval.cqf.senkouSpanB}</div>
            <div><strong>Chikou Span:</strong> {ichiEval.cqf.chikouSpan}</div>
            <div><strong>Cloud Thickness:</strong> {ichiEval.cqf.cloudThicknessPips} pips (x{ichiEval.params.cloudThicknessMultiplier})</div>
            <div><strong>Signal:</strong> <span style={{ color: '#10b981', fontWeight: 700 }}>{ichiEval.signal}</span></div>
          </div>
        </div>

        {/* Adaptive Detection HMM */}
        <div style={{ background: 'rgba(15, 23, 42, 0.8)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(168, 85, 247, 0.25)' }}>
          <h2 style={{ margin: '0 0 12px 0', fontSize: '16px', color: '#c084fc', fontWeight: 700 }}>
            HMM 3-State Regime & Jump Detection
          </h2>
          <div style={{ fontSize: '13px', color: '#cbd5e1', lineHeight: '1.8' }}>
            <div><strong>Active Regime:</strong> <span style={{ color: '#c084fc', fontWeight: 700 }}>{addEval.regimeState.regime}</span></div>
            <div><strong>Hurst Exponent:</strong> {addEval.regimeState.hurstExponent} ({addEval.regimeState.hurstExponent > 0.55 ? 'Persistent Trend' : 'Mean Reverting'})</div>
            <div><strong>Realized / Hist Vol Ratio:</strong> {addEval.regimeState.realizedVolToHistRatio}x</div>
            <div><strong>Dynamic Stop-Loss Multiplier:</strong> {addEval.adaptedParams.adjustedStopLossMultiplier}x ATR</div>
            <div><strong>Effective Z-Score Threshold:</strong> {addEval.adaptedParams.effectiveZThreshold}</div>
            <div><strong>Risk Action:</strong> <span style={{ color: '#10b981', fontWeight: 700 }}>{addEval.riskAction}</span></div>
          </div>
        </div>
      </div>
    </div>
  );
}
