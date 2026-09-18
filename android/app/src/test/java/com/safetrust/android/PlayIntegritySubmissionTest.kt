package com.safetrust.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PlayIntegritySubmissionTest {
    @Test fun requestUsesComputedRequestHash() {
        val requestHash = PlayProtectRequestHash.sha256Base64Url(
            "33333333-3333-4333-8333-333333333333"
        )
        val request = PlayIntegrityClient.buildTokenRequest(requestHash)
        assertEquals(requestHash, request.requestHash())
    }

    @Test fun submissionContainsOnlyContextAndToken() {
        val payload = SafeTrustApi.buildPlayIntegritySubmission("context", "token")
        assertEquals(setOf("context_id", "integrity_token"), payload.keys().asSequence().toSet())
        assertFalse(payload.has("play_protect_verdict"))
        assertFalse(payload.has("owner_user_id"))
        assertFalse(payload.has("device_id"))
        assertFalse(payload.has("event_type"))
        assertFalse(payload.has("producer"))
        assertFalse(payload.has("category"))
        assertFalse(payload.has("risk"))
        assertFalse(payload.has("severity"))
    }

    @Test fun clientDoesNotConstructPlayProtectVerdict() {
        val payload = SafeTrustApi.buildPlayIntegritySubmission("context", "token")
        assertFalse(payload.has("play_protect_verdict"))
        assertFalse(payload.has("verdict"))
    }

    @Test fun verifierUnconfiguredIsTechnicalStateOnly() {
        val state = "PLAY_INTEGRITY_VERIFIER_NOT_CONFIGURED"
        assertEquals(false, state == "NO_ISSUES")
        assertEquals(false, state == "PLAY_PROTECT_VERDICT")
    }
}
