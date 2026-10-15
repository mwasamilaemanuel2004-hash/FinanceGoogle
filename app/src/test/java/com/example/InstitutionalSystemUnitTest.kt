package com.example

import com.example.data.model.*
import com.example.engine.*
import com.example.util.SecurityCryptoManager
import org.junit.Assert.*
import org.junit.Test

/**
 * ============================================================================
 * INSTITUTIONAL SYSTEM UNIT & INTEGRATION TEST SUITE
 * ESMH.TRADE / MWASA AI
 * ============================================================================
 */
class InstitutionalSystemUnitTest {

    @Test
    fun testCanonicalSymbolMapping() {
        assertEquals("BTC/USDT", CanonicalSymbolEngine.resolveCanonicalSymbol("BTCUSDT"))
        assertEquals("BTC/USDT", CanonicalSymbolEngine.resolveCanonicalSymbol("BTC-USDT", "OKX"))
        assertEquals("BTC/USDT", CanonicalSymbolEngine.resolveCanonicalSymbol("XBTUSDT"))
        assertEquals("EUR/USD", CanonicalSymbolEngine.resolveCanonicalSymbol("EURUSD"))
        assertEquals("EUR/USD", CanonicalSymbolEngine.resolveCanonicalSymbol("EURUSDm", "Exness"))
        assertEquals("EUR/USD", CanonicalSymbolEngine.resolveCanonicalSymbol("frxEURUSD", "Deriv"))
        assertEquals("XAU/USD", CanonicalSymbolEngine.resolveCanonicalSymbol("GOLD"))
        assertEquals("XAU/USD", CanonicalSymbolEngine.resolveCanonicalSymbol("XAUUSD"))
        assertEquals("VOLATILITY 75", CanonicalSymbolEngine.resolveCanonicalSymbol("R_75", "Deriv"))
        assertEquals("CRASH 1000", CanonicalSymbolEngine.resolveCanonicalSymbol("CRASH_1000"))
        assertEquals("NVDA", CanonicalSymbolEngine.resolveCanonicalSymbol("NVDA.US"))
        assertEquals("US500", CanonicalSymbolEngine.resolveCanonicalSymbol("SPX500"))

        val btcSpec = CanonicalSymbolEngine.getInstrument("BTC/USDT")
        assertNotNull(btcSpec)
        assertEquals(MarketType.CRYPTO, btcSpec?.assetClass)

        // Quantity normalization test
        val normalizedQty = CanonicalSymbolEngine.normalizeQuantity("BTC/USDT", 0.123456)
        assertEquals(0.123, normalizedQty, 0.0001)

        // Price normalization test
        val normalizedPrice = CanonicalSymbolEngine.normalizePrice("EUR/USD", 1.0845678)
        assertEquals(1.08457, normalizedPrice, 0.00001)
    }

    @Test
    fun testDataQualityEngineAuditAndGating() {
        val symbol = "BTC/USDT"

        // Ingest healthy real tick
        val record = DataQualityEngine.validateAndIngestTick(
            source = "Binance Official REST",
            symbol = symbol,
            price = 91450.0,
            bid = 91448.0,
            ask = 91452.0,
            volume = 15000.0,
            latencyMs = 32L,
            classification = DataClassification.REAL_LIVE_DATA
        )

        assertEquals(DataHealthStatus.HEALTHY, record.dataQuality)
        assertEquals(DataFreshnessStatus.REALTIME_LIVE, record.freshnessStatus)

        val (permitted, _) = DataQualityEngine.isLiveTradingPermittedForSymbol(symbol)
        assertTrue(permitted)

        // Test abnormal / corrupted price (<= 0)
        val corrupted = DataQualityEngine.validateAndIngestTick(
            source = "Unverified Provider",
            symbol = "BAD/SYMBOL",
            price = -50.0,
            classification = DataClassification.REAL_LIVE_DATA
        )
        assertEquals(DataHealthStatus.OFFLINE, corrupted.dataQuality)
    }

    @Test
    fun testMarketStatusAndTradingHours() {
        // Crypto should always be open 24/7/365
        val (cryptoOpen, _) = MarketStatusEngine.isMarketOpenForLiveTrading("BTC/USDT")
        assertTrue(cryptoOpen)

        // Deriv synthetic should always be open
        val (derivOpen, _) = MarketStatusEngine.isMarketOpenForLiveTrading("VOLATILITY 75")
        assertTrue(derivOpen)
    }

