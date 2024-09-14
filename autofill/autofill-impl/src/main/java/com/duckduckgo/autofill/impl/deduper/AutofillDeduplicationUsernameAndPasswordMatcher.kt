

package com.duckduckgo.autofill.impl.deduper

import com.duckduckgo.autofill.api.domain.app.LoginCredentials
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface AutofillDeduplicationUsernameAndPasswordMatcher {

    fun groupDuplicateCredentials(logins: List<LoginCredentials>): Map<Pair<String?, String?>, List<LoginCredentials>>
}

@ContributesBinding(AppScope::class)
class RealAutofillDeduplicationUsernameAndPasswordMatcher @Inject constructor() : AutofillDeduplicationUsernameAndPasswordMatcher {

    override fun groupDuplicateCredentials(logins: List<LoginCredentials>): Map<Pair<String?, String?>, List<LoginCredentials>> {
        return logins.groupBy { it.username to it.password }
    }
}
