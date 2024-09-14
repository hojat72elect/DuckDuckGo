

package com.duckduckgo.app.referral

import com.duckduckgo.app.statistics.AtbInitializerListener
import kotlinx.coroutines.delay

class StubAppReferrerFoundStateListener(
    private val referrer: String,
    private val mockDelayMs: Long = 0,
) : AppInstallationReferrerStateListener, AtbInitializerListener {
    override suspend fun waitForReferrerCode(): ParsedReferrerResult {
        if (mockDelayMs > 0) delay(mockDelayMs)

        return ParsedReferrerResult.CampaignReferrerFound(referrer)
    }

    override fun initialiseReferralRetrieval() {
    }

    override suspend fun beforeAtbInit() {
        waitForReferrerCode()
    }

    override fun beforeAtbInitTimeoutMillis(): Long {
        return mockDelayMs
    }
}
