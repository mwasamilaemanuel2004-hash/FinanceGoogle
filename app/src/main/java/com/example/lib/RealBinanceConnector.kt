package com.example.lib

import android.util.Log
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object RealBinanceConnector {
    private const val TAG = "RealBinanceConnector"

    fun getSignature(query: String, secret: String): String {
        val hmacSha256 = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        hmacSha256.init(secretKeySpec)
        val hash = hmacSha256.doFinal(query.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    suspend fun testSpot(apiKey: String, secret: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val ts = System.currentTimeMillis()
                val sig = getSignature("timestamp=$ts", secret)
                val url = URL("https://api.binance.com/api/v3/account?timestamp=$ts&signature=$sig")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("X-MBX-APIKEY", apiKey)
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                response
            } catch (e: Exception) {
                Log.e(TAG, "testSpot error: ${e.message}")
                "{\"code\":-1, \"msg\":\"${e.message}\"}"
            }
        }
    }

    suspend fun testFutures(apiKey: String, secret: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val ts = System.currentTimeMillis()
                val sig = getSignature("timestamp=$ts", secret)
                val url = URL("https://fapi.binance.com/fapi/v2/balance?timestamp=$ts&signature=$sig")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("X-MBX-APIKEY", apiKey)
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                response
            } catch (e: Exception) {
                Log.e(TAG, "testFutures error: ${e.message}")
                "{\"code\":-1, \"msg\":\"${e.message}\"}"
            }
        }
    }
}