    @Test
    fun testOrderBookAndTradeFlowCalculations() {
        val bids = listOf(Pair(91450.0, 2.5), Pair(91440.0, 5.0), Pair(91430.0, 10.0))
        val asks = listOf(Pair(91460.0, 2.0), Pair(91470.0, 4.0), Pair(91480.0, 8.0))

        val ob = OrderBookAndTradeFlowEngine.processRealOrderBook("BTC/USDT", bids, asks, isRealStream = true)
        assertEquals(91450.0, ob.bestBid, 0.01)
        assertEquals(91460.0, ob.bestAsk, 0.01)
        assertEquals(91455.0, ob.midPrice, 0.01)
        assertTrue(ob.spreadBps > 0)
        assertEquals("REAL_EXCHANGE_L2_STREAM", ob.orderFlowStatus)

        // Trade flow CVD calculation
        val flow = OrderBookAndTradeFlowEngine.processTradeStream("BTC/USDT", 45, 1_500_000.0, 800_000.0)
        assertEquals(700_000.0, flow.cumulativeVolumeDeltaUsd, 0.01)
        assertEquals("INSTITUTIONAL_BUY_ABSORPTION", flow.tradeFlowState)
    }

    @Test
    fun testLsfLseSpeedRateMetrics() {
        val candles = (1..20).map { i ->
            PriceCandle(
                timestamp = System.currentTimeMillis() - (20 - i) * 60000L,
                open = 100.0 + i,
                high = 102.0 + i,
                low = 99.0 + i,
                close = 101.5 + i,
                volume = 5000.0 + (i * 200)
            )
        }

        val speed = LsfLseSpeedRateEngine.calculateSpeedRate("BTC/USDT", 121.5, candles)
        assertTrue(speed.speedScore in 0..100)
        assertTrue(speed.liquiditySweepFrequency >= 0.0)
        assertTrue(speed.liquiditySqueezeExpansionRatio > 0.0)
    }

    @Test
    fun testThreeAgentConsensusAndSafetyGate() {
        val system = CentralThreeAgentSystem.getInstance()

        val dummyTick = MarketTick("BTC/USDT", MarketType.CRYPTO, 91500.0, 2.5, 92000.0, 89000.0, 48000.0, 56.0)
        val dummyCandles = (1..30).map { i ->
            PriceCandle(System.currentTimeMillis() - (30 - i) * 60000L, 90000.0 + i * 50, 90100.0 + i * 50, 89900.0 + i * 50, 90050.0 + i * 50, 2000.0)
        }

        val eval = system.processCycle(
            symbol = "BTC/USDT",
            tick = dummyTick,
            candles = dummyCandles,
            userCapitalUsd = 10000.0,
            dailyDrawdownPct = 0.5,
            killSwitchActive = false
        )

        assertNotNull(eval)
        assertTrue(eval.consensusScorePct in 0..100)
        assertNotNull(eval.decision)

        // Test Emergency Kill Switch Override
        val emergencyEval = system.processCycle(
            symbol = "BTC/USDT",
            tick = dummyTick,
            candles = dummyCandles,
            userCapitalUsd = 10000.0,
            dailyDrawdownPct = 0.5,
            killSwitchActive = true
        )
        assertEquals(SupervisorDecision.EMERGENCY_MODE, emergencyEval.decision)
        assertNull(emergencyEval.approvedSide)
    }

    @Test
    fun testHardRiskGovernorLimits() {
        val governor = HardRiskGovernor()
        assertEquals(5.0, governor.telemetry.value.maxDailyDrawdownPct, 0.01)

        // Test emergency halt trigger
        governor.triggerEmergencyHalt("Test Circuit Breaker")
        assertTrue(governor.telemetry.value.emergencyStopActive)
        assertTrue(governor.telemetry.value.isCircuitBreakerTripped)

        governor.resetEmergencyHalt()
        assertFalse(governor.telemetry.value.emergencyStopActive)
    }

    @Test
    fun testSecurityCryptoManager() {
        val plainApiKey = "binance_live_api_key_secret_xyz12345"
        val encrypted = SecurityCryptoManager.encrypt(plainApiKey)
        assertNotEquals(plainApiKey, encrypted)

        val decrypted = SecurityCryptoManager.decrypt(encrypted)
        assertEquals(plainApiKey, decrypted)

        val masked = SecurityCryptoManager.maskKey(plainApiKey)
        assertTrue(masked.contains("••••"))
        assertFalse(masked.contains("secret_xyz"))
    }

