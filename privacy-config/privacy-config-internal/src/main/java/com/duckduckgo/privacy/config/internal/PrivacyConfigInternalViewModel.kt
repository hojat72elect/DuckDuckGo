

package com.duckduckgo.privacy.config.internal

import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.privacy.config.api.PRIVACY_REMOTE_CONFIG_URL
import com.duckduckgo.privacy.config.impl.PrivacyConfigDownloader
import com.duckduckgo.privacy.config.impl.PrivacyConfigDownloader.ConfigDownloadResult.Error
import com.duckduckgo.privacy.config.impl.PrivacyConfigDownloader.ConfigDownloadResult.Success
import com.duckduckgo.privacy.config.internal.PrivacyConfigInternalViewModel.Command.ConfigDownloaded
import com.duckduckgo.privacy.config.internal.PrivacyConfigInternalViewModel.Command.ConfigError
import com.duckduckgo.privacy.config.internal.PrivacyConfigInternalViewModel.Command.Loading
import com.duckduckgo.privacy.config.internal.store.DevPrivacyConfigSettingsDataStore
import com.duckduckgo.privacy.config.store.PrivacyConfig
import com.duckduckgo.privacy.config.store.PrivacyConfigRepository
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@ContributesViewModel(ActivityScope::class)
class PrivacyConfigInternalViewModel @Inject constructor(
    private val privacyConfigRepository: PrivacyConfigRepository,
    private val downloader: PrivacyConfigDownloader,
    private val store: DevPrivacyConfigSettingsDataStore,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val command = Channel<Command>(1, DROP_OLDEST)
    internal fun commands(): Flow<Command> = command.receiveAsFlow()

    private val viewState = MutableStateFlow(ViewState())

    fun viewState(): StateFlow<ViewState> {
        return viewState
    }

    data class ViewState(
        val latestVersionLoaded: String = "",
        val timestamp: String = "",
        val latestUrl: String = "",
        val customUrl: String = "",
        val toggleState: Boolean = false,
    )

    fun start() {
        viewModelScope.launch(dispatcherProvider.io()) {
            emitLatestConfig()
        }
    }

    fun download() {
        viewModelScope.launch(dispatcherProvider.io()) {
            sendCommand(Loading)
            privacyConfigRepository.insert(PrivacyConfig(version = -1, readme = "", eTag = "", timestamp = ""))
            when (val result = downloader.download()) {
                is Success -> {
                    sendCommand(ConfigDownloaded(getCurrentUrl()))
                    emitLatestConfig()
                }
                is Error -> {
                    sendCommand(ConfigError(result.error ?: "Unknown error"))
                }
            }
        }
    }

    fun changeCustomUrl(url: String) {
        store.remotePrivacyConfigUrl = url
    }

    fun changeToggleState(value: Boolean) {
        store.useCustomPrivacyConfigUrl = value
    }
    fun canUrlBeChanged(): Boolean {
        val storedUrl = store.remotePrivacyConfigUrl
        val isCustomSettingEnabled = store.useCustomPrivacyConfigUrl
        val validHost = runCatching { URI(storedUrl).host.isNotEmpty() }.getOrDefault(false)
        val canBeChanged = isCustomSettingEnabled && !storedUrl.isNullOrEmpty() && URLUtil.isValidUrl(storedUrl) && validHost
        store.canUrlBeChanged = canBeChanged
        return canBeChanged
    }

    private suspend fun emitLatestConfig() {
        val url = getCurrentUrl()
        val config = privacyConfigRepository.get()
        viewState.emit(
            viewState.value.copy(
                latestVersionLoaded = config?.version.toString(),
                timestamp = config?.timestamp.orEmpty(),
                latestUrl = url.orEmpty(),
                customUrl = store.remotePrivacyConfigUrl.orEmpty(),
                toggleState = store.useCustomPrivacyConfigUrl,
            ),
        )
    }

    private fun getCurrentUrl(): String {
        return if (store.useCustomPrivacyConfigUrl) {
            store.remotePrivacyConfigUrl.orEmpty()
        } else {
            PRIVACY_REMOTE_CONFIG_URL
        }
    }

    private fun sendCommand(newCommand: Command) {
        viewModelScope.launch {
            command.send(newCommand)
        }
    }
    sealed class Command {
        data class ConfigError(val message: String) : Command()
        data class ConfigDownloaded(val url: String) : Command()
        object Loading : Command()
    }
}
