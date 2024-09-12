package com.duckduckgo.subscriptions.impl.pixels

import com.duckduckgo.common.utils.plugins.pixel.PixelParamRemovalPlugin
import com.duckduckgo.common.utils.plugins.pixel.PixelParamRemovalPlugin.PixelParameter
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class SubscriptionPixelParamRemovalPlugin @Inject constructor() : PixelParamRemovalPlugin {
    override fun names(): List<Pair<String, Set<PixelParameter>>> {
        return listOf(
            "m_subscribe" to PixelParameter.removeAtb(),
            "m_subscribe" to PixelParameter.removeOSVersion(),
            "m_ppro_feedback" to PixelParameter.removeAtb(),
        )
    }
}
