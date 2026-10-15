/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — USER PROFILE (PERSONALIZED CONFIGURATION)
 * ============================================================================
 */

export interface UserTradingProfile {
  userId: string;
  maxRiskPerTradePct: number; // 0.5% - 2.0% (default 1.5%)
  maxDailyDrawdownTolerancePct: number; // 5.0% circuit breaker
  preferredMarkets: string[]; // ['R_75', 'R_100', '1HZ100V', 'JUMP_100', 'CRASH_500', 'BOOM_1000', 'XAU/USD', 'BTC/USDT']
  avoidMarketsChoppy: string[]; // e.g. ['EUR/USD_CHOPPY', 'GBP/USD_RANGE']
  timezone: string; // 'Africa/Dar_es_Salaam (Arusha +3 EAT)'
  preferredTradingHours: {
    startHourEAT: number; // 14:00
    endHourEAT: number;   // 21:00 (London/NY overlap)
  };
  isHybridReplacementEnabled: boolean;
  isDeepThinking12StepsEnabled: boolean;
  isDigitsEngineEnabled: boolean;
  beautifulMarketBoosterSize: number; // 1.5x
  personalizedTrainedSkills: string[];
  isEntrySystemTrained: boolean;
  trainingCompletedTimestamp: number;
}

export class UserProfileManager {
  private static instance: UserProfileManager;
  private profile: UserTradingProfile = {
    userId: 'usr_sovereign_001',
    maxRiskPerTradePct: 1.5,
    maxDailyDrawdownTolerancePct: 5.0,
    preferredMarkets: [
      'R_75',      // Volatility 75 Index
      'R_100',     // Volatility 100 Index
      '1HZ100V',   // Volatility 100 (1s) Index
      'JUMP_100',  // Jump 100 Index
      'CRASH_500', // Crash 500 Index
      'BOOM_1000', // Boom 1000 Index
      'XAU/USD',   // Gold
      'BTC/USDT'   // Bitcoin
    ],
    avoidMarketsChoppy: ['EUR/USD_LOW_VOL', 'USD/JPY_CHOPPY'],
    timezone: 'Africa/Dar_es_Salaam (Arusha +3 EAT)',
    preferredTradingHours: {
      startHourEAT: 14,
      endHourEAT: 21
    },
    isHybridReplacementEnabled: true,
    isDeepThinking12StepsEnabled: true,
    isDigitsEngineEnabled: true,
    beautifulMarketBoosterSize: 1.5,
    personalizedTrainedSkills: [
      'Tick_Imbalance_Vol75_v1',
      'Digit_Frequency_EvenOdd_v1',
      'Jump100_Tunneling_Breakout_v1',
      'Crash500_Mean_Reversion_v1',
      'Gold_Void_Absorption_v2'
    ],
    isEntrySystemTrained: true,
    trainingCompletedTimestamp: Date.now() - 600000
  };

  private constructor() {}

  public static getInstance(): UserProfileManager {
    if (!UserProfileManager.instance) {
      UserProfileManager.instance = new UserProfileManager();
    }
    return UserProfileManager.instance;
  }

  public getProfile(): UserTradingProfile {
    return { ...this.profile };
  }

  public updateProfile(updates: Partial<UserTradingProfile>): void {
    this.profile = { ...this.profile, ...updates };
  }
}
