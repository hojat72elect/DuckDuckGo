package com.duckduckgo.history.api

import io.reactivex.Single

interface NavigationHistory {

    /**
     * Stores a history entry.
     * @param url The URL of the history entry.
     * @param title The title of the history entry. Can be null.
     */

    suspend fun saveToHistory(url: String, title: String?)

    /**
     * Retrieves all [HistoryEntry].
     * @return [Single] of all [HistoryEntry].
     */
    @Deprecated("RxJava is deprecated, except for Auto-Complete")
    fun getHistorySingle(): Single<List<HistoryEntry>>

    /**
     * Clears all history entries.
     */
    suspend fun clearHistory()

    /**
     * Removes a history entry by URL.
     */
    suspend fun removeHistoryEntryByUrl(url: String)

    /**
     * Removes a history entry by query.
     */
    suspend fun removeHistoryEntryByQuery(query: String)

    /**
     * Returns whether the history is enabled by the user.
     */
    suspend fun isHistoryUserEnabled(): Boolean

    /**
     * Sets whether the history is enabled by the user.
     */
    suspend fun setHistoryUserEnabled(value: Boolean)

    /**
     * Returns whether the history feature is available through RC.
     */
    fun isHistoryFeatureAvailable(): Boolean

    /**
     * Returns if the user has any [HistoryEntry]
     * @return [Boolean] true if has [HistoryEntry], false if there are no [HistoryEntry]
     */
    suspend fun hasHistory(): Boolean
}
