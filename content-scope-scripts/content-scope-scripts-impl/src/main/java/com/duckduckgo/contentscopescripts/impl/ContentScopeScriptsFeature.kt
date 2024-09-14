

package com.duckduckgo.contentscopescripts.impl

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "clientContentFeatures",
)

interface ContentScopeScriptsFeature {
    @Toggle.DefaultValue(true)
    fun self(): Toggle
}
