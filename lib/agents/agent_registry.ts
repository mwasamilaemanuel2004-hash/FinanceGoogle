export interface AgentDescriptor {
  id: string;
  name: string;
  role: string;
  model: string;
  status: 'ACTIVE' | 'BUSY' | 'IDLE';
  requiresUltimate: boolean;
  capabilities: string[];
}

export const TWELVE_AGENTS: AgentDescriptor[] = [
  { id: 'supervisor', name: 'Supervisor / Mother Brain', role: 'Central Orchestration & Multiverse Consensus', model: 'gemini-1.5-pro', status: 'ACTIVE', requiresUltimate: false, capabilities: ['Consensus', 'Kill Switch', 'Directive Issuance'] },
  { id: 'research', name: 'Research Agent', role: 'Alpha Mining & Strategy Discovery', model: 'gemini-1.5-pro', status: 'ACTIVE', requiresUltimate: false, capabilities: ['Whitepaper Analysis', 'Factor Mining', 'Hypothesis Testing'] },
  { id: 'market_analysis', name: 'Market Analysis Agent', role: 'Microstructure & Regime Classification', model: 'gemini-1.5-flash', status: 'ACTIVE', requiresUltimate: false, capabilities: ['Hurst', 'Kyle Lambda', 'Regime Shifts'] },
  { id: 'quant', name: 'Quant Agent', role: 'Continuous Stochastic Calibration (OU, GARCH, Bell)', model: 'gemini-1.5-pro', status: 'ACTIVE', requiresUltimate: true, capabilities: ['Ornstein-Uhlenbeck', 'Hedge Ratios', 'Fat Tail Bell Curve'] },
  { id: 'strategy', name: 'Strategy Agent', role: '35 ML/Deep Ensemble Weighting', model: 'gemini-1.5-pro', status: 'ACTIVE', requiresUltimate: true, capabilities: ['LSTM', 'Transformer', 'XGBoost', 'CatBoost'] },
  { id: 'risk', name: 'Risk Agent', role: 'Extreme Value Theory, EVT, VaR & Multiverse Survival', model: 'gemini-1.5-pro', status: 'ACTIVE', requiresUltimate: false, capabilities: ['Kelly Criterion', 'Multiverse Check', 'Drawdown Guard'] },
  { id: 'execution', name: 'Execution Agent', role: 'Smart Order Routing (TWAP, VWAP, Snipe)', model: 'gemini-1.5-flash', status: 'ACTIVE', requiresUltimate: true, capabilities: ['Order Slicing', 'Slippage Minimization', 'ZeroMQ Router'] },
  { id: 'portfolio', name: 'Portfolio Agent', role: 'Black-Litterman & Risk Parity', model: 'gemini-1.5-pro', status: 'ACTIVE', requiresUltimate: true, capabilities: ['Covariance Matrix', 'Correlation Shrinkage', 'Heat Cap 6%'] },
  { id: 'news_macro', name: 'News & Macro Agent', role: 'FinBERT Sentiment & Forex Factory Scanner', model: 'gemini-1.5-flash', status: 'ACTIVE', requiresUltimate: true, capabilities: ['Economic Calendar', 'Federal Reserve Tone', 'Geopolitical Risk'] },
  { id: 'monitoring', name: 'Monitoring Agent', role: 'PSI/KS Drift Detection & Latency Watchdog', model: 'gemini-1.5-flash', status: 'ACTIVE', requiresUltimate: false, capabilities: ['KS Statistic', 'PSI > 0.2 Trigger', 'Health Ping'] },
  { id: 'user_assistant', name: 'Operations & User Assistant', role: 'Bilingual English/Swahili Explainer', model: 'gemini-1.5-flash', status: 'ACTIVE', requiresUltimate: false, capabilities: ['Swahili Reports', 'Trade Breakdown', 'User Guidance'] },
  { id: 'mispricing', name: 'Prediction Mispricing Agent', role: 'Kalshi & Polymarket Arbitrage Engine', model: 'gemini-1.5-pro', status: 'ACTIVE', requiresUltimate: true, capabilities: ['Implied Probability vs Real Model', 'Binary Contract Arb', 'Zero-Risk Hedging'] },
];

export function getAgents(isUltimateUnlocked: boolean): AgentDescriptor[] {
  return TWELVE_AGENTS.map(agent => ({
    ...agent,
    status: (!agent.requiresUltimate || isUltimateUnlocked) ? 'ACTIVE' : 'IDLE',
  }));
}
