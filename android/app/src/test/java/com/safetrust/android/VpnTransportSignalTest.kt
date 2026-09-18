package com.safetrust.android

import android.net.NetworkCapabilities
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VpnTransportSignalTest {
    @Test fun vpnTransportProducesTrue() {
        val capabilities = NetworkCapabilities.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .build()
        assertEquals(true, VpnTransportSignal.readTransportPresent(capabilities))
    }

    @Test fun nonVpnTransportProducesFalse() {
        val capabilities = NetworkCapabilities.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        assertEquals(false, VpnTransportSignal.readTransportPresent(capabilities))
    }

    @Test fun unavailableActiveNetworkIsExplicitlyUnknown() {
        assertEquals(null, VpnTransportSignal.readTransportPresent(null))
    }

    @Test fun unavailableNetworkCapabilitiesAreExplicitlyUnknown() {
        assertEquals(null, VpnTransportSignal.readTransportPresent(null as NetworkCapabilities?))
    }

    @Test fun payloadContainsNoApplicationIdentity() {
        val json = VpnTransportSignal.buildPayload("2026-09-18T02:00:00Z", true)
        assertEquals(setOf("client_timestamp", "vpn_transport_present"), json.keys().asSequence().toSet())
        assertTrue(!json.has("package_name"))
        assertTrue(!json.has("application_name"))
        assertTrue(!json.has("uid"))
        assertTrue(!json.has("service_name"))
        assertTrue(!json.has("provider_name"))
        assertEquals(true, json.getBoolean("vpn_transport_present"))
    }
}
