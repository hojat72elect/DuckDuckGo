package com.duckduckgo.remote.messaging.internal.setting

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "internalRmfSettings", // will never appear in production
)
interface RmfInternalSettings {
    @Toggle.DefaultValue(false)
    fun self(): Toggle

    @Toggle.DefaultValue(false)
    fun useStatingEndpoint(): Toggle
}
