package com.safetrust.android

import android.app.Activity
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var status: TextView
    private lateinit var deviceId: EditText
    private lateinit var pairingCode: EditText
    private lateinit var sessionStore: SecureSessionStore
    private lateinit var api: SafeTrustApi
    private lateinit var authenticator: DeviceAuthenticator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)
        deviceId = findViewById(R.id.deviceId)
        pairingCode = findViewById(R.id.pairingCode)
        sessionStore = SecureSessionStore(this)
        api = SafeTrustApi(BuildConfig.SAFETRUST_BASE_URL)
        authenticator = DeviceAuthenticator(api, sessionStore)
        findViewById<Button>(R.id.pair).setOnClickListener { pair() }
        findViewById<Button>(R.id.connect).setOnClickListener { authenticate() }
        findViewById<Button>(R.id.connectivity).setOnClickListener { sendConnectivityCheck() }
        findViewById<Button>(R.id.accessibility).setOnClickListener { sendAccessibilityCheck() }
        findViewById<Button>(R.id.vpnTransport).setOnClickListener { sendVpnTransportCheck() }
        findViewById<Button>(R.id.secureLock).setOnClickListener { sendSecureLockCheck() }
        findViewById<Button>(R.id.playProtect).setOnClickListener { runPlayProtectCheck() }
    }

    private fun pair() {
        val id = deviceId.text.toString().trim()
        val code = pairingCode.text.toString().trim()
        if (id.isEmpty() || code.isEmpty()) { status.text = "Pairing required"; return }
        status.text = "Pairing…"
        executor.execute {
            try {
                val keys = Crypto.ensureKeyPair()
                api.pair(id, code, Crypto.publicKeyBase64Url(keys))
                runOnUiThread { status.text = "Key registered" }
            } catch (e: Exception) { runOnUiThread { status.text = safeMessage(e) } }
        }
    }

    private fun authenticate() {
        val id = deviceId.text.toString().trim()
        if (id.isEmpty()) { status.text = "Pairing required"; return }
        status.text = "Authenticating…"
        executor.execute {
            try {
                val keys = Crypto.ensureKeyPair()
                authenticator.authenticate(id, keys)
                val checked = authenticator.verifySession()
                runOnUiThread { status.text = if (checked?.optBoolean("authenticated") == true) "Authenticated" else "Connection error" }
            } catch (e: SafeTrustApiException) {
                runOnUiThread { status.text = if (e.statusCode == 401) "Session expired or device revoked" else "Connection error" }
            } catch (e: Exception) { runOnUiThread { status.text = safeMessage(e) } }
        }
    }

    private fun runPlayProtectCheck() {
        val id = deviceId.text.toString().trim()
        val session = sessionStore.load()
        val cloudProjectNumber = BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER.trim().toLongOrNull()
        if (id.isEmpty() || session == null) { status.text = "Authenticate first"; return }
        if (cloudProjectNumber == null || cloudProjectNumber <= 0L) {
            status.text = "Play Integrity cloud project configuration required"
            return
        }
        status.text = "Starting one-time Play Protect check…"
        executor.execute {
            try {
                val contextResponse = api.createPlayProtectContext(session)
                val contextJson = contextResponse.getJSONObject("context")
                val context = PlayProtectContext(
                    contextId = contextJson.getString("context_id"),
                    purpose = contextJson.getString("purpose"),
                    createdAt = contextJson.getString("created_at"),
                    expiresAt = contextJson.getString("expires_at"),
                    requestHash = contextResponse.getString("request_hash")
                )
                require(context.purpose == PlayProtectRequestHash.PURPOSE) { "Invalid Play Protect context purpose" }
                require(context.requestHash == PlayProtectRequestHash.sha256Base64Url(context.contextId)) { "Play Protect request hash mismatch" }

                val integrityClient = PlayIntegrityClient(this, cloudProjectNumber)
                val token = integrityClient.requestToken(context.requestHash)
                val result = api.submitPlayIntegrityToken(id, session, context.contextId, token)
                val state = result.optString("state")
                runOnUiThread {
                    status.text = when {
                        state == "PLAY_INTEGRITY_VERIFIER_NOT_CONFIGURED" -> "Play Integrity token received; server verification is not configured"
                        result.optBoolean("ok") -> "Play Protect check submitted"
                        else -> "Play Protect check unavailable"
                    }
                }
            } catch (e: SafeTrustApiException) {
                runOnUiThread {
                    status.text = when (e.statusCode) {
                        401 -> "Session expired or device revoked"
                        409 -> "Play Protect check expired or already used"
                        503 -> "Play Integrity server verification is not configured"
                        else -> "Play Protect check unavailable"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { status.text = safeMessage(e) }
            }
        }
    }

    private fun sendConnectivityCheck() {
        val id = deviceId.text.toString().trim()
        val session = sessionStore.load()
        if (id.isEmpty() || session == null) { status.text = "Authenticate first"; return }
        status.text = "Sending connectivity check…"
        executor.execute {
            try {
                val result = api.sendConnectivitySignal(id, session, ConnectivitySignal.currentTimestamp(), BuildConfig.VERSION_NAME)
                runOnUiThread { status.text = if (result.optBoolean("ok")) "Connectivity reported" else "Connection error" }
            } catch (e: SafeTrustApiException) {
                runOnUiThread { status.text = if (e.statusCode == 401) "Session expired or device revoked" else "Connection error" }
            } catch (e: Exception) { runOnUiThread { status.text = safeMessage(e) } }
        }
    }

    private fun sendAccessibilityCheck() {
        val id = deviceId.text.toString().trim()
        val session = sessionStore.load()
        if (id.isEmpty() || session == null) { status.text = "Authenticate first"; return }
        status.text = "Checking accessibility services…"
        executor.execute {
            try {
                val enabled = AccessibilitySignal.readEnabled(contentResolver)
                val result = api.sendAccessibilitySignal(id, session, AccessibilitySignal.currentTimestamp(), enabled)
                runOnUiThread {
                    status.text = if (result.optBoolean("ok")) {
                        if (enabled) "Accessibility services reported enabled" else "Accessibility services reported disabled"
                    } else "Connection error"
                }
            } catch (e: SafeTrustApiException) {
                runOnUiThread { status.text = if (e.statusCode == 401) "Session expired or device revoked" else "Connection error" }
            } catch (e: Exception) { runOnUiThread { status.text = safeMessage(e) } }
        }
    }

    private fun sendVpnTransportCheck() {
        val id = deviceId.text.toString().trim()
        val session = sessionStore.load()
        if (id.isEmpty() || session == null) { status.text = "Authenticate first"; return }
        status.text = "Checking VPN transport…"
        executor.execute {
            try {
                val connectivityManager = getSystemService(ConnectivityManager::class.java)
                val vpnTransportPresent = VpnTransportSignal.readTransportPresent(connectivityManager)
                runOnUiThread {
                    if (vpnTransportPresent == null) {
                        status.text = "VPN transport status unavailable"
                    } else {
                        status.text = "VPN transport status read"
                    }
                }
                if (vpnTransportPresent == null) return@execute
                val result = api.sendVpnTransportSignal(id, session, VpnTransportSignal.currentTimestamp(), vpnTransportPresent)
                runOnUiThread {
                    status.text = if (result.optBoolean("ok")) {
                        if (vpnTransportPresent) "VPN transport reported present" else "VPN transport reported absent"
                    } else "Connection error"
                }
            } catch (e: SafeTrustApiException) {
                runOnUiThread { status.text = if (e.statusCode == 401) "Session expired or device revoked" else "Connection error" }
            } catch (e: Exception) { runOnUiThread { status.text = safeMessage(e) } }
        }
    }

    private fun sendSecureLockCheck() {
        val id = deviceId.text.toString().trim()
        val session = sessionStore.load()
        if (id.isEmpty() || session == null) { status.text = "Authenticate first"; return }
        status.text = "Checking secure lock…"
        executor.execute {
            try {
                val keyguardManager = getSystemService(android.app.KeyguardManager::class.java)
                val secureLockPresent = SecureLockSignal.readSecureLockState(keyguardManager)
                if (secureLockPresent == null) {
                    runOnUiThread { status.text = "Secure lock status unavailable" }
                    return@execute
                }
                val result = api.sendSecureLockSignal(id, session, SecureLockSignal.currentTimestamp(), secureLockPresent)
                runOnUiThread {
                    status.text = if (result.optBoolean("ok")) {
                        if (secureLockPresent) "Secure lock reported present" else "Secure lock reported absent"
                    } else "Connection error"
                }
            } catch (e: SafeTrustApiException) {
                runOnUiThread { status.text = if (e.statusCode == 401) "Session expired or device revoked" else "Connection error" }
            } catch (e: Exception) { runOnUiThread { status.text = safeMessage(e) } }
        }
    }

    private fun safeMessage(error: Exception): String = when (error) {
        is SafeTrustApiException -> "Connection error"
        else -> error.message?.take(80) ?: "Connection error"
    }

    override fun onDestroy() { executor.shutdownNow(); super.onDestroy() }
}