    @Test
    fun testAgenticSkillsEngineAndIndicators() {
        val engine = AgenticSkillsEngine.getInstance()
        val allSkills = engine.skillsList.value
        assertTrue(allSkills.size >= 50)

        // 1. 25 Strategy Skills (Image 1)
        val stratSkills = allSkills.filter { it.family == "STRATEGY_25" }
        assertEquals(25, stratSkills.size)
        assertTrue(stratSkills.any { it.name.contains("SMC") })
        assertTrue(stratSkills.any { it.name.contains("ICT") })
        assertTrue(stratSkills.any { it.name.contains("BOS") })
        assertTrue(stratSkills.any { it.name.contains("CHoCH") })
        assertTrue(stratSkills.any { it.name.contains("FVG") })
        assertTrue(stratSkills.any { it.name.contains("OB") })
        assertTrue(stratSkills.any { it.name.contains("Liquidity") })
        assertTrue(stratSkills.any { it.name.contains("MSS") })
        assertTrue(stratSkills.any { it.name.contains("RR") })
        assertTrue(stratSkills.any { it.name.contains("Algorithmic") })

        // 2. 6-Level Iceberg Deep Formation (Image 2)
        val icebergSkills = allSkills.filter { it.family == "ICEBERG_DEEP" }
        assertTrue(icebergSkills.size >= 20)
        assertTrue(icebergSkills.any { it.levelNumber == 1 })
        assertTrue(icebergSkills.any { it.levelNumber == 2 })
        assertTrue(icebergSkills.any { it.levelNumber == 3 })
        assertTrue(icebergSkills.any { it.levelNumber == 4 })
        assertTrue(icebergSkills.any { it.levelNumber == 5 })
        assertTrue(icebergSkills.any { it.levelNumber == 6 })

        // 3. 5 Quantum Agent Capabilities
        val quantumSkills = allSkills.filter { it.family == "QUANTUM_ROLE" }
        assertEquals(5, quantumSkills.size)

        // 4. Statistical Chance Handler
        val chanceState = engine.evaluateChanceAnomaly(1.8, 1.15)
        assertTrue(chanceState.isAnomalyDetected)
        assertTrue(chanceState.isTradePermitted)
        assertEquals(0.50, chanceState.sizingFactor, 0.001)

        // 5. Autonomous Method Inventor
        val invented = engine.inventNewMethod("XAU/USD")
        assertTrue(invented.name.isNotEmpty())
        assertTrue(invented.testedSharpe > 1.30)
        assertTrue(invented.testedPboPct < 30.0)

        // 6. Institutional Indicator: 7 Components
        val instReport = engine.evaluateInstitutionalFlow("BTC/USDT")
        assertEquals("BUY", instReport.signal)
        assertTrue(instReport.latencyMs < 20.0)
        assertTrue(instReport.cvdDeltaUsd > 0.0)
        assertTrue(instReport.dixDarkPoolPct > 50.0)
        assertTrue(instReport.hasCvdDivergence)
        assertEquals("ACCUMULATION_SPRING_PHASE_C", instReport.wyckoffPhase)

        // 7. Quantum Indicator: 7 Components
        val quantReport = engine.evaluateQuantumFinance("BTC/USDT")
        assertEquals("BUY", quantReport.signal)
        assertTrue(quantReport.latencyMs < 25.0)
        assertTrue(quantReport.probBuy > 0.50)
        assertEquals(1000, quantReport.simulatedPathsCount)
        assertEquals(40, quantReport.dominantCyclePeriodDays)
        assertTrue(quantReport.quantumTunnelingProb > 0.50)
    }

