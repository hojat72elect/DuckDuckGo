package com.duckduckgo.autoconsent.impl.remoteconfig

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "autoconsent",
    settingsStore = AutoconsentFeatureSettingsStore::class,
    exceptionsStore = AutoconsentExceptionsStore::class,
)
/**
 * This is the class that represents the voiceSearch feature flags
 */
interface AutoconsentFeature {
    /**
     * @return `true` when the remote config has the global "voiceSearch" feature flag enabled
     * If the remote feature is not present defaults to `true`
     */
    @Toggle.DefaultValue(true)
    fun self(): Toggle

    @Toggle.DefaultValue(false)
    fun onByDefault(): Toggle
}
