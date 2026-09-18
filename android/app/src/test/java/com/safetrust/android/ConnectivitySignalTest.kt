package com.safetrust.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectivitySignalTest {
    @Test fun payloadContainsOnlyAllowedFields() {
        val payload = ConnectivitySignal.buildPayload("2026-09-18T02:00:00Z", "0.1.0")
        assertEquals(2, payload.length())
        assertEquals("2026-09-18T02:00:00Z", payload.getString("client_timestamp"))
        assertEquals("0.1.0", payload.getString("app_version"))
        assertFalse(payload.has("device_id"))
        assertFalse(payload.has("owner_user_id"))
        assertFalse(payload.has("producer"))
        assertFalse(payload.has("event_type"))
    }

    @Test fun timestampSourceProducesParseableTimestamp() {
        val timestamp = ConnectivitySignal.currentTimestamp()
        assertTrue(timestamp.isNotBlank())
        java.time.Instant.parse(timestamp)
    }

    @Test fun appVersionIsSerializedExactly() {
        val payload = ConnectivitySignal.buildPayload("2026-09-18T02:00:00Z", "0.1.0")
        assertEquals("0.1.0", payload.getString("app_version"))
    }
}
