

package com.duckduckgo.app.browser.urlextraction

import android.content.Context
import android.webkit.WebView
import androidx.annotation.UiThread
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.urlextraction.UrlExtractionJavascriptInterface.Companion.URL_EXTRACTION_JAVASCRIPT_INTERFACE_NAME

interface DOMUrlExtractor {
    fun addUrlExtraction(webView: WebView, onUrlExtracted: (extractedUrl: String?) -> Unit)
    fun injectUrlExtractionJS(webView: WebView)
}

class JsUrlExtractor : DOMUrlExtractor {
    private val javaScriptDetector = JavaScriptDetector()

    override fun addUrlExtraction(webView: WebView, onUrlExtracted: (extractedUrl: String?) -> Unit) {
        webView.addJavascriptInterface(UrlExtractionJavascriptInterface(onUrlExtracted), URL_EXTRACTION_JAVASCRIPT_INTERFACE_NAME)
    }

    @UiThread
    override fun injectUrlExtractionJS(webView: WebView) {
        webView.evaluateJavascript("javascript:${javaScriptDetector.getUrlExtractionJS(webView.context)}", null)
    }

    private class JavaScriptDetector {
        private lateinit var functions: String

        fun getUrlExtractionJS(context: Context): String {
            if (!this::functions.isInitialized) {
                functions = context.resources.openRawResource(R.raw.url_extraction).bufferedReader().use { it.readText() }
            }
            return functions
        }
    }
}
