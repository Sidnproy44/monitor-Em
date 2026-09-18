package com.safetrust.android

data class PlayProtectContext(
    val contextId: String,
    val purpose: String,
    val createdAt: String,
    val expiresAt: String,
    val requestHash: String
)
