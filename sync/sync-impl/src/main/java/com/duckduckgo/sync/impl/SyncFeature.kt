

package com.duckduckgo.sync.impl

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "sync",
)
interface SyncFeature {
    @Toggle.DefaultValue(false)
    @Toggle.InternalAlwaysEnabled
    fun self(): Toggle

    @Toggle.DefaultValue(true)
    fun level0ShowSync(): Toggle

    @Toggle.DefaultValue(true)
    fun level1AllowDataSyncing(): Toggle

    @Toggle.DefaultValue(true)
    fun level2AllowSetupFlows(): Toggle

    @Toggle.DefaultValue(true)
    fun level3AllowCreateAccount(): Toggle

    @Toggle.DefaultValue(true)
    fun gzipPatchRequests(): Toggle
}
