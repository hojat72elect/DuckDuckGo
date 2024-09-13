package com.duckduckgo.privacy.dashboard.api.ui

interface WebBrokenSiteForm {
    /**
     * Returns true if web version of the broken site form should be used instead of the native implementation.
     */
    fun shouldUseWebBrokenSiteForm(): Boolean
}
