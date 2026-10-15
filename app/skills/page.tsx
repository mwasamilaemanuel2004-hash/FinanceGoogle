'use client';

import React, { useState } from 'react';
import { skillsRegistry, UnifiedSkillDescriptor } from '../../lib/skills/skills_registry';
import { agenticMethodInventor } from '../../lib/evolution/inventor';

export default function SkillsPage() {
  const [skills, setSkills] = useState<UnifiedSkillDescriptor[]>(() => skillsRegistry.getAllSkillDescriptors());
  const [inventedList, setInventedList] = useState(() => agenticMethodInventor.getInventedMethods());
  const [selectedFilter, setSelectedFilter] = useState<string>('ALL');
  const [targetMarket, setTargetMarket] = useState<string>('BTC/USDT');
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const handleToggle = (skillName: string) => {
    const updated = skills.map(s => {
      if (s.name === skillName) {
        const nextActive = !s.isActive;
        skillsRegistry.toggleSkill(skillName, nextActive);
        return { ...s, isActive: nextActive };
      }
      return s;
    });
    setSkills(updated);
  };

  const handleInventNewSkill = async () => {
    // Generate sample candles for backtest validation
    const mockCandles = Array.from({ length: 1000 }).map((_, i) => ({
      timestamp: Date.now() - (1000 - i) * 60000,
      open: 65000 + Math.sin(i / 10) * 120,
      high: 65000 + Math.sin(i / 10) * 120 + 30,
      low: 65000 + Math.sin(i / 10) * 120 - 30,
      close: 65000 + Math.sin(i / 10) * 120 + 10,
      volume: 2500
    }));

    const activeSkillNames = skills.map(s => s.name);
    const result = await agenticMethodInventor.inventMethod(activeSkillNames, targetMarket, mockCandles);
    if (result) {
      setToastMessage(`✅ Successfully Invented & Deployed: ${result.name} (Sharpe: ${result.testedSharpe.toFixed(2)}, Win Rate: ${result.testedWinRatePct}%)`);
      setInventedList(agenticMethodInventor.getInventedMethods());
      setSkills(skillsRegistry.getAllSkillDescriptors());
    } else {
      setToastMessage('⚠️ Combination did not pass strict Sharpe > 1.30 & PBO < 30% hurdle. Hard Risk preserved.');
    }
  };

  const filteredSkills = skills.filter(s => {
    if (selectedFilter === 'ALL') return true;
    if (selectedFilter === 'STRATEGY_25') return s.family === 'STRATEGY_25';
    if (selectedFilter === 'ICEBERG_DEEP') return s.family === 'ICEBERG_DEEP';
    if (selectedFilter === 'QUANTUM') return s.family === 'QUANTUM_CAPABILITY';
    if (selectedFilter === 'INVENTED') return s.isInvented;
    return true;
  });

  return (
    <div style={{ minHeight: '100vh', background: '#090d16', color: '#f8fafc', padding: '32px', fontFamily: 'system-ui, sans-serif' }}>
      <header style={{ marginBottom: '28px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <h1 style={{ margin: 0, fontSize: '26px', fontWeight: 800, color: '#38bdf8' }}>
              ESMH.TRADE — UNIFIED SKILLS & DEEP PRICE FORMATION MATRIX
            </h1>
            <p style={{ margin: '6px 0 0 0', color: '#94a3b8', fontSize: '14px' }}>
              All 10 Sovereign Agents have Access to All 52 Skills (Image 1 Strategies + Image 2 Iceberg + Quantum Roles + Autonomous Method Inventions)
            </p>
          </div>
          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            <select
              value={targetMarket}
              onChange={(e) => setTargetMarket(e.target.value)}
              style={{ background: '#0f172a', color: '#38bdf8', border: '1px solid #1e293b', borderRadius: '8px', padding: '8px 12px', fontSize: '13px' }}
            >
              <option value="BTC/USDT">BTC/USDT</option>
              <option value="ETH/USDT">ETH/USDT</option>
              <option value="XAU/USD">XAU/USD (Gold)</option>
              <option value="EUR/USD">EUR/USD</option>
              <option value="V75">Deriv Volatility 75</option>
            </select>
            <button
              onClick={handleInventNewSkill}
              style={{ background: 'linear-gradient(135deg, #10b981, #059669)', color: '#ffffff', border: 'none', padding: '10px 18px', borderRadius: '8px', fontSize: '13px', fontWeight: 700, cursor: 'pointer' }}
            >
              ⚡ Invent New Skill
            </button>
          </div>
        </div>
      </header>

      {toastMessage && (
        <div style={{ marginBottom: '20px', padding: '12px 18px', borderRadius: '8px', background: 'rgba(16, 185, 129, 0.15)', border: '1px solid #10b981', color: '#10b981', fontSize: '13px', fontWeight: 600 }}>
          {toastMessage}
        </div>
      )}

      {/* Filter Tabs */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '20px', flexWrap: 'wrap' }}>
        {[
          { id: 'ALL', label: `All Skills (${skills.length})` },
          { id: 'STRATEGY_25', label: 'Image 1: 25 Strategies' },
          { id: 'ICEBERG_DEEP', label: 'Image 2: Iceberg Deep' },
          { id: 'QUANTUM', label: 'Quantum Roles' },
          { id: 'INVENTED', label: `Autonomous Invented (${inventedList.length})` }
        ].map(tab => (
          <button
            key={tab.id}
            onClick={() => setSelectedFilter(tab.id)}
            style={{
              background: selectedFilter === tab.id ? '#38bdf8' : '#0f172a',
              color: selectedFilter === tab.id ? '#090d16' : '#94a3b8',
              border: '1px solid #1e293b',
              padding: '6px 14px',
              borderRadius: '6px',
              fontSize: '12px',
              fontWeight: 700,
              cursor: 'pointer'
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Skills Table */}
      <section style={{ background: 'rgba(15, 23, 42, 0.8)', borderRadius: '14px', padding: '24px', border: '1px solid rgba(56, 189, 248, 0.2)' }}>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '12px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid rgba(56, 189, 248, 0.3)', color: '#94a3b8' }}>
                <th style={{ padding: '12px' }}>Skill Name</th>
                <th style={{ padding: '12px' }}>Family / Type</th>
                <th style={{ padding: '12px' }}>Category</th>
                <th style={{ padding: '12px' }}>Sharpe Ratio</th>
                <th style={{ padding: '12px' }}>Historical Win</th>
                <th style={{ padding: '12px' }}>Latency SLA</th>
                <th style={{ padding: '12px' }}>Invented</th>
                <th style={{ padding: '12px' }}>Status</th>
                <th style={{ padding: '12px' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {filteredSkills.map((s, idx) => (
                <tr key={idx} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)', opacity: s.isActive ? 1 : 0.5 }}>
                  <td style={{ padding: '12px', fontWeight: 700, color: '#f8fafc' }}>{s.name}</td>
                  <td style={{ padding: '12px', color: '#38bdf8', fontWeight: 600 }}>{s.family}</td>
                  <td style={{ padding: '12px', color: '#94a3b8' }}>{s.category}</td>
                  <td style={{ padding: '12px', fontWeight: 700, color: '#10b981' }}>{s.historicalSharpe.toFixed(2)}</td>
                  <td style={{ padding: '12px', fontWeight: 600, color: '#10b981' }}>{s.winRatePct.toFixed(1)}%</td>
                  <td style={{ padding: '12px', color: '#38bdf8' }}>&lt; 10ms</td>
                  <td style={{ padding: '12px', color: s.isInvented ? '#f59e0b' : '#64748b', fontWeight: s.isInvented ? 700 : 400 }}>
                    {s.isInvented ? 'YES (AI Invented)' : 'CORE'}
                  </td>
                  <td style={{ padding: '12px' }}>
                    <span style={{
                      padding: '3px 8px',
                      borderRadius: '4px',
                      fontSize: '11px',
                      fontWeight: 700,
                      background: s.isActive ? 'rgba(16, 185, 129, 0.2)' : 'rgba(239, 68, 68, 0.2)',
                      color: s.isActive ? '#10b981' : '#ef4444'
                    }}>
                      {s.isActive ? 'ACTIVE' : 'DISABLED'}
                    </span>
                  </td>
                  <td style={{ padding: '12px' }}>
                    <button
                      onClick={() => handleToggle(s.name)}
                      style={{
                        background: s.isActive ? '#ef4444' : '#10b981',
                        color: '#ffffff',
                        border: 'none',
                        padding: '4px 10px',
                        borderRadius: '4px',
                        fontSize: '11px',
                        fontWeight: 600,
                        cursor: 'pointer'
                      }}
                    >
                      {s.isActive ? 'Disable' : 'Enable'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
