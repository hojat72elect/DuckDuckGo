package com.duckduckgo.history.impl

import com.duckduckgo.app.browser.DuckDuckGoUrlDetector
import com.duckduckgo.common.utils.CurrentTimeProvider
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.history.api.HistoryEntry
import com.duckduckgo.history.api.NavigationHistory
import com.duckduckgo.history.impl.remoteconfig.HistoryFeature
import com.squareup.anvil.annotations.ContributesBinding
import io.reactivex.Single
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

interface InternalNavigationHistory : NavigationHistory {
    suspend fun clearOldEntries()
}

@ContributesBinding(AppScope::class, boundType = NavigationHistory::class)
@ContributesBinding(AppScope::class, boundType = InternalNavigationHistory::class)
class RealNavigationHistory @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val duckDuckGoUrlDetector: DuckDuckGoUrlDetector,
    private val currentTimeProvider: CurrentTimeProvider,
    private val historyFeature: HistoryFeature,
    private val dispatcherProvider: DispatcherProvider,
) : InternalNavigationHistory {
    override suspend fun saveToHistory(
        url: String,
        title: String?,
    ) {
        if (!historyFeature.shouldStoreHistory || !isHistoryUserEnabled()) {
            return
        }
        val ddgUrl = duckDuckGoUrlDetector.isDuckDuckGoQueryUrl(url)
        val query = if (ddgUrl) duckDuckGoUrlDetector.extractQuery(url) else null

        historyRepository.saveToHistory(url, title, query, query != null)
    }

    override fun getHistorySingle(): Single<List<HistoryEntry>> {
        val isHistoryUserEnabled = runBlocking(dispatcherProvider.io()) { isHistoryUserEnabled() }
        return if (isHistoryFeatureAvailable() && isHistoryUserEnabled) historyRepository.getHistoryObservable() else Single.just(
            emptyList()
        )
    }

    override suspend fun clearHistory() {
        historyRepository.clearHistory()
    }

    override suspend fun removeHistoryEntryByUrl(url: String) {
        historyRepository.removeHistoryEntryByUrl(url)
    }

    override suspend fun removeHistoryEntryByQuery(query: String) {
        historyRepository.removeHistoryEntryByQuery(query)
    }

    override suspend fun clearOldEntries() {
        historyRepository.clearEntriesOlderThan(
            currentTimeProvider.localDateTimeNow().minusDays(30)
        )
    }

    override suspend fun isHistoryUserEnabled(): Boolean {
        return historyRepository.isHistoryUserEnabled(historyFeature.shouldStoreHistory)
    }

    override suspend fun setHistoryUserEnabled(value: Boolean) {
        historyRepository.setHistoryUserEnabled(value)
    }

    override fun isHistoryFeatureAvailable(): Boolean {
        return historyFeature.shouldStoreHistory
    }

    override suspend fun hasHistory(): Boolean {
        return historyRepository.hasHistory()
    }
}
