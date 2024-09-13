package com.duckduckgo.sync.impl.favicons

import androidx.annotation.WorkerThread
import com.duckduckgo.sync.api.favicons.FaviconsFetchingPrompt
import com.duckduckgo.sync.api.favicons.FaviconsFetchingStore
import com.duckduckgo.sync.impl.Result
import com.duckduckgo.sync.impl.Result.Success
import com.duckduckgo.sync.impl.SyncAccountRepository
import timber.log.Timber

class SyncFaviconsFetchingPrompt(
    private val faviconsFetchingStore: FaviconsFetchingStore,
    private val syncAccountRepository: SyncAccountRepository,
) : FaviconsFetchingPrompt {

    // should show prompt when
    // Have Sync enabled.
    // Have at least 2 devices in the Sync account.
    // Visit bookmarks list OR new tab page.
    // Have favicons fetching disabled
    // Have at least 1 bookmark without favicon loaded on current screen.
    // https://app.asana.com/0/1157893581871903/1206440765317539
    @WorkerThread
    override fun shouldShow(): Boolean {
        if (faviconsFetchingStore.promptShown) {
            return false
        }

        if (faviconsFetchingStore.isFaviconsFetchingEnabled) {
            return false
        }

        return if (syncAccountRepository.isSignedIn()) {
            val result = syncAccountRepository.getConnectedDevices()
            Timber.d("Sync: Connected Devices $result")
            when (result) {
                is Result.Error -> false
                is Success -> result.data.size >= MIN_CONNECTED_DEVICES_FOR_PROMPT
            }
        } else {
            false
        }
    }

    override fun onPromptAnswered(fetchingEnabled: Boolean) {
        Timber.d("Favicons: Feching en")
        faviconsFetchingStore.promptShown = true
        faviconsFetchingStore.isFaviconsFetchingEnabled = fetchingEnabled
    }

    companion object {
        private const val MIN_CONNECTED_DEVICES_FOR_PROMPT = 2
    }
}
