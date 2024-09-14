

package com.duckduckgo.fingerprintprotection.impl.features.fingerprintingcanvas

import com.duckduckgo.fingerprintprotection.store.FingerprintingCanvasEntity
import com.duckduckgo.fingerprintprotection.store.features.fingerprintingcanvas.FingerprintingCanvasRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FingerprintingCanvasContentScopeConfigPluginTest {

    lateinit var testee: FingerprintingCanvasContentScopeConfigPlugin

    private val mockFingerprintingCanvasRepository: FingerprintingCanvasRepository = mock()

    @Before
    fun before() {
        testee = FingerprintingCanvasContentScopeConfigPlugin(mockFingerprintingCanvasRepository)
    }

    @Test
    fun whenGetConfigThenReturnCorrectlyFormattedJson() {
        whenever(mockFingerprintingCanvasRepository.fingerprintingCanvasEntity).thenReturn(FingerprintingCanvasEntity(json = config))
        assertEquals("\"fingerprintingCanvas\":$config", testee.config())
    }

    @Test
    fun whenGetPreferencesThenReturnNull() {
        assertNull(testee.preferences())
    }

    companion object {
        const val config = "{\"key\":\"value\"}"
    }
}
