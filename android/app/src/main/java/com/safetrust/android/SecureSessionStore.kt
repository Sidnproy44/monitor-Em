package com.safetrust.android

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureSessionStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("safetrust_device_state", Context.MODE_PRIVATE)
    private val alias = "safetrust-session-aes-v1"

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = ks.getKey(alias, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build())
        return generator.generateKey()
    }

    fun save(credential: String, expiresAt: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val ciphertext = cipher.doFinal(credential.toByteArray(StandardCharsets.UTF_8))
        prefs.edit().putString("session_ciphertext", Crypto.base64Url(ciphertext))
            .putString("session_iv", Crypto.base64Url(cipher.iv))
            .putString("session_expires_at", expiresAt).apply()
    }

    fun load(): String? {
        val ciphertext = prefs.getString("session_ciphertext", null) ?: return null
        val iv = prefs.getString("session_iv", null) ?: return null
        val expiresAt = prefs.getString("session_expires_at", null) ?: return null
        if (java.time.Instant.parse(expiresAt).isBefore(java.time.Instant.now())) {
            clear()
            return null
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val decodeFlags = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(iv, decodeFlags)))
        return String(cipher.doFinal(Base64.decode(ciphertext, decodeFlags)), StandardCharsets.UTF_8)
    }

    fun clear() { prefs.edit().clear().apply() }
}
