package com.duckduckgo.history.impl.remoteconfig

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.feature.toggles.api.Toggle.InternalAlwaysEnabled

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "history",
)
interface HistoryRemoteFeature {
    @Toggle.DefaultValue(false)
    @InternalAlwaysEnabled
    fun self(): Toggle

    @Toggle.DefaultValue(false)
    @InternalAlwaysEnabled
    fun storeHistory(): Toggle
}
