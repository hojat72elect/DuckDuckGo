

package com.duckduckgo.networkprotection.impl.configuration

import com.duckduckgo.app.global.api.ApiInterceptorPlugin
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.appbuildconfig.api.isInternalBuild
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.networkprotection.impl.BuildConfig
import com.duckduckgo.subscriptions.api.Subscriptions
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import logcat.logcat
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

@ContributesMultibinding(
    scope = AppScope::class,
    boundType = ApiInterceptorPlugin::class,
)
class NetpControllerRequestInterceptor @Inject constructor(
    private val appBuildConfig: AppBuildConfig,
    private val subscriptions: Subscriptions,
) : ApiInterceptorPlugin, Interceptor {

    override fun getInterceptor(): Interceptor = this

    override fun intercept(chain: Chain): Response {
        val url = chain.request().url
        val newRequest = chain.request().newBuilder()
        return if (ENDPOINTS_PATTERN_MATCHER.any { url.toString().endsWith(it) }) {
            logcat { "Adding Authorization Bearer token to request $url" }
            newRequest.addHeader(
                name = "Authorization",
                // this runBlocking is fine as we're already in a background thread
                value = runBlocking { authorizationHeaderValue() },
            )

            if (appBuildConfig.isInternalBuild()) {
                newRequest.addHeader(
                    name = "NetP-Debug-Code",
                    value = BuildConfig.NETP_DEBUG_SERVER_TOKEN,
                )
            }

            chain.proceed(
                newRequest.build().also { logcat { "headers: ${it.headers}" } },
            )
        } else {
            chain.proceed(newRequest.build())
        }
    }

    private suspend fun authorizationHeaderValue(): String {
        return "bearer ddg:${subscriptions.getAccessToken()}"
    }

    companion object {
        // The NetP environments are for now https://<something>.netp.duckduckgo.com/<endpoint>
        private val ENDPOINTS_PATTERN_MATCHER = listOf(
            "netp.duckduckgo.com/servers",
            "netp.duckduckgo.com/register",
            "netp.duckduckgo.com/locations",
        )
    }
}
