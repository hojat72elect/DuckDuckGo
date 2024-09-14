

package com.duckduckgo.autoconsent.impl.handlers

import android.webkit.WebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.autoconsent.api.AutoconsentCallback
import com.duckduckgo.autoconsent.impl.FakeSettingsRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class PopUpFoundMessageHandlerPluginTest {

    private val mockCallback: AutoconsentCallback = mock()
    private val webView: WebView = WebView(InstrumentationRegistry.getInstrumentation().targetContext)
    private val repository = FakeSettingsRepository()

    private val popupFoundHandler = PopUpFoundMessageHandlerPlugin(repository)

    @Test
    fun whenProcessIfMessageTypeIsNotPopUpFoundThenDoNothing() {
        popupFoundHandler.process("noMatching", "", webView, mockCallback)

        verify(mockCallback, never()).onFirstPopUpHandled()
    }

    @Test
    fun whenProcessIfSettingEnabledThenDoNothing() {
        repository.userSetting = true

        popupFoundHandler.process(popupFoundHandler.supportedTypes.first(), "", webView, mockCallback)

        verify(mockCallback, never()).onFirstPopUpHandled()
    }

    @Test
    fun whenProcessIfSettingDisabledAndCmpIsNotTopThenCallCallback() {
        repository.userSetting = false

        popupFoundHandler.process(popupFoundHandler.supportedTypes.first(), message("test"), webView, mockCallback)

        verify(mockCallback).onFirstPopUpHandled()
    }

    @Test
    fun whenProcessIfSettingDisabledAndCmpIsTopThenDoNothing() {
        repository.userSetting = false

        popupFoundHandler.process(popupFoundHandler.supportedTypes.first(), message("test-top"), webView, mockCallback)

        verify(mockCallback, never()).onFirstPopUpHandled()
    }

    private fun message(cmp: String): String {
        return """
            {"type":"${popupFoundHandler.supportedTypes.first()}", "cmp": "$cmp", "url": "http://example.com"}
        """.trimIndent()
    }
}
