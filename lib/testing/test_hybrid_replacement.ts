import { hybridReplacementEngine } from '../execution/hybrid_replacement_engine';

export interface HybridTestResult {
  testName: string;
  passed: boolean;
  actualReturnPct: number;
  expectedThresholdDescription: string;
  details: string;
}

export class HybridReplacementTestRunner {
  public static runAllTests(): { allPassed: boolean; results: HybridTestResult[] } {
    const engine = hybridReplacementEngine;
    const results: HybridTestResult[] = [];

    // 1. Test Scenario A: Both Win (Gold 2023-08-25)
    const resA = engine.simulateScenario('SCENARIO_A', 2040.0);
    const passA = resA.netRealizedReturnPct >= 0.50; // Expected +0.765%
    results.push({
      testName: 'Scenario A: Both Legs Win (Weak TP + Strong 1.45% Runner)',
      passed: passA,
      actualReturnPct: resA.netRealizedReturnPct,
      expectedThresholdDescription: 'Net return >= +0.50% (Expected +0.765%)',
      details: resA.comparisonVsOldWeakOnly
    });

    // 2. Test Scenario B: Weak Win + Strong Break-Even
    const resB = engine.simulateScenario('SCENARIO_B', 2040.0);
    const passB = resB.netRealizedReturnPct > 0.0 && resB.trade.strongLeg.status === 'CLOSED_BREAKEVEN';
    results.push({
      testName: 'Scenario B: Weak Win + Strong Break-Even (Zero Loss Guaranteed)',
      passed: passB,
      actualReturnPct: resB.netRealizedReturnPct,
      expectedThresholdDescription: 'Net return > 0.0% and Strong Leg SL at Break-Even',
      details: resB.comparisonVsOldWeakOnly
    });

    // 3. Test Scenario C: Weak Loss + Strong Win
    const resC = engine.simulateScenario('SCENARIO_C', 2040.0);
    const passC = resC.netRealizedReturnPct > 0.0; // Expected +0.475%
    results.push({
      testName: 'Scenario C: Weak Loss Overwhelmed by Strong Runner (+0.475%)',
      passed: passC,
      actualReturnPct: resC.netRealizedReturnPct,
      expectedThresholdDescription: 'Net return > 0.0% (Strong compensates weak loss)',
      details: resC.comparisonVsOldWeakOnly
    });

    // 4. Test Scenario D: Both Loss (Worst Case Anti-Loss Protection)
    const resD = engine.simulateScenario('SCENARIO_D', 2040.0);
    // Worst case loss should be -0.275%, which is 45% less than the old -0.50%
    const passD = resD.netRealizedReturnPct >= -0.30 && resD.antiLossEffectivenessPct >= 40.0;
    results.push({
      testName: 'Scenario D: Worst-Case Anti-Loss Protection (45% Loss Reduction)',
      passed: passD,
      actualReturnPct: resD.netRealizedReturnPct,
      expectedThresholdDescription: 'Net loss limited to >= -0.30% (45% loss reduction vs old -0.50%)',
      details: resD.comparisonVsOldWeakOnly
    });

    // 5. Test Replacement State Transition
    const passRepl = resA.trade.replacementActive && resA.trade.strongLeg.stopLossPrice === 2040.0;
    results.push({
      testName: 'Replacement State Transition: Strong SL moved to BE on Weak TP',
      passed: passRepl,
      actualReturnPct: 0.0,
      expectedThresholdDescription: 'replacementActive == true & strong SL == entry price 2040.0',
      details: 'Strong runner converted to 100% Risk-Free status upon micro-profit lock.'
    });

    const allPassed = results.every(r => r.passed);
    return { allPassed, results };
  }
}

// Auto-run if executed directly
if (typeof process !== 'undefined' && process.env?.NODE_ENV !== 'production') {
  const out = HybridReplacementTestRunner.runAllTests();
  // Output status
}
