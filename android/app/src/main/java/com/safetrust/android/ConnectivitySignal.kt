package com.safetrust.android

import org.json.JSONObject
import java.time.Instant

data class ConnectivityPayload(val clientTimestamp: String, val appVersion: String)

object ConnectivitySignal {
    fun currentTimestamp(): String = Instant.now().toString()

    fun payload(clientTimestamp: String, appVersion: String): ConnectivityPayload =
        ConnectivityPayload(clientTimestamp, appVersion)

    fun buildPayload(clientTimestamp: String, appVersion: String): JSONObject {
        val payload = payload(clientTimestamp, appVersion)
        return JSONObject()
            .put("client_timestamp", payload.clientTimestamp)
            .put("app_version", payload.appVersion)
    }
}
