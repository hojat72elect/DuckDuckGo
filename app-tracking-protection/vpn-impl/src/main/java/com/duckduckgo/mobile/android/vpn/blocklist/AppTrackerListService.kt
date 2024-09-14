

package com.duckduckgo.mobile.android.vpn.blocklist

import com.duckduckgo.anvil.annotations.ContributesServiceApi
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.mobile.android.vpn.trackers.JsonAppBlockingList
import retrofit2.Call
import retrofit2.http.GET

@ContributesServiceApi(AppScope::class)
interface AppTrackerListService {
    @GET("https://staticcdn.duckduckgo.com/trackerblocking/appTP/2.1/android-tds.json")
    fun appTrackerBlocklist(): Call<JsonAppBlockingList>
}
