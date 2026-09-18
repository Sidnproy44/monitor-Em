package com.safetrust.android

import android.net.NetworkCapabilities
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONObject

class VpnTransportSignalTest {
    @Test fun vpnTransportProducesTrue() {
        val capabilities = NetworkCapabilities.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .build()
        assertEquals(true, capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
    }

    @Test fun nonVpnTransportProducesFalse() {
        val capabilities = NetworkCapabilities.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        assertEquals(false, capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
    }

    @Test fun unavailableActiveNetworkIsExplicitlyUnknown() {
        assertEquals(null, VpnTransportSignal.readTransportPresent(null))
    }

    @Test fun unavailableCapabilitiesAreExplicitlyUnknown() {
        val emptyCapabilities = NetworkCapabilities.Builder().build()
        assertFalse(emptyCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
        assertEquals(false, emptyCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
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
        assertEquals(JSONObject.NULL, json.opt("unsupported"))
    }
}
