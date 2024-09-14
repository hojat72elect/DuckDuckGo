

package com.duckduckgo.networkprotection.internal.feature

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "netpInternal",
    // we define store because we need something multi-process
    toggleStore = NetPInternalFeatureToggleStore::class,
)
interface NetPInternalFeatureToggles {
    @Toggle.DefaultValue(defaultValue = true)
    fun self(): Toggle

    @Toggle.DefaultValue(defaultValue = false)
    fun excludeSystemApps(): Toggle

    @Toggle.DefaultValue(defaultValue = false)
    fun cloudflareDnsFallback(): Toggle

    @Toggle.DefaultValue(defaultValue = false)
    fun enablePcapRecording(): Toggle

    @Toggle.DefaultValue(defaultValue = false)
    fun useVpnStagingEnvironment(): Toggle
}
