

package com.duckduckgo.app.browser.defaultbrowsing

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import com.duckduckgo.app.statistics.api.BrowserFeatureStateReporterPlugin
import com.duckduckgo.app.statistics.pixels.Pixel.PixelParameter
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import timber.log.Timber

interface DefaultBrowserDetector {
    fun deviceSupportsDefaultBrowserConfiguration(): Boolean
    fun isDefaultBrowser(): Boolean
    fun hasDefaultBrowser(): Boolean
}

@ContributesMultibinding(scope = AppScope::class, boundType = BrowserFeatureStateReporterPlugin::class)
@ContributesBinding(scope = AppScope::class, boundType = DefaultBrowserDetector::class)
class AndroidDefaultBrowserDetector @Inject constructor(
    private val context: Context,
    private val appBuildConfig: AppBuildConfig,
) : DefaultBrowserDetector, BrowserFeatureStateReporterPlugin {

    override fun deviceSupportsDefaultBrowserConfiguration(): Boolean {
        // previously was ensuring that device was >= Build.VERSION_CODES.N. Returning true here to minimize further changes.
        return true
    }

    override fun isDefaultBrowser(): Boolean {
        val defaultBrowserPackage = defaultBrowserPackage()
        val defaultAlready = defaultBrowserPackage == appBuildConfig.applicationId
        Timber.i("Default browser identified as $defaultBrowserPackage")
        return defaultAlready
    }

    override fun hasDefaultBrowser(): Boolean = defaultBrowserPackage() != null

    private fun defaultBrowserPackage(): String? {
        val intent = Intent(ACTION_VIEW, Uri.parse("https://duckduckgo.com/"))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        val resolutionInfo: ResolveInfo? = context.packageManager.resolveActivityCompat(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolutionInfo?.activityInfo?.packageName
    }

    override fun featureState(): Pair<Boolean, String> {
        return Pair(isDefaultBrowser(), PixelParameter.DEFAULT_BROWSER)
    }

    @Suppress("NewApi") // we use appBuildConfig
    private fun PackageManager.resolveActivityCompat(intent: Intent, flag: Int): ResolveInfo? {
        return if (appBuildConfig.sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            resolveActivity(intent, PackageManager.ResolveInfoFlags.of(flag.toLong()))
        } else {
            resolveActivity(intent, flag)
        }
    }
}
