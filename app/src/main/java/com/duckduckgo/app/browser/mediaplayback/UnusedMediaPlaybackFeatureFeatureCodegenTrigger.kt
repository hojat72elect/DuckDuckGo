package com.duckduckgo.app.browser.mediaplayback

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.app.browser.mediaplayback.store.MediaPlaybackStore
import com.duckduckgo.di.scopes.AppScope

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "mediaPlaybackRequiresUserGesture",
    exceptionsStore = MediaPlaybackStore::class,
    boundType = MediaPlaybackFeature::class,
)
@Suppress("unused")
private interface UnusedMediaPlaybackFeatureFeatureCodegenTrigger
