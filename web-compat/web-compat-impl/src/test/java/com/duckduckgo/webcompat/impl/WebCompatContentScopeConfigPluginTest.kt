

package com.duckduckgo.webcompat.impl

import com.duckduckgo.webcompat.store.WebCompatEntity
import com.duckduckgo.webcompat.store.WebCompatRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class WebCompatContentScopeConfigPluginTest {

    lateinit var testee: WebCompatContentScopeConfigPlugin

    private val mockWebCompatRepository: WebCompatRepository = mock()

    @Before
    fun before() {
        testee = WebCompatContentScopeConfigPlugin(mockWebCompatRepository)
    }

    @Test
    fun whenGetConfigThenReturnCorrectlyFormattedJson() {
        whenever(mockWebCompatRepository.getWebCompatEntity()).thenReturn(WebCompatEntity(json = config))
        assertEquals("\"webCompat\":$config", testee.config())
    }

    @Test
    fun whenGetPreferencesThenReturnNull() {
        assertNull(testee.preferences())
    }

    companion object {
        const val config = "{\"key\":\"value\"}"
    }
}
