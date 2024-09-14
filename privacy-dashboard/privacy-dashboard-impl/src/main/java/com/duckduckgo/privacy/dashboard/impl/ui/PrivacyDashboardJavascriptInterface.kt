

package com.duckduckgo.privacy.dashboard.impl.ui

import android.webkit.JavascriptInterface

class PrivacyDashboardJavascriptInterface constructor(
    val onBrokenSiteClicked: () -> Unit,
    val onPrivacyProtectionsClicked: (Boolean) -> Unit,
    val onUrlClicked: (String) -> Unit,
    val onOpenSettings: (String) -> Unit,
    val onClose: () -> Unit,
    val onSubmitBrokenSiteReport: (String) -> Unit,
) {
    @JavascriptInterface
    fun toggleAllowlist(newValue: String) {
        onPrivacyProtectionsClicked(newValue.toBoolean())
    }

    @JavascriptInterface
    fun close() {
        onClose()
    }

    @JavascriptInterface
    fun showBreakageForm() {
        onBrokenSiteClicked()
    }

    @JavascriptInterface
    fun openInNewTab(payload: String) {
        onUrlClicked(payload)
    }

    @JavascriptInterface
    fun openSettings(payload: String) {
        onOpenSettings(payload)
    }

    @JavascriptInterface
    fun submitBrokenSiteReport(payload: String) {
        onSubmitBrokenSiteReport(payload)
    }

    companion object {
        // Interface name used inside js layer
        const val JAVASCRIPT_INTERFACE_NAME = "PrivacyDashboard"
    }
}
