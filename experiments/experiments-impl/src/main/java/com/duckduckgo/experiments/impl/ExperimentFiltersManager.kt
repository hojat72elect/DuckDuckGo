

package com.duckduckgo.experiments.impl

import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.experiments.api.VariantConfig
import com.duckduckgo.experiments.impl.ExperimentFiltersManagerImpl.ExperimentFilterType.ANDROID_VERSION
import com.duckduckgo.experiments.impl.ExperimentFiltersManagerImpl.ExperimentFilterType.LOCALE
import com.squareup.anvil.annotations.ContributesBinding
import java.util.Locale
import javax.inject.Inject

interface ExperimentFiltersManager {
    fun addFilters(entity: VariantConfig): (AppBuildConfig) -> Boolean
}

@ContributesBinding(AppScope::class)
class ExperimentFiltersManagerImpl @Inject constructor(
    private val appBuildConfig: AppBuildConfig,
) : ExperimentFiltersManager {
    override fun addFilters(entity: VariantConfig): (AppBuildConfig) -> Boolean {
        if (entity.variantKey == "sc" || entity.variantKey == "se") {
            return { isSerpRegionToggleCountry() }
        }

        val filters: MutableMap<ExperimentFilterType, Boolean> = mutableMapOf(
            LOCALE to true,
            ANDROID_VERSION to true,
        )

        if (!entity.filters?.locale.isNullOrEmpty()) {
            val userLocale = Locale.getDefault()
            filters[LOCALE] = entity.filters!!.locale.contains(userLocale.toString())
        }
        if (!entity.filters?.androidVersion.isNullOrEmpty()) {
            val userAndroidVersion = appBuildConfig.sdkInt.toString()
            filters[ANDROID_VERSION] = entity.filters!!.androidVersion.contains(userAndroidVersion)
        }

        return { filters.filter { !it.value }.isEmpty() }
    }

    private val serpRegionToggleTargetCountries = listOf(
        "AU",
        "AT",
        "DK",
        "FI",
        "FR",
        "DE",
        "IT",
        "IE",
        "NZ",
        "NO",
        "ES",
        "SE",
        "GB",
    )

    private fun isSerpRegionToggleCountry(): Boolean {
        val locale = Locale.getDefault()
        return serpRegionToggleTargetCountries.contains(locale.country)
    }

    enum class ExperimentFilterType {
        LOCALE,
        ANDROID_VERSION,
    }
}
