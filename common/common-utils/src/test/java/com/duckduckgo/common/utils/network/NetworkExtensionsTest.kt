package com.duckduckgo.common.utils.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.net.Inet4Address
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkExtensionsTest {

    @Test
    fun `test isCGNAT`() {
        // cover all CGNAT range
        for (octet2 in 64..127) {
            for (octet3 in 0..255) {
                for (octet4 in 0..255) {
                    assertTrue(Inet4Address.getByName("100.$octet2.$octet3.$octet4").isCGNATed())
                }
            }
        }

        // previous and next IP address
        assertFalse(Inet4Address.getByName("100.63.255.255").isCGNATed())
        assertFalse(Inet4Address.getByName("100.128.0.0").isCGNATed())
    }
}
