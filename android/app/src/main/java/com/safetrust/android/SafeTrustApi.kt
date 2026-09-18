package com.safetrust.android

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SafeTrustApi(private val baseUrl: String) {
    private fun request(method: String, path: String, body: JSONObject? = null, session: String? = null): JSONObject {
        val url = URL(baseUrl.trimEnd('/') + path)
        require(url.protocol == "https") { "Production SafeTrust communication must use HTTPS" }
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            if (body != null) { doOutput = true; setRequestProperty("Content-Type", "application/json") }
            if (session != null) setRequestProperty("X-SafeTrust-Device-Session", session)
        }
        try {
            if (body != null) connection.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.use { BufferedReader(InputStreamReader(it, StandardCharsets.UTF_8)).readText() } ?: "{}"
            val json = JSONObject(text)
            if (code !in 200..299) throw SafeTrustApiException(code, json.optString("error", "SafeTrust request failed"))
            return json
        } finally { connection.disconnect() }
    }
    fun pair(deviceId: String, pairingCode: String, publicKey: String): JSONObject = request("POST", "/api/devices/$deviceId/pair", JSONObject().put("pairing_code", pairingCode).put("key_algorithm", Crypto.ALGORITHM).put("public_key", publicKey))
    fun challenge(deviceId: String): JSONObject = request("POST", "/api/devices/$deviceId/challenge")
    fun authenticate(deviceId: String, challengeId: String, signature: String): JSONObject = request("POST", "/api/devices/$deviceId/authenticate", JSONObject().put("challenge_id", challengeId).put("signature", signature))
    fun checkSession(session: String): JSONObject = request("GET", "/api/device-session/check", session = session)
    fun sendConnectivitySignal(deviceId: String, session: String, clientTimestamp: String, appVersion: String): JSONObject =
        request("POST", "/api/devices/$deviceId/signals/connectivity", ConnectivitySignal.buildPayload(clientTimestamp, appVersion), session)

    fun sendAccessibilitySignal(deviceId: String, session: String, clientTimestamp: String, enabled: Boolean): JSONObject =
        request("POST", "/api/devices/$deviceId/signals/accessibility", AccessibilitySignal.buildPayload(clientTimestamp, enabled), session)

    fun sendVpnTransportSignal(deviceId: String, session: String, clientTimestamp: String, vpnTransportPresent: Boolean): JSONObject =
        request("POST", "/api/devices/$deviceId/signals/vpn-transport", VpnTransportSignal.buildPayload(clientTimestamp, vpnTransportPresent), session)
}

class SafeTrustApiException(val statusCode: Int, override val message: String): Exception(message)
