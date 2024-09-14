

package com.duckduckgo.autofill.impl.deviceauth

import com.duckduckgo.autofill.impl.time.TimeProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import javax.inject.Inject
import timber.log.Timber

/**
 * A grace period for autofill authorization.
 * This is used to allow autofill authorization to be skipped for a short period of time after a successful authorization.
 */
interface AutofillAuthorizationGracePeriod {

    /**
     * Can be used to determine if device auth is required. If not required, it can be bypassed.
     * @return true if authorization is required, false otherwise
     */
    fun isAuthRequired(): Boolean

    /**
     * Records the timestamp of a successful device authorization
     */
    fun recordSuccessfulAuthorization()

    /**
     * Invalidates the grace period, so that the next call to [isAuthRequired] will return true
     */
    fun invalidate()
}

@SingleInstanceIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AutofillTimeBasedAuthorizationGracePeriod @Inject constructor(
    private val timeProvider: TimeProvider,
) : AutofillAuthorizationGracePeriod {

    private var lastSuccessfulAuthTime: Long? = null

    override fun recordSuccessfulAuthorization() {
        lastSuccessfulAuthTime = timeProvider.currentTimeMillis()
        Timber.v("Recording timestamp of successful auth")
    }

    override fun isAuthRequired(): Boolean {
        lastSuccessfulAuthTime?.let { lastAuthTime ->
            val timeSinceLastAuth = timeProvider.currentTimeMillis() - lastAuthTime
            Timber.v("Last authentication was $timeSinceLastAuth ms ago")
            if (timeSinceLastAuth <= AUTH_GRACE_PERIOD_MS) {
                Timber.v("Within grace period; auth not required")
                return false
            }
        }
        Timber.v("No last auth time recorded or outside grace period; auth required")

        return true
    }

    override fun invalidate() {
        lastSuccessfulAuthTime = null
    }

    companion object {
        private const val AUTH_GRACE_PERIOD_MS = 15_000
    }
}