    @Test
    fun testCognitiveThinkingAndWeakProfitManeuvers() {
        val thinking = CognitiveThinkingEngine.getInstance()

        // 1. Chain-of-Thought (5 steps, latency < 20ms, isolated sub-trade)
        val thought = thinking.think("XAU/USD", 2040.0)
        assertEquals(5, thought.steps.size)
        assertEquals("Perceive", thought.steps[0].stepName)
        assertEquals("Recognize Pattern", thought.steps[1].stepName)
        assertEquals("Differentiate", thought.steps[2].stepName)
        assertEquals("Decide Maneuver", thought.steps[3].stepName)
        assertEquals("Learn & Evolve", thought.steps[4].stepName)

        assertTrue(thought.totalLatencyMs < 25.0)
        assertEquals("SELL", thought.primaryMacroBias) // Main macro logic preserved
        assertTrue(thought.weakProfitPermitted)
        assertEquals("BUY", thought.subTradeSignal)   // Isolated micro-scalp
        assertEquals(0.08, thought.subTradeReturnPct, 0.001)
        assertEquals(0.30, thought.subTradeKellyFactor, 0.001)
        assertTrue(thought.isIsolationStrict)

        // 2. Differentiate Weak Profit vs Loss Hazard
        // Valid Weak Profit: High DOM imbalance in LVN void
        val weakProfitEval = thinking.classifyOpportunity(
            vpinToxicity = 0.20,
            isSpoofingActive = false,
            domImbalancePct = 72.0,
            inLiquidityVoid = true
        )
        assertEquals("WEAK_PROFIT", weakProfitEval.type)
        assertTrue(weakProfitEval.isSubTradeApproved)
        assertTrue(weakProfitEval.expectedValuePct > 0.0)
        assertEquals(0.30, weakProfitEval.recommendedSizeFactor, 0.001)

        // Loss Trap: Toxic predator flow VPIN > 0.65
        val hazardEval = thinking.classifyOpportunity(
            vpinToxicity = 0.72,
            isSpoofingActive = true,
            domImbalancePct = 40.0,
            inLiquidityVoid = false
        )
        assertEquals("LOSS_HAZARD", hazardEval.type)
        assertFalse(hazardEval.isSubTradeApproved)
        assertEquals(0.0, hazardEval.recommendedSizeFactor, 0.001)

        // 3. Adaptive Maneuver within Immutable Boundaries
        val maneuver = thinking.performManeuver(82.0)
        assertEquals(0.60, maneuver.sizeFactor, 0.001)
        assertTrue(maneuver.immutableLimitsRespected)

        // 4. Big Results Dashboard State
        val bigResults = thinking.bigResultsState.value
        assertEquals(67, bigResults.weakProfitsCount)
        assertTrue(bigResults.weakProfitsCumulativeReturnPct > 4.0)
        assertEquals(3, bigResults.majorTrapsBlockedCount)
        assertEquals(8, bigResults.activeWorkerPool)
        assertTrue(bigResults.cacheHitRatioPct > 80.0)
    }

    @Test
    fun testHybridReplacementAndAntiLossEngine() {
        val hybrid = HybridReplacementEngine.getInstance()

        // 1. Scenario A: Both Legs Win (+0.765% net vs old 0.06%)
        val sessionA = hybrid.simulateScenarioA(2040.0)
        assertEquals("CLOSED_PROFIT", sessionA.weakLeg.status)
        assertEquals("CLOSED_PROFIT", sessionA.strongLeg.status)
        assertEquals(0.765, sessionA.netReturnPct, 0.001)
        assertTrue(sessionA.replacementActive)
        assertEquals(2040.0, sessionA.strongLeg.stopLossPrice, 0.01) // SL moved to BE

        // 2. Scenario B: Weak Win + Strong Break-Even (+0.04% net, zero loss)
        val sessionB = hybrid.simulateScenarioB(2040.0)
        assertEquals("CLOSED_PROFIT", sessionB.weakLeg.status)
        assertEquals("CLOSED_BREAKEVEN", sessionB.strongLeg.status)
        assertEquals(0.04, sessionB.netReturnPct, 0.001)

        // 3. Scenario C: Weak Loss + Strong Win (+0.475% net)
        val sessionC = hybrid.simulateScenarioC(2040.0)
        assertEquals("CLOSED_SL", sessionC.weakLeg.status)
        assertEquals("CLOSED_PROFIT", sessionC.strongLeg.status)
        assertEquals(0.475, sessionC.netReturnPct, 0.001)

        // 4. Scenario D: Both Loss Anti-Loss (-0.275% vs old -0.50% -> 45% less loss)
        val sessionD = hybrid.simulateScenarioD(2040.0)
        assertEquals("CLOSED_SL", sessionD.weakLeg.status)
        assertEquals("CLOSED_SL", sessionD.strongLeg.status)
        assertEquals(-0.275, sessionD.netReturnPct, 0.001)
        assertTrue(sessionD.netReturnPct > -0.30) // Bound by 45% anti-loss guard

        // 5. Telemetry & High-Impact Compounding
        val telemetry = hybrid.telemetryState.value
        assertEquals(12, telemetry.totalHybridTrades)
        assertTrue(telemetry.weakLegWinRatePct > 60.0)
        assertTrue(telemetry.strongLegWinRatePct > 50.0)
        assertTrue(telemetry.netAverageReturnPerTradePct > 0.40)
        assertEquals(7.8, telemetry.improvementMultiplierVsWeakOnly, 0.1)
        assertEquals(5, telemetry.lossesPreventedByReplacementCount)
        assertEquals(1.25, telemetry.capitalSavedPct, 0.01)
    }

