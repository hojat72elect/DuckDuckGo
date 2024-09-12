package com.duckduckgo.app.browser.print

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "singlePrintSafeguard",
)

interface SinglePrintSafeguardFeature {

    @Toggle.DefaultValue(true)
    fun self(): Toggle
}
