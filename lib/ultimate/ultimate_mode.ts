import { masterHub } from '../hub';

export interface UserTokenRecord {
  platformId: string;
  tokenEncrypted: string;
  isActive: boolean;
  addedAt: string;
}

export class UltimateModeManager {
  private static instance: UltimateModeManager;
  private userTokens: Map<string, UserTokenRecord> = new Map();

  private constructor() {}

  public static getInstance(): UltimateModeManager {
    if (!UltimateModeManager.instance) {
      UltimateModeManager.instance = new UltimateModeManager();
    }
    return UltimateModeManager.instance;
  }

  public saveToken(platformId: string, token: string): boolean {
    if (!token || token.trim().length === 0) return false;
    
    // Encrypt or store token
    this.userTokens.set(platformId, {
      platformId,
      tokenEncrypted: Buffer.from(token).toString('base64'),
      isActive: true,
      addedAt: new Date().toISOString(),
    });

    // Update Hub
    masterHub.updateToken(platformId, token);
    return true;
  }

  public getUnlockedStatus() {
    const activeTokens = Array.from(this.userTokens.values()).filter(t => t.isActive).length;
    const isUnlocked = activeTokens >= 3;
    return {
      activeTokens,
      requiredTokens: 3,
      isUnlocked,
      tier: isUnlocked ? 'ULTIMATE_GOD_MODE' : 'FREE_COMMUNITY_MODE',
      accessibleAgents: isUnlocked ? 12 : 3,
    };
  }
}

export const ultimateManager = UltimateModeManager.getInstance();
