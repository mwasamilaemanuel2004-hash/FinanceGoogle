'use client';

import React from 'react';

interface StatusBarProps {
  greenConnectionsCount?: number;
  latencyMs?: number;
  selfAwarenessPct?: number;
  activeEaCount?: number;
}

export function UltimateStatusBar({
  greenConnectionsCount = 62,
  latencyMs = 5.4,
  selfAwarenessPct = 98.4,
  activeEaCount = 34,
}: StatusBarProps) {
  return (
    <div className="w-full bg-[#0A0A0F]/90 backdrop-blur-md border-b border-[#1A1A24] px-4 py-2 flex flex-wrap items-center justify-between text-xs font-mono text-[#94A3B8] sticky top-0 z-40">
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-[#00FF94] shadow-[0_0_8px_#00FF94]" />
          <span className="text-[#F8FAFC] font-bold">ESMH QUANT AGI</span>
          <span className="px-1.5 py-0.5 rounded text-[10px] bg-[#8B5CF6]/20 text-[#8B5CF6] border border-[#8B5CF6]/40">
            CONSCIOUS
          </span>
        </div>

        <div className="hidden sm:flex items-center gap-1.5">
          <span>Connections:</span>
          <span className="text-[#00FF94] font-semibold">{greenConnectionsCount}/62 ACTIVE</span>
        </div>
      </div>

      <div className="flex items-center gap-4">
        <div className="flex items-center gap-1.5">
          <span>Latency:</span>
          <span className="text-[#00F5FF] font-semibold">{latencyMs}ms</span>
        </div>

        <div className="flex items-center gap-1.5">
          <span>Self-Awareness:</span>
          <span className="text-[#00FF94] font-semibold">{selfAwarenessPct}%</span>
        </div>

        <div className="flex items-center gap-1.5">
          <span>Bots / EAs:</span>
          <span className="text-[#8B5CF6] font-semibold">{activeEaCount} RUNNING</span>
        </div>
      </div>
    </div>
  );
}
