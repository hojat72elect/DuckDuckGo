

package com.duckduckgo.fingerprintprotection.impl.features.fingerprintingtemporarystorage

import com.duckduckgo.fingerprintprotection.api.FingerprintProtectionFeatureName
import com.duckduckgo.fingerprintprotection.store.FingerprintingTemporaryStorageEntity
import com.duckduckgo.fingerprintprotection.store.features.fingerprintingtemporarystorage.FingerprintingTemporaryStorageRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class FingerprintingTemporaryStorageFeaturePluginTest {
    lateinit var testee: FingerprintingTemporaryStorageFeaturePlugin

    private val mockFingerprintingTemporaryStorageRepository: FingerprintingTemporaryStorageRepository = mock()

    @Before
    fun before() {
        testee = FingerprintingTemporaryStorageFeaturePlugin(mockFingerprintingTemporaryStorageRepository)
    }

    @Test
    fun whenFeatureNameDoesNotMatchFingerprintingTemporaryStorageThenReturnFalse() {
        FingerprintProtectionFeatureName.values().filter { it != FEATURE_NAME }.forEach {
            assertFalse(testee.store(it.value, JSON_STRING))
        }
    }

    @Test
    fun whenFeatureNameMatchesFingerprintingTemporaryStorageThenReturnTrue() {
        assertTrue(testee.store(FEATURE_NAME_VALUE, JSON_STRING))
    }

    @Test
    fun whenFeatureNameMatchesFingerprintingTemporaryStorageThenUpdateAll() {
        testee.store(FEATURE_NAME_VALUE, JSON_STRING)
        val captor = argumentCaptor<FingerprintingTemporaryStorageEntity>()
        verify(mockFingerprintingTemporaryStorageRepository).updateAll(captor.capture())
        assertEquals(JSON_STRING, captor.firstValue.json)
    }

    companion object {
        private val FEATURE_NAME = FingerprintProtectionFeatureName.FingerprintingTemporaryStorage
        private val FEATURE_NAME_VALUE = FEATURE_NAME.value
        private const val JSON_STRING = "{\"key\":\"value\"}"
    }
}
