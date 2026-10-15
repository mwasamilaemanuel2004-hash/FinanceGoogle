export type ManeuverType = 'SIZE_MANEUVER' | 'TIMING_MANEUVER' | 'VENUE_MANEUVER' | 'STRATEGY_MANEUVER' | 'INVENT_MANEUVER';

export interface ManeuverRecord {
  id: string;
  timestamp: number;
  type: ManeuverType;
  symbol: string;
  description: string;
  previousState: string;
  adaptedState: string;
  immutableBoundariesRespected: {
    maxDailyDrawdownSafe: boolean;
    varLimitSafe: boolean;
    kellyCeilingRespected: boolean; // Must be <= 2% max
    mainLogicPreserved: boolean;
  };
  metrics: Record<string, number | string>;
}

export class ManeuverLogger {
  private static instance: ManeuverLogger;
  private logs: ManeuverRecord[] = [];

  private constructor() {}

  public static getInstance(): ManeuverLogger {
    if (!ManeuverLogger.instance) {
      ManeuverLogger.instance = new ManeuverLogger();
    }
    return ManeuverLogger.instance;
  }

  public logManeuver(record: ManeuverRecord): void {
    this.logs.unshift(record);
    if (this.logs.length > 500) {
      this.logs.pop();
    }
  }

  public getRecentLogs(limit = 20): ManeuverRecord[] {
    return this.logs.slice(0, limit);
  }
}

export const maneuverLogger = ManeuverLogger.getInstance();
