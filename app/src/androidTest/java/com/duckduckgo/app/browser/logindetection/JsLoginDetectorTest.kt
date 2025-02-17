

package com.duckduckgo.app.browser.logindetection

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.test.annotation.UiThreadTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.browser.logindetection.LoginDetectionJavascriptInterface.Companion.JAVASCRIPT_INTERFACE_NAME
import com.duckduckgo.app.fire.fireproofwebsite.ui.AutomaticFireproofSetting
import com.duckduckgo.app.settings.db.SettingsDataStore
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

class JsLoginDetectorTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val settingsDataStore: SettingsDataStore = mock()
    private val testee = JsLoginDetector(settingsDataStore)

    @UiThreadTest
    @Test
    @SdkSuppress(minSdkVersion = 24)
    fun whenAddLoginDetectionThenJSInterfaceAdded() = runTest {
        val webView = spy(WebView(InstrumentationRegistry.getInstrumentation().targetContext))
        testee.addLoginDetection(webView) {}
        verify(webView).addJavascriptInterface(any<LoginDetectionJavascriptInterface>(), eq(JAVASCRIPT_INTERFACE_NAME))
    }

    @UiThreadTest
    @Test
    @SdkSuppress(minSdkVersion = 24)
    fun whenLoginDetectionDisabledAndPageStartedEventThenNoWebViewInteractions() = runTest {
        whenever(settingsDataStore.automaticFireproofSetting).thenReturn(AutomaticFireproofSetting.NEVER)
        val webView = spy(WebView(InstrumentationRegistry.getInstrumentation().targetContext))
        testee.onEvent(WebNavigationEvent.OnPageStarted(webView))
        verifyNoInteractions(webView)
    }

    @UiThreadTest
    @Test
    @SdkSuppress(minSdkVersion = 24)
    fun whenLoginDetectionDisabledAndInterceptRequestEventThenNoWebViewInteractions() = runTest {
        whenever(settingsDataStore.automaticFireproofSetting).thenReturn(AutomaticFireproofSetting.NEVER)
        val webView = spy(WebView(InstrumentationRegistry.getInstrumentation().targetContext))
        val webResourceRequest = aWebResourceRequest()
        testee.onEvent(WebNavigationEvent.ShouldInterceptRequest(webView, webResourceRequest))
        verifyNoInteractions(webView)
    }

    @UiThreadTest
    @Test
    @SdkSuppress(minSdkVersion = 24)
    fun whenLoginDetectionEnabledAndPageStartedEventThenJSLoginDetectionInjected() = runTest {
        whenever(settingsDataStore.automaticFireproofSetting).thenReturn(AutomaticFireproofSetting.ASK_EVERY_TIME)
        val webView = spy(WebView(InstrumentationRegistry.getInstrumentation().targetContext))
        testee.onEvent(WebNavigationEvent.OnPageStarted(webView))
        verify(webView).evaluateJavascript(any(), anyOrNull())
    }

    @UiThreadTest
    @Test
    @SdkSuppress(minSdkVersion = 24)
    fun whenLoginDetectionEnabledAndLoginPostRequestCapturedThenJSLoginDetectionInjected() = runTest {
        whenever(settingsDataStore.automaticFireproofSetting).thenReturn(AutomaticFireproofSetting.ASK_EVERY_TIME)
        val webView = spy(WebView(InstrumentationRegistry.getInstrumentation().targetContext))
        val webResourceRequest = aWebResourceRequest("POST", "login")
        testee.onEvent(WebNavigationEvent.ShouldInterceptRequest(webView, webResourceRequest))
        verify(webView).evaluateJavascript(any(), anyOrNull())
    }

    @UiThreadTest
    @Test
    @SdkSuppress(minSdkVersion = 24)
    fun whenLoginDetectionEnabledAndNoLoginPostRequestCapturedThenNoWebViewInteractions() = runTest {
        whenever(settingsDataStore.automaticFireproofSetting).thenReturn(AutomaticFireproofSetting.ASK_EVERY_TIME)
        val webView = spy(WebView(InstrumentationRegistry.getInstrumentation().targetContext))
        val webResourceRequest = aWebResourceRequest("POST", "")
        testee.onEvent(WebNavigationEvent.ShouldInterceptRequest(webView, webResourceRequest))
        verifyNoInteractions(webView)
    }

    @UiThreadTest
    @Test
    @SdkSuppress(minSdkVersion = 24)
    fun whenLoginDetectionEnabledAndGetRequestCapturedThenNoWebViewInteractions() = runTest {
        whenever(settingsDataStore.automaticFireproofSetting).thenReturn(AutomaticFireproofSetting.ASK_EVERY_TIME)
        val webView = spy(WebView(InstrumentationRegistry.getInstrumentation().targetContext))
        val webResourceRequest = aWebResourceRequest("GET")
        testee.onEvent(WebNavigationEvent.ShouldInterceptRequest(webView, webResourceRequest))
        verifyNoInteractions(webView)
    }

    private fun aWebResourceRequest(
        httpMethod: String = "POST",
        path: String = "login",
    ): WebResourceRequest {
        return object : WebResourceRequest {
            override fun getUrl(): Uri = Uri.parse("https://example.com/$path")

            override fun isRedirect(): Boolean = false

            override fun getMethod(): String = httpMethod

            override fun getRequestHeaders(): MutableMap<String, String> = mutableMapOf()

            override fun hasGesture(): Boolean = false

            override fun isForMainFrame(): Boolean = false
        }
    }
}
