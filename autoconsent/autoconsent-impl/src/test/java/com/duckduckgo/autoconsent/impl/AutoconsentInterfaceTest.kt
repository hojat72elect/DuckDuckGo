

package com.duckduckgo.autoconsent.impl

import android.webkit.WebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.autoconsent.api.AutoconsentCallback
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class AutoconsentInterfaceTest {
    private val mockWebView: WebView = mock()
    private val mockAutoconsentCallback: AutoconsentCallback = mock()
    private val pluginPoint = FakePluginPoint()

    lateinit var autoconsentInterface: AutoconsentInterface

    @Before
    fun setup() {
        autoconsentInterface = AutoconsentInterface(pluginPoint, mockWebView, mockAutoconsentCallback)
    }

    @Test
    fun whenMessagedParsedIfTypeMatchesThenCallProcess() {
        val message = """{"type":"fake"}"""

        autoconsentInterface.process(message)

        assertEquals(1, pluginPoint.plugin.count)
    }

    @Test
    fun whenMessagedParsedIfTypeDoesNotMatchThenDoNotCallProcess() {
        val message = """{"type":"noMatchingType"}"""

        autoconsentInterface.process(message)

        assertEquals(0, pluginPoint.plugin.count)
    }
}
