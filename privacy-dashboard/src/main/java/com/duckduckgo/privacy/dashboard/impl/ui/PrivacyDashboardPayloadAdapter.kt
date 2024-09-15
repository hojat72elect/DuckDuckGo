

package com.duckduckgo.privacy.dashboard.impl.ui

import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.privacy.dashboard.impl.ui.AppPrivacyDashboardPayloadAdapter.BreakageReportRequest
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Named

interface PrivacyDashboardPayloadAdapter {
    fun onUrlClicked(payload: String): String
    fun onOpenSettings(payload: String): String
    fun onSubmitBrokenSiteReport(payload: String): BreakageReportRequest?
}

@ContributesBinding(AppScope::class)
class AppPrivacyDashboardPayloadAdapter @Inject constructor(
    @Named("privacyDashboard") private val moshi: Moshi,
) : PrivacyDashboardPayloadAdapter {
    override fun onUrlClicked(payload: String): String {
        val payloadAdapter = moshi.adapter(Payload::class.java)
        return kotlin.runCatching { payloadAdapter.fromJson(payload)?.url ?: "" }.getOrDefault("")
    }
    override fun onOpenSettings(payload: String): String {
        val payloadAdapter = moshi.adapter(SettingsPayload::class.java)
        return kotlin.runCatching { payloadAdapter.fromJson(payload)?.target ?: "" }.getOrDefault("")
    }

    override fun onSubmitBrokenSiteReport(payload: String): BreakageReportRequest? {
        val payloadAdapter = moshi.adapter(BreakageReportRequest::class.java)
        return kotlin.runCatching { payloadAdapter.fromJson(payload) }.getOrNull()
    }

    data class Payload(val url: String)
    data class SettingsPayload(val target: String)

    data class BreakageReportRequest(
        val category: String,
        val description: String,
    )
}
