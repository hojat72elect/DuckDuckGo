

package com.duckduckgo.privacy.dashboard.impl.ui

import android.webkit.WebView
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.CookiePromptManagementState
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.EntityViewState
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.ProtectionStatusViewState
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.RemoteFeatureSettingsViewState
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.RequestDataViewState
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.SiteViewState
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.ViewState
import com.squareup.moshi.Moshi
import timber.log.Timber

class PrivacyDashboardRenderer(
    private val webView: WebView,
    private val onPrivacyProtectionSettingChanged: (Boolean) -> Unit,
    private val moshi: Moshi,
    private val onBrokenSiteClicked: () -> Unit,
    private val onPrivacyProtectionsClicked: (Boolean) -> Unit,
    private val onUrlClicked: (String) -> Unit,
    private val onOpenSettings: (String) -> Unit,
    private val onClose: () -> Unit,
    private val onSubmitBrokenSiteReport: (String) -> Unit,
) {

    private var lastSeenPrivacyDashboardViewState: ViewState? = null

    fun loadDashboard(webView: WebView, initialScreen: InitialScreen) {
        webView.addJavascriptInterface(
            PrivacyDashboardJavascriptInterface(
                onBrokenSiteClicked = { onBrokenSiteClicked() },
                onPrivacyProtectionsClicked = { newValue ->
                    onPrivacyProtectionsClicked(newValue)
                },
                onUrlClicked = {
                    onUrlClicked(it)
                },
                onOpenSettings = {
                    onOpenSettings(it)
                },
                onClose = { onClose() },
                onSubmitBrokenSiteReport = onSubmitBrokenSiteReport,
            ),
            PrivacyDashboardJavascriptInterface.JAVASCRIPT_INTERFACE_NAME,
        )
        webView.loadUrl("file:///android_asset/html/android.html?screen=${initialScreen.value}")
    }

    fun render(viewState: ViewState) {
        Timber.i("PrivacyDashboard viewState $viewState")
        val siteViewStateAdapter = moshi.adapter(SiteViewState::class.java)
        val siteViewStateJson = siteViewStateAdapter.toJson(viewState.siteViewState)

        val requestDataAdapter = moshi.adapter(RequestDataViewState::class.java)
        val requestDataJson = requestDataAdapter.toJson(viewState.requestData)

        onPrivacyProtectionSettingChanged(viewState.userChangedValues)

        val cookiePromptManagementStatusAdapter = moshi.adapter(CookiePromptManagementState::class.java)
        val cookiePromptManagementStatusJson = cookiePromptManagementStatusAdapter.toJson(viewState.cookiePromptManagementStatus)
        webView.evaluateJavascript("javascript:onChangeConsentManaged($cookiePromptManagementStatusJson);", null)

        // remote feature settings
        val remoteFeatureSettingsAdapter = moshi.adapter(RemoteFeatureSettingsViewState::class.java)
        val remoteFeatureSettingsJson = remoteFeatureSettingsAdapter.toJson(viewState.remoteFeatureSettings)
        webView.evaluateJavascript("javascript:onChangeFeatureSettings($remoteFeatureSettingsJson);", null)

        if (viewState.siteViewState.locale != lastSeenPrivacyDashboardViewState?.siteViewState?.locale) {
            webView.evaluateJavascript("javascript:onChangeLocale($siteViewStateJson);", null)
        }
        if (viewState.protectionStatus != lastSeenPrivacyDashboardViewState?.protectionStatus) {
            val protectionsAdapter = moshi.adapter(ProtectionStatusViewState::class.java)
            val protectionsJson = protectionsAdapter.toJson(viewState.protectionStatus)
            webView.evaluateJavascript("javascript:onChangeProtectionStatus($protectionsJson);", null)
        }
        if (viewState.siteViewState.parentEntity != lastSeenPrivacyDashboardViewState?.siteViewState?.parentEntity) {
            val parentEntityAdapter = moshi.adapter(EntityViewState::class.java)
            val parentEntityJson = parentEntityAdapter.toJson(viewState.siteViewState.parentEntity)
            webView.evaluateJavascript("javascript:onChangeParentEntity($parentEntityJson);", null)
        }
        if (viewState.siteViewState.secCertificateViewModels != lastSeenPrivacyDashboardViewState?.siteViewState?.secCertificateViewModels) {
            webView.evaluateJavascript("javascript:onChangeCertificateData($siteViewStateJson);", null)
        }
        if (viewState.siteViewState.upgradedHttps != lastSeenPrivacyDashboardViewState?.siteViewState?.upgradedHttps) {
            webView.evaluateJavascript("javascript:onChangeUpgradedHttps(${viewState.siteViewState.upgradedHttps});", null)
        }
        webView.evaluateJavascript("javascript:onChangeRequestData(\"${viewState.siteViewState.url}\", $requestDataJson);", null)

        lastSeenPrivacyDashboardViewState = viewState
    }

    enum class InitialScreen(val value: String) {
        PRIMARY("primaryScreen"),
        BREAKAGE_FORM("breakageForm"),
    }
}
