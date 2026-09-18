package com.safetrust.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecureLockSignalTest {
    @Test fun timestampIsGenerated() {
        assertTrue(SecureLockSignal.currentTimestamp().isNotBlank())
    }

    @Test fun secureStateProducesTrue() {
        assertEquals(true, SecureLockSignal.readSecureLockState(true))
    }

    @Test fun insecureStateProducesFalse() {
        assertEquals(false, SecureLockSignal.readSecureLockState(false))
    }

    @Test fun unavailableStateIsExplicitlyUnknown() {
        assertEquals(null, SecureLockSignal.readSecureLockState(null))
    }

    @Test fun payloadContainsOnlyIntendedBooleanSignal() {
        val json = SecureLockSignal.buildPayload(true)
        assertEquals(setOf("secure_lock_present"), json.keys().asSequence().toSet())
        assertTrue(json.getBoolean("secure_lock_present"))
    }

    @Test fun payloadContainsNoCredentialOrIdentityFields() {
        val json = SecureLockSignal.buildPayload(false)
        val serialized = json.toString()
        assertFalse(serialized.contains("password", ignoreCase = true))
        assertFalse(serialized.contains("pin", ignoreCase = true))
        assertFalse(serialized.contains("pattern", ignoreCase = true))
        assertFalse(serialized.contains("biometric", ignoreCase = true))
        assertFalse(serialized.contains("credential", ignoreCase = true))
        assertFalse(serialized.contains("package", ignoreCase = true))
        assertFalse(serialized.contains("application", ignoreCase = true))
        assertFalse(serialized.contains("uid", ignoreCase = true))
        assertEquals(false, json.getBoolean("secure_lock_present"))
    }
}
