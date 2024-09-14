

package com.duckduckgo.app.statistics.pixels

import com.duckduckgo.app.anr.AnrPixelName
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.autofill.impl.pixel.AutofillPixelNames
import com.duckduckgo.mobile.android.vpn.pixels.DeviceShieldPixelNames
import org.junit.Assert.fail
import org.junit.Test

class PixelNameTest {

    @Test
    fun verifyNoDuplicatePixelNames() {
        val existingNames = mutableSetOf<String>()
        AppPixelName.values().forEach {
            if (!existingNames.add(it.pixelName)) {
                fail("Duplicate pixel name in AppPixelName: ${it.pixelName}")
            }
        }
        Pixel.StatisticsPixelName.values().forEach {
            if (!existingNames.add(it.pixelName)) {
                fail("Duplicate pixel name in StatisticsPixelName: ${it.pixelName}")
            }
        }
        DeviceShieldPixelNames.values().forEach {
            if (!existingNames.add(it.pixelName)) {
                fail("Duplicate pixel name in DeviceShieldPixelNames: ${it.pixelName}")
            }
        }
        AnrPixelName.values().forEach {
            if (!existingNames.add(it.pixelName)) {
                fail("Duplicate pixel name in AnrPixelName: ${it.pixelName}")
            }
        }
        AutofillPixelNames.values().forEach {
            if (!existingNames.add(it.pixelName)) {
                fail("Duplicate pixel name in AutofillPixelNames: ${it.pixelName}")
            }
        }
    }
}
