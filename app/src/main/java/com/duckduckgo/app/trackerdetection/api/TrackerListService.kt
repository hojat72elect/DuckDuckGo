

package com.duckduckgo.app.trackerdetection.api

import com.duckduckgo.anvil.annotations.ContributesServiceApi
import com.duckduckgo.di.scopes.AppScope
import retrofit2.Call
import retrofit2.http.GET

@ContributesServiceApi(AppScope::class)
interface TrackerListService {
    @GET("${TDS_URL}current/android-tds.json")
    fun tds(): Call<TdsJson>

    @GET("/contentblocking/trackers-unprotected-temporary.txt")
    fun temporaryAllowList(): Call<String>
}

const val TDS_URL = "https://staticcdn.duckduckgo.com/trackerblocking/v5/"
