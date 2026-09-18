package com.safetrust.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayProtectRequestHashTest {
    private val contextId = "33333333-3333-4333-8333-333333333333"

    @Test fun canonicalSerializationIsExact() {
        assertEquals(
            "SafeTrust.PlayProtect.v1\nplay_protect_check\n$contextId",
            PlayProtectRequestHash.canonical(contextId)
        )
    }

    @Test fun sameContextProducesSameHash() {
        assertEquals(
            PlayProtectRequestHash.sha256Base64Url(contextId),
            PlayProtectRequestHash.sha256Base64Url(contextId)
        )
    }

    @Test fun contextIdChangesHash() {
        assertNotEquals(
            PlayProtectRequestHash.sha256Base64Url(contextId),
            PlayProtectRequestHash.sha256Base64Url("44444444-4444-4444-8444-444444444444")
        )
    }

    @Test fun hashIsBase64UrlWithoutPadding() {
        val hash = PlayProtectRequestHash.sha256Base64Url(contextId)
        assertEquals(43, hash.length)
        assertFalse(hash.contains("="))
        assertFalse(hash.contains("+"))
        assertFalse(hash.contains("/"))
    }

    @Test fun knownBackendHashMatchesContract() {
        assertEquals(
            "REPLACE_WITH_BACKEND_VECTOR",
            PlayProtectRequestHash.sha256Base64Url(contextId)
        )
    }
}
