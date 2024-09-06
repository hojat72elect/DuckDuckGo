package com.duckduckgo.cookies.impl

import android.webkit.CookieManager
import android.webkit.ValueCallback
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.cookies.api.CookieManagerProvider
import com.duckduckgo.cookies.api.RemoveCookiesStrategy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

private data class Cookie(
    val url: String,
    val value: String,
)

class WebViewCookieManagerTest {
    @get:Rule
    @Suppress("unused")
    val coroutineRule = CoroutineTestRule()

    private val removeCookieStrategy = mock<RemoveCookiesStrategy>()
    private val cookieManagerProvider = mock<CookieManagerProvider>()
    private val cookieManager = mock<CookieManager>()
    private val ddgCookie = Cookie(DDG_HOST, "da=abc")
    private val externalHostCookie = Cookie("example.com", "dz=zyx")
    private val testee: WebViewCookieManager = WebViewCookieManager(
        cookieManagerProvider,
        removeCookieStrategy,
        coroutineRule.testDispatcherProvider,
    )

    @Before
    fun setup() {
        whenever(cookieManagerProvider.get()).thenReturn(cookieManager)
        whenever(cookieManager.setCookie(any(), any(), any())).then {
            (it.getArgument(2) as ValueCallback<Boolean>).onReceiveValue(true)
        }
    }

    @Test
    fun whenCookiesRemovedThenInternalCookiesRecreated() = runTest {
        givenCookieManagerWithCookies(ddgCookie, externalHostCookie)

        withContext(coroutineRule.testDispatcherProvider.main()) {
            testee.removeExternalCookies()
        }

        verify(cookieManager).setCookie(eq(ddgCookie.url), eq(ddgCookie.value), any())
    }

    @Test
    fun whenCookiesStoredThenRemoveCookiesExecuted() = runTest {
        givenCookieManagerWithCookies(ddgCookie, externalHostCookie)

        withContext(coroutineRule.testDispatcherProvider.main()) {
            testee.removeExternalCookies()
        }

        verify(removeCookieStrategy).removeCookies()
    }

    @Test
    fun whenCookiesStoredThenFlushBeforeAndAfterInteractingWithCookieManager() = runTest {
        givenCookieManagerWithCookies(ddgCookie, externalHostCookie)

        withContext(coroutineRule.testDispatcherProvider.main()) {
            testee.removeExternalCookies()
        }

        cookieManager.inOrder {
            verify().flush()
            verify().getCookie(DDG_HOST)
            verify().hasCookies()
            verify().setCookie(eq(DDG_HOST), any(), any())
            verify().flush()
        }
    }

    @Test
    fun whenNoCookiesThenRemoveProcessNotExecuted() = runTest {
        givenCookieManagerWithCookies()

        withContext(coroutineRule.testDispatcherProvider.main()) {
            testee.removeExternalCookies()
        }

        verifyNoInteractions(removeCookieStrategy)
    }

    private fun givenCookieManagerWithCookies(vararg cookies: Cookie) {
        if (cookies.isEmpty()) {
            whenever(cookieManager.hasCookies()).thenReturn(false)
        } else {
            whenever(cookieManager.hasCookies()).thenReturn(true)
            cookies.forEach { cookie ->
                whenever(cookieManager.getCookie(cookie.url)).thenReturn(cookie.value)
            }
        }
    }

    companion object {
        private const val DDG_HOST = "https://duckduckgo.com"
    }
}
