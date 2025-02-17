package com.duckduckgo.subscriptions.impl.feedback

import android.util.Base64
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.mobile.android.vpn.state.VpnStateCollector
import com.duckduckgo.subscriptions.api.Subscriptions
import com.duckduckgo.subscriptions.impl.feedback.SubscriptionFeedbackCategory.SUBS_AND_PAYMENTS
import com.duckduckgo.subscriptions.impl.feedback.SubscriptionFeedbackCategory.VPN
import com.duckduckgo.subscriptions.impl.repository.isActive
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import kotlinx.coroutines.withContext
import org.json.JSONObject

interface FeedbackCustomMetadataProvider {
    suspend fun getCustomMetadata(
        category: SubscriptionFeedbackCategory,
        appPackageId: String? = null,
    ): String
}

@ContributesBinding(ActivityScope::class)
class RealFeedbackCustomMetadataProvider @Inject constructor(
    private val vpnStateCollector: VpnStateCollector,
    private val dispatcherProvider: DispatcherProvider,
    private val subscriptions: Subscriptions,
) : FeedbackCustomMetadataProvider {
    override suspend fun getCustomMetadata(
        category: SubscriptionFeedbackCategory,
        appPackageId: String?,
    ): String {
        return withContext(dispatcherProvider.io()) {
            when (category) {
                VPN -> Base64.encodeToString(
                    generateVPNCustomMetadata(appPackageId).toByteArray(),
                    Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE,
                )

                SUBS_AND_PAYMENTS -> Base64.encodeToString(
                    generateSubscriptionCustomMetadata().toByteArray(),
                    Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE,
                )

                else -> ""
            }
        }
    }

    private suspend fun generateVPNCustomMetadata(appPackageId: String?): String {
        val state = vpnStateCollector.collectVpnState(appPackageId)
        return state.toString()
    }

    private suspend fun generateSubscriptionCustomMetadata(): String {
        val subsState = JSONObject()
        subsState.put(
            KEY_CUSTOM_METADATA_SUBS_STATUS,
            subscriptions.getSubscriptionStatus().isActive().toString()
        )
        return subsState.toString()
    }

    companion object {
        private const val KEY_CUSTOM_METADATA_SUBS_STATUS = "subscriptionActive"
    }
}
