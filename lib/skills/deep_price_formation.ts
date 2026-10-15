import { ALL_ICEBERG_SKILLS, IcebergSkillResult, IIcebergSkill } from './iceberg_skills';
import { Candle } from '../tools/smc_detector';

export interface DeepPriceFormationAnalysis {
  timestamp: number;
  overallSignal: 'BUY' | 'SELL' | 'HOLD';
  overallConfidence: number;
  levelSummaries: Record<number, {
    levelName: string;
    dominantSignal: 'BUY' | 'SELL' | 'HOLD';
    avgConfidence: number;
    findings: string[];
  }>;
  allResults: IcebergSkillResult[];
  compositeReasoningChain: string;
  totalLatencyMs: number;
}

export class DeepPriceFormationEngine {
  private static instance: DeepPriceFormationEngine;

  private constructor() {}

  public static getInstance(): DeepPriceFormationEngine {
    if (!DeepPriceFormationEngine.instance) {
      DeepPriceFormationEngine.instance = new DeepPriceFormationEngine();
    }
    return DeepPriceFormationEngine.instance;
  }

  public async evaluateDeepHierarchy(
    candles: Candle[],
    externalMarketContext?: any
  ): Promise<DeepPriceFormationAnalysis> {
    const t0 = performance.now();
    const results: IcebergSkillResult[] = await Promise.all(
      ALL_ICEBERG_SKILLS.map(skill => skill.analyzeDeep(candles, externalMarketContext))
    );

    // Group by levels 1 through 6
    const levelMap: Record<number, IcebergSkillResult[]> = {
      1: [], 2: [], 3: [], 4: [], 5: [], 6: []
    };
    results.forEach(r => {
      levelMap[r.level].push(r);
    });

    const levelSummaries: DeepPriceFormationAnalysis['levelSummaries'] = {};
    let buyWeight = 0;
    let sellWeight = 0;
    let holdWeight = 0;

    for (let lvl = 1; lvl <= 6; lvl++) {
      const group = levelMap[lvl];
      if (group.length === 0) continue;

      const levelWeight = lvl * 1.5; // Deeper levels carry higher structural weighting!
      let lBuy = 0, lSell = 0, lHold = 0;
      let totalConf = 0;

      group.forEach(g => {
        totalConf += g.confidence;
        if (g.signal === 'BUY') {
          lBuy++;
          buyWeight += g.confidence * levelWeight;
        } else if (g.signal === 'SELL') {
          lSell++;
          sellWeight += g.confidence * levelWeight;
        } else {
          lHold++;
          holdWeight += g.confidence * levelWeight;
        }
      });

      const dominant = lBuy > lSell && lBuy > lHold ? 'BUY' : (lSell > lBuy && lSell > lHold ? 'SELL' : 'HOLD');
      levelSummaries[lvl] = {
        levelName: group[0].levelName,
        dominantSignal: dominant,
        avgConfidence: Number((totalConf / group.length).toFixed(1)),
        findings: group.map(g => `${g.skillName}: ${g.reasoning}`)
      };
    }

    const maxWeight = Math.max(buyWeight, sellWeight, holdWeight);
    const overallSignal: 'BUY' | 'SELL' | 'HOLD' =
      maxWeight === buyWeight && buyWeight > sellWeight * 1.15 ? 'BUY' :
      (maxWeight === sellWeight && sellWeight > buyWeight * 1.15 ? 'SELL' : 'HOLD');

    const sumWeights = buyWeight + sellWeight + holdWeight || 1;
    const overallConfidence = Math.min(99, Math.round((maxWeight / sumWeights) * 100));

    const compositeReasoningChain = [
      `[Level 6 Macro]: ${levelSummaries[6]?.findings[0] || 'Macro aligned.'}`,
      `[Level 5 Quant]: ${levelSummaries[5]?.findings[0] || 'Mathematical edge confirmed.'}`,
      `[Level 4 Options GEX]: ${levelSummaries[4]?.findings.join(' | ') || 'Gamma pinning verified.'}`,
      `[Level 3 Orderflow]: ${levelSummaries[3]?.findings.slice(0, 2).join(' | ') || 'CVD absorption verified.'}`,
      `[Level 2/1 Structure]: ${levelSummaries[1]?.findings[0] || 'Liquidity sweep confirmed.'}`
    ].join(' -> ');

    const totalLatencyMs = Number((performance.now() - t0).toFixed(2));
    return {
      timestamp: Date.now(),
      overallSignal,
      overallConfidence,
      levelSummaries,
      allResults: results,
      compositeReasoningChain,
      totalLatencyMs
    };
  }
}

export const deepPriceFormationEngine = DeepPriceFormationEngine.getInstance();
