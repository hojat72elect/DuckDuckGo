

package com.duckduckgo.privacy.config.impl.features.gpc

import com.duckduckgo.privacy.config.store.features.gpc.GpcRepository
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GpcContentScopeConfigPluginTest {

    lateinit var testee: GpcContentScopeConfigPlugin

    private val mockGpcRepository: GpcRepository = mock()

    @Before
    fun before() {
        testee = GpcContentScopeConfigPlugin(mockGpcRepository)
    }

    @Test
    fun whenGetConfigThenReturnCorrectlyFormattedJson() {
        whenever(mockGpcRepository.gpcContentScopeConfig).thenReturn(config)
        assertEquals("\"gpc\":$config", testee.config())
    }

    @Test
    fun whenGetPreferencesThenReturnCorrectlyFormattedJsonWhenGpcIsEnabled() {
        whenever(mockGpcRepository.isGpcEnabled()).thenReturn(true)
        assertEquals(testee.preferences(), "\"globalPrivacyControlValue\":true")
    }

    @Test
    fun whenGetPreferencesThenReturnCorrectlyFormattedJsonWhenGpcIsDisabled() {
        whenever(mockGpcRepository.isGpcEnabled()).thenReturn(false)
        assertEquals(testee.preferences(), "\"globalPrivacyControlValue\":false")
    }

    companion object {
        const val config = "{\"exceptions\":[{\"domain\":\"example.com\"}]," +
            "\"settings\":{\"gpcHeaderEnabledSites\":[\"foo.com\"]}," +
            "\"state\":\"enabled\"}"
    }
}
