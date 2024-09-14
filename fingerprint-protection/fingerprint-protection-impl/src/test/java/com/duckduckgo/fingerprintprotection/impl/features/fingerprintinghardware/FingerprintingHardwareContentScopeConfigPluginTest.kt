

package com.duckduckgo.fingerprintprotection.impl.features.fingerprintinghardware

import com.duckduckgo.fingerprintprotection.store.FingerprintingHardwareEntity
import com.duckduckgo.fingerprintprotection.store.features.fingerprintinghardware.FingerprintingHardwareRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FingerprintingHardwareContentScopeConfigPluginTest {

    lateinit var testee: FingerprintingHardwareContentScopeConfigPlugin

    private val mockFingerprintingHardwareRepository: FingerprintingHardwareRepository = mock()

    @Before
    fun before() {
        testee = FingerprintingHardwareContentScopeConfigPlugin(mockFingerprintingHardwareRepository)
    }

    @Test
    fun whenGetConfigThenReturnCorrectlyFormattedJson() {
        whenever(mockFingerprintingHardwareRepository.fingerprintingHardwareEntity).thenReturn(FingerprintingHardwareEntity(json = config))
        assertEquals("\"fingerprintingHardware\":$config", testee.config())
    }

    @Test
    fun whenGetPreferencesThenReturnNull() {
        assertNull(testee.preferences())
    }

    companion object {
        const val config = "{\"key\":\"value\"}"
    }
}
