

package com.duckduckgo.app.email

import android.webkit.WebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.browser.DuckDuckGoUrlDetectorImpl
import com.duckduckgo.autofill.api.Autofill
import com.duckduckgo.autofill.api.AutofillFeature
import com.duckduckgo.autofill.api.email.EmailManager
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.feature.toggles.api.Toggle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class EmailJavascriptInterfaceTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockEmailManager: EmailManager = mock()
    private val mockWebView: WebView = mock()
    private lateinit var autofillFeature: AutofillFeature
    private val mockAutofill: Autofill = mock()
    lateinit var testee: EmailJavascriptInterface
    private var counter = 0

    @Before
    fun setup() {
        autofillFeature = com.duckduckgo.autofill.api.FakeAutofillFeature.create()

        testee = EmailJavascriptInterface(
            mockEmailManager,
            mockWebView,
            DuckDuckGoUrlDetectorImpl(),
            coroutineRule.testDispatcherProvider,
            autofillFeature,
            mockAutofill,
        ) { counter++ }

        autofillFeature.self().setEnabled(Toggle.State(enable = true))
        whenever(mockAutofill.isAnException(any())).thenReturn(false)
    }

    @Test
    fun whenIsSignedInAndUrlIsDuckDuckGoEmailThenIsSignedInCalled() {
        whenever(mockWebView.url).thenReturn(DUCKDUCKGO_EMAIL_URL)

        testee.isSignedIn()

        verify(mockEmailManager).isSignedIn()
    }

    @Test
    fun whenIsSignedInAndUrlIsNotDuckDuckGoEmailThenIsSignedInNotCalled() {
        whenever(mockWebView.url).thenReturn(NON_EMAIL_URL)

        testee.isSignedIn()

        verify(mockEmailManager, never()).isSignedIn()
    }

    @Test
    fun whenStoreCredentialsAndUrlIsDuckDuckGoEmailThenStoreCredentialsCalledWithCorrectParameters() {
        whenever(mockWebView.url).thenReturn(DUCKDUCKGO_EMAIL_URL)

        testee.storeCredentials("token", "username", "cohort")

        verify(mockEmailManager).storeCredentials("token", "username", "cohort")
    }

    @Test
    fun whenStoreCredentialsAndUrlIsNotDuckDuckGoEmailThenStoreCredentialsNotCalled() {
        whenever(mockWebView.url).thenReturn(NON_EMAIL_URL)

        testee.storeCredentials("token", "username", "cohort")

        verify(mockEmailManager, never()).storeCredentials("token", "username", "cohort")
    }

    @Test
    fun whenGetUserDataAndUrlIsDuckDuckGoEmailThenGetUserDataCalled() {
        whenever(mockWebView.url).thenReturn(DUCKDUCKGO_EMAIL_URL)

        testee.getUserData()

        verify(mockEmailManager).getUserData()
    }

    @Test
    fun whenGetUserDataAndUrlIsNotDuckDuckGoEmailThenGetUserDataIsNotCalled() {
        whenever(mockWebView.url).thenReturn(NON_EMAIL_URL)

        testee.getUserData()

        verify(mockEmailManager, never()).getUserData()
    }

    @Test
    fun whenShowTooltipThenLambdaCalled() {
        whenever(mockWebView.url).thenReturn(NON_EMAIL_URL)

        testee.showTooltip()

        assertEquals(1, counter)
    }

    @Test
    fun whenShowTooltipAndFeatureDisabledThenLambdaNotCalled() {
        whenever(mockWebView.url).thenReturn(NON_EMAIL_URL)
        autofillFeature.self().setEnabled(Toggle.State(enable = false))

        testee.showTooltip()

        assertEquals(0, counter)
    }

    @Test
    fun whenShowTooltipAndUrlIsAnExceptionThenLambdaNotCalled() {
        whenever(mockWebView.url).thenReturn(NON_EMAIL_URL)
        whenever(mockAutofill.isAnException(any())).thenReturn(true)

        testee.showTooltip()

        assertEquals(0, counter)
    }

    @Test
    fun whenGetDeviceCapabilitiesAndUrlIsDuckDuckGoEmailThenReturnNonEmptyString() {
        whenever(mockWebView.url).thenReturn(DUCKDUCKGO_EMAIL_URL)

        assert(testee.getDeviceCapabilities().isNotBlank())
    }

    @Test
    fun whenGetDeviceCapabilitiesAndUrlIsNotDuckDuckGoEmailThenReturnEmptyString() {
        whenever(mockWebView.url).thenReturn(NON_EMAIL_URL)

        assert(testee.getDeviceCapabilities().isBlank())
    }

    companion object {
        const val DUCKDUCKGO_EMAIL_URL = "https://duckduckgo.com/email"
        const val NON_EMAIL_URL = "https://example.com"
    }
}
