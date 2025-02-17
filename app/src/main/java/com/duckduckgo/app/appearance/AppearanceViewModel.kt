

package com.duckduckgo.app.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.webkit.WebViewFeature
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.app.icon.api.AppIcon
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.settings.db.SettingsDataStore
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.ui.DuckDuckGoTheme
import com.duckduckgo.common.ui.store.ThemingDataStore
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ActivityScope
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@ContributesViewModel(ActivityScope::class)
class AppearanceViewModel @Inject constructor(
    private val themingDataStore: ThemingDataStore,
    private val settingsDataStore: SettingsDataStore,
    private val pixel: Pixel,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    data class ViewState(
        val theme: DuckDuckGoTheme = DuckDuckGoTheme.LIGHT,
        val appIcon: AppIcon = AppIcon.DEFAULT,
        val forceDarkModeEnabled: Boolean = false,
        val canForceDarkMode: Boolean = false,
        val supportsForceDarkMode: Boolean = true,
    )

    sealed class Command {
        data class LaunchThemeSettings(val theme: DuckDuckGoTheme) : Command()
        object LaunchAppIcon : Command()
        object UpdateTheme : Command()
    }

    private val viewState = MutableStateFlow(ViewState())
    private val command = Channel<Command>(1, BufferOverflow.DROP_OLDEST)

    fun viewState(): Flow<ViewState> = viewState.onStart {
        viewModelScope.launch {
            viewState.emit(
                currentViewState().copy(
                    theme = themingDataStore.theme,
                    appIcon = settingsDataStore.appIcon,
                    forceDarkModeEnabled = settingsDataStore.experimentalWebsiteDarkMode,
                    canForceDarkMode = canForceDarkMode(),
                    supportsForceDarkMode = WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING),
                ),
            )
        }
    }

    fun commands(): Flow<Command> {
        return command.receiveAsFlow()
    }

    private fun canForceDarkMode(): Boolean {
        return themingDataStore.theme != DuckDuckGoTheme.LIGHT
    }

    fun userRequestedToChangeTheme() {
        viewModelScope.launch { command.send(Command.LaunchThemeSettings(viewState.value.theme)) }
        pixel.fire(AppPixelName.SETTINGS_THEME_OPENED)
    }

    fun userRequestedToChangeIcon() {
        viewModelScope.launch { command.send(Command.LaunchAppIcon) }
        pixel.fire(AppPixelName.SETTINGS_APP_ICON_PRESSED)
    }

    fun onThemeSelected(selectedTheme: DuckDuckGoTheme) {
        Timber.d("User toggled theme, theme to set: $selectedTheme")
        if (themingDataStore.isCurrentlySelected(selectedTheme)) {
            Timber.d("User selected same theme they've already set: $selectedTheme; no need to do anything else")
            return
        }
        viewModelScope.launch(dispatcherProvider.io()) {
            themingDataStore.theme = selectedTheme
            withContext(dispatcherProvider.main()) {
                viewState.emit(currentViewState().copy(theme = selectedTheme, forceDarkModeEnabled = canForceDarkMode()))
                command.send(Command.UpdateTheme)
            }
        }

        val pixelName =
            when (selectedTheme) {
                DuckDuckGoTheme.LIGHT -> AppPixelName.SETTINGS_THEME_TOGGLED_LIGHT
                DuckDuckGoTheme.DARK -> AppPixelName.SETTINGS_THEME_TOGGLED_DARK
                DuckDuckGoTheme.SYSTEM_DEFAULT -> AppPixelName.SETTINGS_THEME_TOGGLED_SYSTEM_DEFAULT
            }
        pixel.fire(pixelName)
    }

    private fun currentViewState(): ViewState {
        return viewState.value
    }

    fun onForceDarkModeSettingChanged(checked: Boolean) {
        viewModelScope.launch(dispatcherProvider.io()) {
            if (checked) {
                pixel.fire(AppPixelName.FORCE_DARK_MODE_ENABLED)
            } else {
                pixel.fire(AppPixelName.FORCE_DARK_MODE_DISABLED)
            }
            settingsDataStore.experimentalWebsiteDarkMode = checked
        }
    }
}
