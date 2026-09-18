package com.safetrust.android

import org.json.JSONObject
import java.time.Instant

object ConnectivitySignal {
    fun currentTimestamp(): String = Instant.now().toString()

    fun buildPayload(clientTimestamp: String, appVersion: String): JSONObject =
        JSONObject()
            .put("client_timestamp", clientTimestamp)
            .put("app_version", appVersion)
}
