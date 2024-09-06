package com.duckduckgo.cookies.impl

import com.duckduckgo.cookies.store.CookieEntity
import com.duckduckgo.cookies.store.contentscopescripts.ContentScopeScriptsCookieRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CookiesContentScopeConfigPluginTest {

    lateinit var testee: CookiesContentScopeConfigPlugin

    private val mockContentScopeScriptsCookieRepository: ContentScopeScriptsCookieRepository =
        mock()

    @Before
    fun before() {
        testee = CookiesContentScopeConfigPlugin(mockContentScopeScriptsCookieRepository)
    }

    @Test
    fun whenGetConfigThenReturnCorrectlyFormattedJson() {
        whenever(mockContentScopeScriptsCookieRepository.getCookieEntity()).thenReturn(
            CookieEntity(
                json = config
            )
        )
        assertEquals("\"cookie\":$config", testee.config())
    }

    @Test
    fun whenGetPreferencesThenReturnNull() {
        assertNull(testee.preferences())
    }

    companion object {
        const val config = "{\"key\":\"value\"}"
    }
}
