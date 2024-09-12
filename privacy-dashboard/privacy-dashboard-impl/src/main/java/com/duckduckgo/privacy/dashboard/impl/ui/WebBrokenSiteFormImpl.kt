package com.duckduckgo.privacy.dashboard.impl.ui

import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.privacy.dashboard.api.ui.WebBrokenSiteForm
import com.duckduckgo.privacy.dashboard.impl.WebBrokenSiteFormFeature
import com.duckduckgo.privacy.dashboard.impl.isEnabled
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class WebBrokenSiteFormImpl @Inject constructor(
    private val webBrokenSiteFormFeature: WebBrokenSiteFormFeature,
) : WebBrokenSiteForm {
    override fun shouldUseWebBrokenSiteForm(): Boolean = webBrokenSiteFormFeature.isEnabled()
}
