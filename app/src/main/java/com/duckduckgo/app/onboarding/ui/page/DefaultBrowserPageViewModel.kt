

package com.duckduckgo.app.onboarding.ui.page

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.app.browser.defaultbrowsing.DefaultBrowserDetector
import com.duckduckgo.app.global.install.AppInstallStore
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.utils.SingleLiveEvent
import com.duckduckgo.di.scopes.FragmentScope
import javax.inject.Inject

@ContributesViewModel(FragmentScope::class)
class DefaultBrowserPageViewModel @Inject constructor(
    private val defaultBrowserDetector: DefaultBrowserDetector,
    private val pixel: Pixel,
    private val installStore: AppInstallStore,
) : ViewModel() {

    sealed class ViewState {
        object DefaultBrowserSettingsUI : ViewState()
        data class DefaultBrowserDialogUI(val showInstructionsCard: Boolean = false) : ViewState()
        object ContinueToBrowser : ViewState()
    }

    sealed class Command {
        class OpenDialog(val url: String = DEFAULT_URL) : Command()
        object OpenSettings : Command()
        object ContinueToBrowser : Command()
    }

    sealed class Origin {
        object InternalBrowser : Origin()
        object ExternalBrowser : Origin()
        object Settings : Origin()
        object DialogDismissed : Origin()
    }

    val viewState: MutableLiveData<ViewState> = MutableLiveData()
    val command: SingleLiveEvent<Command> = SingleLiveEvent()
    var timesPressedJustOnce: Int = 0

    init {
        viewState.value = newViewState()
    }

    fun loadUI() {
        nextViewState()?.let {
            refreshViewStateIfTypeChanged(it)
        }
    }

    fun onContinueToBrowser() {
        command.value = Command.ContinueToBrowser
    }

    fun onDefaultBrowserClicked() {
        val currentState = viewState.value
        if (currentState is ViewState.DefaultBrowserSettingsUI) {
            command.value = Command.OpenSettings
        } else if (currentState is ViewState.DefaultBrowserDialogUI) {
            timesPressedJustOnce++
            command.value = Command.OpenDialog()
            viewState.value = currentState.copy(showInstructionsCard = true)
        }
    }

    fun handleResult(origin: Origin) {
        when (origin) {
            is Origin.InternalBrowser -> {
                val navigateToBrowser = handleOriginInternalBrowser()
                reduceToNewState(origin, navigateToBrowser)
            }
            is Origin.DialogDismissed -> {
                fireDefaultBrowserPixelAndResetTimesPressedJustOnce(originValue = Pixel.PixelValues.DEFAULT_BROWSER_DIALOG_DISMISSED)
                reduceToNewState(origin)
            }
            is Origin.ExternalBrowser -> {
                fireDefaultBrowserPixelAndResetTimesPressedJustOnce(originValue = Pixel.PixelValues.DEFAULT_BROWSER_EXTERNAL)
                reduceToNewState(origin)
            }
            is Origin.Settings -> {
                fireDefaultBrowserPixelAndResetTimesPressedJustOnce(originValue = Pixel.PixelValues.DEFAULT_BROWSER_SETTINGS)
                reduceToNewState(origin)
            }
        }
    }

    private fun reduceToNewState(
        origin: Origin,
        navigateToBrowser: Boolean = false,
    ) {
        val newViewState = nextViewState(origin)

        if (newViewState == null || navigateToBrowser) {
            command.value = Command.ContinueToBrowser
            return
        }

        viewState.value = newViewState
    }

    private fun nextViewState(origin: Origin? = null): ViewState? {
        return when {
            defaultBrowserDetector.isDefaultBrowser() -> null
            defaultBrowserDetector.hasDefaultBrowser() -> {
                ViewState.DefaultBrowserSettingsUI
            }
            else -> {
                ViewState.DefaultBrowserDialogUI(showInstructionsCard = origin is Origin.InternalBrowser)
            }
        }
    }

    private fun handleOriginInternalBrowser(): Boolean {
        var navigateToBrowser = false
        if (defaultBrowserDetector.isDefaultBrowser()) {
            fireDefaultBrowserPixelAndResetTimesPressedJustOnce(originValue = Pixel.PixelValues.DEFAULT_BROWSER_DIALOG)
        } else {
            if (timesPressedJustOnce < MAX_DIALOG_ATTEMPTS) {
                timesPressedJustOnce++
                command.value = Command.OpenDialog()
            } else {
                fireDefaultBrowserPixelAndResetTimesPressedJustOnce(originValue = Pixel.PixelValues.DEFAULT_BROWSER_JUST_ONCE_MAX)
                navigateToBrowser = true
            }
        }
        return navigateToBrowser
    }

    private fun fireDefaultBrowserPixelAndResetTimesPressedJustOnce(originValue: String) {
        timesPressedJustOnce = 0
        if (defaultBrowserDetector.isDefaultBrowser()) {
            installStore.defaultBrowser = true
            val params = mapOf(
                Pixel.PixelParameter.DEFAULT_BROWSER_SET_FROM_ONBOARDING to true.toString(),
                Pixel.PixelParameter.DEFAULT_BROWSER_SET_ORIGIN to originValue,
            )
            pixel.fire(AppPixelName.DEFAULT_BROWSER_SET, params)
        } else {
            installStore.defaultBrowser = false
            val params = mapOf(
                Pixel.PixelParameter.DEFAULT_BROWSER_SET_ORIGIN to originValue,
            )
            pixel.fire(AppPixelName.DEFAULT_BROWSER_NOT_SET, params)
        }
    }

    private fun currentViewState(): ViewState = viewState.value!!

    private fun newViewState(): ViewState = when {
        defaultBrowserDetector.isDefaultBrowser() -> ViewState.ContinueToBrowser
        defaultBrowserDetector.hasDefaultBrowser() -> ViewState.DefaultBrowserSettingsUI
        else -> ViewState.DefaultBrowserDialogUI()
    }

    private fun refreshViewStateIfTypeChanged(createViewState: ViewState) {
        if (createViewState.javaClass != currentViewState().javaClass) {
            viewState.value = createViewState
        }
    }

    companion object {
        const val MAX_DIALOG_ATTEMPTS = 2
        const val DEFAULT_URL = "https://duckduckgo.com"
    }
}
