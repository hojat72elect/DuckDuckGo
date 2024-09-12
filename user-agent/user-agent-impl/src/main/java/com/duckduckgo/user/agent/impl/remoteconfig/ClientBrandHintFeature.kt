package com.duckduckgo.user.agent.impl.remoteconfig

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "clientBrandHint",
    settingsStore = ClientBrandHintFeatureSettingsStore::class,
)

interface ClientBrandHintFeature {
    @Toggle.DefaultValue(true)
    fun self(): Toggle
}
