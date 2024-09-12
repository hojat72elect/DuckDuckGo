package com.duckduckgo.autofill.impl.reporting.remoteconfig

import com.duckduckgo.autofill.store.reporting.AutofillSiteBreakageReportingEntity
import com.duckduckgo.autofill.store.reporting.AutofillSiteBreakageReportingFeatureRepository
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.FeatureExceptions
import com.duckduckgo.feature.toggles.api.RemoteFeatureStoreNamed
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@RemoteFeatureStoreNamed(AutofillSiteBreakageReportingFeature::class)
class AutofillSiteBreakageReportingExceptionsPersister @Inject constructor(
    private val repository: AutofillSiteBreakageReportingFeatureRepository,
) : FeatureExceptions.Store {
    override fun insertAll(exception: List<FeatureExceptions.FeatureException>) {
        repository.updateAllExceptions(
            exception.map {
                AutofillSiteBreakageReportingEntity(
                    domain = it.domain,
                    reason = it.reason.orEmpty()
                )
            },
        )
    }
}
