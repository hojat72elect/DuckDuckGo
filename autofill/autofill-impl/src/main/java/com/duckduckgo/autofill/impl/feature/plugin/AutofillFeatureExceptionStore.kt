

package com.duckduckgo.autofill.impl.feature.plugin

import com.duckduckgo.autofill.api.AutofillFeature
import com.duckduckgo.autofill.store.AutofillExceptionEntity
import com.duckduckgo.autofill.store.feature.AutofillFeatureRepository
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.FeatureExceptions
import com.duckduckgo.feature.toggles.api.RemoteFeatureStoreNamed
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@RemoteFeatureStoreNamed(AutofillFeature::class)
class AutofillFeatureExceptionStore @Inject constructor(
    private val autofillFeatureRepository: AutofillFeatureRepository,
) : FeatureExceptions.Store {
    override fun insertAll(exception: List<FeatureExceptions.FeatureException>) {
        autofillFeatureRepository.updateAllExceptions(
            exception.map { AutofillExceptionEntity(domain = it.domain, reason = it.reason.orEmpty()) },
        )
    }
}
