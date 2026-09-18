package com.safetrust.android

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.json.JSONObject
import java.time.Instant

data class VpnTransportPayload(
    val clientTimestamp: String,
    val vpnTransportPresent: Boolean
)

object VpnTransportSignal {
    fun currentTimestamp(): String = Instant.now().toString()

    fun readTransportPresent(connectivityManager: ConnectivityManager?): Boolean? {
        val activeNetwork = connectivityManager?.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return null
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    fun payload(clientTimestamp: String, vpnTransportPresent: Boolean): VpnTransportPayload =
        VpnTransportPayload(clientTimestamp, vpnTransportPresent)

    fun buildPayload(clientTimestamp: String, vpnTransportPresent: Boolean): JSONObject {
        val payload = payload(clientTimestamp, vpnTransportPresent)
        return JSONObject()
            .put("client_timestamp", payload.clientTimestamp)
            .put("vpn_transport_present", payload.vpnTransportPresent)
    }
}
