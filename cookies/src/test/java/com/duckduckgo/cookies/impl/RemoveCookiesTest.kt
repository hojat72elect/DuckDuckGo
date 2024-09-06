package com.duckduckgo.cookies.impl

import com.duckduckgo.cookies.api.CookieRemover
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class RemoveCookiesTest {

    private val selectiveCookieRemover = mock<CookieRemover>()
    private val cookieManagerRemover = mock<CookieRemover>()
    private val removeCookies = RemoveCookies(cookieManagerRemover, selectiveCookieRemover)

    @Test
    fun whenSelectiveCookieRemoverSucceedsThenNoMoreInteractions() = runTest {
        selectiveCookieRemover.succeeds()

        removeCookies.removeCookies()

        verifyNoInteractions(cookieManagerRemover)
    }

    @Test
    fun whenSelectiveCookieRemoverFailsThenFallbackToCookieManagerRemover() = runTest {
        selectiveCookieRemover.fails()

        removeCookies.removeCookies()

        verify(cookieManagerRemover).removeCookies()
    }

    private suspend fun CookieRemover.succeeds() {
        whenever(this.removeCookies()).thenReturn(true)
    }

    private suspend fun CookieRemover.fails() {
        whenever(this.removeCookies()).thenReturn(false)
    }
}
