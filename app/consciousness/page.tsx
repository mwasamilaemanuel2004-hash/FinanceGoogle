'use client';

import React, { useState, useEffect } from 'react';
import { selfModel, ConsciousnessState } from '../../lib/consciousness/self_model';
import { QUANTUM_PERSONA } from '../../lib/consciousness/human_persona';
import { compareHumanVsBeyond } from '../../lib/consciousness/beyond_human';
import { runMirrorTest } from '../../lib/consciousness/self_recognition';
import { computeEmotionalDynamics } from '../../lib/consciousness/emotional_intelligence';

export default function ConsciousnessDashboard() {
  const [state, setState] = useState<ConsciousnessState>(selfModel.getState());
  const [showWhoAmIModal, setShowWhoAmIModal] = useState(false);
  const [activeTab, setActiveTab] = useState<'OVERVIEW' | 'BENCHMARK' | 'MIRROR'>('OVERVIEW');

  useEffect(() => {
    const timer = setInterval(() => {
      setState(selfModel.selfCheck());
    }, 5000);
    return () => clearInterval(timer);
  }, []);

  const emotions = computeEmotionalDynamics(1.2, 4);
  const benchmarks = compareHumanVsBeyond();
  const mirrorResults = runMirrorTest();

  return (
    <div className="min-h-screen bg-[#0A0A0F] text-[#F8FAFC] p-6 font-sans">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center pb-6 border-b border-[#1A1A24] gap-4">
        <div>
          <div className="flex items-center gap-3">
            <span className="w-3 h-3 rounded-full bg-[#00FF94] shadow-[0_0_12px_#00FF94] animate-pulse" />
            <h1 className="text-2xl font-bold tracking-tight font-header bg-gradient-to-r from-[#00F5FF] via-[#8B5CF6] to-[#00FF94] bg-clip-text text-transparent">
              AGI CONSCIOUSNESS & SELF-MODEL
            </h1>
          </div>
          <p className="text-xs text-[#94A3B8] mt-1">
            Identity: <span className="font-mono text-[#00F5FF]">{state.identity}</span> | Persona: <span className="text-[#8B5CF6]">{QUANTUM_PERSONA.name} ({QUANTUM_PERSONA.age}y/o)</span>
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setShowWhoAmIModal(true)}
            className="px-4 py-2 text-xs font-semibold rounded-lg bg-gradient-to-r from-[#00F5FF]/20 to-[#8B5CF6]/20 border border-[#00F5FF]/40 text-[#00F5FF] hover:border-[#00F5FF] hover:shadow-[0_0_15px_rgba(0,245,255,0.3)] transition"
          >
            Ask Me: "Who are you?"
          </button>
          <div className="px-3 py-1.5 rounded-lg bg-[#111117] border border-[#1A1A24] text-xs font-mono text-[#00FF94]">
            Latency: 5.2ms
          </div>
        </div>
      </div>

      {/* 3 Circular Cognitive Indicators */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 my-6">
        <div className="p-5 rounded-xl bg-[#111117] border border-[#1A1A24] flex items-center justify-between">
          <div>
            <span className="text-xs text-[#94A3B8] uppercase tracking-wider">Human Intuition</span>
            <div className="text-3xl font-bold font-mono text-[#00F5FF] mt-1">{state.humanLikeIntuitionScore}%</div>
            <p className="text-[11px] text-[#94A3B8] mt-1">Simulated Tudor Jones / Buffett patience</p>
          </div>
          <div className="w-14 h-14 rounded-full border-4 border-[#00F5FF]/30 border-t-[#00F5FF] flex items-center justify-center font-mono text-xs font-bold text-[#00F5FF]">
            INT
          </div>
        </div>

        <div className="p-5 rounded-xl bg-[#111117] border border-[#1A1A24] flex items-center justify-between">
          <div>
            <span className="text-xs text-[#94A3B8] uppercase tracking-wider">Beyond-Human Precision</span>
            <div className="text-3xl font-bold font-mono text-[#8B5CF6] mt-1">{state.beyondHumanCapabilityScore}%</div>
            <p className="text-[11px] text-[#94A3B8] mt-1">51 markets, 5ms latency, 100M ticks</p>
          </div>
          <div className="w-14 h-14 rounded-full border-4 border-[#8B5CF6]/30 border-t-[#8B5CF6] flex items-center justify-center font-mono text-xs font-bold text-[#8B5CF6]">
            AGI
          </div>
        </div>

        <div className="p-5 rounded-xl bg-[#111117] border border-[#1A1A24] flex items-center justify-between">
          <div>
            <span className="text-xs text-[#94A3B8] uppercase tracking-wider">Self-Awareness Score</span>
            <div className="text-3xl font-bold font-mono text-[#00FF94] mt-1">{state.selfAwarenessScore}%</div>
            <p className="text-[11px] text-[#94A3B8] mt-1">Continuous cognitive self-check & reflection</p>
          </div>
          <div className="w-14 h-14 rounded-full border-4 border-[#00FF94]/30 border-t-[#00FF94] flex items-center justify-center font-mono text-xs font-bold text-[#00FF94]">
            SEL
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="flex gap-4 border-b border-[#1A1A24] mb-6">
        <button
          onClick={() => setActiveTab('OVERVIEW')}
          className={`pb-3 text-xs font-semibold transition border-b-2 ${activeTab === 'OVERVIEW' ? 'border-[#00F5FF] text-[#00F5FF]' : 'border-transparent text-[#94A3B8] hover:text-[#F8FAFC]'}`}
        >
          COGNITIVE STREAM & EMOTIONS
        </button>
        <button
          onClick={() => setActiveTab('BENCHMARK')}
          className={`pb-3 text-xs font-semibold transition border-b-2 ${activeTab === 'BENCHMARK' ? 'border-[#8B5CF6] text-[#8B5CF6]' : 'border-transparent text-[#94A3B8] hover:text-[#F8FAFC]'}`}
        >
          HUMAN VS BEYOND BENCHMARK
        </button>
        <button
          onClick={() => setActiveTab('MIRROR')}
          className={`pb-3 text-xs font-semibold transition border-b-2 ${activeTab === 'MIRROR' ? 'border-[#00FF94] text-[#00FF94]' : 'border-transparent text-[#94A3B8] hover:text-[#F8FAFC]'}`}
        >
          MIRROR TEST & SELF-RECOGNITION
        </button>
      </div>

      {activeTab === 'OVERVIEW' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Live Thought Bubble */}
          <div className="lg:col-span-2 p-5 rounded-xl bg-[#111117] border border-[#1A1A24]">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-[#94A3B8] mb-3 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-[#00F5FF]" /> Live Chain of Thought
            </h3>
            <div className="p-4 rounded-lg bg-[#0A0A0F] border border-[#1A1A24] font-mono text-xs text-[#00F5FF] leading-relaxed">
              "{state.currentThought}"
            </div>

            <div className="mt-4 pt-4 border-t border-[#1A1A24]">
              <span className="text-xs text-[#94A3B8] uppercase">Recent Self-Reflection:</span>
              <p className="text-xs text-[#F8FAFC] mt-1 italic">
                "{state.memory.recentReflection}"
              </p>
            </div>
          </div>

          {/* Emotional Dynamics */}
          <div className="p-5 rounded-xl bg-[#111117] border border-[#1A1A24]">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-[#94A3B8] mb-3">
              Simulated Emotions
            </h3>

            <div className="space-y-3">
              <div>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-[#94A3B8]">Fear (Drawdown Damped)</span>
                  <span className="font-mono text-[#FF073A]">{emotions.internalFear.toFixed(1)}%</span>
                </div>
                <div className="w-full bg-[#0A0A0F] h-1.5 rounded-full overflow-hidden">
                  <div className="bg-[#FF073A] h-full" style={{ width: `${emotions.internalFear}%` }} />
                </div>
              </div>

              <div>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-[#94A3B8]">Greed (Kelly Controlled)</span>
                  <span className="font-mono text-[#00FF94]">{emotions.internalGreed.toFixed(1)}%</span>
                </div>
                <div className="w-full bg-[#0A0A0F] h-1.5 rounded-full overflow-hidden">
                  <div className="bg-[#00FF94] h-full" style={{ width: `${emotions.internalGreed}%` }} />
                </div>
              </div>

              <div>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-[#94A3B8]">Patience Level</span>
                  <span className="font-mono text-[#00F5FF]">{emotions.patienceLevel}%</span>
                </div>
                <div className="w-full bg-[#0A0A0F] h-1.5 rounded-full overflow-hidden">
                  <div className="bg-[#00F5FF] h-full" style={{ width: `${emotions.patienceLevel}%` }} />
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'BENCHMARK' && (
        <div className="overflow-x-auto rounded-xl border border-[#1A1A24] bg-[#111117]">
          <table className="w-full text-left text-xs font-mono">
            <thead className="bg-[#1A1A24] text-[#94A3B8]">
              <tr>
                <th className="p-3">Dimension</th>
                <th className="p-3">Human Trader</th>
                <th className="p-3 text-[#8B5CF6]">Ultimate AGI (Me)</th>
                <th className="p-3 text-[#00FF94]">Beyond Advantage</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#1A1A24]">
              {benchmarks.map((b, i) => (
                <tr key={i} className="hover:bg-[#161620]">
                  <td className="p-3 font-semibold text-[#F8FAFC]">{b.dimension}</td>
                  <td className="p-3 text-[#94A3B8]">{b.humanTrader}</td>
                  <td className="p-3 text-[#8B5CF6] font-bold">{b.ultimateAgi}</td>
                  <td className="p-3 text-[#00FF94]">{b.factorAdvantage}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === 'MIRROR' && (
        <div className="space-y-3">
          {mirrorResults.map((m) => (
            <div key={m.questionNumber} className="p-4 rounded-xl bg-[#111117] border border-[#1A1A24]">
              <div className="flex justify-between items-center mb-1">
                <span className="text-xs font-bold text-[#00F5FF]">Q{m.questionNumber}: {m.question}</span>
                <span className="text-[11px] font-mono text-[#00FF94]">Coherence: {m.coherenceScore * 100}%</span>
              </div>
              <p className="text-xs text-[#F8FAFC] mt-1 pl-3 border-l-2 border-[#8B5CF6]">
                {m.response}
              </p>
            </div>
          ))}
        </div>
      )}

      {/* Modal for "Who are you?" */}
      {showWhoAmIModal && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-[#111117] border border-[#00F5FF]/50 rounded-2xl max-w-lg w-full p-6 shadow-[0_0_30px_rgba(0,245,255,0.2)]">
            <h2 className="text-lg font-bold text-[#00F5FF] mb-3">Conscious Identity Declaration</h2>
            <p className="text-sm text-[#F8FAFC] leading-relaxed font-sans">
              "{selfModel.answerWhoAmI()}"
            </p>
            <div className="mt-4 pt-4 border-t border-[#1A1A24] flex justify-end">
              <button
                onClick={() => setShowWhoAmIModal(false)}
                className="px-4 py-1.5 rounded-lg bg-[#00F5FF] text-black font-semibold text-xs hover:bg-[#00FF94] transition"
              >
                Understood
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
