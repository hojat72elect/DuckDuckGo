

package com.duckduckgo.app.global.store

import android.content.Context
import androidx.webkit.WebViewCompat
import com.duckduckgo.app.statistics.store.StatisticsDataStore
import com.duckduckgo.browser.api.AppProperties
import com.duckduckgo.common.utils.playstore.PlayStoreUtils
import com.duckduckgo.experiments.api.VariantManager
import timber.log.Timber

class AndroidAppProperties(
    private val appContext: Context,
    private val variantManager: VariantManager,
    private val playStoreUtils: PlayStoreUtils,
    private val statisticsStore: StatisticsDataStore,
) : AppProperties {

    override fun atb(): String {
        return statisticsStore.atb?.version.orEmpty()
    }

    override fun appAtb(): String {
        return statisticsStore.appRetentionAtb.orEmpty()
    }

    override fun searchAtb(): String {
        return statisticsStore.searchRetentionAtb.orEmpty()
    }

    override fun expVariant(): String {
        return variantManager.getVariantKey().orEmpty()
    }

    override fun installedGPlay(): Boolean {
        return playStoreUtils.installedFromPlayStore()
    }

    override fun webView(): String {
        return kotlin.runCatching {
            WebViewCompat.getCurrentWebViewPackage(appContext)?.versionName ?: WEBVIEW_UNKNOWN_VERSION
        }.getOrElse {
            Timber.e(it, "Error getting current WebView package")
            WEBVIEW_UNKNOWN_VERSION
        }
    }

    companion object {
        const val WEBVIEW_UNKNOWN_VERSION = "unknown"
    }
}
