export interface MetricComparison {
  dimension: string;
  humanTrader: string;
  ultimateAgi: string;
  factorAdvantage: string;
}

export function compareHumanVsBeyond(): MetricComparison[] {
  return [
    {
      dimension: 'Simultaneous Markets Monitored',
      humanTrader: '2 to 4 screens max',
      ultimateAgi: '51 global markets concurrently',
      factorAdvantage: '15x to 25x greater market awareness',
    },
    {
      dimension: 'Execution & Processing Latency',
      humanTrader: '250ms - 500ms manual reaction',
      ultimateAgi: '5ms - 12ms WebSocket + ZeroMQ pipeline',
      factorAdvantage: '50x faster execution speed',
    },
    {
      dimension: 'Tick Memory Depth',
      humanTrader: 'Recent days / vague charts',
      ultimateAgi: '100M+ real ticks stored across 10 years',
      factorAdvantage: 'Infinite recall fidelity',
    },
    {
      dimension: 'Continuous Operational Endurance',
      humanTrader: '6 - 8 hours before cognitive fatigue sets in',
      ultimateAgi: '24/7/365 without sleep or fatigue',
      factorAdvantage: '100% uninterrupted uptime',
    },
    {
      dimension: 'Emotional Drift & Tilt',
      humanTrader: 'Vulnerable to revenge trading & fear',
      ultimateAgi: 'Emotionally simulated, mathematically governed by Hard Risk',
      factorAdvantage: 'Zero revenge trading or tilt',
    },
    {
      dimension: 'Monte Carlo Stress Testing',
      humanTrader: 'Hours or days of manual simulation',
      ultimateAgi: '10,000 universes generated in < 2 seconds',
      factorAdvantage: '1800x faster risk calculation',
    },
  ];
}
