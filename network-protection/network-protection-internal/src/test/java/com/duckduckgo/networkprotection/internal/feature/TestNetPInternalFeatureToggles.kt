

package com.duckduckgo.networkprotection.internal.feature

import com.duckduckgo.feature.toggles.api.FakeToggleStore
import com.duckduckgo.feature.toggles.api.FeatureToggles
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TestNetPInternalFeatureToggles {

    private lateinit var toggles: NetPInternalFeatureToggles

    @Before
    fun setup() {
        toggles = FeatureToggles.Builder()
            .store(FakeToggleStore())
            .appVersionProvider { Int.MAX_VALUE }
            .featureName("test")
            .build()
            .create(NetPInternalFeatureToggles::class.java)
    }

    @Test
    fun testDefaultValues() {
        assertTrue(toggles.self().isEnabled())
        assertFalse(toggles.cloudflareDnsFallback().isEnabled())
        assertFalse(toggles.excludeSystemApps().isEnabled())
        assertFalse(toggles.enablePcapRecording().isEnabled())
    }
}
