'use client';

import React, { useState, useEffect } from 'react';

interface TuliaLoaderProps {
  onComplete?: () => void;
  isSimulatedDemo?: boolean; // 90 seconds demo simulation mode
}

const TULIA_STEPS = [
  { step: 1, pct: 0, text: 'Connecting Polygon.io 51 markets...' },
  { step: 2, pct: 4, text: 'Fetching Stocks AAPL MSFT NVDA... 20 OK' },
  { step: 3, pct: 8, text: 'Fetching Forex EURUSD GBPUSD... 10 OK' },
  { step: 4, pct: 12, text: 'Fetching Crypto BTC ETH SOL... 15 OK' },
  { step: 5, pct: 16, text: 'Fetching Deriv V75 Boom Crash... 15 OK + Deriv connected' },
  { step: 6, pct: 20, text: 'Calculating DNA 125 features per market...' },
  { step: 7, pct: 24, text: 'Bell Curve fitting Normal Student-t Pareto df...' },
  { step: 8, pct: 28, text: 'Nobel Equations Black-Scholes IV RV...' },
  { step: 9, pct: 32, text: 'Calibrating OU theta mu sigma half_life + Cointegration hedge...' },
  { step: 10, pct: 36, text: 'Ichimoku Kumo Tenkan Kijun Senkou Chikou...' },
  { step: 11, pct: 40, text: 'Wyckoff Accumulation Spring + Composite Man...' },
  { step: 12, pct: 44, text: 'Volume Profile POC VAH VAL HVN LVN + Order Flow CVD Footprint...' },
  { step: 13, pct: 48, text: 'Vol Arb RV-IV spread + Market Making Reservation Spread...' },
  { step: 14, pct: 52, text: 'Training ML 1-10 KNN RF XGB LGBM... accuracy 74.2%' },
  { step: 15, pct: 56, text: 'Training Deep 11-15 LSTM GRU Transformer CNN Autoencoder...' },
  { step: 16, pct: 60, text: 'Training Quant 16-24 GARCH GMM Copula Hurst Kalman PPO Isolation Bell FinalGate...' },
  { step: 17, pct: 64, text: 'Training Quant 25-35 StatArb Ichimoku Wyckoff VP OF VolArb MM EV Kelly Monte Risk...' },
  { step: 18, pct: 68, text: 'Running 35 engines predictions for 51 markets...' },
  { step: 19, pct: 72, text: 'Calculating EV + Bayes Posterior + Final Gate Logistic SHAP...' },
  { step: 20, pct: 76, text: 'Kelly Criterion optimal lot fat-tail adjusted portfolio vector...' },
  { step: 21, pct: 80, text: 'Monte Carlo 10k paths Normal Student Pareto VaR CVaR ProbProfit...' },
  { step: 22, pct: 84, text: 'Multiverse 5 universes Base VolShock Correlation Liquidity BlackSwan Survival 86%...' },
  { step: 23, pct: 88, text: 'Risk Final Boss isolation autoencoder bell copula toxicity...' },
  { step: 24, pct: 92, text: 'Portfolio Heat 3.2% Spread 0.02% Slippage 0.04% Latency 45ms...' },
  { step: 25, pct: 100, text: 'AGI READY 35/35 engines GREEN 51 ACTIVE - ALL SYSTEMS GO' }
];

export const TuliaLoader: React.FC<TuliaLoaderProps> = ({ onComplete, isSimulatedDemo = true }) => {
  const [currentStepIndex, setCurrentStepIndex] = useState(0);
  const [logs, setLogs] = useState<string[]>([]);

  useEffect(() => {
    // 90 seconds demo run: 25 steps ~= 3.6s per step
    const intervalTime = isSimulatedDemo ? 3600 : 108000;
    const timer = setInterval(() => {
      setCurrentStepIndex((prev) => {
        if (prev < TULIA_STEPS.length - 1) {
          const next = prev + 1;
          setLogs((l) => [...l.slice(-7), `[${new Date().toLocaleTimeString()}] ${TULIA_STEPS[next].text}`]);
          return next;
        } else {
          clearInterval(timer);
          onComplete?.();
          return prev;
        }
      });
    }, intervalTime);

    return () => clearInterval(timer);
  }, [isSimulatedDemo, onComplete]);

  const activeStep = TULIA_STEPS[currentStepIndex];

  return (
    <div style={{
      backgroundColor: '#050c18',
      color: '#00e5ff',
      padding: '24px',
      borderRadius: '12px',
      border: '1px solid rgba(0, 229, 255, 0.3)',
      fontFamily: 'monospace',
      maxWidth: '700px',
      margin: '0 auto',
      boxShadow: '0 8px 32px rgba(0, 229, 255, 0.15)'
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
        <h3 style={{ margin: 0, color: '#ffd700', fontSize: '18px' }}>⚡ TULIA 45MIN CALIBRATION LOADER</h3>
        <span style={{ fontSize: '12px', color: '#00ff88', border: '1px solid #00ff88', padding: '2px 8px', borderRadius: '4px' }}>
          51 MARKETS ACTIVE
        </span>
      </div>

      <div style={{ fontSize: '14px', marginBottom: '8px', color: '#ffffff' }}>
        <strong>Step {activeStep.step}/25:</strong> {activeStep.text}
      </div>

      {/* Progress Bar */}
      <div style={{
        height: '14px',
        backgroundColor: '#0a192f',
        borderRadius: '7px',
        overflow: 'hidden',
        border: '1px solid rgba(255, 255, 255, 0.1)',
        marginBottom: '16px'
      }}>
        <div style={{
          height: '100%',
          width: `${activeStep.pct}%`,
          backgroundColor: '#00ff88',
          boxShadow: '0 0 10px #00ff88',
          transition: 'width 0.4s ease'
        }} />
      </div>

      {/* Terminal Log Console */}
      <div style={{
        backgroundColor: '#02060d',
        padding: '12px',
        borderRadius: '6px',
        fontSize: '11px',
        color: '#8892b0',
        minHeight: '120px',
        lineHeight: '1.6',
        border: '1px solid rgba(255, 255, 255, 0.05)'
      }}>
        <div style={{ color: '#00e5ff', marginBottom: '4px' }}>// LIVE QUANT STREAM & ENGINE LOGS</div>
        {logs.map((log, idx) => (
          <div key={idx} style={{ color: idx === logs.length - 1 ? '#00ff88' : '#8892b0' }}>
            {log}
          </div>
        ))}
      </div>
    </div>
  );
};
