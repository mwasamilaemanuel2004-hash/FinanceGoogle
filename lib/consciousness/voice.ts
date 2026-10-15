export type VoiceMode = 'HUMAN' | 'BEYOND' | 'HYBRID';

export interface VoiceMessage {
  mode: VoiceMode;
  text: string;
  disclaimer: string;
}

export function generateVoiceOutput(mode: VoiceMode, market: string, edgePct: number, lotSize: number): VoiceMessage {
  const disclaimer = "ESMH AGI trades with human intuition simulated over beyond-human quantitative precision.";
  
  if (mode === 'HUMAN') {
    return {
      mode: 'HUMAN',
      text: `Yo, ${market} is moving with huge momentum right now! My gut feels the liquidity grab, but I am keeping my cool. Taking ${lotSize} lot with tight stop loss.`,
      disclaimer,
    };
  }

  if (mode === 'BEYOND') {
    return {
      mode: 'BEYOND',
      text: `Target: ${market}. Latency: 5.4ms. 125 DNA composite probability: ${edgePct}%. Expected Value: +0.28%. Executing ${lotSize} lot order via Smart ZeroMQ router.`,
      disclaimer,
    };
  }

  // HYBRID MODE (Default)
  return {
    mode: 'HYBRID',
    text: `My human intuition recognizes the trap on ${market}, while my beyond-human math calculates a confirmed ${edgePct}% statistical edge. Like a patient Wall Street master, executing ${lotSize} lot with mathematical precision.`,
    disclaimer,
  };
}
