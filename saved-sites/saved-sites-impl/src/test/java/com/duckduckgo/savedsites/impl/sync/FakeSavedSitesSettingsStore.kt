

package com.duckduckgo.savedsites.impl.sync

import com.duckduckgo.savedsites.store.FavoritesDisplayMode
import com.duckduckgo.savedsites.store.FavoritesDisplayMode.NATIVE
import com.duckduckgo.savedsites.store.SavedSitesSettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FakeSavedSitesSettingsStore(
    private val coroutineScope: CoroutineScope,
) : SavedSitesSettingsStore {
    val flow = MutableStateFlow(NATIVE)
    override var favoritesDisplayMode: FavoritesDisplayMode
        get() = flow.value
        set(value) {
            coroutineScope.launch {
                flow.emit(value)
            }
        }
    override fun favoritesFormFactorModeFlow(): Flow<FavoritesDisplayMode> = flow
}
