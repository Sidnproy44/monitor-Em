package com.safetrust.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONObject
import java.time.Instant

class AccessibilitySignalTest {
    @Test fun booleanStateIsPreserved() {
        assertEquals(true, AccessibilitySignal.payload("2026-09-18T02:00:00Z", true).accessibilityServicesEnabled)
        assertEquals(false, AccessibilitySignal.payload("2026-09-18T02:00:00Z", false).accessibilityServicesEnabled)
    }

    @Test fun timestampSourceProducesParseableTimestamp() {
        val timestamp = AccessibilitySignal.currentTimestamp()
        assertTrue(timestamp.isNotBlank())
        Instant.parse(timestamp)
    }

    @Test fun requestPayloadContainsOnlyAllowedFields() {
        val json = AccessibilitySignal.buildPayload("2026-09-18T02:00:00Z", true)
        assertEquals(2, json.length())
        assertEquals("2026-09-18T02:00:00Z", json.getString("client_timestamp"))
        assertEquals(true, json.getBoolean("accessibility_services_enabled"))
    }

    @Test fun falseStateSerializesAsBooleanFalse() {
        val json = AccessibilitySignal.buildPayload("2026-09-18T02:00:00Z", false)
        assertEquals(false, json.getBoolean("accessibility_services_enabled"))
        assertEquals(JSONObject.NULL, json.opt("unsupported"))
    }
}
