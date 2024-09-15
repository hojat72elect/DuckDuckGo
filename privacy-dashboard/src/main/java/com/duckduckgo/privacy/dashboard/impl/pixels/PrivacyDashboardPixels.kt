

package com.duckduckgo.privacy.dashboard.impl.pixels

import com.duckduckgo.app.statistics.pixels.Pixel

enum class PrivacyDashboardPixels(override val pixelName: String, val enqueue: Boolean = false) : Pixel.PixelName {
    PRIVACY_DASHBOARD_OPENED("mp"),
    PRIVACY_DASHBOARD_ALLOWLIST_ADD("mp_wla"),
    PRIVACY_DASHBOARD_ALLOWLIST_REMOVE("mp_wlr"),
    PRIVACY_DASHBOARD_FIRST_TIME_OPENED("m_privacy_dashboard_first_time_used"),
}

enum class PrivacyDashboardCustomTabPixelNames(override val pixelName: String) : Pixel.PixelName {
    CUSTOM_TABS_PRIVACY_DASHBOARD_ALLOW_LIST_ADD("m_custom_tabs_privacy_dashboard_allow_list_add"),
    CUSTOM_TABS_PRIVACY_DASHBOARD_ALLOW_LIST_REMOVE("m_custom_tabs_privacy_dashboard_allow_list_remove"),
}
