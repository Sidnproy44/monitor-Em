package com.safetrust.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityStateTest {
    @Test fun booleanStateIsPreserved() {
        assertEquals(true, AccessibilitySignal.payload("2026-09-18T02:00:00Z", true).accessibilityServicesEnabled)
        assertEquals(false, AccessibilitySignal.payload("2026-09-18T02:00:00Z", false).accessibilityServicesEnabled)
    }

    @Test fun timestampSourceProducesParseableTimestamp() {
        val timestamp = AccessibilitySignal.currentTimestamp()
        assertTrue(timestamp.isNotBlank())
        java.time.Instant.parse(timestamp)
    }

    @Test fun requestPayloadContainsOnlyAllowedFields() {
        val json = AccessibilitySignal.buildPayload("2026-09-18T02:00:00Z", true)
        val keys = json.keys().asSequence().toSet()
        assertEquals(setOf("client_timestamp", "accessibility_services_enabled"), keys)
        assertEquals("2026-09-18T02:00:00Z", json.getString("client_timestamp"))
        assertEquals(true, json.getBoolean("accessibility_services_enabled"))
    }

    @Test fun falseStateSerializesAsBooleanFalse() {
        val json = AccessibilitySignal.buildPayload("2026-09-18T02:00:00Z", false)
        assertTrue(!json.getBoolean("accessibility_services_enabled"))
    }

    @Test fun unsupportedFieldIsNotProduced() {
        val json = AccessibilitySignal.buildPayload("2026-09-18T02:00:00Z", true)
        assertTrue(!json.has("package_names"))
        assertTrue(!json.has("service_names"))
        assertTrue(!json.has("installed_apps"))
    }
}
