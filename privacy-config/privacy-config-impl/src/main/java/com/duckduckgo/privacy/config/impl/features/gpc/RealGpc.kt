

package com.duckduckgo.privacy.config.impl.features.gpc

import androidx.annotation.VisibleForTesting
import com.duckduckgo.app.browser.UriString.Companion.sameOrSubdomain
import com.duckduckgo.app.privacy.db.UserAllowListRepository
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.FeatureToggle
import com.duckduckgo.privacy.config.api.Gpc
import com.duckduckgo.privacy.config.api.PrivacyFeatureName
import com.duckduckgo.privacy.config.api.UnprotectedTemporary
import com.duckduckgo.privacy.config.store.features.gpc.GpcRepository
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
class RealGpc @Inject constructor(
    private val featureToggle: FeatureToggle,
    private val gpcRepository: GpcRepository,
    private val unprotectedTemporary: UnprotectedTemporary,
    private val userAllowListRepository: UserAllowListRepository,
) : Gpc {

    override fun isEnabled(): Boolean {
        return gpcRepository.isGpcEnabled()
    }

    override fun getHeaders(url: String): Map<String, String> {
        return if (canGpcBeUsedByUrl(url)) {
            mapOf(GPC_HEADER to GPC_HEADER_VALUE)
        } else {
            emptyMap()
        }
    }

    override fun canUrlAddHeaders(
        url: String,
        existingHeaders: Map<String, String>,
    ): Boolean {
        return if (canGpcBeUsedByUrl(url) && !containsGpcHeader(existingHeaders)) {
            gpcRepository.headerEnabledSites.any { sameOrSubdomain(url, it.domain) }
        } else {
            false
        }
    }

    override fun enableGpc() {
        gpcRepository.enableGpc()
    }

    override fun disableGpc() {
        gpcRepository.disableGpc()
    }

    @VisibleForTesting
    fun canGpcBeUsedByUrl(url: String): Boolean {
        return isFeatureEnabled() && isEnabled() && !isAnException(url)
    }

    private fun isFeatureEnabled(): Boolean {
        return featureToggle.isFeatureEnabled(PrivacyFeatureName.GpcFeatureName.value)
    }

    private fun containsGpcHeader(headers: Map<String, String>): Boolean {
        return headers.containsKey(GPC_HEADER)
    }

    @VisibleForTesting
    fun isAnException(url: String): Boolean {
        return matches(url) || unprotectedTemporary.isAnException(url) || userAllowListRepository.isUrlInUserAllowList(url)
    }

    private fun matches(url: String): Boolean {
        return gpcRepository.exceptions.any { sameOrSubdomain(url, it.domain) }
    }

    companion object {
        const val GPC_HEADER = "sec-gpc"
        const val GPC_HEADER_VALUE = "1"
    }
}
