

package com.duckduckgo.securestorage

import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.autofill.impl.securestorage.DerivedKeySecretFactory
import com.duckduckgo.autofill.impl.securestorage.RealSecureStorageKeyGenerator
import java.security.Key
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealSecureStorageKeyGeneratorTest {
    @Mock
    private lateinit var appBuildConfig: AppBuildConfig

    @Mock
    private lateinit var derivedKeySecretFactory: DerivedKeySecretFactory

    @Mock
    private lateinit var legacyDerivedKeySecretFactory: DerivedKeySecretFactory

    private lateinit var testee: RealSecureStorageKeyGenerator

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        val key = mock(Key::class.java)
        whenever(key.encoded).thenReturn(randomBytes)
        whenever(derivedKeySecretFactory.getKey(any())).thenReturn(key)
        whenever(legacyDerivedKeySecretFactory.getKey(any())).thenReturn(key)

        testee = RealSecureStorageKeyGenerator { derivedKeySecretFactory }
    }

    @Test
    fun whenKeyIsGeneratedThenAlgorithmShouldBeAES() {
        assertEquals("AES", testee.generateKey().algorithm)
    }

    @Test
    fun whenKeyIsGeneratedFromKeyMaterialThenAlgorithmShouldBeAES() {
        val keyMaterial = randomBytes
        assertEquals("AES", testee.generateKeyFromKeyMaterial(keyMaterial).algorithm)
    }

    @Test
    fun whenKeyIsGeneratedFromPasswordForSDK26MaterialThenUseDerivedKeySecretFactoryAndAlgorithmShouldBeAES() {
        whenever(appBuildConfig.sdkInt).thenReturn(26)

        val result = testee.generateKeyFromPassword("password", randomBytes)

        verify(derivedKeySecretFactory).getKey(any())
        assertEquals("AES", result.algorithm)
    }

    companion object {
        private val randomBytes = "Zm9vYg==".toByteArray()
    }
}
