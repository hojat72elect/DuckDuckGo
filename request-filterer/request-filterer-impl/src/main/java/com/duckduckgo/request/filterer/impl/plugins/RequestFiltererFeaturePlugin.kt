

package com.duckduckgo.request.filterer.impl.plugins

import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.privacy.config.api.PrivacyFeaturePlugin
import com.duckduckgo.request.filterer.api.RequestFiltererFeatureName
import com.duckduckgo.request.filterer.impl.RequestFiltererFeature
import com.duckduckgo.request.filterer.impl.requestFiltererFeatureValueOf
import com.duckduckgo.request.filterer.store.RealRequestFiltererRepository.Companion.DEFAULT_WINDOW_IN_MS
import com.duckduckgo.request.filterer.store.RequestFiltererExceptionEntity
import com.duckduckgo.request.filterer.store.RequestFiltererFeatureToggleRepository
import com.duckduckgo.request.filterer.store.RequestFiltererFeatureToggles
import com.duckduckgo.request.filterer.store.RequestFiltererRepository
import com.duckduckgo.request.filterer.store.SettingsEntity
import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class RequestFiltererFeaturePlugin @Inject constructor(
    private val requestFiltererRepository: RequestFiltererRepository,
    private val requestFiltererFeatureToggleRepository: RequestFiltererFeatureToggleRepository,
) : PrivacyFeaturePlugin {

    override fun store(featureName: String, jsonString: String): Boolean {
        val requestFiltererFeatureName = requestFiltererFeatureValueOf(featureName) ?: return false
        if (requestFiltererFeatureName.value == this.featureName) {
            val moshi = Moshi.Builder().build()
            val jsonAdapter: JsonAdapter<RequestFiltererFeature> =
                moshi.adapter(RequestFiltererFeature::class.java)

            val requestFiltererFeature: RequestFiltererFeature? = jsonAdapter.fromJson(jsonString)

            val exceptions = requestFiltererFeature?.exceptions?.map {
                RequestFiltererExceptionEntity(domain = it.domain, reason = it.reason.orEmpty())
            }.orEmpty()

            val windowInMs = requestFiltererFeature?.settings?.windowInMs ?: DEFAULT_WINDOW_IN_MS
            val settings = SettingsEntity(windowInMs = windowInMs)

            requestFiltererRepository.updateAll(exceptions, settings)
            val isEnabled = requestFiltererFeature?.state == "enabled"
            requestFiltererFeatureToggleRepository.insert(
                RequestFiltererFeatureToggles(requestFiltererFeatureName, isEnabled, requestFiltererFeature?.minSupportedVersion),
            )
            return true
        }
        return false
    }

    override val featureName: String = RequestFiltererFeatureName.RequestFilterer.value
}
