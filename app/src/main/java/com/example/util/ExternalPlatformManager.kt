package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.MarketType

data class ExternalPlatformTarget(
    val id: String,
    val name: String,
    val packageName: String?,
    val webFallbackUrl: String
)

object ExternalPlatformManager {
    val platforms = listOf(
        ExternalPlatformTarget("binance", "Binance App", "com.binance.dev", "https://www.binance.com/en/trade"),
        ExternalPlatformTarget("tradingview", "TradingView", "com.tradingview.chart", "https://www.tradingview.com/chart"),
        ExternalPlatformTarget("deriv", "Deriv Trader", "com.deriv.app", "https://app.deriv.com"),
        ExternalPlatformTarget("mt5", "MetaTrader 5", "net.metaquotes.metatrader5", "https://www.metatrader5.com")
    )

    fun openBrowserUrl(context: Context, url: String) {
        try {
            val validUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Browser launch error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPlatform(context: Context, platformId: String, symbol: String = "BTCUSDT", marketType: MarketType = MarketType.CRYPTO) {
        val target = platforms.firstOrNull { it.id == platformId } ?: platforms.first()
        val packageName = target.packageName
        if (packageName != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                try {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return
                } catch (e: Exception) {
                    // fallback to web
                }
            }
        }
        openBrowserUrl(context, target.webFallbackUrl)
    }

    fun dialPhoneNumber(context: Context, phoneNumber: String) {
        try {
            val clean = phoneNumber.replace(" ", "").replace("-", "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Dialer error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendWhatsAppMessage(context: Context, phoneNumber: String, message: String) {
        try {
            val clean = phoneNumber.replace(" ", "").replace("-", "").replace("+", "")
            val encoded = Uri.encode(message)
            val url = if (clean.isNotBlank()) "https://api.whatsapp.com/send?phone=$clean&text=$encoded" else "https://api.whatsapp.com/send?text=$encoded"
            openBrowserUrl(context, url)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
