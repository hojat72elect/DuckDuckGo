package com.duckduckgo.app.browser.mediaplayback.store

import com.duckduckgo.app.browser.mediaplayback.MediaPlaybackFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.FeatureExceptions
import com.duckduckgo.feature.toggles.api.RemoteFeatureStoreNamed
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@RemoteFeatureStoreNamed(MediaPlaybackFeature::class)
class MediaPlaybackStore @Inject constructor(
    private val mediaPlaybackRepository: MediaPlaybackRepository,
) : FeatureExceptions.Store {
    override fun insertAll(exception: List<FeatureExceptions.FeatureException>) {
        mediaPlaybackRepository.updateAll(
            exception.map { MediaPlaybackExceptionEntity(domain = it.domain) },
        )
    }
}
