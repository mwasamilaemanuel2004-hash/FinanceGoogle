'use client';

import React, { useState } from 'react';
import { sovereignAgentSystem, SovereignAgentConfig } from '../../lib/agents/agent_system';
import { agenticMethodInventor } from '../../lib/evolution/inventor';
import { chanceHandler } from '../../lib/core/chance_handler';

export default function AgentsPage() {
  const [agents] = useState<SovereignAgentConfig[]>(() => sovereignAgentSystem.getAgents());
  const [inventedMethods, setInventedMethods] = useState(() => agenticMethodInventor.getInventedMethods());
  const [isRunningCycle, setIsRunningCycle] = useState(false);
  const [latestCycleOutput, setLatestCycleOutput] = useState<any>(null);

  const handleRunAgenticCycle = async () => {
    setIsRunningCycle(true);
    // Generate realistic sample candles
    const candles = Array.from({ length: 100 }).map((_, i) => ({
      timestamp: Date.now() - (100 - i) * 60000,
      open: 2040.0 + Math.sin(i / 5) * 6.0,
      high: 2040.0 + Math.sin(i / 5) * 6.0 + 2.5,
      low: 2040.0 + Math.sin(i / 5) * 6.0 - 2.5,
      close: 2040.0 + Math.sin(i / 5) * 6.0 + 1.2,
      volume: 3400
    }));

    const result = await sovereignAgentSystem.runAgenticCycle('XAU/USD', candles);
    setLatestCycleOutput(result);
    setInventedMethods(agenticMethodInventor.getInventedMethods());
    setIsRunningCycle(false);
  };

  return (
    <div style={{ minHeight: '100vh', background: '#090d16', color: '#f8fafc', padding: '32px', fontFamily: 'system-ui, sans-serif' }}>
      <header style={{ marginBottom: '28px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <h1 style={{ margin: 0, fontSize: '26px', fontWeight: 800, color: '#10b981' }}>
              ESMH.TRADE — 10-AGENT SOVEREIGN SYSTEM & AGENTIC LOOP
            </h1>
            <p style={{ margin: '6px 0 0 0', color: '#94a3b8', fontSize: '14px' }}>
              Every Agent Has ALL 52 Skills with Specialization Weights + Full 7-Step Autonomous Loop (Perceive ➔ Think ➔ Analyze ➔ Synthesize ➔ Act ➔ Reflect ➔ Evolve)
            </p>
          </div>
          <button
            onClick={handleRunAgenticCycle}
            disabled={isRunningCycle}
            style={{
              background: isRunningCycle ? '#475569' : 'linear-gradient(135deg, #38bdf8, #0284c7)',
              color: '#ffffff',
              border: 'none',
              padding: '12px 22px',
              borderRadius: '8px',
              fontSize: '14px',
              fontWeight: 700,
              cursor: isRunningCycle ? 'not-allowed' : 'pointer'
            }}
          >
            {isRunningCycle ? '⚡ Running 7-Step Loop...' : '▶ Run Live 7-Step Agentic Cycle'}
          </button>
        </div>
      </header>

      {/* Latest Cycle Execution Results */}
      {latestCycleOutput && (
        <section style={{ marginBottom: '28px', background: 'rgba(15, 23, 42, 0.9)', borderRadius: '12px', padding: '20px', border: '1px solid rgba(16, 185, 129, 0.4)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
            <h2 style={{ margin: 0, fontSize: '18px', color: '#10b981', fontWeight: 800 }}>
              Live Consensus Verdict: {latestCycleOutput.consensusSignal} ({latestCycleOutput.consensusConfidence}%) — Latency: {latestCycleOutput.totalLatencyMs}ms
            </h2>
            <span style={{ fontSize: '12px', color: '#94a3b8' }}>Market: {latestCycleOutput.market}</span>
          </div>
          <p style={{ margin: '0 0 16px 0', fontSize: '13px', color: '#e2e8f0', background: 'rgba(0,0,0,0.3)', padding: '10px', borderRadius: '6px' }}>
            {latestCycleOutput.motherDirectives}
          </p>

          <h3 style={{ margin: '0 0 8px 0', fontSize: '14px', color: '#38bdf8' }}>7-Step Agentic Loop Execution Trail:</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '8px' }}>
            {latestCycleOutput.loopTrail.map((step: any, idx: number) => (
              <div key={idx} style={{ background: '#090d16', padding: '10px', borderRadius: '6px', border: '1px solid #1e293b', fontSize: '11px' }}>
                <span style={{ color: '#10b981', fontWeight: 700 }}>[{step.step}]</span> {step.details}
              </div>
            ))}
          </div>
        </section>
      )}

      {/* 10 Agents Grid */}
      <h2 style={{ fontSize: '18px', color: '#f1f5f9', marginBottom: '16px', fontWeight: 700 }}>
        10 Sovereign Agents (Full Skill Access Matrix)
      </h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '16px', marginBottom: '32px' }}>
        {agents.map((agent) => (
          <div
            key={agent.id}
            style={{
              background: 'rgba(15, 23, 42, 0.75)',
              borderRadius: '12px',
              padding: '18px',
              border: '1px solid rgba(56, 189, 248, 0.2)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span style={{ fontSize: '14px', fontWeight: 800, color: '#38bdf8' }}>{agent.name}</span>
              <span style={{ fontSize: '10px', fontWeight: 700, padding: '2px 8px', borderRadius: '4px', background: 'rgba(16, 185, 129, 0.2)', color: '#10b981' }}>
                {agent.status}
              </span>
            </div>
            <p style={{ margin: '0 0 10px 0', fontSize: '12px', color: '#94a3b8' }}>{agent.primaryRole}</p>
            <div style={{ display: 'flex', gap: '12px', fontSize: '11px', marginBottom: '12px' }}>
              <span style={{ color: '#10b981', fontWeight: 700 }}>Sharpe: {agent.historicalSharpe.toFixed(2)}</span>
              <span style={{ color: '#38bdf8', fontWeight: 700 }}>Win Rate: {agent.recentWinRatePct.toFixed(1)}%</span>
              <span style={{ color: '#f59e0b', fontWeight: 700 }}>Skills: All 52 Active</span>
            </div>

            <div style={{ fontSize: '11px', color: '#cbd5e1' }}>
              <span style={{ color: '#64748b' }}>Specialization Weights:</span>
              <ul style={{ margin: '4px 0 0 0', paddingLeft: '16px', color: '#94a3b8' }}>
                {Object.entries(agent.specializationWeights).map(([sk, wt]) => (
                  <li key={sk}>
                    <span style={{ color: '#f8fafc' }}>{sk}:</span> {(wt * 100).toFixed(0)}%
                  </li>
                ))}
              </ul>
            </div>
          </div>
        ))}
      </div>

      {/* Autonomous Evolution Catalog */}
      <section style={{ background: 'rgba(15, 23, 42, 0.8)', borderRadius: '12px', padding: '20px', border: '1px solid rgba(245, 158, 11, 0.3)' }}>
        <h2 style={{ margin: '0 0 14px 0', fontSize: '18px', color: '#f59e0b', fontWeight: 800 }}>
          Autonomous Self-Evolution & Invented Methods Catalog ({inventedMethods.length})
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '12px' }}>
          {inventedMethods.map((inv) => (
            <div key={inv.id} style={{ background: '#090d16', padding: '14px', borderRadius: '8px', border: '1px solid #1e293b' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: '#10b981', fontWeight: 700, fontSize: '13px' }}>{inv.name}</span>
                <span style={{ color: '#f59e0b', fontSize: '11px', fontWeight: 700 }}>Sharpe: {inv.testedSharpe.toFixed(2)}</span>
              </div>
              <p style={{ margin: '4px 0', fontSize: '11px', color: '#94a3b8' }}>
                Market: {inv.targetMarket} • Win: {inv.testedWinRatePct.toFixed(1)}% • PBO: {inv.testedPboPct.toFixed(1)}%
              </p>
              <p style={{ margin: '4px 0 0 0', fontSize: '11px', color: '#cbd5e1' }}>
                Synthesized: {inv.sourceSkillNames.join(' + ')}
              </p>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
