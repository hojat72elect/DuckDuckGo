

package com.duckduckgo.mobile.android.vpn.trackers

import com.duckduckgo.common.test.FileUtilities.loadText
import com.squareup.moshi.Moshi
import org.junit.Assert
import org.junit.Test

class AppTrackerJsonParserTest {

    private val moshi = Moshi.Builder().build()

    @Test
    fun whenJsonIsValidThenBlocklistIsParsed() {
        val json = loadText(javaClass.classLoader!!, "full_app_trackers_blocklist.json")
        val blocklist = AppTrackerJsonParser.parseAppTrackerJson(moshi, json)

        Assert.assertEquals("1639624032609", blocklist.version)
        Assert.assertEquals(260, blocklist.trackers.count())
        Assert.assertEquals(433, blocklist.packages.count())
        Assert.assertEquals(128, blocklist.entities.count())
    }
}
