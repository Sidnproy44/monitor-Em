package com.safetrust.android

import org.json.JSONObject

class DeviceAuthenticator(private val api: SafeTrustApi, private val sessionStore: SecureSessionStore) {
    fun authenticate(deviceId: String, keyPair: java.security.KeyPair): String {
        val challenge = api.challenge(deviceId).getJSONObject("challenge")
        val challengeId = challenge.getString("challenge_id")
        val nonce = challenge.getString("nonce")
        val signature = Crypto.sign(keyPair, Crypto.challengeMessage(deviceId, challengeId, nonce))
        val authentication = api.authenticate(deviceId, challengeId, signature).getJSONObject("authentication")
        val session = authentication.getJSONObject("session")
        val credential = authentication.getString("session_credential")
        sessionStore.save(credential, session.getString("expires_at"))
        return credential
    }
    fun verifySession(): JSONObject? {
        val credential = sessionStore.load() ?: return null
        return try { api.checkSession(credential) } catch (e: SafeTrustApiException) {
            if (e.statusCode == 401) sessionStore.clear()
            throw e
        }
    }
}
