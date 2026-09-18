package com.safetrust.android

import android.util.Base64
import java.security.MessageDigest

object PlayProtectRequestHash {
    const val VERSION = "SafeTrust.PlayProtect.v1"
    const val PURPOSE = "play_protect_check"

    fun canonical(contextId: String): String {
        require(contextId.isNotBlank()) { "context ID is required" }
        return "$VERSION\n$PURPOSE\n$contextId"
    }

    fun sha256Base64Url(contextId: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonical(contextId).toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(
            digest,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
    }
}
