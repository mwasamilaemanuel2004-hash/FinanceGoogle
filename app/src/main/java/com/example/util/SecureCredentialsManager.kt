package com.example.util

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Enterprise-grade AES-256-GCM encryption manager.
 * Securely encrypts and decrypts user API keys, MT5 passwords, and broker credentials.
 * Credentials are never logged or stored in plain text.
 */
object SecureCredentialsManager {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val PREFS_NAME = "esmh_secure_vault_prefs"
    private const val MASTER_KEY_ALIAS = "esmh_aes256_master_key"

    private fun getOrCreateMasterKey(context: Context): SecretKey {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encodedKey = prefs.getString(MASTER_KEY_ALIAS, null)
        if (encodedKey != null) {
            val keyBytes = Base64.decode(encodedKey, Base64.NO_WRAP)
            return SecretKeySpec(keyBytes, ALGORITHM)
        }

        // Generate a new high-entropy 256-bit AES key
        val keyGen = KeyGenerator.getInstance(ALGORITHM)
        keyGen.init(256, SecureRandom())
        val secretKey = keyGen.generateKey()
        val base64Key = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
        prefs.edit().putString(MASTER_KEY_ALIAS, base64Key).apply()
        return secretKey
    }

    /**
     * Encrypts plaintext string using AES-256-GCM with a random IV.
     * Returns a safe base64 representation of IV + Ciphertext + Auth Tag.
     */
    fun encrypt(context: Context, plainText: String): String {
        if (plainText.isEmpty()) return ""
        try {
            val secretKey = getOrCreateMasterKey(context)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

            val cipherText = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            // Prepend IV to ciphertext
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            return ""
        }
    }

    /**
     * Decrypts an AES-256-GCM encoded string.
     */
    fun decrypt(context: Context, encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        try {
            val secretKey = getOrCreateMasterKey(context)
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size < GCM_IV_LENGTH) return ""

            val iv = ByteArray(GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            val cipherTextSize = combined.size - GCM_IV_LENGTH
            val cipherText = ByteArray(cipherTextSize)
            System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherTextSize)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(cipherText)
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            return ""
        }
    }

    /**
     * Masks sensitive credentials for UI display (e.g., "vm83••••••••••••a4f2").
     */
    fun maskCredential(credential: String): String {
        if (credential.length <= 6) return "••••••••"
        val prefix = credential.take(3)
        val suffix = credential.takeLast(3)
        return "$prefix••••••••$suffix"
    }
}
