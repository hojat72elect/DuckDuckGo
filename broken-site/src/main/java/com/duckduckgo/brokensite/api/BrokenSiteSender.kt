package com.duckduckgo.brokensite.api

interface BrokenSiteSender {
    fun submitBrokenSiteFeedback(brokenSite: BrokenSite)
}

data class BrokenSite(
    val category: String?,
    val description: String?,
    val siteUrl: String,
    val upgradeHttps: Boolean,
    val blockedTrackers: String,
    val surrogates: String,
    val siteType: String,
    val urlParametersRemoved: Boolean,
    val consentManaged: Boolean,
    val consentOptOutFailed: Boolean,
    val consentSelfTestFailed: Boolean,
    val errorCodes: String,
    val httpErrorCodes: String,
    val loginSite: String?,
    val reportFlow: ReportFlow?,
    val userRefreshCount: Int,
    val openerContext: String?,
    val jsPerformance: List<Double>?,
)

enum class ReportFlow { DASHBOARD, MENU }
