package com.safetrust.android

import android.app.KeyguardManager
import org.json.JSONObject

data class SecureLockPayload(
    val secureLockPresent: Boolean
)

object SecureLockSignal {
    fun readSecureLockState(keyguardManager: KeyguardManager?): Boolean? =
        readSecureLockState(keyguardManager?.isDeviceSecure)

    fun readSecureLockState(isDeviceSecure: Boolean?): Boolean? =
        isDeviceSecure

    fun payload(secureLockPresent: Boolean): SecureLockPayload =
        SecureLockPayload(secureLockPresent)

    fun buildPayload(secureLockPresent: Boolean): JSONObject =
        JSONObject().put("secure_lock_present", payload(secureLockPresent).secureLockPresent)
}
