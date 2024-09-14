

package com.duckduckgo.app.pixels.remoteconfig

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle

/**
 * This is the class that represents the browser feature flags
 */
@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "androidBrowserConfig",
)
interface AndroidBrowserConfigFeature {

    /**
     * @return `true` when the remote config has the global "androidBrowserConfig" feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    fun self(): Toggle

    /**
     * @return `true` when the remote config has the global "collectFullWebViewVersion" androidBrowserConfig
     * sub-feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    fun collectFullWebViewVersion(): Toggle

    /**
     * @return `true` when the remote config has the global "screenLock" androidBrowserConfig
     * sub-feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    fun screenLock(): Toggle

    /**
     * @return `true` when the remote config has the global "optimizeTrackerEvaluation" androidBrowserConfig
     * sub-feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    fun optimizeTrackerEvaluation(): Toggle

    /**
     * @return `true` when the remote config has the global "optimizeTrackerEvaluationV2" androidBrowserConfig
     * sub-feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    fun optimizeTrackerEvaluationV2(): Toggle
}
