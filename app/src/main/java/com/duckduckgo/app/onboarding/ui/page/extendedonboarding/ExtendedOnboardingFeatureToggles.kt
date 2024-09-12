package com.duckduckgo.app.onboarding.ui.page.extendedonboarding

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.feature.toggles.api.Toggle.Experiment

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "extendedOnboarding",
)
interface ExtendedOnboardingFeatureToggles {

    @Toggle.DefaultValue(false)
    fun self(): Toggle

    @Toggle.DefaultValue(true)
    fun aestheticUpdates(): Toggle

    @Toggle.DefaultValue(false)
    @Experiment
    fun noBrowserCtas(): Toggle
}
