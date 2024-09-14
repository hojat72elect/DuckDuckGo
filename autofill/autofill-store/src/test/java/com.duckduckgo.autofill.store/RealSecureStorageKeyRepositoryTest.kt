

package com.duckduckgo.autofill.store

import com.duckduckgo.securestorage.store.RealSecureStorageKeyRepository
import com.duckduckgo.securestorage.store.keys.SecureStorageKeyStore
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealSecureStorageKeyRepositoryTest {
    @Mock
    private lateinit var keyStore: SecureStorageKeyStore
    private lateinit var testee: RealSecureStorageKeyRepository
    private val testValue = "TestValue".toByteArray()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testee = RealSecureStorageKeyRepository(keyStore)
    }

    @Test
    fun whenPasswordIsSetThenUpdateKeyForPasswordInKeyStore() {
        testee.password = testValue

        verify(keyStore).updateKey("KEY_GENERATED_PASSWORD", testValue)
    }

    @Test
    fun whenGettingPasswordThenGetKeyForPasswordInKeyStore() {
        whenever(keyStore.getKey("KEY_GENERATED_PASSWORD")).thenReturn(testValue)

        assertEquals(testValue, testee.password)
    }

    @Test
    fun whenL1KeyIsSetThenUpdateKeyForL1KeyInKeyStore() {
        testee.l1Key = testValue

        verify(keyStore).updateKey("KEY_L1KEY", testValue)
    }

    @Test
    fun whenGettingL1KeyThenGetKeyForL1KeyInKeyStore() {
        whenever(keyStore.getKey("KEY_L1KEY")).thenReturn(testValue)

        assertEquals(testValue, testee.l1Key)
    }

    @Test
    fun whenPasswordSaltIsSetThenUpdateKeyForPasswordSaltInKeyStore() {
        testee.passwordSalt = testValue

        verify(keyStore).updateKey("KEY_PASSWORD_SALT", testValue)
    }

    @Test
    fun whenGettingPasswordSaltThenGetKeyForPasswordSaltKeyInKeyStore() {
        whenever(keyStore.getKey("KEY_PASSWORD_SALT")).thenReturn(testValue)

        assertEquals(testValue, testee.passwordSalt)
    }

    @Test
    fun whenEncryptedL2KeyIsSetThenUpdateKeyForEncryptedL2KeyInKeyStore() {
        testee.encryptedL2Key = testValue

        verify(keyStore).updateKey("KEY_ENCRYPTED_L2KEY", testValue)
    }

    @Test
    fun whenGettingEncryptedL2KeyThenGetKeyForEncryptedL2KeyInKeyStore() {
        whenever(keyStore.getKey("KEY_ENCRYPTED_L2KEY")).thenReturn(testValue)

        assertEquals(testValue, testee.encryptedL2Key)
    }

    @Test
    fun whenEncryptedL2KeyIVIsSetThenUpdateKeyForEncryptedL2KeyIVInKeyStore() {
        testee.encryptedL2KeyIV = testValue

        verify(keyStore).updateKey("KEY_ENCRYPTED_L2KEY_IV", testValue)
    }

    @Test
    fun whenGettingEncryptedL2KeyIVThenGetKeyForEncryptedL2KeyIVInKeyStore() {
        whenever(keyStore.getKey("KEY_ENCRYPTED_L2KEY_IV")).thenReturn(testValue)

        assertEquals(testValue, testee.encryptedL2KeyIV)
    }
}
