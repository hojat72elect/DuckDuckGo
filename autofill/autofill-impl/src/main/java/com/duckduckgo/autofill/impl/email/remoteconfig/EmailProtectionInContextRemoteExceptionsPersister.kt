

package com.duckduckgo.autofill.impl.email.remoteconfig

import com.duckduckgo.autofill.impl.email.incontext.EmailProtectionInContextSignupFeature
import com.duckduckgo.autofill.store.feature.email.incontext.EmailInContextExceptionEntity
import com.duckduckgo.autofill.store.feature.email.incontext.EmailProtectionInContextFeatureRepository
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.FeatureExceptions
import com.duckduckgo.feature.toggles.api.RemoteFeatureStoreNamed
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@RemoteFeatureStoreNamed(EmailProtectionInContextSignupFeature::class)
class EmailProtectionInContextRemoteExceptionsPersister @Inject constructor(
    private val emailProtectionFeatureRepository: EmailProtectionInContextFeatureRepository,
) : FeatureExceptions.Store {
    override fun insertAll(exception: List<FeatureExceptions.FeatureException>) {
        emailProtectionFeatureRepository.updateAllExceptions(
            exception.map { EmailInContextExceptionEntity(domain = it.domain, reason = it.reason.orEmpty()) },
        )
    }
}
