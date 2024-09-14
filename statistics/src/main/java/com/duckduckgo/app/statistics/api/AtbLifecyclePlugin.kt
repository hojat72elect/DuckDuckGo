

package com.duckduckgo.app.statistics.api

import com.duckduckgo.anvil.annotations.ContributesPluginPoint
import com.duckduckgo.di.scopes.AppScope

@ContributesPluginPoint(AppScope::class)
interface AtbLifecyclePlugin {
    /**
     * Will be called right after we have refreshed the ATB retention on search
     */
    fun onSearchRetentionAtbRefreshed() {
        // default is no-op
    }

    /**
     * Will be called right after we have refreshed the ATB retention on search
     */
    fun onAppRetentionAtbRefreshed() {
        // default is no-op
    }

    /**
     * Will be called right after the ATB is first initialized and successfully sent via exti call
     */
    fun onAppAtbInitialized() {
        // default is no-op
    }
}
