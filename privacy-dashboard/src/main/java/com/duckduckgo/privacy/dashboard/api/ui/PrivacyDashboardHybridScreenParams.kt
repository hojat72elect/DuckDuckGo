

package com.duckduckgo.privacy.dashboard.api.ui

import com.duckduckgo.navigation.api.GlobalActivityStarter

sealed class PrivacyDashboardHybridScreenParams : GlobalActivityStarter.ActivityParams {

    abstract val tabId: String

    /**
     * Use this parameter to launch the privacy dashboard hybrid activity with the given tabId
     * @param tabId The tab ID
     */
    data class PrivacyDashboardPrimaryScreen(override val tabId: String) : PrivacyDashboardHybridScreenParams()

    /**
     * Use this parameter to launch the site breakage reporting form.
     * @param tabId The tab ID
     */
    data class BrokenSiteForm(override val tabId: String) : PrivacyDashboardHybridScreenParams()
}
