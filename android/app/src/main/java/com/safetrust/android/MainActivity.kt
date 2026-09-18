package com.safetrust.android

import android.app.Activity
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

    private fun safeMessage(error: Exception): String = when (error) {
        is SafeTrustApiException -> "Connection error"
        else -> error.message?.take(80) ?: "Connection error"
    }

    override fun onDestroy() { executor.shutdownNow(); super.onDestroy() }
}
