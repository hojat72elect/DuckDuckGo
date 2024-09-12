package com.duckduckgo.autofill.sync.provider

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "credentialsLocalFieldValidation",
)
interface CredentialsSyncLocalValidationFeature {
    @Toggle.DefaultValue(true)
    fun self(): Toggle
}
