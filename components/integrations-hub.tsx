'use client';

import React, { useState } from 'react';
import { masterHub, IntegrationStatus } from '../lib/integrations/hub';
import { ultimateManager } from '../lib/ultimate/ultimate_mode';

export function IntegrationsHubGrid() {
  const [statuses, setStatuses] = useState<IntegrationStatus[]>(masterHub.getAllStatuses());
  const [tokenInput, setTokenInput] = useState<{ [key: string]: string }>({});
  const [unlockedState, setUnlockedState] = useState(ultimateManager.getUnlockedStatus());

  const handleSaveToken = (platformId: string) => {
    const val = tokenInput[platformId];
    if (val) {
      ultimateManager.saveToken(platformId, val);
      setStatuses(masterHub.getAllStatuses());
      setUnlockedState(ultimateManager.getUnlockedStatus());
    }
  };

  return (
    <div className="p-6 bg-slate-950 text-white min-h-screen">
      {/* Ultimate Mode Banner */}
      <div className={`p-4 rounded-xl mb-6 flex justify-between items-center border ${unlockedState.isUnlocked ? 'bg-amber-950/40 border-amber-500/50 text-amber-200' : 'bg-slate-900 border-slate-800 text-slate-300'}`}>
        <div>
          <h2 className="text-xl font-bold flex items-center gap-2">
            {unlockedState.isUnlocked ? '👑 ULTIMATE MODE UNLOCKED' : '🛡️ FREE / COMMUNITY MODE'}
          </h2>
          <p className="text-sm opacity-80">
            {unlockedState.isUnlocked 
              ? 'All 12 Autonomous Agents Active. Full Quant Pipeline Enabled.' 
              : `Add at least 3 Platform Tokens to unlock Ultimate Mode (${unlockedState.activeTokens}/3 added). Free mode works via public fallbacks.`}
          </p>
        </div>
        <div className="text-right">
          <span className="text-xs px-3 py-1 rounded-full bg-slate-800 font-mono">
            {unlockedState.tier}
          </span>
        </div>
      </div>

      <h1 className="text-2xl font-bold mb-4">Master Integration Hub (25 Platforms)</h1>

      {/* Grid of 25 Platforms */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {statuses.map(p => {
          const isGreen = p.status === 'GREEN_CONNECTED';
          const isYellow = p.status === 'YELLOW_LIMITED_FREE';

          return (
            <div key={p.platformId} className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col justify-between hover:border-slate-700 transition">
              <div>
                <div className="flex justify-between items-start mb-2">
                  <h3 className="font-semibold text-lg">{p.name}</h3>
                  <span className={`w-3 h-3 rounded-full ${isGreen ? 'bg-emerald-500 shadow-emerald-500/50 shadow' : isYellow ? 'bg-amber-500 shadow-amber-500/50 shadow' : 'bg-rose-500'}`} />
                </div>
                <div className="flex gap-2 text-xs mb-3">
                  <span className="bg-slate-800 px-2 py-0.5 rounded text-slate-400">{p.category}</span>
                  <span className={`px-2 py-0.5 rounded font-mono ${p.mode === 'TOKEN' ? 'bg-emerald-950 text-emerald-400' : 'bg-amber-950 text-amber-400'}`}>
                    {p.mode} MODE
                  </span>
                  <span className="text-slate-500">{p.latencyMs}ms</span>
                </div>
                <p className="text-xs text-slate-400 mb-3">
                  Features: {p.features.join(', ')}
                </p>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-800 flex flex-col gap-2">
                <div className="flex gap-2">
                  <input
                    type="password"
                    placeholder="Enter API token / key"
                    className="bg-slate-950 border border-slate-800 text-xs px-2 py-1.5 rounded flex-1 focus:outline-none focus:border-slate-600"
                    value={tokenInput[p.platformId] || ''}
                    onChange={(e) => setTokenInput({ ...tokenInput, [p.platformId]: e.target.value })}
                  />
                  <button
                    onClick={() => handleSaveToken(p.platformId)}
                    className="bg-blue-600 hover:bg-blue-500 text-xs px-3 py-1.5 rounded font-medium transition"
                  >
                    Save
                  </button>
                </div>
                <a
                  href={p.freeUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-[11px] text-slate-500 hover:text-slate-300 underline text-right"
                >
                  Visit official {p.name} →
                </a>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
