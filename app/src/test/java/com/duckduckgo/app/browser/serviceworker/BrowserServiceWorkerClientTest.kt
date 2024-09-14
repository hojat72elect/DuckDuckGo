

package com.duckduckgo.app.browser.serviceworker

import android.webkit.WebResourceRequest
import androidx.core.net.toUri
import androidx.test.filters.SdkSuppress
import com.duckduckgo.app.browser.RequestInterceptor
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@SdkSuppress(minSdkVersion = 24)
class BrowserServiceWorkerClientTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val requestInterceptor: RequestInterceptor = mock()

    private lateinit var testee: BrowserServiceWorkerClient

    @Before
    fun setup() {
        testee = BrowserServiceWorkerClient(requestInterceptor)
    }

    @Test
    fun whenShouldInterceptRequestAndOriginHeaderExistThenSendItToInterceptor() = runTest {
        val webResourceRequest: WebResourceRequest = mock()
        whenever(webResourceRequest.requestHeaders).thenReturn(mapOf("Origin" to "example.com"))

        testee.shouldInterceptRequest(webResourceRequest)

        verify(requestInterceptor).shouldInterceptFromServiceWorker(webResourceRequest, "example.com".toUri())
    }

    @Test
    fun whenShouldInterceptRequestAndOriginHeaderDoesNotExistButRefererExistThenSendItToInterceptor() = runTest {
        val webResourceRequest: WebResourceRequest = mock()
        whenever(webResourceRequest.requestHeaders).thenReturn(mapOf("Referer" to "example.com"))

        testee.shouldInterceptRequest(webResourceRequest)

        verify(requestInterceptor).shouldInterceptFromServiceWorker(webResourceRequest, "example.com".toUri())
    }

    @Test
    fun whenShouldInterceptRequestAndNoOriginOrRefererHeadersExistThenSendNullToInterceptor() = runTest {
        val webResourceRequest: WebResourceRequest = mock()
        whenever(webResourceRequest.requestHeaders).thenReturn(mapOf())

        testee.shouldInterceptRequest(webResourceRequest)

        verify(requestInterceptor).shouldInterceptFromServiceWorker(webResourceRequest, null)
    }
}
