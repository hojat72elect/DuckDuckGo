

package com.duckduckgo.site.permissions.impl.drmblock

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "emeBlock",
    exceptionsStore = DrmBlockFeatureExceptionStore::class,
)
interface DrmBlockFeature {
    @Toggle.DefaultValue(false)
    fun self(): Toggle
}
