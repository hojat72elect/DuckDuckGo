

package com.duckduckgo.autoconsent.impl.handlers

import android.webkit.WebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.autoconsent.api.AutoconsentCallback
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@RunWith(AndroidJUnit4::class)
class SelfTestResultMessageHandlerPluginTest {

    private val mockCallback: AutoconsentCallback = mock()
    private val webView: WebView = WebView(InstrumentationRegistry.getInstrumentation().targetContext)

    private val selfTestPlugin = SelfTestResultMessageHandlerPlugin()

    @Test
    fun whenProcessIfMessageTypeIsNotSelfTestThenDoNothing() {
        selfTestPlugin.process("noMatching", "", webView, mockCallback)

        verifyNoInteractions(mockCallback)
    }

    @Test
    fun whenProcessIfCannotParseMessageThenDoNothing() {
        val message = """
            {"type":"${selfTestPlugin.supportedTypes.first()}", cmp: "test", "result": true, "url": "http://example.com"}
        """.trimIndent()

        selfTestPlugin.process(selfTestPlugin.supportedTypes.first(), message, webView, mockCallback)

        verifyNoInteractions(mockCallback)
    }

    @Test
    fun whenProcessThenCallDashboardWithCorrectParameters() {
        val message = """
            {"type":"${selfTestPlugin.supportedTypes.first()}", "cmp": "test", "result": true, "url": "http://example.com"}
        """.trimIndent()

        selfTestPlugin.process(selfTestPlugin.supportedTypes.first(), message, webView, mockCallback)

        verify(mockCallback).onResultReceived(consentManaged = true, optOutFailed = false, selfTestFailed = true, isCosmetic = null)

        val anotherMessage = """
            {"type":"${selfTestPlugin.supportedTypes}", "cmp": "test", "result": false, "url": "http://example.com"}
        """.trimIndent()

        selfTestPlugin.process(selfTestPlugin.supportedTypes.first(), anotherMessage, webView, mockCallback)

        verify(mockCallback).onResultReceived(consentManaged = true, optOutFailed = false, selfTestFailed = false, isCosmetic = null)
    }
}
