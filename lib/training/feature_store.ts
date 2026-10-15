export interface FeatureStoreRecord {
  market: string;
  timeframe: string;
  timestamp: number;
  dnaFeatures: number[]; // 1 to 125 features
  bellStats: {
    bestDist: string;
    df: number;
    fatTailFactor: number;
  };
  ichimokuVals: {
    kumoScore: number;
    tenkanKijunCross: boolean;
  };
  wyckoffVals: {
    phase: string;
    compositeMan: string;
  };
  vpVals: {
    poc: number;
    vah: number;
    val: number;
  };
  ofVals: {
    cvd: number;
    toxicity: number;
  };
  ouVals: {
    theta: number;
    halfLife: number;
    zScore: number;
  };
}

const inMemoryFeatureCache = new Map<string, FeatureStoreRecord>();

export async function putFeatures(record: FeatureStoreRecord): Promise<void> {
  const key = `${record.market}:${record.timeframe}`;
  inMemoryFeatureCache.set(key, record);
}

export async function getLatestFeatures(market: string, timeframe: string = '1m'): Promise<FeatureStoreRecord | null> {
  const key = `${market}:${timeframe}`;
  return inMemoryFeatureCache.get(key) || null;
}
