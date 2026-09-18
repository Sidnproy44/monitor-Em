package com.safetrust.android

import android.content.ContentResolver
import android.provider.Settings
import org.json.JSONObject
import java.time.Instant

data class AccessibilityStatePayload(
    val clientTimestamp: String,
    val accessibilityServicesEnabled: Boolean
)

object AccessibilityState {
    fun isAccessibilityServicesEnabled(contentResolver: ContentResolver): Boolean =
        Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1

    fun currentTimestamp(): String = Instant.now().toString()

    fun payload(clientTimestamp: String, accessibilityServicesEnabled: Boolean): AccessibilityStatePayload =
        AccessibilityStatePayload(clientTimestamp, accessibilityServicesEnabled)

    fun buildPayload(clientTimestamp: String, accessibilityServicesEnabled: Boolean): JSONObject {
        val payload = payload(clientTimestamp, accessibilityServicesEnabled)
        return JSONObject()
            .put("client_timestamp", payload.clientTimestamp)
            .put("accessibility_services_enabled", payload.accessibilityServicesEnabled)
    }
}
