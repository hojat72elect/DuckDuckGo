

package com.duckduckgo.app.onboarding.ui.page

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.duckduckgo.app.browser.defaultbrowsing.DefaultBrowserDetector
import com.duckduckgo.app.global.install.AppInstallStore
import com.duckduckgo.app.onboarding.ui.page.DefaultBrowserPageViewModel.Command
import com.duckduckgo.app.onboarding.ui.page.DefaultBrowserPageViewModel.Companion.MAX_DIALOG_ATTEMPTS
import com.duckduckgo.app.onboarding.ui.page.DefaultBrowserPageViewModel.Origin
import com.duckduckgo.app.onboarding.ui.page.DefaultBrowserPageViewModel.ViewState.*
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.pixels.Pixel.PixelValues.DEFAULT_BROWSER_DIALOG
import com.duckduckgo.app.statistics.pixels.Pixel.PixelValues.DEFAULT_BROWSER_SETTINGS
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class DefaultBrowserPageViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockDefaultBrowserDetector: DefaultBrowserDetector

    @Mock
    private lateinit var mockPixel: Pixel

    @Mock
    private lateinit var mockInstallStore: AppInstallStore

    @Mock
    private lateinit var mockCommandObserver: Observer<Command>

    private val commandCaptor = argumentCaptor<Command>()

    private lateinit var testee: DefaultBrowserPageViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testee = DefaultBrowserPageViewModel(mockDefaultBrowserDetector, mockPixel, mockInstallStore)
        testee.command.observeForever(mockCommandObserver)
    }

    @After
    fun after() {
        testee.command.removeObserver(mockCommandObserver)
    }

    @Test
    fun whenInitializingIfThereIsADefaultBrowserThenShowSettingsUI() {
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(true)

        testee = DefaultBrowserPageViewModel(mockDefaultBrowserDetector, mockPixel, mockInstallStore)

        assertTrue(viewState() is DefaultBrowserSettingsUI)
    }

    @Test
    fun whenInitializingIfThereIsNotADefaultBrowserThenShowDialogUI() {
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(false)

        testee = DefaultBrowserPageViewModel(mockDefaultBrowserDetector, mockPixel, mockInstallStore)

        assertTrue(viewState() is DefaultBrowserDialogUI)
    }

    @Test
    fun whenLoadUiThenShowSettingsUiIfDefaultBrowserIsTrue() {
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(true)

        testee.loadUI()

        assertTrue(viewState() is DefaultBrowserSettingsUI)
    }

    @Test
    fun whenLoadUiThenShowDialogUiIfDefaultBrowserIsFalse() {
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(false)

        testee.loadUI()

        assertTrue(viewState() is DefaultBrowserDialogUI)
    }

    @Test
    fun whenLoadUIAfterDefaultButtonClickedThenSameState() {
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(false)
        testee.loadUI()
        testee.onDefaultBrowserClicked()
        val expectedViewState = viewState()

        testee.loadUI()

        assertEquals(expectedViewState, viewState())
    }

    @Test
    fun whenContinueButtonClickedWithoutTryingToSetDDGAsDefaultThenSendPixelAndExecuteContinueToBrowserCommand() {
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        testee.loadUI()

        testee.onContinueToBrowser()

        assertTrue(captureCommands().lastValue is Command.ContinueToBrowser)
    }

    @Test
    fun whenDefaultButtonClickedWithDefaultBrowserThenExecuteOpenSettingsCommandAndFireDefaultBrowserLaunchedPixel() {
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(true)
        val params = mapOf(
            Pixel.PixelParameter.DEFAULT_BROWSER_BEHAVIOUR_TRIGGERED to DEFAULT_BROWSER_SETTINGS,
        )
        testee.loadUI()

        testee.onDefaultBrowserClicked()

        assertTrue(captureCommands().lastValue is Command.OpenSettings)
    }

    @Test
    fun whenDefaultButtonClickedWithoutDefaultBrowserThenExecuteOpenDialogCommandAndShowInstructionsCard() {
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(false)
        testee.loadUI()

        testee.onDefaultBrowserClicked()

        assertTrue(captureCommands().lastValue is Command.OpenDialog)
        assertEquals(DefaultBrowserDialogUI(showInstructionsCard = true), viewState())
    }

    @Test
    fun whenUserSetDDGAsDefaultFromDialogThenContinueToBrowserAndFirePixel() {
        val params = mapOf(
            Pixel.PixelParameter.DEFAULT_BROWSER_SET_FROM_ONBOARDING to true.toString(),
            Pixel.PixelParameter.DEFAULT_BROWSER_SET_ORIGIN to DEFAULT_BROWSER_DIALOG,
        )
        testee.loadUI()
        testee.onDefaultBrowserClicked()
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(true)

        testee.handleResult(Origin.InternalBrowser)

        assertTrue(captureCommands().lastValue is Command.ContinueToBrowser)
        verify(mockPixel).fire(AppPixelName.DEFAULT_BROWSER_SET, params)
    }

    @Test
    fun whenUserSetDDGAsJustOnceForFirstTimeThenShowInstructionsAgainOpenDialogAndFirePixel() {
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        testee.loadUI()
        testee.onDefaultBrowserClicked()

        testee.handleResult(Origin.InternalBrowser)

        assertEquals(viewState(), DefaultBrowserDialogUI(showInstructionsCard = true))
        assertTrue(captureCommands().lastValue is Command.OpenDialog)
        assertEquals(2, testee.timesPressedJustOnce)
    }

    @Test
    fun whenUserSetDDGAsJustOnceTheMaxAllowedTimesThenTakeUserToBrowser() {
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        val params = mapOf(
            Pixel.PixelParameter.DEFAULT_BROWSER_SET_ORIGIN to Pixel.PixelValues.DEFAULT_BROWSER_JUST_ONCE_MAX,
        )
        testee.loadUI()
        testee.onDefaultBrowserClicked()
        testee.timesPressedJustOnce = MAX_DIALOG_ATTEMPTS

        testee.handleResult(Origin.InternalBrowser)

        assertTrue(captureCommands().lastValue is Command.ContinueToBrowser)
        verify(mockPixel).fire(AppPixelName.DEFAULT_BROWSER_NOT_SET, params)
    }

    @Test
    fun whenUserDismissedDialogThenShowDialogUIAndFirePixel() {
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(false)
        val params = mapOf(
            Pixel.PixelParameter.DEFAULT_BROWSER_SET_ORIGIN to Pixel.PixelValues.DEFAULT_BROWSER_DIALOG_DISMISSED,
        )
        testee.loadUI()
        testee.onDefaultBrowserClicked()

        testee.handleResult(Origin.DialogDismissed)

        assertEquals(viewState(), DefaultBrowserDialogUI(showInstructionsCard = false))
        verify(mockPixel).fire(AppPixelName.DEFAULT_BROWSER_NOT_SET, params)
    }

    @Test
    fun whenUserSetAnotherBrowserAsDefaultThenShowSettingsUI() {
        val params = mapOf(
            Pixel.PixelParameter.DEFAULT_BROWSER_SET_ORIGIN to Pixel.PixelValues.DEFAULT_BROWSER_EXTERNAL,
        )
        testee.loadUI()
        testee.onDefaultBrowserClicked()
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(true)

        testee.handleResult(Origin.ExternalBrowser)

        assertTrue(viewState() is DefaultBrowserSettingsUI)
        verify(mockPixel).fire(AppPixelName.DEFAULT_BROWSER_NOT_SET, params)
    }

    @Test
    fun whenUserSetDDGAsDefaultThenContinueToBrowser() {
        val params = mapOf(
            Pixel.PixelParameter.DEFAULT_BROWSER_SET_FROM_ONBOARDING to true.toString(),
            Pixel.PixelParameter.DEFAULT_BROWSER_SET_ORIGIN to Pixel.PixelValues.DEFAULT_BROWSER_EXTERNAL,
        )
        testee.loadUI()
        testee.onDefaultBrowserClicked()
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(true)
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(true)

        testee.handleResult(Origin.ExternalBrowser)

        assertTrue(captureCommands().lastValue is Command.ContinueToBrowser)
        verify(mockPixel).fire(AppPixelName.DEFAULT_BROWSER_SET, params)
    }

    @Test
    fun whenUserWasTakenToSettingsAndSelectedDDGAsDefaultThenContinueToBrowser() {
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(true)
        testee.loadUI()
        testee.onDefaultBrowserClicked()
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(true)

        testee.handleResult(Origin.Settings)

        assertTrue(captureCommands().lastValue is Command.ContinueToBrowser)
    }

    @Test
    fun whenUserWasTakenToSettingsAndDidNotSelectDDGAsDefaultThenShowSettingsUI() {
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(true)
        testee.loadUI()
        testee.onDefaultBrowserClicked()

        testee.handleResult(Origin.Settings)

        assertTrue(viewState() is DefaultBrowserSettingsUI)
    }

    @Test
    fun whenUserSelectedDDGAsDefaultInSettingsScreenThenFirePixel() {
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(true)
        val params = mapOf(
            Pixel.PixelParameter.DEFAULT_BROWSER_SET_FROM_ONBOARDING to true.toString(),
            Pixel.PixelParameter.DEFAULT_BROWSER_SET_ORIGIN to DEFAULT_BROWSER_SETTINGS,
        )
        testee.loadUI()
        testee.onDefaultBrowserClicked()
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(true)

        testee.handleResult(Origin.Settings)

        verify(mockPixel).fire(AppPixelName.DEFAULT_BROWSER_SET, params)
    }

    @Test
    fun whenUserDoesNotSelectedDDGAsDefaultInSettingsThenFirePixel() {
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        whenever(mockDefaultBrowserDetector.hasDefaultBrowser()).thenReturn(true)
        val params = mapOf(
            Pixel.PixelParameter.DEFAULT_BROWSER_SET_ORIGIN to DEFAULT_BROWSER_SETTINGS,
        )
        testee.loadUI()
        testee.onDefaultBrowserClicked()

        testee.handleResult(Origin.Settings)

        verify(mockPixel).fire(AppPixelName.DEFAULT_BROWSER_NOT_SET, params)
    }

    @Test
    fun whenOriginReceivedIsSettingsThenResetTimesPressedJustOnce() {
        testee.loadUI()
        testee.timesPressedJustOnce = 1

        testee.handleResult(Origin.Settings)

        assertEquals(0, testee.timesPressedJustOnce)
    }

    @Test
    fun whenOriginReceivedIsDialogDismissedThenResetTimesPressedJustOnce() {
        testee.loadUI()
        testee.timesPressedJustOnce = 1

        testee.handleResult(Origin.DialogDismissed)

        assertEquals(0, testee.timesPressedJustOnce)
    }

    @Test
    fun whenOriginReceivedIsExternalBrowserThenResetTimesPressedJustOnce() {
        testee.timesPressedJustOnce = 1

        testee.handleResult(Origin.ExternalBrowser)

        assertEquals(0, testee.timesPressedJustOnce)
    }

    @Test
    fun whenOriginReceivedIsInternalBrowserAndDDGIsTheDefaultBrowserThenResetTimesPressedJustOnce() {
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(true)
        testee.timesPressedJustOnce = 1

        testee.handleResult(Origin.InternalBrowser)

        assertEquals(0, testee.timesPressedJustOnce)
    }

    @Test
    fun whenOriginReceivedIsInternalBrowserAndDDGIsNotTheDefaultBrowserThenIncreaseTimesPressedJustOnceIfIsLessThanTheMaxNumberOfAttempts() {
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        testee.timesPressedJustOnce = 1

        testee.handleResult(Origin.InternalBrowser)

        assertEquals(2, testee.timesPressedJustOnce)
    }

    @Test
    fun whenOriginReceivedIsInternalBrowserAndDDGIsNotTheDefaultBrowserThenResetTimesPressedJustOnceIfIsGreaterOrEqualThanTheMaxNumberOfAttempts() {
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        testee.timesPressedJustOnce = MAX_DIALOG_ATTEMPTS

        testee.handleResult(Origin.InternalBrowser)

        assertEquals(0, testee.timesPressedJustOnce)
    }

    private fun captureCommands(): KArgumentCaptor<Command> {
        verify(mockCommandObserver, atLeastOnce()).onChanged(commandCaptor.capture())
        return commandCaptor
    }

    private fun viewState() = testee.viewState.value!!
}
