

package com.duckduckgo.app.global.api

import com.duckduckgo.app.browser.WebViewPixelName
import com.duckduckgo.app.browser.httperrors.HttpErrorPixelName
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.pixels.Pixel.StatisticsPixelName
import com.duckduckgo.common.utils.AppUrl
import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.common.utils.plugins.pixel.PixelInterceptorPlugin
import com.duckduckgo.common.utils.plugins.pixel.PixelParamRemovalPlugin
import com.duckduckgo.common.utils.plugins.pixel.PixelParamRemovalPlugin.PixelParameter
import com.duckduckgo.common.utils.plugins.pixel.PixelParamRemovalPlugin.PixelParameter.APP_VERSION
import com.duckduckgo.common.utils.plugins.pixel.PixelParamRemovalPlugin.PixelParameter.ATB
import com.duckduckgo.common.utils.plugins.pixel.PixelParamRemovalPlugin.PixelParameter.OS_VERSION
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

@ContributesMultibinding(
    scope = AppScope::class,
    boundType = PixelInterceptorPlugin::class,
)
class PixelParamRemovalInterceptor @Inject constructor(
    private val pixelsPlugin: PluginPoint<PixelParamRemovalPlugin>,
) : Interceptor, PixelInterceptorPlugin {

    val pixels by lazy {
        pixelsPlugin.getPlugins().flatMap { it.names() }.toSet()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val pixel = chain.request().url.pathSegments.last()
        val url = chain.request().url.newBuilder().apply {
            val atbs = pixels.filter { it.second.contains(ATB) }.map { it.first }
            val versions = pixels.filter { it.second.contains(APP_VERSION) }.map { it.first }
            val oses = pixels.filter { it.second.contains(OS_VERSION) }.map { it.first }
            if (atbs.any { pixel.startsWith(it) }) {
                removeAllQueryParameters(AppUrl.ParamKey.ATB)
            }
            if (versions.any { pixel.startsWith(it) }) {
                removeAllQueryParameters(Pixel.PixelParameter.APP_VERSION)
            }
            if (oses.any { pixel.startsWith(it) }) {
                removeAllQueryParameters(Pixel.PixelParameter.OS_VERSION)
            }
        }.build()

        return chain.proceed(request.url(url).build())
    }

    override fun getInterceptor(): Interceptor {
        return this
    }
}

@ContributesMultibinding(
    scope = AppScope::class,
    boundType = PixelParamRemovalPlugin::class,
)
object PixelInterceptorPixelsRequiringDataCleaning : PixelParamRemovalPlugin {
    override fun names(): List<Pair<String, Set<PixelParameter>>> {
        return listOf(
            AppPixelName.EMAIL_COPIED_TO_CLIPBOARD.pixelName to PixelParameter.removeAll(),
            StatisticsPixelName.BROWSER_DAILY_ACTIVE_FEATURE_STATE.pixelName to PixelParameter.removeAll(),
            WebViewPixelName.WEB_PAGE_LOADED.pixelName to PixelParameter.removeAll(),
            WebViewPixelName.WEB_PAGE_PAINTED.pixelName to PixelParameter.removeAll(),
            AppPixelName.REFERRAL_INSTALL_UTM_CAMPAIGN.pixelName to PixelParameter.removeAtb(),
            HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY.pixelName to PixelParameter.removeAtb(),
            HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_4XX_DAILY.pixelName to PixelParameter.removeAtb(),
            HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_5XX_DAILY.pixelName to PixelParameter.removeAtb(),
        )
    }
}
