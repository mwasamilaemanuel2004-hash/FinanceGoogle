/**
 * ESMH.TRADE — RESEND EMAIL API ROUTE
 * Path: /api/send-email/route.ts
 *
 * Serverless / HTTP handler for email dispatches.
 * Validates request payload and triggers EmailNotificationSystem.
 */

import { EmailNotificationSystem } from '../../lib/notifications/email_system';

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { to, subject, html, apiKey, alertType, data } = body;

    const emailSystem = EmailNotificationSystem.getInstance();
    if (apiKey) {
      emailSystem.setApiKey(apiKey);
    }

    if (alertType && data) {
      switch (alertType) {
        case 'TRADE':
          const tradeRes = await emailSystem.sendTradeAlert(data, to);
          return new Response(JSON.stringify(tradeRes), {
            status: tradeRes.success ? 200 : tradeRes.statusCode || 400,
            headers: { 'Content-Type': 'application/json' },
          });
        case 'RISK':
          const riskRes = await emailSystem.sendRiskAlert(data, to);
          return new Response(JSON.stringify(riskRes), {
            status: riskRes.success ? 200 : riskRes.statusCode || 400,
            headers: { 'Content-Type': 'application/json' },
          });
        case 'HEALTH':
          const healthRes = await emailSystem.sendSystemHealthAlert(data, to);
          return new Response(JSON.stringify(healthRes), {
            status: healthRes.success ? 200 : healthRes.statusCode || 400,
            headers: { 'Content-Type': 'application/json' },
          });
        case 'DAILY':
          const dailyRes = await emailSystem.sendDailyReport(data, to);
          return new Response(JSON.stringify(dailyRes), {
            status: dailyRes.success ? 200 : dailyRes.statusCode || 400,
            headers: { 'Content-Type': 'application/json' },
          });
        case 'WEEKLY':
          const weeklyRes = await emailSystem.sendWeeklyReport(data, to);
          return new Response(JSON.stringify(weeklyRes), {
            status: weeklyRes.success ? 200 : weeklyRes.statusCode || 400,
            headers: { 'Content-Type': 'application/json' },
          });
        case 'LEARNING':
          const learnRes = await emailSystem.sendLearningAlert(data, to);
          return new Response(JSON.stringify(learnRes), {
            status: learnRes.success ? 200 : learnRes.statusCode || 400,
            headers: { 'Content-Type': 'application/json' },
          });
        case 'FORECAST':
          const forecastRes = await emailSystem.sendForecastAlert(data, to);
          return new Response(JSON.stringify(forecastRes), {
            status: forecastRes.success ? 200 : forecastRes.statusCode || 400,
            headers: { 'Content-Type': 'application/json' },
          });
      }
    }

    // Generic email dispatch
    const result = await emailSystem.sendEmail({
      to,
      subject,
      html,
    });

    return new Response(JSON.stringify(result), {
      status: result.success ? 200 : result.statusCode || 400,
      headers: { 'Content-Type': 'application/json' },
    });
  } catch (error: any) {
    return new Response(
      JSON.stringify({
        success: false,
        error: error?.message || 'Internal server error in send-email API',
      }),
      {
        status: 500,
        headers: { 'Content-Type': 'application/json' },
      }
    );
  }
}
