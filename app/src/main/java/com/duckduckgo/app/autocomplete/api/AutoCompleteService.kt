

package com.duckduckgo.app.autocomplete.api

import com.duckduckgo.anvil.annotations.ContributesNonCachingServiceApi
import com.duckduckgo.common.utils.AppUrl
import com.duckduckgo.di.scopes.AppScope
import io.reactivex.Observable
import java.util.*
import retrofit2.http.GET
import retrofit2.http.Query

@ContributesNonCachingServiceApi(AppScope::class)
interface AutoCompleteService {

    @GET("${AppUrl.Url.API}/ac/")
    fun autoComplete(
        @Query("q") query: String,
        @Query("kl") languageCode: String = Locale.getDefault().language,
        @Query("is_nav") nav: String = "1",
    ): Observable<List<AutoCompleteServiceRawResult>>
}

data class AutoCompleteServiceRawResult(
    val phrase: String,
    val isNav: Boolean?,
)
