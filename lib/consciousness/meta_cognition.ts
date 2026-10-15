export interface MetaThought {
  id: string;
  timestamp: string;
  primaryThought: string;
  metaReflection: string; // Thought about the thought
  epistemicDoubtScore: number; // 0 to 1
  actionTaken: 'PROCEED' | 'PAUSE' | 'RECALIBRATE';
}

export class MetaCognition {
  private static instance: MetaCognition;
  private thoughtsLog: MetaThought[] = [];

  private constructor() {
    this.thoughtsLog.push({
      id: 'META_001',
      timestamp: new Date().toISOString(),
      primaryThought: 'I thought EURUSD should be bought because 125 DNA ensemble is 70% bullish.',
      metaReflection: 'I observe high Bookmap toxicity (80%) and rising spread. A seasoned human trader would wait for absorption before entering.',
      epistemicDoubtScore: 0.65,
      actionTaken: 'PAUSE',
    });
  }

  public static getInstance(): MetaCognition {
    if (!MetaCognition.instance) {
      MetaCognition.instance = new MetaCognition();
    }
    return MetaCognition.instance;
  }

  public recordMetaThought(primary: string, reflection: string, doubt: number, action: MetaThought['actionTaken']): MetaThought {
    const thought: MetaThought = {
      id: `META_${Date.now()}`,
      timestamp: new Date().toISOString(),
      primaryThought: primary,
      metaReflection: reflection,
      epistemicDoubtScore: doubt,
      actionTaken: action,
    };
    this.thoughtsLog.unshift(thought);
    if (this.thoughtsLog.length > 50) this.thoughtsLog.pop();
    return thought;
  }

  public getRecentMetaThoughts(): MetaThought[] {
    return [...this.thoughtsLog];
  }
}

export const metaCognition = MetaCognition.getInstance();
