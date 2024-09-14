

package com.duckduckgo.securestorage

import com.duckduckgo.autofill.impl.securestorage.RealL2DataTransformer
import com.duckduckgo.autofill.impl.securestorage.SecureStorageKeyProvider
import java.security.Key
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class RealL2DataTransformerTest {
    private lateinit var testee: RealL2DataTransformer
    private lateinit var encryptionHelper: FakeEncryptionHelper

    @Mock
    private lateinit var secureStorageKeyProvider: SecureStorageKeyProvider

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val key = mock(Key::class.java)
        whenever(secureStorageKeyProvider.getl2Key()).thenReturn(key)
        encryptionHelper = FakeEncryptionHelper(expectedEncryptedData, expectedEncryptedIv, expectedDecryptedData)
        testee = RealL2DataTransformer(encryptionHelper, secureStorageKeyProvider)
    }

    @Test
    fun whenCanProcessDataThenReturnCanAccessKeyStoreTrue() {
        whenever(secureStorageKeyProvider.canAccessKeyStore()).thenReturn(true)

        assertTrue(testee.canProcessData())
    }

    @Test
    fun whenCanProcessDataFalseThenReturnCanAccessKeyStoreFalse() {
        whenever(secureStorageKeyProvider.canAccessKeyStore()).thenReturn(false)

        assertFalse(testee.canProcessData())
    }

    @Test
    fun whenDataIsEncryptedThenDelegateEncryptionToEncryptionHelper() {
        val result = testee.encrypt("test123")

        assertEquals(expectedEncryptedData, result.data)
        assertEquals(expectedEncryptedIv, result.iv)
    }

    @Test
    fun whenDataIsDecryptedThenDelegateDecryptionToEncryptionHelper() {
        val result = testee.decrypt("test123", "iv")

        assertEquals(decodedDecryptedData, result)
    }

    companion object {
        private const val expectedEncryptedData: String = "ZXhwZWN0ZWRFbmNyeXB0ZWREYXRh"
        private const val expectedEncryptedIv: String = "ZXhwZWN0ZWRFbmNyeXB0ZWRJVg=="
        private const val expectedDecryptedData: String = "ZXhwZWN0ZWREZWNyeXB0ZWREYXRh"
        private const val decodedDecryptedData: String = "expectedDecryptedData"
    }
}
