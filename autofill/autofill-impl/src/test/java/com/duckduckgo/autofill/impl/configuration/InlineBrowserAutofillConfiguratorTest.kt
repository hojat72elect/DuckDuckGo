

package com.duckduckgo.autofill.impl.configuration

import android.webkit.WebView
import com.duckduckgo.autofill.api.AutofillCapabilityChecker
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class InlineBrowserAutofillConfiguratorTest {

    @get:Rule var coroutineRule = CoroutineTestRule()

    private lateinit var inlineBrowserAutofillConfigurator: InlineBrowserAutofillConfigurator

    private val autofillRuntimeConfigProvider: AutofillRuntimeConfigProvider = mock()
    private val webView: WebView = mock()
    private val autofillCapabilityChecker: AutofillCapabilityChecker = mock()
    private val autofillJavascriptLoader: AutofillJavascriptLoader = mock()

    @Before
    fun before() = runTest {
        whenever(autofillJavascriptLoader.getAutofillJavascript()).thenReturn("")
        whenever(autofillRuntimeConfigProvider.getRuntimeConfiguration(any(), any())).thenReturn("")

        inlineBrowserAutofillConfigurator = InlineBrowserAutofillConfigurator(
            autofillRuntimeConfigProvider,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            autofillCapabilityChecker,
            autofillJavascriptLoader,
        )
    }

    @Test
    fun whenFeatureIsNotEnabledThenDoNotInject() = runTest {
        givenFeatureIsDisabled()
        inlineBrowserAutofillConfigurator.configureAutofillForCurrentPage(webView, "https://example.com")

        verify(webView, never()).evaluateJavascript("javascript:", null)
    }

    @Test
    fun whenFeatureIsEnabledThenInject() = runTest {
        givenFeatureIsEnabled()
        inlineBrowserAutofillConfigurator.configureAutofillForCurrentPage(webView, "https://example.com")

        verify(webView).evaluateJavascript("javascript:", null)
    }

    private suspend fun givenFeatureIsEnabled() {
        whenever(autofillCapabilityChecker.isAutofillEnabledByConfiguration(any())).thenReturn(true)
    }

    private suspend fun givenFeatureIsDisabled() {
        whenever(autofillCapabilityChecker.isAutofillEnabledByConfiguration(any())).thenReturn(false)
    }
}
