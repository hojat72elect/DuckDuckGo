package com.duckduckgo.autoconsent.impl.remoteconfig

import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.FeatureExceptions
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException
import com.duckduckgo.feature.toggles.api.RemoteFeatureStoreNamed
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@RemoteFeatureStoreNamed(AutoconsentFeature::class)
class AutoconsentExceptionsStore @Inject constructor(
    val autoconsentExceptionsRepository: AutoconsentExceptionsRepository,
) : FeatureExceptions.Store {
    override fun insertAll(exception: List<FeatureException>) {
        autoconsentExceptionsRepository.insertAllExceptions(exception)
    }
}
