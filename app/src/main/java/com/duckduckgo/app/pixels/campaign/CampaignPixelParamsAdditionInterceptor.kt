package com.duckduckgo.app.pixels.campaign

import com.duckduckgo.app.pixels.campaign.params.AdditionalPixelParamsGenerator
import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.common.utils.plugins.pixel.PixelInterceptorPlugin
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

@ContributesMultibinding(
    scope = AppScope::class,
    boundType = PixelInterceptorPlugin::class,
)
class CampaignPixelParamsAdditionInterceptor @Inject constructor(
    private val pixelsPlugin: PluginPoint<CampaignPixelParamsAdditionPlugin>,
    private val additionalPixelParamsGenerator: AdditionalPixelParamsGenerator,
    private val additionalPixelParamsFeature: AdditionalPixelParamsFeature,
    private val additionalPixelParamsDataStore: AdditionalPixelParamsDataStore,
) : Interceptor, PixelInterceptorPlugin {
    override fun intercept(chain: Chain): Response {
        val request = chain.request().newBuilder()
        val url = chain.request().url.newBuilder()

        if (additionalPixelParamsFeature.self().isEnabled()) {
            val queryParamsString = chain.request().url.query
            if (queryParamsString != null) {
                val pixel = chain.request().url.pathSegments.last()
                pixelsPlugin.getPlugins().forEach { plugin ->
                    if (plugin.names().any { pixel.startsWith(it) }) {
                        val queryParams = queryParamsString.toParamsMap()
                        if (plugin.isEligible(queryParams)) {
                            runBlocking {
                                additionalPixelParamsGenerator.generateAdditionalParams()
                                    .forEach { (key, value) ->
                                        url.addQueryParameter(key, value)
                                    }
                            }
                        }
                    }
                }
            }
        }

        return chain.proceed(request.url(url.build()).build())
    }

    private fun CampaignPixelParamsAdditionPlugin.isEligible(queryParams: Map<String, String>): Boolean {
        val campaign = this.extractCampaign(queryParams)
        return campaign != null && additionalPixelParamsDataStore.includedOrigins.contains(campaign)
    }

    private fun String.toParamsMap(): Map<String, String> {
        return split("&").associate {
            val param = it.split("=")
            param[0] to param[1]
        }
    }

    override fun getInterceptor(): Interceptor = this
}
