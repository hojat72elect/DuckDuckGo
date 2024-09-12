package com.duckduckgo.app.pixels.campaign.params

import com.duckduckgo.app.statistics.store.StatisticsDataStore
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class ReinstallAdditionalPixelParamPlugin @Inject constructor(
    private val statisticsDataStore: StatisticsDataStore,
) : AdditionalPixelParamPlugin {
    override suspend fun params(): Pair<String, String> = Pair(
        "isReinstall",
        "${statisticsDataStore.variant == "ru"}",
    )
}
