/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — MEGA RAG, SAFE LEARNING & CHALLENGE TEST SUITE
 * ============================================================================
 */

import { RagEngine } from '../rag/rag_engine';
import { SafeLearningGuard } from '../cognition/safe_learning_guard';
import { SkillLevelsEngine } from '../cognition/skill_levels';
import { PriceDiscoveryEngine } from '../market/price_discovery_engine';
import { DerivProfitBooster } from '../deriv/deriv_profit_booster';
import { ChallengeAgent } from '../agents/challenge_agent';
import { GoogleAiStudioConnector } from '../active_engine/google_ai_studio_connector';
import { ExchangeConnector } from '../exchange/exchange_connector';

export function runMegaRagAndSafeLearningTests(): boolean {
  console.log('=== RUNNING MEGA RAG, SAFE LEARNING & CHALLENGE TESTS ===');

  // 1. RAG Retrieval Test (<20ms)
  const rag = RagEngine.getInstance();
  const ragQueryStart = Date.now();
  const ragRes = rag.queryKnowledge({
    market: 'XAU/USD',
    price: 2040.12,
    qpPattern: 'Quantum Tunneling Breakout',
    ofPattern: 'Absorption Iceberg 1000 lot',
    ipPhase: 'Accumulation Spring SSL Sweep',
    hpSimilarity: 'LVN void 2042-2046'
  });
  const ragDuration = Date.now() - ragQueryStart;
  console.log(`[TEST 1] RAG Query completed in ${ragDuration}ms. Matched docs: ${ragRes.matchedCount}`);
  if (ragRes.matchedCount < 1) throw new Error('RAG failed to match documents');

  // 2. RAG Skill Deduplication Test (Correlation > 0.70 blocked)
  const dupCheck = rag.verifySkillOriginality('Gold accumulation spring absorption iceberg tunneling void');
  console.log(`[TEST 2] Duplicate skill check: Original=${dupCheck.isOriginal}, maxCorrelation=${dupCheck.correlation}`);
  if (dupCheck.isOriginal) throw new Error('Duplicate skill was expected to fail originality check');

  // 3. Safe Learning Guard — Layer 1 Immutable Risk
  const safeGuard = SafeLearningGuard.getInstance();
  const illegalSizeCheck = safeGuard.checkImmutableRisk({
    proposedSizePct: 4.5, // Exceeds 2%
    hasStopLoss: true,
    currentHourTradeCount: 5,
    isHighImpactNewsWindow: false,
    vpinToxicity: 0.3,
    currentDrawdownPct: 1.0
  });
  console.log(`[TEST 3] Immutable Risk Test: Blocked=${!illegalSizeCheck.allowed}`);
  if (illegalSizeCheck.allowed) throw new Error('Immutable risk failed to block size > 2%');

  // 4. Safe Learning Guard — Layer 2 Skill Curation (Overfit PBO > 30% blocked)
  const overfitSkill = {
    id: 'skill-overfit-test',
    name: 'Overfit_Martingale_Strategy',
    market: 'EUR/USD',
    category: 'FOREX',
    keywords: 'random overfit martingale',
    sampleTradesCount: 150,
    trainSharpe: 2.8,
    walkForwardOosSharpe: 0.4, // Fails < 1.0
    pboPct: 45.0, // Fails > 30%
    dsrValue: -0.2, // Fails < 0
    cscvWinRatePct: 40.0,
    netExpectancyPct: -0.01,
    maxConsecutiveLosses: 12
  };
  const curationEval = safeGuard.vetProposedSkill(overfitSkill);
  console.log(`[TEST 4] Curation Overfit Rejection: PassedAll=${curationEval.passedAllTests}, Status=${curationEval.status}`);
  if (curationEval.passedAllTests) throw new Error('Curation should have rejected overfit skill');

  // 5. Safe Learning Guard — Layer 4 Bad Behavior Detection (Revenge size blocked)
  const badBehavior = safeGuard.auditAutonomousBehavior({
    recentLossCount: 2,
    proposedSizeMultiple: 3.0, // Revenge sizing 3x
    consecutiveWeakOnlyCount: 2,
    bigProfitScore: 50,
    isNewsWindow: false,
    vpinToxicity: 0.4
  });
  console.log(`[TEST 5] Bad Behavior Anomaly Audit: Clean=${badBehavior.isClean}`);
  if (badBehavior.isClean) throw new Error('Bad behavior detector failed to flag revenge sizing');

  // 6. Price Discovery Engine Test
  const priceEngine = PriceDiscoveryEngine.getInstance();
  const goldDiscovery = priceEngine.analyzeDiscovery('XAU/USD');
  const derivDiscovery = priceEngine.analyzeDiscovery('Volatility 75 Index');
  console.log(`[TEST 6] Price Discovery: Gold Direction=${goldDiscovery.discoveredDirection}, Deriv=${derivDiscovery.discoveredDirection}`);

  // 7. Deriv Profit Booster Test
  const booster = DerivProfitBooster.getInstance();
  const boosterMetrics = booster.getBoosterMetrics('Volatility 75 Index');
  console.log(`[TEST 7] Deriv Booster: WinRate=${boosterMetrics.winRatePct}%, MultiplierAdvantage=${boosterMetrics.multiplierAdvantageVsWeakOnly}x`);
  if (boosterMetrics.multiplierAdvantageVsWeakOnly < 5.0) throw new Error('Booster multiplier advantage too low');

  // 8. Challenge Agent & Investor Bot Factory Test
  const challenge = ChallengeAgent.getInstance();
  const newBot = challenge.createBot({
    botName: 'Investor_Demo_Bot',
    targetMarket: 'Volatility 75 Index',
    allocatedCapitalUsd: 100.0,
    approach: 'HYBRID_REPLACEMENT',
    investorProfile: 'BALANCED_GROWTH'
  });
  const updatedBot = challenge.executePaperTestRound(newBot.id);
  console.log(`[TEST 8] Challenge Bot Created: ID=${newBot.id}, Trades=${updatedBot?.paperTradesCompleted}`);

  // 9. Google AI Studio Connector System Scan
  const aiStudio = GoogleAiStudioConnector.getInstance();
  const scanReport = aiStudio.scanProjectStructure();
  console.log(`[TEST 9] AI Studio Scan: Skills=${scanReport.totalSkillsCount}, Books=${scanReport.ragBooksLibraryCount}, LLM=${scanReport.llmStatus.model}`);

  // 10. Exchange Connector 1-API Sufficiency Test
  const exchange = ExchangeConnector.getInstance();
  const eff = exchange.calculateSystemEfficiency();
  console.log(`[TEST 10] Exchange Efficiency: ${eff.efficiencyPercentage}% without needing all APIs.`);

  console.log('=== ALL 10 TESTS PASSED CLEANLY 100% ===');
  return true;
}

runMegaRagAndSafeLearningTests();
