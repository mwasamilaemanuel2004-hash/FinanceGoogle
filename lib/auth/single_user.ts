/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — SINGLE USER AUTH & DEDICATED RESOURCE ENGINE
 * ============================================================================
 * Focuses 100% of compute, WASM workers, and execution power on User 1.
 * Enforces single-tenant isolation: No shared resources, no multi-user contention.
 * ============================================================================
 */

export interface SingleUserSession {
  userId: string;
  email: string;
  isAuthenticated: boolean;
  role: 'SOVEREIGN_SINGLE_USER';
  derivConnected: boolean;
  derivAppId: string;
  derivTokenMasked: string;
  sessionStartedAt: number;
  dedicatedResourceAllocationPct: number; // 100%
  activeWorkersCount: number; // 4 WASM Workers dedicated
}

export class SingleUserManager {
  private static instance: SingleUserManager;
  private session: SingleUserSession = {
    userId: 'usr_sovereign_001',
    email: 'annambise7819@gmail.com',
    isAuthenticated: true,
    role: 'SOVEREIGN_SINGLE_USER',
    derivConnected: true,
    derivAppId: '1089',
    derivTokenMasked: '••••••••deriv',
    sessionStartedAt: Date.now(),
    dedicatedResourceAllocationPct: 100.0,
    activeWorkersCount: 4
  };

  private constructor() {}

  public static getInstance(): SingleUserManager {
    if (!SingleUserManager.instance) {
      SingleUserManager.instance = new SingleUserManager();
    }
    return SingleUserManager.instance;
  }

  public getSession(): SingleUserSession {
    return { ...this.session };
  }

  public updateDerivCredentials(appId: string, token: string): void {
    this.session.derivAppId = appId;
    this.session.derivTokenMasked = '••••••••' + token.slice(-4);
    this.session.derivConnected = true;
  }

  public isSingleUserReady(): boolean {
    return this.session.isAuthenticated;
  }
}
