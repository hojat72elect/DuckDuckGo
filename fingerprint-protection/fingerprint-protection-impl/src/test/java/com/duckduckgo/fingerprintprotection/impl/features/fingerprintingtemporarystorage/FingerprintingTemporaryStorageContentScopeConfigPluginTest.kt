

package com.duckduckgo.fingerprintprotection.impl.features.fingerprintingtemporarystorage

import com.duckduckgo.fingerprintprotection.store.FingerprintingTemporaryStorageEntity
import com.duckduckgo.fingerprintprotection.store.features.fingerprintingtemporarystorage.FingerprintingTemporaryStorageRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FingerprintingTemporaryStorageContentScopeConfigPluginTest {

    lateinit var testee: FingerprintingTemporaryStorageContentScopeConfigPlugin

    private val mockFingerprintingTemporaryStorageRepository: FingerprintingTemporaryStorageRepository = mock()

    @Before
    fun before() {
        testee = FingerprintingTemporaryStorageContentScopeConfigPlugin(mockFingerprintingTemporaryStorageRepository)
    }

    @Test
    fun whenGetConfigThenReturnCorrectlyFormattedJson() {
        whenever(mockFingerprintingTemporaryStorageRepository.fingerprintingTemporaryStorageEntity)
            .thenReturn(FingerprintingTemporaryStorageEntity(json = config))
        assertEquals("\"fingerprintingTemporaryStorage\":$config", testee.config())
    }

    @Test
    fun whenGetPreferencesThenReturnNull() {
        assertNull(testee.preferences())
    }

    companion object {
        const val config = "{\"key\":\"value\"}"
    }
}
