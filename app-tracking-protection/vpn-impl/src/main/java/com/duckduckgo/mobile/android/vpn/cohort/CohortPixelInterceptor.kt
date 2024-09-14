

package com.duckduckgo.mobile.android.vpn.cohort

import androidx.annotation.VisibleForTesting
import com.duckduckgo.common.utils.plugins.pixel.PixelInterceptorPlugin
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import logcat.logcat
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

@ContributesMultibinding(
    scope = AppScope::class,
    boundType = PixelInterceptorPlugin::class,
)
class CohortPixelInterceptor @Inject constructor(
    private val cohortCalculator: CohortCalculator,
    private val cohortStore: CohortStore,
) : PixelInterceptorPlugin, Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val pixel = chain.request().url.pathSegments.last()

        val url = if (pixel.startsWith(PIXEL_PREFIX) && !EXCEPTIONS.any { exception -> pixel.startsWith(exception) }) {
            // IF there is no cohort for ATP we just drop the pixel request
            // ELSE we add the cohort param
            cohortStore.getCohortStoredLocalDate()?.let {
                chain.request().url.newBuilder().addQueryParameter(COHORT_PARAM, cohortCalculator.calculateCohortForDate(it)).build()
            } ?: return dummyResponse(chain)
        } else {
            chain.request().url
        }

        return chain.proceed(request.url(url).build())
    }

    private fun dummyResponse(chain: Interceptor.Chain): Response {
        logcat { "Pixel URL request dropped: ${chain.request()}" }

        return Response.Builder()
            .code(200)
            .protocol(Protocol.HTTP_2)
            .body("ATP pixel dropped".toResponseBody())
            .message("Dropped ATP pixel because no cohort is assigned")
            .request(chain.request())
            .build()
    }

    override fun getInterceptor(): Interceptor {
        return this
    }

    companion object {
        @VisibleForTesting
        internal const val COHORT_PARAM = "atp_cohort"
        private const val PIXEL_PREFIX = "m_atp_"
        private val EXCEPTIONS = listOf(
            "m_atp_ev_enabled_onboarding_",
            "m_atp_ev_cpu_usage_above_",
            "m_atp_unprotected_apps_bucket_",
            "m_atp_breakage_report",
        )
    }
}
