

package com.duckduckgo.mobile.android.vpn.pixels

import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceShieldPixelNamesTest {
    @Test
    fun allAppTrackingProtectionPixelsShallBePrefixed() {
        DeviceShieldPixelNames.values()
            .map { it.pixelName }
            .forEach { pixel ->
                assertTrue(pixel.startsWith("m_atp") || pixel.startsWith("m_vpn") || pixel.startsWith("m_new_tab_page"))
            }
    }
}
