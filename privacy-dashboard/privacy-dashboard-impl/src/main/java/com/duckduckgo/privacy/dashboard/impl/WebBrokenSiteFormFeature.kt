package com.duckduckgo.privacy.dashboard.impl

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.feature.toggles.api.Toggle.DefaultValue

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "webBrokenSiteForm",
)
interface WebBrokenSiteFormFeature {
    @DefaultValue(false)
    fun self(): Toggle
}

fun WebBrokenSiteFormFeature.isEnabled(): Boolean = self().isEnabled()
