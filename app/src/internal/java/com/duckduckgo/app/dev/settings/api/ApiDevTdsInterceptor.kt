

package com.duckduckgo.app.dev.settings.api

import com.duckduckgo.app.dev.settings.db.DevSettingsDataStore
import com.duckduckgo.app.global.api.ApiInterceptorPlugin
import com.duckduckgo.app.trackerdetection.api.TDS_URL
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

@ContributesMultibinding(
    scope = AppScope::class,
    boundType = ApiInterceptorPlugin::class,
)
class ApiDevTdsInterceptor @Inject constructor(
    private val devSettingsDataStore: DevSettingsDataStore,
) : ApiInterceptorPlugin, Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

        val url = chain.request().url
        if (url.toString().contains(TDS_URL)) {
            val tds = if (devSettingsDataStore.nextTdsEnabled) {
                NEXT_TDS
            } else {
                TDS
            }
            request.url("$TDS_URL$tds")
        }

        return chain.proceed(request.build())
    }

    override fun getInterceptor(): Interceptor {
        return this
    }

    companion object {
        private const val NEXT_TDS = "next/android-tds.json"
        private const val TDS = "current/android-tds.json"
    }
}
