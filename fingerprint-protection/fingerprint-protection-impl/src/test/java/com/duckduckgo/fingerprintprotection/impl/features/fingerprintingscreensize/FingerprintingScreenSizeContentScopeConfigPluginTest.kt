

package com.duckduckgo.fingerprintprotection.impl.features.fingerprintingscreensize

import com.duckduckgo.fingerprintprotection.store.FingerprintingScreenSizeEntity
import com.duckduckgo.fingerprintprotection.store.features.fingerprintingscreensize.FingerprintingScreenSizeRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FingerprintingScreenSizeContentScopeConfigPluginTest {

    lateinit var testee: FingerprintingScreenSizeContentScopeConfigPlugin

    private val mockFingerprintingScreenSizeRepository: FingerprintingScreenSizeRepository = mock()

    @Before
    fun before() {
        testee = FingerprintingScreenSizeContentScopeConfigPlugin(mockFingerprintingScreenSizeRepository)
    }

    @Test
    fun whenGetConfigThenReturnCorrectlyFormattedJson() {
        whenever(mockFingerprintingScreenSizeRepository.fingerprintingScreenSizeEntity).thenReturn(FingerprintingScreenSizeEntity(json = config))
        assertEquals("\"fingerprintingScreenSize\":$config", testee.config())
    }

    @Test
    fun whenGetPreferencesThenReturnNull() {
        assertNull(testee.preferences())
    }

    companion object {
        const val config = "{\"key\":\"value\"}"
    }
}
