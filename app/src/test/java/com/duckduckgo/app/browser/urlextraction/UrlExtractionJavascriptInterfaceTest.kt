

package com.duckduckgo.app.browser.urlextraction

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UrlExtractionJavascriptInterfaceTest {

    @Test
    fun whenUrlExtractedThenInvokeCallbackWithUrl() {
        val onUrlExtracted = mock<(extractedUrl: String?) -> Unit>()
        val urlExtractionInterface = UrlExtractionJavascriptInterface(onUrlExtracted)

        urlExtractionInterface.urlExtracted("example.com")

        verify(onUrlExtracted).invoke("example.com")
    }

    @Test
    fun whenUrlIsUndefinedThenInvokeCallbackWithNull() {
        val onUrlExtracted = mock<(extractedUrl: String?) -> Unit>()
        val urlExtractionInterface = UrlExtractionJavascriptInterface(onUrlExtracted)

        urlExtractionInterface.urlExtracted(null)

        verify(onUrlExtracted).invoke(null)
    }
}
