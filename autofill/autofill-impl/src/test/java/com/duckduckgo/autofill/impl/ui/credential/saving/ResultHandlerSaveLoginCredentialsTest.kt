package com.duckduckgo.autofill.impl.ui.credential.saving

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.autofill.api.AutofillEventListener
import com.duckduckgo.autofill.api.CredentialSavePickerDialog
import com.duckduckgo.autofill.api.domain.app.LoginCredentials
import com.duckduckgo.autofill.impl.AutofillFireproofDialogSuppressor
import com.duckduckgo.autofill.impl.store.InternalAutofillStore
import com.duckduckgo.autofill.impl.ui.credential.saving.declines.AutofillDeclineCounter
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class ResultHandlerSaveLoginCredentialsTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val callback: AutofillEventListener = mock()

    private val autofillFireproofDialogSuppressor: AutofillFireproofDialogSuppressor = mock()
    private val declineCounter: AutofillDeclineCounter = mock()
    private val autofillStore: InternalAutofillStore = mock()
    private val appBuildConfig: AppBuildConfig = mock()

    private val testee = ResultHandlerSaveLoginCredentials(
        autofillFireproofDialogSuppressor = autofillFireproofDialogSuppressor,
        dispatchers = coroutineTestRule.testDispatcherProvider,
        declineCounter = declineCounter,
        autofillStore = autofillStore,
        appBuildConfig = appBuildConfig,
        appCoroutineScope = coroutineTestRule.testScope,
    )

    @Test
    fun whenSaveBundleMissingUrlThenNoAttemptToSaveMade() = runTest {
        val bundle = bundle(url = null, credentials = someLoginCredentials())
        testee.processResult(bundle, context, "tab-id-123", Fragment(), callback)
        verifySaveNeverCalled()
        verifyNoInteractions(callback)
    }

    @Test
    fun whenSaveBundleMissingCredentialsThenNoAttemptToSaveMade() = runTest {
        val bundle = bundle(url = "example.com", credentials = null)
        testee.processResult(bundle, context, "tab-id-123", Fragment(), callback)
        verifySaveNeverCalled()
        verifyNoInteractions(callback)
    }

    @Test
    fun whenSaveBundleWellFormedThenCredentialsAreSaved() = runTest {
        val loginCredentials =
            LoginCredentials(domain = "example.com", username = "foo", password = "bar")
        val bundle = bundle("example.com", loginCredentials)
        whenever(autofillStore.saveCredentials(any(), any())).thenReturn(loginCredentials)
        testee.processResult(bundle, context, "tab-id-123", Fragment(), callback)
        verify(autofillStore).saveCredentials(eq("example.com"), eq(loginCredentials))
        verify(callback).onSavedCredentials(loginCredentials)
    }

    @Test
    fun whenSaveCredentialsForFirstTimeThenDisableDeclineCountMonitoringFlag() = runTest {
        val loginCredentials =
            LoginCredentials(domain = "example.com", username = "foo", password = "bar")
        val bundle = bundle("example.com", loginCredentials)
        whenever(autofillStore.saveCredentials(any(), any())).thenReturn(loginCredentials)
        testee.processResult(bundle, context, "tab-id-123", Fragment(), callback)
        verify(declineCounter).disableDeclineCounter()
    }

    @Test
    fun whenSaveCredentialsUnsuccessfulThenDoesNotDisableDeclineCountMonitoringFlag() = runTest {
        val bundle = bundle("example.com", someLoginCredentials())
        whenever(autofillStore.saveCredentials(any(), any())).thenReturn(null)
        testee.processResult(bundle, context, "tab-id-123", Fragment(), callback)
        verify(declineCounter, never()).disableDeclineCounter()
    }

    private suspend fun verifySaveNeverCalled() {
        verify(autofillStore, never()).saveCredentials(any(), any())
    }

    private fun bundle(
        url: String? = "example.com",
        credentials: LoginCredentials?,
    ): Bundle {
        return Bundle().also {
            it.putString(CredentialSavePickerDialog.KEY_URL, url)
            it.putParcelable(CredentialSavePickerDialog.KEY_CREDENTIALS, credentials)
        }
    }

    private fun someLoginCredentials() =
        LoginCredentials(domain = "example.com", username = "foo", password = "bar")
}
