

package com.duckduckgo.app.browser.logindetection

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.annotation.UiThread
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.logindetection.LoginDetectionJavascriptInterface.Companion.JAVASCRIPT_INTERFACE_NAME
import com.duckduckgo.app.fire.fireproofwebsite.ui.AutomaticFireproofSetting
import com.duckduckgo.app.settings.db.SettingsDataStore
import com.duckduckgo.common.utils.getValidUrl
import javax.inject.Inject
import timber.log.Timber

interface DOMLoginDetector {
    fun addLoginDetection(
        webView: WebView,
        onLoginDetected: () -> Unit,
    )

    fun onEvent(event: WebNavigationEvent)
}

sealed class WebNavigationEvent {
    data class OnPageStarted(val webView: WebView) : WebNavigationEvent()
    data class ShouldInterceptRequest(
        val webView: WebView,
        val request: WebResourceRequest,
    ) : WebNavigationEvent()
}

class JsLoginDetector @Inject constructor(private val settingsDataStore: SettingsDataStore) :
    DOMLoginDetector {
    private val javaScriptDetector = JavaScriptDetector()
    private val loginPathRegex = Regex("login|sign-in|signin|session")

    override fun addLoginDetection(
        webView: WebView,
        onLoginDetected: () -> Unit,
    ) {
        webView.addJavascriptInterface(LoginDetectionJavascriptInterface { onLoginDetected() }, JAVASCRIPT_INTERFACE_NAME)
    }

    @UiThread
    override fun onEvent(event: WebNavigationEvent) {
        if (settingsDataStore.automaticFireproofSetting != AutomaticFireproofSetting.NEVER) {
            when (event) {
                is WebNavigationEvent.OnPageStarted -> injectLoginFormDetectionJS(event.webView)
                is WebNavigationEvent.ShouldInterceptRequest -> {
                    if (evaluateIfLoginPostRequest(event.request)) {
                        scanForPasswordFields(event.webView)
                    }
                }
            }
        }
    }

    private fun evaluateIfLoginPostRequest(request: WebResourceRequest): Boolean {
        if (request.method == HTTP_POST) {
            Timber.i("LoginDetector: evaluate ${request.url}")
            val validUrl = request.url.getValidUrl() ?: return false
            if (validUrl.path?.contains(loginPathRegex) == true || validUrl.isOAuthUrl()) {
                Timber.v("LoginDetector: post login DETECTED")
                return true
            }
        }
        return false
    }

    @UiThread
    private fun scanForPasswordFields(webView: WebView) {
        webView.evaluateJavascript("javascript:${javaScriptDetector.loginFormDetector(webView.context)}", null)
    }

    @UiThread
    private fun injectLoginFormDetectionJS(webView: WebView) {
        webView.evaluateJavascript("javascript:${javaScriptDetector.loginFormEventsDetector(webView.context)}", null)
    }

    private class JavaScriptDetector {
        private lateinit var functions: String
        private lateinit var handlers: String

        private fun getFunctionsJS(context: Context): String {
            if (!this::functions.isInitialized) {
                functions = context.resources.openRawResource(R.raw.login_form_detection_functions).bufferedReader().use { it.readText() }
            }
            return functions
        }

        private fun getHandlersJS(context: Context): String {
            if (!this::handlers.isInitialized) {
                handlers = context.resources.openRawResource(R.raw.login_form_detection_handlers).bufferedReader().use { it.readText() }
            }
            return handlers
        }

        fun loginFormEventsDetector(context: Context): String {
            return wrapIntoAnonymousFunction(getFunctionsJS(context) + getHandlersJS(context))
        }

        fun loginFormDetector(context: Context): String {
            return wrapIntoAnonymousFunction(getFunctionsJS(context) + "scanForPasswordField();")
        }

        private fun wrapIntoAnonymousFunction(rawJavaScript: String): String {
            return "(function() { $rawJavaScript })();"
        }
    }

    companion object {
        const val HTTP_POST = "POST"
    }
}
