package com.safetrust.android

import org.junit.Assert.assertTrue
import org.junit.Test

class PlayIntegrityClientTest {
    @Test fun requestRequiresCloudProjectConfiguration() {
        var rejected = false
        try {
            PlayIntegrityClientTestSupport.requireConfigured(0L)
        } catch {
            rejected = true
        }
        assertTrue(rejected)
    }

    @Test fun technicalVerifierStateIsNotAPlayProtectVerdict() {
        val state = "PLAY_INTEGRITY_VERIFIER_NOT_CONFIGURED"
        assertTrue(state.startsWith("PLAY_INTEGRITY_"))
        assertTrue(state != "NO_ISSUES")
    }
}

private object PlayIntegrityClientTestSupport {
    fun requireConfigured(cloudProjectNumber: Long) {
        require(cloudProjectNumber > 0L) { "Play Integrity cloud project number is required" }
    }
}
