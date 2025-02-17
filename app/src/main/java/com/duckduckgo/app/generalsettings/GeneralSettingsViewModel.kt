package com.duckduckgo.app.generalsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.app.pixels.AppPixelName.AUTOCOMPLETE_GENERAL_SETTINGS_TOGGLED_OFF
import com.duckduckgo.app.pixels.AppPixelName.AUTOCOMPLETE_GENERAL_SETTINGS_TOGGLED_ON
import com.duckduckgo.app.pixels.AppPixelName.AUTOCOMPLETE_RECENT_SITES_GENERAL_SETTINGS_TOGGLED_OFF
import com.duckduckgo.app.pixels.AppPixelName.AUTOCOMPLETE_RECENT_SITES_GENERAL_SETTINGS_TOGGLED_ON
import com.duckduckgo.app.settings.db.SettingsDataStore
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.history.api.NavigationHistory
import com.duckduckgo.voice.api.VoiceSearchAvailability
import com.duckduckgo.voice.impl.VoiceSearchPixelNames.VOICE_SEARCH_GENERAL_SETTINGS_OFF
import com.duckduckgo.voice.impl.VoiceSearchPixelNames.VOICE_SEARCH_GENERAL_SETTINGS_ON
import com.duckduckgo.voice.store.VoiceSearchRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesViewModel(ActivityScope::class)
class GeneralSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val pixel: Pixel,
    private val history: NavigationHistory,
    private val voiceSearchAvailability: VoiceSearchAvailability,
    private val voiceSearchRepository: VoiceSearchRepository,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    data class ViewState(
        val autoCompleteSuggestionsEnabled: Boolean,
        val autoCompleteRecentlyVisitedSitesSuggestionsUserEnabled: Boolean,
        val storeHistoryEnabled: Boolean,
        val showVoiceSearch: Boolean,
        val voiceSearchEnabled: Boolean,
    )

    private val _viewState = MutableStateFlow<ViewState?>(null)
    val viewState = _viewState.asStateFlow()

    init {
        viewModelScope.launch(dispatcherProvider.io()) {
            val autoCompleteEnabled = settingsDataStore.autoCompleteSuggestionsEnabled
            if (!autoCompleteEnabled) {
                history.setHistoryUserEnabled(false)
            }
            _viewState.value = ViewState(
                autoCompleteSuggestionsEnabled = settingsDataStore.autoCompleteSuggestionsEnabled,
                autoCompleteRecentlyVisitedSitesSuggestionsUserEnabled = history.isHistoryUserEnabled(),
                storeHistoryEnabled = history.isHistoryFeatureAvailable(),
                showVoiceSearch = voiceSearchAvailability.isVoiceSearchSupported,
                voiceSearchEnabled = voiceSearchAvailability.isVoiceSearchAvailable,
            )
        }
    }

    fun onAutocompleteSettingChanged(enabled: Boolean) {
        Timber.i("User changed autocomplete setting, is now enabled: $enabled")
        viewModelScope.launch(dispatcherProvider.io()) {
            settingsDataStore.autoCompleteSuggestionsEnabled = enabled
            if (!enabled) {
                history.setHistoryUserEnabled(false)
            }
            if (enabled) {
                pixel.fire(AUTOCOMPLETE_GENERAL_SETTINGS_TOGGLED_ON)
            } else {
                pixel.fire(AUTOCOMPLETE_GENERAL_SETTINGS_TOGGLED_OFF)
            }
            _viewState.value = _viewState.value?.copy(
                autoCompleteSuggestionsEnabled = enabled,
                autoCompleteRecentlyVisitedSitesSuggestionsUserEnabled = history.isHistoryUserEnabled(),
            )
        }
    }

    fun onAutocompleteRecentlyVisitedSitesSettingChanged(enabled: Boolean) {
        Timber.i("User changed autocomplete recently visited sites setting, is now enabled: $enabled")
        viewModelScope.launch(dispatcherProvider.io()) {
            history.setHistoryUserEnabled(enabled)
            if (enabled) {
                pixel.fire(AUTOCOMPLETE_RECENT_SITES_GENERAL_SETTINGS_TOGGLED_ON)
            } else {
                pixel.fire(AUTOCOMPLETE_RECENT_SITES_GENERAL_SETTINGS_TOGGLED_OFF)
            }
            _viewState.value =
                _viewState.value?.copy(autoCompleteRecentlyVisitedSitesSuggestionsUserEnabled = enabled)
        }
    }

    fun onVoiceSearchChanged(checked: Boolean) {
        viewModelScope.launch(dispatcherProvider.io()) {
            voiceSearchRepository.setVoiceSearchUserEnabled(checked)
            if (checked) {
                voiceSearchRepository.resetVoiceSearchDismissed()
                pixel.fire(VOICE_SEARCH_GENERAL_SETTINGS_ON)
            } else {
                pixel.fire(VOICE_SEARCH_GENERAL_SETTINGS_OFF)
            }
            _viewState.value =
                _viewState.value?.copy(voiceSearchEnabled = voiceSearchAvailability.isVoiceSearchAvailable)
        }
    }
}
