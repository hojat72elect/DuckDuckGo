

package com.duckduckgo.privacy.dashboard.impl.ui

import android.webkit.WebView
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.privacy.dashboard.impl.ui.RendererViewHolder.WebviewRenderer
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Named

interface PrivacyDashboardRendererFactory {
    fun createRenderer(renderer: RendererViewHolder): PrivacyDashboardRenderer
}

sealed class RendererViewHolder {
    data class WebviewRenderer(
        val holder: WebView,
        val onPrivacyProtectionSettingChanged: (Boolean) -> Unit,
        val onBrokenSiteClicked: () -> Unit,
        val onPrivacyProtectionsClicked: (Boolean) -> Unit,
        val onUrlClicked: (String) -> Unit,
        val onOpenSettings: (String) -> Unit,
        val onClose: () -> Unit,
        val onSubmitBrokenSiteReport: (String) -> Unit,
    ) : RendererViewHolder()
}

@ContributesBinding(ActivityScope::class)
class BrowserPrivacyDashboardRendererFactory @Inject constructor(
    @Named("privacyDashboard") val moshi: Moshi,
) : PrivacyDashboardRendererFactory {

    override fun createRenderer(renderer: RendererViewHolder): PrivacyDashboardRenderer {
        return when (renderer) {
            is WebviewRenderer -> PrivacyDashboardRenderer(
                renderer.holder,
                renderer.onPrivacyProtectionSettingChanged,
                moshi,
                renderer.onBrokenSiteClicked,
                renderer.onPrivacyProtectionsClicked,
                renderer.onUrlClicked,
                renderer.onOpenSettings,
                renderer.onClose,
                renderer.onSubmitBrokenSiteReport,
            )
        }
    }
}
