package com.duckduckgo.app.pixels.campaign

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "additionalCampaignPixelParams",
    settingsStore = AdditionalPproPixelParamFeatureStore::class,
)
interface AdditionalPixelParamsFeature {

    /**
     * @return `true` to enable the privacy pro promo feature of appending a random subset of additional pixel parameters
     */
    @Toggle.DefaultValue(false)
    fun self(): Toggle
}
