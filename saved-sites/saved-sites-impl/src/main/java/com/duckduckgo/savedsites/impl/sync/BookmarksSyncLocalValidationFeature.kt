package com.duckduckgo.savedsites.impl.sync

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "bookmarksLocalFieldValidation",
)
interface BookmarksSyncLocalValidationFeature {
    @Toggle.DefaultValue(true)
    fun self(): Toggle
}
