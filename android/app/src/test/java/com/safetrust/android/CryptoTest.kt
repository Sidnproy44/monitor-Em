package com.safetrust.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoTest {
    @Test fun domainSeparatedMessageIsExact() {
        assertEquals("SafeTrust.DeviceAuth.v1\ndevice\nchallenge\nnonce", Crypto.challengeMessage("device", "challenge", "nonce"))
    }
    @Test fun base64UrlHasNoPadding() {
        val value = Crypto.base64Url(ByteArray(32) { it.toByte() })
        assertTrue(value.none { it == '=' || it == '+' || it == '/' })
    }
}
