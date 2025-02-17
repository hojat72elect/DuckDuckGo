package com.duckduckgo.newtabpage.impl.shortcuts

import android.annotation.SuppressLint
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ViewScope
import com.duckduckgo.newtabpage.api.NewTabPageShortcutPlugin
import com.duckduckgo.newtabpage.impl.pixels.NewTabPixels
import com.duckduckgo.newtabpage.impl.settings.NewTabSettingsStore
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@SuppressLint("NoLifecycleObserver") // we don't observe app lifecycle
@ContributesViewModel(ViewScope::class)
class ShortcutsViewModel @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val newTabSettingsStore: NewTabSettingsStore,
    private val newTabShortcutsProvider: NewTabShortcutsProvider,
    private val pixels: NewTabPixels,
) : ViewModel(), DefaultLifecycleObserver {

    data class ViewState(val shortcuts: List<ShortcutItem> = emptyList())

    private val _viewState = MutableStateFlow(ViewState())
    val viewState = _viewState.asStateFlow()

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        newTabShortcutsProvider.provideActiveShortcuts().onEach { views ->
            val shortcuts = views.map { ShortcutItem(it) }
            _viewState.update {
                viewState.value.copy(
                    shortcuts = shortcuts,
                )
            }
        }.flowOn(dispatchers.io()).launchIn(viewModelScope)
    }

    fun onQuickAccessListChanged(newShortcuts: List<String>) {
        newTabSettingsStore.shortcutSettings = newShortcuts
    }

    fun onShortcutPressed(shortcutPlugin: NewTabPageShortcutPlugin) {
        pixels.fireShortcutPressed(shortcutPlugin.getShortcut().name())
    }
}
