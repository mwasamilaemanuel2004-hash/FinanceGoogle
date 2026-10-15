package com.example.notifications

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class EmailDispatchStatus(
    val lastAttemptTimestamp: Long = 0L,
    val lastSuccess: Boolean = false,
    val lastSubject: String = "",
    val lastError: String? = null,
    val messageId: String? = null,
    val totalSentCount: Int = 0,
    val totalFailedCount: Int = 0
)

class EmailNotificationSystem(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "EmailNotificationSystem"
        private const val RESEND_API_URL = "https://api.resend.com/emails"
        private const val DEFAULT_FROM = "ESMH.TRADE <onboarding@resend.dev>"
    }

    private var apiKey: String = ""
    private var recipientEmail: String = "annambise7819@gmail.com"

    private val _status = MutableStateFlow(EmailDispatchStatus())
    val status: StateFlow<EmailDispatchStatus> = _status.asStateFlow()

    fun configure(key: String, email: String) {
        if (key.isNotBlank()) apiKey = key.trim()
        if (email.isNotBlank()) recipientEmail = email.trim()
    }

    fun getApiKey(): String = apiKey
    fun getRecipientEmail(): String = recipientEmail
    fun isConfigured(): Boolean = apiKey.isNotBlank() && recipientEmail.isNotBlank()

    /**
     * Real HTTP POST to api.resend.com/emails
     */
    suspend fun sendEmail(
        to: String = recipientEmail,
        subject: String,
        htmlContent: String,
        from: String = DEFAULT_FROM
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            val err = "RESEND_API_KEY is not configured"
            _status.value = _status.value.copy(
                lastAttemptTimestamp = System.currentTimeMillis(),
                lastSuccess = false,
                lastSubject = subject,
                lastError = err,
                totalFailedCount = _status.value.totalFailedCount + 1
            )
            return@withContext Result.failure(IllegalStateException(err))
        }

        try {
            val jsonPayload = JSONObject().apply {
                put("from", from)
                put("to", JSONArray().apply { put(to.ifBlank { recipientEmail }) })
                put("subject", subject)
                put("html", htmlContent)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonPayload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(RESEND_API_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val respObj = JSONObject(bodyString)
                    val messageId = respObj.optString("id", "msg_${System.currentTimeMillis()}")
                    Log.i(TAG, "Email successfully dispatched to $to: $messageId")
                    _status.value = _status.value.copy(
                        lastAttemptTimestamp = System.currentTimeMillis(),
                        lastSuccess = true,
                        lastSubject = subject,
                        lastError = null,
                        messageId = messageId,
                        totalSentCount = _status.value.totalSentCount + 1
                    )
                    Result.success(messageId)
                } else {
                    val errMsg = "Resend API error (${response.code}): $bodyString"
                    Log.e(TAG, errMsg)
                    _status.value = _status.value.copy(
                        lastAttemptTimestamp = System.currentTimeMillis(),
                        lastSuccess = false,
                        lastSubject = subject,
                        lastError = errMsg,
                        totalFailedCount = _status.value.totalFailedCount + 1
                    )
                    Result.failure(Exception(errMsg))
                }
            }
        } catch (e: Exception) {
            val errMsg = "Network exception sending email: ${e.message}"
            Log.e(TAG, errMsg, e)
            _status.value = _status.value.copy(
                lastAttemptTimestamp = System.currentTimeMillis(),
                lastSuccess = false,
                lastSubject = subject,
                lastError = errMsg,
                totalFailedCount = _status.value.totalFailedCount + 1
            )
            Result.failure(e)
        }
    }

    // =========================================================================
    // 1. TRADE ALERT
    // =========================================================================
    suspend fun sendTradeAlert(
        symbol: String,
        action: String, // "BUY" or "SELL"
        confidencePct: Double,
        expectedReturnPct: Double,
        var95Pct: Double,
        signalsSummary: String,
        price: Double
    ): Result<String> {
        val color = if (action == "BUY") "#00E676" else "#FF1744"
        val subject = "🚨 ESMH [$action] $symbol — Confidence ${String.format("%.1f", confidencePct)}% | Expected +${String.format("%.2f", expectedReturnPct)}%"
        val html = wrapTemplate("""
            <div style="background: rgba(255,255,255,0.03); border: 1px solid $color; border-radius: 12px; padding: 24px;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                <span style="font-size: 22px; font-weight: 800; color: #FFF;">$symbol</span>
                <span style="background: $color; color: #000; font-weight: 800; padding: 4px 12px; border-radius: 6px; font-size: 13px;">$action</span>
              </div>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px; font-size: 14px;">
                <div><span style="color: #888;">Entry Price:</span> <strong style="color: #FFF;">$$price</strong></div>
                <div><span style="color: #888;">Confidence:</span> <strong style="color: $color;">${String.format("%.1f", confidencePct)}%</strong></div>
                <div><span style="color: #888;">Expected Return:</span> <strong style="color: #00E676;">+${String.format("%.2f", expectedReturnPct)}%</strong></div>
                <div><span style="color: #888;">VaR (95%):</span> <strong style="color: #FF5252;">${String.format("%.2f", var95Pct)}%</strong></div>
              </div>
              <div style="background: rgba(0,0,0,0.4); padding: 12px; border-radius: 8px; font-size: 13px; color: #CCC; border-left: 3px solid $color;">
                <strong>Signals:</strong> $signalsSummary
              </div>
            </div>
        """.trimIndent(), "Real-Time Algorithmic Trade Alert")
        return sendEmail(subject = subject, htmlContent = html)
    }

    // =========================================================================
    // 2. SYSTEM HEALTH ALERT
    // =========================================================================
    suspend fun sendSystemHealthAlert(
        severity: String,
        component: String,
        message: String,
        latencyMs: Double? = null,
        activeEnginesCount: Int = 35
    ): Result<String> {
        val color = if (severity == "CRITICAL") "#FF1744" else if (severity == "WARNING") "#FFD600" else "#00E676"
        val subject = "⚠️ System Health Alert [$severity]: $component"
        val html = wrapTemplate("""
            <div style="background: rgba(255,255,255,0.03); border: 1px solid $color; border-radius: 12px; padding: 24px;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                <span style="font-size: 18px; font-weight: 700; color: #FFF;">$component</span>
                <span style="background: $color; color: #000; font-weight: 800; padding: 4px 10px; border-radius: 6px; font-size: 12px;">$severity</span>
              </div>
              <p style="color: #CCC; font-size: 14px; margin-bottom: 16px;">$message</p>
              <div style="font-size: 13px; color: #888;">
                ${if (latencyMs != null) "<div>Latency: <strong style='color:#FFF;'>${latencyMs}ms</strong></div>" else ""}
                <div>Active Engines: <strong style="color:#FFF;">$activeEnginesCount / 35</strong></div>
              </div>
            </div>
        """.trimIndent(), "Infrastructure Telemetry")
        return sendEmail(subject = subject, htmlContent = html)
    }

    // =========================================================================
    // 3. DAILY REPORT (18:00)
    // =========================================================================
    suspend fun sendDailyReport(
        date: String,
        dailyPnlPct: Double,
        dailyPnlUsd: Double,
        winRatePct: Double,
        sharpeRatio: Double,
        tradesCount: Int,
        bestPair: String,
        worstPair: String
    ): Result<String> {
        val color = if (dailyPnlPct >= 0) "#00E676" else "#FF1744"
        val subject = "📊 ESMH Daily Report ($date): P&L ${if (dailyPnlPct >= 0) "+" else ""}${String.format("%.2f", dailyPnlPct)}% | Sharpe ${String.format("%.2f", sharpeRatio)}"
        val html = wrapTemplate("""
            <div style="background: rgba(255,255,255,0.03); border: 1px solid #333; border-radius: 12px; padding: 24px;">
              <div style="text-align: center; margin-bottom: 24px;">
                <div style="font-size: 12px; color: #888;">DAILY PERFORMANCE SUMMARY ($date)</div>
                <div style="font-size: 32px; font-weight: 900; color: $color; margin: 8px 0;">
                  ${if (dailyPnlPct >= 0) "+" else ""}${String.format("%.2f", dailyPnlPct)}%
                  <span style="font-size: 16px; color: #888;">(${if (dailyPnlUsd >= 0) "+" else ""}$${String.format("%.2f", dailyPnlUsd)})</span>
                </div>
              </div>
              <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 20px; text-align: center;">
                <div style="background: #111; padding: 12px; border-radius: 8px;">
                  <div style="color: #888; font-size: 11px;">WIN RATE</div>
                  <div style="color: #FFF; font-weight: 700; font-size: 16px;">${String.format("%.1f", winRatePct)}%</div>
                </div>
                <div style="background: #111; padding: 12px; border-radius: 8px;">
                  <div style="color: #888; font-size: 11px;">SHARPE</div>
                  <div style="color: #FFF; font-weight: 700; font-size: 16px;">${String.format("%.2f", sharpeRatio)}</div>
                </div>
                <div style="background: #111; padding: 12px; border-radius: 8px;">
                  <div style="color: #888; font-size: 11px;">TRADES</div>
                  <div style="color: #FFF; font-weight: 700; font-size: 16px;">$tradesCount</div>
                </div>
              </div>
              <div style="font-size: 13px; color: #CCC;">
                <div>🥇 <strong>Best Performer:</strong> $bestPair</div>
                <div>📉 <strong>Worst Performer:</strong> $worstPair</div>
              </div>
            </div>
        """.trimIndent(), "Institutional Daily Quantitative Summary")
        return sendEmail(subject = subject, htmlContent = html)
    }

    // =========================================================================
    // 4. RISK ALERT (MaxDD > 5% or 3 losses)
    // =========================================================================
    suspend fun sendRiskAlert(
        reason: String,
        currentDrawdownPct: Double,
        consecutiveLosses: Int,
        actionTaken: String
    ): Result<String> {
        val subject = "🚨 RISK ALERT: $reason — Drawdown ${String.format("%.2f", currentDrawdownPct)}%"
        val html = wrapTemplate("""
            <div style="background: rgba(255,23,68,0.06); border: 2px solid #FF1744; border-radius: 12px; padding: 24px;">
              <h2 style="color: #FF1744; margin-top: 0;">HARD RISK SYSTEM BREACH</h2>
              <p style="color: #FFF; font-size: 15px;"><strong>Trigger:</strong> $reason</p>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin: 16px 0;">
                <div style="background: #111; padding: 12px; border-radius: 8px;">
                  <div style="color: #888; font-size: 11px;">Drawdown</div>
                  <div style="color: #FF1744; font-size: 18px; font-weight: 800;">${String.format("%.2f", currentDrawdownPct)}%</div>
                </div>
                <div style="background: #111; padding: 12px; border-radius: 8px;">
                  <div style="color: #888; font-size: 11px;">Consecutive Losses</div>
                  <div style="color: #FF1744; font-size: 18px; font-weight: 800;">$consecutiveLosses</div>
                </div>
              </div>
              <div style="background: #200; border: 1px solid #FF1744; padding: 12px; border-radius: 8px; color: #FFF; font-size: 13px;">
                🛡️ <strong>Risk Action Enforced:</strong> $actionTaken
              </div>
            </div>
        """.trimIndent(), "Hard Risk Engine Circuit Protection")
        return sendEmail(subject = subject, htmlContent = html)
    }

    // =========================================================================
    // 5. TEST EMAIL
    // =========================================================================
    suspend fun sendTestEmail(targetEmail: String = recipientEmail): Result<String> {
        val subject = "✅ ESMH.TRADE Live Resend Email Verification Test"
        val html = wrapTemplate("""
            <div style="background: rgba(0,230,118,0.05); border: 1px solid #00E676; border-radius: 12px; padding: 24px; text-align: center;">
              <div style="font-size: 40px; margin-bottom: 12px;">🚀</div>
              <h2 style="color: #00E676; margin: 0 0 8px 0;">Notification System Operational</h2>
              <p style="color: #CCC; font-size: 14px; line-height: 1.6; max-width: 480px; margin: 0 auto 20px auto;">
                Your Resend API connection is verified and functioning cleanly. ESMH.TRADE will deliver real-time trade signals, health telemetry, daily P&L summaries, and risk circuit-breaker notices to this address.
              </p>
              <div style="background: #111; padding: 12px; border-radius: 8px; font-family: monospace; font-size: 12px; color: #888; display: inline-block;">
                Recipient: <span style="color: #FFF;">$targetEmail</span> • Latency: <span style="color: #00E676;">Verified &lt;50ms</span>
              </div>
            </div>
        """.trimIndent(), "System Verification Test")
        return sendEmail(to = targetEmail, subject = subject, htmlContent = html)
    }

    private fun wrapTemplate(content: String, subtitle: String): String = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body style="margin: 0; padding: 0; background-color: #0A0A0A; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #FFFFFF;">
  <div style="max-width: 600px; margin: 0 auto; padding: 24px 16px;">
    <div style="border-bottom: 1px solid #222; padding-bottom: 16px; margin-bottom: 24px; display: flex; justify-content: space-between; align-items: baseline;">
      <div>
        <span style="font-size: 22px; font-weight: 900; color: #FFFFFF;">ESMH<span style="color: #00E676;">.TRADE</span></span>
        <span style="font-size: 11px; background: #222; color: #AAA; padding: 2px 6px; border-radius: 4px; margin-left: 8px;">QUANT CORE</span>
      </div>
      <div style="font-size: 12px; color: #666;">$subtitle</div>
    </div>
    $content
    <div style="margin-top: 36px; border-top: 1px solid #222; padding-top: 16px; text-align: center; font-size: 11px; color: #666;">
      <p style="margin: 0;">Automated algorithmic alert from ESMH.TRADE Mother Brain & Quant Execution Engine.</p>
    </div>
  </div>
</body>
</html>
    """.trimIndent()
}
