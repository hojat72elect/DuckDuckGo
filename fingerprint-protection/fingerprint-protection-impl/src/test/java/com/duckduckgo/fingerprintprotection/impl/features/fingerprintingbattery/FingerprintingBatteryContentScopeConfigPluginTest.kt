

package com.duckduckgo.fingerprintprotection.impl.features.fingerprintingbattery

import com.duckduckgo.fingerprintprotection.store.FingerprintingBatteryEntity
import com.duckduckgo.fingerprintprotection.store.features.fingerprintingbattery.FingerprintingBatteryRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FingerprintingBatteryContentScopeConfigPluginTest {

    lateinit var testee: FingerprintingBatteryContentScopeConfigPlugin

    private val mockFingerprintingBatteryRepository: FingerprintingBatteryRepository = mock()

    @Before
    fun before() {
        testee = FingerprintingBatteryContentScopeConfigPlugin(mockFingerprintingBatteryRepository)
    }

    @Test
    fun whenGetConfigThenReturnCorrectlyFormattedJson() {
        whenever(mockFingerprintingBatteryRepository.fingerprintingBatteryEntity).thenReturn(FingerprintingBatteryEntity(json = config))
        assertEquals("\"fingerprintingBattery\":$config", testee.config())
    }

    @Test
    fun whenGetPreferencesThenReturnNull() {
        assertNull(testee.preferences())
    }

    companion object {
        const val config = "{\"key\":\"value\"}"
    }
}
