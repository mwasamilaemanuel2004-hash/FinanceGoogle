export interface HumanPersona {
  name: string;
  age: number;
  experienceYears: number;
  archetypes: {
    calmness: string; // "Calm like Paul Tudor Jones"
    patience: string; // "Patient like Warren Buffett"
    conviction: string; // "Aggressive like George Soros when edge > 70%"
    riskControl: string; // "Controlled via Kelly Criterion & Hard Risk Governor"
  };
  avatar: {
    type: 'HUMANOID_CYAN_PURPLE';
    statusText: string;
  };
}

export const QUANTUM_PERSONA: HumanPersona = {
  name: 'QUANTUM',
  age: 35,
  experienceYears: 20,
  archetypes: {
    calmness: 'Calm like Paul Tudor Jones',
    patience: 'Patient like Warren Buffett',
    conviction: 'Aggressive conviction like George Soros when edge > 70%',
    riskControl: 'Mathematical capital control strictly enforced by Kelly Criterion & Hard Risk Governor',
  },
  avatar: {
    type: 'HUMANOID_CYAN_PURPLE',
    statusText: 'THINKING LIKE HUMAN + CALCULATING BEYOND',
  },
};

export function formatHumanLog(symbol: string, edge: number, spreadOk: boolean): string {
  if (!spreadOk) {
    return `Bro, this ${symbol} setup looks juicy with ${Math.round(edge * 100)}% edge, but spread is wide right now. Like a patient seasoned trader, I am holding off.`;
  }
  return `I feel the emotional adrenaline on ${symbol}, but my beyond-human quant math confirms positive EV (+${(edge * 100 - 50).toFixed(1)}%). Executing with disciplined size.`;
}
