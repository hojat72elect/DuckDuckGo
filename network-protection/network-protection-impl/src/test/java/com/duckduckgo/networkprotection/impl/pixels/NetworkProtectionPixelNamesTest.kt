

package com.duckduckgo.networkprotection.impl.pixels

import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkProtectionPixelNamesTest {
    @Test
    fun allNetworkProtectionPixelsShallBePrefixed() {
        NetworkProtectionPixelNames.values().map { it.pixelName }.forEach { pixel ->
            assertTrue(pixel.startsWith("m_netp") || pixel.startsWith("m_vpn"))
        }
    }
}
