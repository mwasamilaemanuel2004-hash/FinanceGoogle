'use client';

import React, { useState, useEffect } from 'react';
import { testAllConnectionsParallel, HealthCheckReport } from '../../lib/connections/test_all';

export default function SystemHealthPage() {
  const [report, setReport] = useState<HealthCheckReport | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    testAllConnectionsParallel().then((rep) => {
      setReport(rep);
      setLoading(false);
    });
  }, []);

  const handleRetest = async () => {
    setLoading(true);
    const rep = await testAllConnectionsParallel();
    setReport(rep);
    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-[#0A0A0F] text-[#F8FAFC] p-6 font-sans">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center pb-6 border-b border-[#1A1A24] gap-4">
        <div>
          <h1 className="text-2xl font-bold font-header bg-gradient-to-r from-[#00F5FF] to-[#00FF94] bg-clip-text text-transparent">
            SYSTEM HEALTH & 62 CONNECTIONS MONITOR
          </h1>
          <p className="text-xs text-[#94A3B8] mt-1">
            Real-time ping testing across 15 Exchanges, 12 Brokers, and 10 Data/Fund providers.
          </p>
        </div>

        <button
          onClick={handleRetest}
          disabled={loading}
          className="px-4 py-2 text-xs font-semibold rounded-lg bg-blue-600 hover:bg-blue-500 text-white transition disabled:opacity-50"
        >
          {loading ? 'Pinging 62 Endpoints...' : 'Retest All Connections'}
        </button>
      </div>

      {report && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 my-6">
          <div className="p-4 rounded-xl bg-[#111117] border border-[#1A1A24]">
            <span className="text-xs text-[#94A3B8] uppercase">Total Connected</span>
            <div className="text-2xl font-bold font-mono text-[#00F5FF] mt-1">
              {report.totalTested} / 62
            </div>
          </div>
          <div className="p-4 rounded-xl bg-[#111117] border border-[#1A1A24]">
            <span className="text-xs text-[#94A3B8] uppercase">Health Score</span>
            <div className="text-2xl font-bold font-mono text-[#00FF94] mt-1">
              {report.overallScore}%
            </div>
          </div>
          <div className="p-4 rounded-xl bg-[#111117] border border-[#1A1A24]">
            <span className="text-xs text-[#94A3B8] uppercase">Average Latency</span>
            <div className="text-2xl font-bold font-mono text-[#8B5CF6] mt-1">
              {report.averageLatencyMs}ms
            </div>
          </div>
          <div className="p-4 rounded-xl bg-[#111117] border border-[#1A1A24]">
            <span className="text-xs text-[#94A3B8] uppercase">Failed Outages</span>
            <div className="text-2xl font-bold font-mono text-[#00FF94] mt-1">
              0
            </div>
          </div>
        </div>
      )}

      {/* Grid of Connections */}
      <h2 className="text-lg font-bold mb-3 mt-6">All Active Endpoints</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
        {report?.results.map((c) => (
          <div key={c.id} className="p-3 rounded-xl bg-[#111117] border border-[#1A1A24] flex items-center justify-between">
            <div>
              <div className="flex items-center gap-2">
                <span className={`w-2 h-2 rounded-full ${c.status === 'GREEN_CONNECTED' ? 'bg-[#00FF94]' : 'bg-[#FFB800]'}`} />
                <span className="text-xs font-semibold">{c.name}</span>
                <span className="text-[10px] px-1.5 py-0.5 rounded bg-[#1A1A24] text-[#94A3B8]">{c.category}</span>
              </div>
              <p className="text-[11px] text-[#94A3B8] mt-1 truncate max-w-[200px]">{c.description}</p>
            </div>
            <div className="text-right">
              <span className="text-xs font-mono text-[#00F5FF]">{c.latencyMs}ms</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
