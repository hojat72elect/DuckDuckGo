package com.duckduckgo.app.browser.webview

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "webViewBlobDownload",
)
interface WebViewBlobDownloadFeature {

    @Toggle.DefaultValue(false)
    fun self(): Toggle
}
