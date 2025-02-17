

package com.duckduckgo.autoconsent.impl.ui

import android.webkit.WebView
import app.cash.turbine.test
import com.duckduckgo.autoconsent.api.Autoconsent
import com.duckduckgo.autoconsent.api.AutoconsentCallback
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AutoconsentSettingsViewModelTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val autoconsent: Autoconsent = FakeAutoconsent()

    private val viewModel = AutoconsentSettingsViewModel(autoconsent)

    @Test
    fun whenViewModelCreatedThenEmitViewState() = runTest {
        viewModel.viewState.test {
            assertFalse(awaitItem().autoconsentEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenOnLearnMoreSelectedCalledThenLaunchLearnMoreWebPageCommandIsSent() = runTest {
        viewModel.commands().test {
            viewModel.onLearnMoreSelected()
            assertEquals(AutoconsentSettingsViewModel.Command.LaunchLearnMoreWebPage(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenOnUserToggleAutoconsentToTrueThenAutoconsentEnabledIsTrue() = runTest {
        viewModel.viewState.test {
            assertFalse(awaitItem().autoconsentEnabled)
            viewModel.onUserToggleAutoconsent(true)
            assertTrue(autoconsent.isSettingEnabled())
            assertTrue(awaitItem().autoconsentEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenOnUserToggleAutoconsentToFalseThenAutoconsentEnabledIsFalse() = runTest {
        viewModel.viewState.test {
            viewModel.onUserToggleAutoconsent(false)
            assertFalse(awaitItem().autoconsentEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    internal class FakeAutoconsent : Autoconsent {
        var test: Boolean = false

        override fun injectAutoconsent(webView: WebView, url: String) {
            // NO OP
        }

        override fun addJsInterface(
            webView: WebView,
            autoconsentCallback: AutoconsentCallback,
        ) {
            // NO OP
        }

        override fun changeSetting(setting: Boolean) {
            test = setting
        }

        override fun isSettingEnabled(): Boolean = test

        override fun isAutoconsentEnabled(): Boolean {
            return isSettingEnabled()
        }

        override fun setAutoconsentOptOut(webView: WebView) {
            // NO OP
        }

        override fun setAutoconsentOptIn() {
            // NO OP
        }

        override fun firstPopUpHandled() {
            // NO OP
        }
    }
}
