

package com.duckduckgo.site.permissions.impl.drmblock

import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.FeatureExceptions
import com.duckduckgo.feature.toggles.api.RemoteFeatureStoreNamed
import com.duckduckgo.site.permissions.store.drmblock.DrmBlockExceptionEntity
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@RemoteFeatureStoreNamed(DrmBlockFeature::class)
class DrmBlockFeatureExceptionStore @Inject constructor(
    private val drmBlockRepository: DrmBlockRepository,
) : FeatureExceptions.Store {
    override fun insertAll(exception: List<FeatureExceptions.FeatureException>) {
        drmBlockRepository.updateAll(
            exception.map { DrmBlockExceptionEntity(domain = it.domain) },
        )
    }
}
