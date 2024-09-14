

package com.duckduckgo.app.statistics.api

import com.duckduckgo.anvil.annotations.ContributesPluginPoint
import com.duckduckgo.app.statistics.pixels.Pixel.StatisticsPixelName
import com.duckduckgo.di.scopes.AppScope

@ContributesPluginPoint(AppScope::class)
interface BrowserFeatureStateReporterPlugin {

    /**
     * Used by the [StatisticsPixelName.BROWSER_DAILY_ACTIVE_FEATURE_STATE] pixel,
     * to notify the state of a feature
     * @return true if the feature is enabled, false if not, alongside the feature name
     */
    fun featureState(): Pair<Boolean, String>
}
