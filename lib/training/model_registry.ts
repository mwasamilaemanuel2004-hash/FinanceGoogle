export interface ModelRecord {
  engine: string;
  market: string;
  version: number;
  accuracy: number;
  f1: number;
  sharpeBacktest: number;
  trainedAt: string;
  modelPath: string;
  status: 'ACTIVE' | 'ARCHIVED';
}

const inMemoryRegistry = new Map<string, ModelRecord>();

export async function registerModel(record: ModelRecord): Promise<void> {
  const key = `${record.engine}:${record.market}`;
  inMemoryRegistry.set(key, record);
}

export async function getActiveModel(engine: string, market: string): Promise<ModelRecord | null> {
  const key = `${engine}:${market}`;
  if (inMemoryRegistry.has(key)) {
    return inMemoryRegistry.get(key)!;
  }

  // Default active baseline model
  return {
    engine,
    market,
    version: 1,
    accuracy: 0.724,
    f1: 0.718,
    sharpeBacktest: 2.15,
    trainedAt: new Date().toISOString(),
    modelPath: `models/${engine}/${market.replace('/', '_')}.bin`,
    status: 'ACTIVE'
  };
}
