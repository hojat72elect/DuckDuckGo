

package com.duckduckgo.fingerprintprotection.impl.features.fingerprintinghardware

import com.duckduckgo.fingerprintprotection.api.FingerprintProtectionFeatureName
import com.duckduckgo.fingerprintprotection.store.FingerprintingHardwareEntity
import com.duckduckgo.fingerprintprotection.store.features.fingerprintinghardware.FingerprintingHardwareRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class FingerprintingHardwareFeaturePluginTest {
    lateinit var testee: FingerprintingHardwareFeaturePlugin

    private val mockFingerprintingHardwareRepository: FingerprintingHardwareRepository = mock()

    @Before
    fun before() {
        testee = FingerprintingHardwareFeaturePlugin(mockFingerprintingHardwareRepository)
    }

    @Test
    fun whenFeatureNameDoesNotMatchFingerprintingHardwareThenReturnFalse() {
        FingerprintProtectionFeatureName.values().filter { it != FEATURE_NAME }.forEach {
            assertFalse(testee.store(it.value, JSON_STRING))
        }
    }

    @Test
    fun whenFeatureNameMatchesFingerprintingHardwareThenReturnTrue() {
        assertTrue(testee.store(FEATURE_NAME_VALUE, JSON_STRING))
    }

    @Test
    fun whenFeatureNameMatchesFingerprintingHardwareThenUpdateAll() {
        testee.store(FEATURE_NAME_VALUE, JSON_STRING)
        val captor = argumentCaptor<FingerprintingHardwareEntity>()
        verify(mockFingerprintingHardwareRepository).updateAll(captor.capture())
        assertEquals(JSON_STRING, captor.firstValue.json)
    }

    companion object {
        private val FEATURE_NAME = FingerprintProtectionFeatureName.FingerprintingHardware
        private val FEATURE_NAME_VALUE = FEATURE_NAME.value
        private const val JSON_STRING = "{\"key\":\"value\"}"
    }
}