    @Test
    fun testDeepThinkingV2TwelveStepsAndMetacognition() {
        val thinking = CognitiveThinkingEngine.getInstance()

        // 1. Full 12-Step Chain-of-Thought with Confluence
        val deepResult = thinking.deepThink("XAU/USD", 2040.12, hasPattern = true)
        assertEquals(12, deepResult.steps.size)
        assertEquals("BUY", deepResult.action)
        assertEquals("HYBRID_REPLACEMENT", deepResult.executionMode)
        assertEquals("4/4", deepResult.confluenceScore)
        assertTrue(deepResult.confluenceConfidencePct >= 80.0)
        assertEquals(1.5, deepResult.allocatedSizeMultiplier, 0.01)
        assertEquals(82, deepResult.marketQualityScore)
        assertTrue(deepResult.totalLatencyMs <= 40.0)

        // Verify individual steps 1 through 12
        assertEquals("PERCEIVE", deepResult.steps[0].stepName)
        assertEquals("RECOGNIZE_QP", deepResult.steps[1].stepName)
        assertEquals("RECOGNIZE_OF", deepResult.steps[2].stepName)
        assertEquals("RECOGNIZE_IP", deepResult.steps[3].stepName)
        assertEquals("RECOGNIZE_HP", deepResult.steps[4].stepName)
        assertEquals("MERGE_CONFLUENCE", deepResult.steps[5].stepName)
        assertEquals("DIFFERENTIATE", deepResult.steps[6].stepName)
        assertEquals("RESEARCH_MACRO", deepResult.steps[7].stepName)
        assertEquals("FOCUS_BOOSTER", deepResult.steps[8].stepName)
        assertEquals("DECIDE_HYBRID", deepResult.steps[9].stepName)
        assertEquals("METACOGNITION", deepResult.steps[10].stepName)
        assertEquals("ACT_LEARN_LOG", deepResult.steps[11].stepName)

        // 2. Flat Chop without Pattern -> MUST HOLD
        val holdResult = thinking.deepThink("XAU/USD", 2040.0, hasPattern = false)
        assertEquals("HOLD", holdResult.action)
        assertEquals("HOLD", holdResult.executionMode)
        assertEquals("0/4", holdResult.confluenceScore)
        assertEquals(0.0, holdResult.allocatedSizeMultiplier, 0.01)

        // 3. Metacognition Bias Check
        val meta = thinking.metacognitionReport.value
        assertTrue(meta.biasPassed)
        assertEquals("PASS_OBJECTIVE", meta.biasVerdict)
        assertTrue(meta.pboProbabilityBacktestOverfittingPct < 30.0)
        assertTrue(meta.dsrDeflatedSharpeRatio > 0.0)
        assertEquals(2035.0, meta.predefinedInvalidationPrice, 0.01)
        assertTrue(meta.deepThinkingHistoricalWinRatePct > meta.shallowThinkingHistoricalWinRatePct)

        // 4. Focus Allocation V2
        val focus = thinking.focusReport.value
        assertEquals("XAU/USD", focus.topRankedSymbol)
        assertEquals(40.0, focus.topRankedAllocationPct, 0.01)
        assertTrue(focus.allocations.size >= 5)
        assertEquals(1, focus.allocations[0].rank)
        assertTrue(focus.allocations[0].isBeautifulMarket)
        assertEquals(1.5, focus.allocations[0].maxAllowedSizeFactor, 0.01)

        // 5. Self-Growth V2
        val growth = thinking.selfGrowthReport.value
        assertTrue(growth.totalRegisteredSkillsCount >= 85)
        assertTrue(growth.lastInventedSkillName.contains("Void_Absorption_Tunneling"))
        assertTrue(growth.lastInventedSharpe > 1.30)
        assertTrue(growth.deepThinkingUsageRatioPct >= 75.0)
    }

