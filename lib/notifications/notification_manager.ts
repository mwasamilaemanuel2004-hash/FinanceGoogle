/**
 * ESMH.TRADE — NOTIFICATION MANAGER ORCHESTRATOR
 * Path: /lib/notifications/notification_manager.ts
 *
 * Coordinates alerts from:
 * 1. Advance Smart Engine decisions (confidence > 70%)
 * 2. Hard Risk Engine breaches (MaxDD > 5%, VaR 99% breach, 3 losses)
 * 3. System Health & latency monitors
 * 4. Advance Intelligence regime forecast shifts
 * 5. Continuous Learning recalibrations
 * 6. Daily (18:00) & Weekly (Sunday 20:00) institutional reports
 *
 * Enforces cooldowns and rate limits to prevent alert storms.
 */

import {
  EmailNotificationSystem,
  TradeAlertData,
  SystemHealthAlertData,
  DailyReportData,
  WeeklyReportData,
  LearningAlertData,
  RiskAlertData,
  ForecastAlertData,
} from './email_system';

export interface NotificationSettings {
  enabled: boolean;
  recipientEmail: string;
  resendApiKey: string;
  enableTradeAlerts: boolean;
  minTradeConfidencePct: number; // default 70
  enableRiskAlerts: boolean;
  enableHealthAlerts: boolean;
  enableLearningAlerts: boolean;
  enableForecastAlerts: boolean;
  enableDailyReport: boolean;
  enableWeeklyReport: boolean;
}

export class NotificationManager {
  private static instance: NotificationManager;
  private emailSystem: EmailNotificationSystem;
  private settings: NotificationSettings = {
    enabled: true,
    recipientEmail: '',
    resendApiKey: '',
    enableTradeAlerts: true,
    minTradeConfidencePct: 70,
    enableRiskAlerts: true,
    enableHealthAlerts: true,
    enableLearningAlerts: true,
    enableForecastAlerts: true,
    enableDailyReport: true,
    enableWeeklyReport: true,
  };

  // Cooldown map: key -> timestamp
  private alertCooldowns: Map<string, number> = new Map();
  private readonly TRADE_ALERT_COOLDOWN_MS = 15 * 60 * 1000; // 15 mins per symbol
  private readonly HEALTH_ALERT_COOLDOWN_MS = 10 * 60 * 1000; // 10 mins per component
  private readonly RISK_ALERT_COOLDOWN_MS = 5 * 60 * 1000;   // 5 mins per risk event

  private constructor() {
    this.emailSystem = EmailNotificationSystem.getInstance();
  }

  public static getInstance(): NotificationManager {
    if (!NotificationManager.instance) {
      NotificationManager.instance = new NotificationManager();
    }
    return NotificationManager.instance;
  }

  public updateSettings(partial: Partial<NotificationSettings>): void {
    this.settings = { ...this.settings, ...partial };
    if (partial.resendApiKey !== undefined) {
      this.emailSystem.setApiKey(partial.resendApiKey);
    }
    if (partial.recipientEmail !== undefined) {
      this.emailSystem.setDefaultRecipient(partial.recipientEmail);
    }
  }

  public getSettings(): NotificationSettings {
    return { ...this.settings };
  }

  // =========================================================================
  // HANDLERS
  // =========================================================================

  public async onTradeDecision(trade: TradeAlertData): Promise<boolean> {
    if (!this.settings.enabled || !this.settings.enableTradeAlerts) return false;
    if (trade.confidencePct < this.settings.minTradeConfidencePct) return false;

    const cooldownKey = `trade_${trade.symbol}_${trade.action}`;
    const lastAlert = this.alertCooldowns.get(cooldownKey) || 0;
    if (Date.now() - lastAlert < this.TRADE_ALERT_COOLDOWN_MS) {
      return false; // suppressed by cooldown
    }

    this.alertCooldowns.set(cooldownKey, Date.now());
    const res = await this.emailSystem.sendTradeAlert(trade, this.settings.recipientEmail);
    return res.success;
  }

  public async onRiskEvent(risk: RiskAlertData): Promise<boolean> {
    if (!this.settings.enabled || !this.settings.enableRiskAlerts) return false;

    const cooldownKey = `risk_${risk.reason}`;
    const lastAlert = this.alertCooldowns.get(cooldownKey) || 0;
    if (Date.now() - lastAlert < this.RISK_ALERT_COOLDOWN_MS) {
      return false;
    }

    this.alertCooldowns.set(cooldownKey, Date.now());
    const res = await this.emailSystem.sendRiskAlert(risk, this.settings.recipientEmail);
    return res.success;
  }

  public async onHealthIssue(health: SystemHealthAlertData): Promise<boolean> {
    if (!this.settings.enabled || !this.settings.enableHealthAlerts) return false;

    const cooldownKey = `health_${health.component}_${health.severity}`;
    const lastAlert = this.alertCooldowns.get(cooldownKey) || 0;
    if (Date.now() - lastAlert < this.HEALTH_ALERT_COOLDOWN_MS) {
      return false;
    }

    this.alertCooldowns.set(cooldownKey, Date.now());
    const res = await this.emailSystem.sendSystemHealthAlert(health, this.settings.recipientEmail);
    return res.success;
  }

  public async onLearningRecalibration(learning: LearningAlertData): Promise<boolean> {
    if (!this.settings.enabled || !this.settings.enableLearningAlerts) return false;
    const res = await this.emailSystem.sendLearningAlert(learning, this.settings.recipientEmail);
    return res.success;
  }

  public async onForecastShift(forecast: ForecastAlertData): Promise<boolean> {
    if (!this.settings.enabled || !this.settings.enableForecastAlerts) return false;
    const res = await this.emailSystem.sendForecastAlert(forecast, this.settings.recipientEmail);
    return res.success;
  }

  public async dispatchDailyReport(daily: DailyReportData): Promise<boolean> {
    if (!this.settings.enabled || !this.settings.enableDailyReport) return false;
    const res = await this.emailSystem.sendDailyReport(daily, this.settings.recipientEmail);
    return res.success;
  }

  public async dispatchWeeklyReport(weekly: WeeklyReportData): Promise<boolean> {
    if (!this.settings.enabled || !this.settings.enableWeeklyReport) return false;
    const res = await this.emailSystem.sendWeeklyReport(weekly, this.settings.recipientEmail);
    return res.success;
  }

  public async sendTestEmail(targetEmail?: string): Promise<{ success: boolean; message: string }> {
    const to = targetEmail || this.settings.recipientEmail;
    if (!to) {
      return { success: false, message: 'Recipient email is not provided' };
    }
    if (!this.emailSystem.isConfigured()) {
      return { success: false, message: 'Resend API key is not configured' };
    }

    const testTrade: TradeAlertData = {
      symbol: 'BTC/USDT',
      action: 'BUY',
      confidencePct: 82.5,
      expectedReturnPct: 1.45,
      var95Pct: 0.85,
      signalsSummary: 'Advance Smart Stacking (LGBM + XGBoost + LSTM) + Ichimoku Tenkan/Kijun cross + OFI +0.42',
      price: 68420.50,
      timestamp: Date.now(),
    };

    const res = await this.emailSystem.sendTradeAlert(testTrade, to);
    if (res.success) {
      return { success: true, message: `Test email successfully dispatched to ${to} (Message ID: ${res.messageId})` };
    } else {
      return { success: false, message: `Failed to dispatch test email: ${res.error}` };
    }
  }
}
