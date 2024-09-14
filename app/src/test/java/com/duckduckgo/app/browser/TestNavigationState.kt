

package com.duckduckgo.app.browser

import com.duckduckgo.app.browser.history.NavigationHistoryEntry

data class TestNavigationState(
    override val originalUrl: String?,
    override val currentUrl: String?,
    override val title: String?,
    override val stepsToPreviousPage: Int,
    override val canGoBack: Boolean,
    override val canGoForward: Boolean,
    override val hasNavigationHistory: Boolean,
    override val progress: Int?,
    override val navigationHistory: List<NavigationHistoryEntry> = emptyList(),
) : WebNavigationState
