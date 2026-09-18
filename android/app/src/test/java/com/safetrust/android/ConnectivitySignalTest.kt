package com.safetrust.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectivitySignalTest {
    @Test fun payloadContainsOnlyAllowedFields() {
        val payload = ConnectivitySignal.payload("2026-09-18T02:00:00Z", "0.1.0")
        assertEquals("2026-09-18T02:00:00Z", payload.clientTimestamp)
        assertEquals("0.1.0", payload.appVersion)
    }

    @Test fun timestampSourceProducesParseableTimestamp() {
        val timestamp = ConnectivitySignal.currentTimestamp()
        assertTrue(timestamp.isNotBlank())
        java.time.Instant.parse(timestamp)
    }

    @Test fun appVersionIsSerializedIntoTheAllowedPayloadModel() {
        val payload = ConnectivitySignal.payload("2026-09-18T02:00:00Z", "0.1.0")
        assertEquals("0.1.0", payload.appVersion)
    }
}
