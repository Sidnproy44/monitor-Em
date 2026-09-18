package com.safetrust.android

import android.provider.Settings
import org.json.JSONObject
import java.time.Instant

data class AccessibilityPayload(
    val clientTimestamp: String,
    val accessibilityServicesEnabled: Boolean
)

object AccessibilitySignal {
    fun currentTimestamp(): String = Instant.now().toString()

    fun readEnabled(contentResolver: android.content.ContentResolver): Boolean =
        Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1

    fun buildPayload(clientTimestamp: String, enabled: Boolean): JSONObject =
        JSONObject()
            .put("client_timestamp", clientTimestamp)
            .put("accessibility_services_enabled", enabled)

    fun payload(clientTimestamp: String, enabled: Boolean): AccessibilityPayload =
        AccessibilityPayload(clientTimestamp, enabled)
}