    @Test
    fun testSingleUserPersonalizedTrainingEngine() {
        val training = SingleUserTrainingEngine.getInstance()
        val profile = training.userProfile.value

        assertEquals("usr_sovereign_001", profile.userId)
        assertEquals("annambise7819@gmail.com", profile.email)
        assertEquals(1.5, profile.maxRiskPerTradePct, 0.01)
        assertEquals(5.0, profile.maxDailyDrawdownTolerancePct, 0.01)
        assertEquals(100.0, profile.dedicatedComputeResourcesPct, 0.01)
        assertTrue(profile.preferredMarkets.contains("R_75"))
        assertTrue(profile.preferredMarkets.contains("JUMP_100"))
        assertTrue(profile.preferredMarkets.contains("CRASH_500"))
        assertTrue(profile.timezone.contains("Arusha"))

        val trained = training.startPersonalizedTraining()
        assertTrue(trained.isEntrySystemTrained)
        assertEquals(100, trained.trainingProgressPct)
        assertEquals(50000000L, trained.ticksScannedCount)
        assertTrue(trained.achievedSharpeRatio >= 1.30)
        assertTrue(trained.personalizedSkills.size >= 4)
    }

    @Test
    fun testDerivAllMarketsAndDigitsEngine() {
        val deriv = DerivAllMarketsEngine.getInstance()
        val markets = deriv.instruments.value
        assertTrue(markets.size >= 25)

        // Verify key categories present
        assertTrue(markets.any { it.symbol == "R_75" && it.category == "CONTINUOUS" })
        assertTrue(markets.any { it.symbol == "1HZ100V" && it.category == "CONTINUOUS_1S" })
        assertTrue(markets.any { it.symbol == "JUMP_100" && it.category == "JUMP" })
        assertTrue(markets.any { it.symbol == "CRASH_500" && it.category == "CRASH_BOOM" })
        assertTrue(markets.any { it.symbol == "BOOM_1000" && it.category == "CRASH_BOOM" })

        // Digits Engine Statistical Bias Test
        val digitReport = deriv.evaluateDigits("R_75")
        assertEquals(1000, digitReport.sampleSizeTicks)
        assertTrue(digitReport.evenCount > digitReport.oddCount)
        assertTrue(digitReport.evenZScore >= 2.0) // Statistically significant bias
        assertEquals("DIGIT_EVEN", digitReport.recommendedContract)
        assertTrue(digitReport.expectedValuePct > 0.0)
        assertTrue(digitReport.latencyMs < 15.0)

        // Regime and Contract Evaluation
        val contractEval = deriv.latestContractEval.value
        assertEquals("RISE_FALL", contractEval.contractType)
        assertEquals("RISE", contractEval.recommendedSide)
        assertEquals("HYBRID_REPLACEMENT", contractEval.executionMode)
        assertTrue(contractEval.regimeAnalysis.isTunnelingBreakoutImminent)
        assertEquals(78.0, contractEval.regimeAnalysis.tunnelingBreakoutProbPct, 0.1)
    }

    @Test
    fun testSelfLearningNoCopyGuard() {
        val selfLearning = SelfLearningNoCopyEngine.getInstance()

        // 1. Autonomous Self Trade
        val autoVerdict = selfLearning.evaluateSignalSafety(
            isExternalSignal = false,
            selfConfluenceCount = 4,
            expectedValuePct = 0.85
        )
        assertFalse(autoVerdict.isCopyAttempt)
        assertFalse(autoVerdict.isBlocked)

        // 2. External Copy Signal with Negative EV -> MUST BLOCK
        val blockNegativeEv = selfLearning.evaluateSignalSafety(
            isExternalSignal = true,
            selfConfluenceCount = 3,
            expectedValuePct = -0.15
        )
        assertTrue(blockNegativeEv.isCopyAttempt)
        assertTrue(blockNegativeEv.isBlocked)
        assertTrue(blockNegativeEv.verdictMessage.contains("ANTI-COPY GUARD"))

        // 3. External Copy Signal with Low Self Confluence (< 2/4) -> MUST BLOCK
        val blockLowConfluence = selfLearning.evaluateSignalSafety(
            isExternalSignal = true,
            selfConfluenceCount = 1,
            expectedValuePct = 0.40
        )
        assertTrue(blockLowConfluence.isCopyAttempt)
        assertTrue(blockLowConfluence.isBlocked)

        // 4. Learning from verified trade updates Sharpe
        val updated = selfLearning.learnFromVerifiedTrade(0.08)
        assertTrue(updated.verifiedOwnTradesCount >= 88)
        assertTrue(updated.currentSharpeRatio >= 1.50)
        assertTrue(updated.isCopyTradingStrictlyBlocked)
    }
}



