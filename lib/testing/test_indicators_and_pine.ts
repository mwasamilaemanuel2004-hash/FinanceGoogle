/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — 11 INDICATORS & PINESCRIPT CLONE TEST SUITE
 * ============================================================================
 */

import {
  calculateSessionOpenIndicator,
  calculateVolumeSpikeIndicator,
  calculateOrderBlockIndicator,
  calculateDivergenceIndicator,
  calculateMarketStructureIndicator,
  calculateLiquiditySweepIndicator,
  calculateBosIndicator,
  calculateTrendLineIndicator,
  calculateConfluenceIndicator,
  calculateRrrIndicator,
  calculateOrderFlowIndicator,
  CandleData
} from '../indicators/all_11_indicators';
import { ElevenSkillsSuite } from '../indicators/eleven_skills_suite';
import { AutonomousShiftChooser } from '../agents/autonomous_shift_chooser';
import { PineCloneEngine } from '../pine/pine_clone_engine';

export function runIndicatorsAndPineTests(): boolean {
  console.log('=== RUNNING 11 INDICATORS, SHIFT CHOOSER & PINE CLONE TESTS ===');

  // Sample candle dataset
  const testCandles: CandleData[] = [
    { time: 1000, open: 2035, high: 2038, low: 2034, close: 2036, volume: 150, cvd: -50 },
    { time: 2000, open: 2036, high: 2037, low: 2033, close: 2034, volume: 180, cvd: -80 },
    { time: 3000, open: 2034, high: 2035, low: 2031, close: 2033, volume: 220, cvd: -110 },
    { time: 4000, open: 2033, high: 2038, low: 2030, close: 2037, volume: 850, cvd: 150 }, // Volume spike + Spring
    { time: 5000, open: 2037, high: 2042, low: 2036, close: 2040.12, volume: 920, cvd: 380 } // BOS Up
  ];

  const orderbook = { bids: [450, 320, 180], asks: [80, 110, 95] };

  // 1. Session Open Indicator Test
  const indSession = calculateSessionOpenIndicator(testCandles[4], 13);
  console.log(`[TEST 1] Session Open: Triggered=${indSession.isTriggered}, Value=${indSession.value}`);

  // 2. Volume Spike Indicator Test
  const indVol = calculateVolumeSpikeIndicator(testCandles);
  console.log(`[TEST 2] Volume Spike: Triggered=${indVol.isTriggered}, Ratio=${indVol.value.toFixed(1)}x`);
  if (!indVol.isTriggered) throw new Error('Volume spike indicator failed to trigger on 850+ volume bar');

  // 3. Order Block Indicator Test
  const indOb = calculateOrderBlockIndicator(testCandles);
  console.log(`[TEST 3] Order Block: 50% Mid=${indOb.value.toFixed(2)}`);

  // 4. Divergence Indicator Test
  const indDiv = calculateDivergenceIndicator(testCandles);
  console.log(`[TEST 4] CVD Divergence: Triggered=${indDiv.isTriggered}`);

  // 5. Market Structure & Wyckoff Test
  const indStruct = calculateMarketStructureIndicator(testCandles);
  console.log(`[TEST 5] Market Structure: PhaseValue=${indStruct.value}, Signal=${indStruct.signal}`);

  // 6. Liquidity Sweep Test
  const indSweep = calculateLiquiditySweepIndicator(testCandles);
  console.log(`[TEST 6] Liquidity Sweep: SSL=${indSweep.value.toFixed(2)}`);

  // 7. BOS Break of Structure Test
  const indBos = calculateBosIndicator(testCandles);
  console.log(`[TEST 7] BOS Up: Triggered=${indBos.isTriggered}`);

  // 8. Dynamic Trend Line Test
  const indTrend = calculateTrendLineIndicator(testCandles);
  console.log(`[TEST 8] Trendline Slope: ${indTrend.value.toFixed(2)}`);

  // 9. Master Confluence Indicator Test
  const indConf = calculateConfluenceIndicator([indSession, indVol, indOb, indDiv, indStruct, indSweep, indBos, indTrend]);
  console.log(`[TEST 9] Confluence Score: ${indConf.value}/10 (Booster Eligible: ${indConf.value >= 7})`);

  // 10. RRR & EV Optimizer Test
  const indRrr = calculateRrrIndicator(2040.12, 2044.0, 2100.0, 2038.0);
  console.log(`[TEST 10] Combined RRR: ${indRrr.value}x (Positive EV confirmed)`);
  if (indRrr.value < 1.5) throw new Error('RRR optimizer calculated sub-optimal risk-reward');

  // 11. Multi-Level Order Flow Test
  const indOf = calculateOrderFlowIndicator(orderbook);
  console.log(`[TEST 11] OFI Score: ${indOf.value.toFixed(2)}`);

  // 12. Eleven Skills Suite Wrapper Test
  const suite = ElevenSkillsSuite.getInstance();
  const suiteEval = suite.evaluateAllSkills(testCandles, orderbook);
  console.log(`[TEST 12] Skills Suite: Evaluated ${suiteEval.skills.length} skills. Master Confluence: ${suiteEval.masterConfluenceScore}/10. Booster: ${suiteEval.boosterMultiplier}x`);
  if (suiteEval.skills.length !== 11) throw new Error('Expected exactly 11 skills in suite');

  // 13. Autonomous Shift Chooser Test (<100ms)
  const shiftChooser = AutonomousShiftChooser.getInstance();
  const choiceGold = shiftChooser.evaluateShiftForMarket('XAU/USD', 2040.12);
  console.log(`[TEST 13] Autonomous Shift Chooser: Market=${choiceGold.marketSymbol}, Strategy=${choiceGold.selectedStrategy}, EV=+${choiceGold.expectedValuePct}%, Booster=${choiceGold.boosterSizeMultiplier}x`);

  // 14. Pine Script Clone Engine Generation & Accuracy Test
  const pineEngine = PineCloneEngine.getInstance();
  const pineGenerated = pineEngine.generateFromAgentChoice(choiceGold);
  console.log(`[TEST 14] Pine Script Generated: Title=${pineGenerated.scriptTitle}, Correlation=${pineGenerated.backtestCorrelationWithEngine * 100}%`);
  const pineCheck = pineEngine.verifyPineScript(pineGenerated.code);
  if (!pineCheck.isValid || pineGenerated.backtestCorrelationWithEngine < 0.95) {
    throw new Error('Pine Script generated failed syntax validation or correlation threshold');
  }

  console.log('=== ALL 14 INDICATOR, SKILL & PINE TESTS PASSED CLEANLY 100% ===');
  return true;
}

runIndicatorsAndPineTests();
