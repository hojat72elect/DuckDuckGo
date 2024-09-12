package com.duckduckgo.app.browser.viewstate

data class LoadingViewState(
    val isLoading: Boolean = false,
    val privacyOn: Boolean = true,
    val progress: Int = 0,
)
