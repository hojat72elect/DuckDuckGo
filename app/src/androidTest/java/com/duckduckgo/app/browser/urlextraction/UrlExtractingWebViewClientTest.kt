

package com.duckduckgo.app.browser.urlextraction

import android.webkit.CookieManager
import android.webkit.WebView
import androidx.core.net.toUri
import androidx.test.annotation.UiThreadTest
import com.duckduckgo.app.browser.*
import com.duckduckgo.app.browser.certificates.rootstore.TrustedCertificateStore
import com.duckduckgo.app.browser.cookies.ThirdPartyCookieManager
import com.duckduckgo.app.browser.httpauth.WebViewHttpAuthStore
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.cookies.api.CookieManagerProvider
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UrlExtractingWebViewClientTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private lateinit var testee: UrlExtractingWebViewClient

    private val requestInterceptor: RequestInterceptor = mock()
    private val cookieManagerProvider: CookieManagerProvider = mock()
    private val cookieManager: CookieManager = mock()
    private val trustedCertificateStore: TrustedCertificateStore = mock()
    private val webViewHttpAuthStore: WebViewHttpAuthStore = mock()
    private val thirdPartyCookieManager: ThirdPartyCookieManager = mock()
    private val urlExtractor: DOMUrlExtractor = mock()
    private val mockWebView: WebView = mock()

    @UiThreadTest
    @Before
    fun setup() {
        testee = UrlExtractingWebViewClient(
            webViewHttpAuthStore,
            trustedCertificateStore,
            requestInterceptor,
            cookieManagerProvider,
            thirdPartyCookieManager,
            TestScope(),
            coroutinesTestRule.testDispatcherProvider,
            urlExtractor,
        )
        whenever(cookieManagerProvider.get()).thenReturn(cookieManager)
    }

    @UiThreadTest
    @Test
    fun whenOnPageStartedCalledThenInjectUrlExtractionJS() {
        testee.onPageStarted(mockWebView, BrowserWebViewClientTest.EXAMPLE_URL, null)
        verify(urlExtractor).injectUrlExtractionJS(mockWebView)
    }

    @UiThreadTest
    @Test
    fun whenOnPageStartedCalledThenProcessUriForThirdPartyCookiesCalled() = runTest {
        testee.onPageStarted(mockWebView, EXAMPLE_URL, null)
        verify(thirdPartyCookieManager).processUriForThirdPartyCookies(mockWebView, EXAMPLE_URL.toUri())
    }

    @UiThreadTest
    @Test
    fun whenOnPageFinishedCalledThenFlushCookies() {
        testee.onPageFinished(mockWebView, null)
        verify(cookieManager).flush()
    }

    companion object {
        const val EXAMPLE_URL = "example.com"
    }
}
