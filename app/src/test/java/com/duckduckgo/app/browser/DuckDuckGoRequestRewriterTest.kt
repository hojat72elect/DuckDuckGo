

package com.duckduckgo.app.browser

import android.net.Uri
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.referral.AppReferrerDataStore
import com.duckduckgo.app.statistics.model.Atb
import com.duckduckgo.app.statistics.store.StatisticsDataStore
import com.duckduckgo.common.utils.AppUrl.ParamKey
import com.duckduckgo.experiments.api.VariantManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class DuckDuckGoRequestRewriterTest {

    private lateinit var testee: DuckDuckGoRequestRewriter
    private val mockStatisticsStore: StatisticsDataStore = mock()
    private val mockVariantManager: VariantManager = mock()
    private val mockAppReferrerDataStore: AppReferrerDataStore = mock()
    private lateinit var builder: Uri.Builder
    private val currentUrl = "http://www.duckduckgo.com"

    @Before
    fun before() {
        whenever(mockVariantManager.getVariantKey()).thenReturn("")
        whenever(mockAppReferrerDataStore.installedFromEuAuction).thenReturn(false)
        testee = DuckDuckGoRequestRewriter(
            DuckDuckGoUrlDetectorImpl(),
            mockStatisticsStore,
            mockVariantManager,
            mockAppReferrerDataStore,
        )
        builder = Uri.Builder()
    }

    @Test
    fun whenAddingCustomParamsSourceParameterIsAdded() {
        testee.addCustomQueryParams(builder)
        val uri = builder.build()
        assertTrue(uri.queryParameterNames.contains(ParamKey.SOURCE))
        assertEquals("ddg_android", uri.getQueryParameter(ParamKey.SOURCE))
    }

    @Test
    fun whenAddingCustomParamsAndUserSourcedFromEuAuctionThenEuSourceParameterIsAdded() {
        whenever(mockAppReferrerDataStore.installedFromEuAuction).thenReturn(true)
        testee.addCustomQueryParams(builder)
        val uri = builder.build()
        assertTrue(uri.queryParameterNames.contains(ParamKey.SOURCE))
        assertEquals("ddg_androideu", uri.getQueryParameter(ParamKey.SOURCE))
    }

    @Test
    fun whenAddingCustomParamsIfStoreContainsAtbIsAdded() {
        whenever(mockStatisticsStore.atb).thenReturn(Atb("v105-2ma"))
        testee.addCustomQueryParams(builder)
        val uri = builder.build()
        assertTrue(uri.queryParameterNames.contains(ParamKey.ATB))
        assertEquals("v105-2ma", uri.getQueryParameter(ParamKey.ATB))
    }

    @Test
    fun whenAddingCustomParamsIfIsStoreMissingAtbThenAtbIsNotAdded() {
        whenever(mockStatisticsStore.atb).thenReturn(null)

        testee.addCustomQueryParams(builder)
        val uri = builder.build()
        assertFalse(uri.queryParameterNames.contains(ParamKey.ATB))
    }

    @Test
    fun whenSerpRemovalFeatureIsActiveThenHideParamIsAddedToSerpUrl() {
        testee.addCustomQueryParams(builder)

        val uri = builder.build()
        assertTrue(uri.queryParameterNames.contains(ParamKey.HIDE_SERP))
    }

    @Test
    fun whenShouldRewriteRequestAndUrlIsSerpQueryThenReturnTrue() {
        val uri = "http://duckduckgo.com/?q=weather".toUri()
        assertTrue(testee.shouldRewriteRequest(uri))
    }

    @Test
    fun whenShouldRewriteRequestAndUrlIsSerpQueryWithSourceAndAtbThenReturnFalse() {
        val uri = "http://duckduckgo.com/?q=weather&atb=test&t=test".toUri()
        assertFalse(testee.shouldRewriteRequest(uri))
    }

    @Test
    fun whenShouldRewriteRequestAndUrlIsADuckDuckGoStaticUrlThenReturnTrue() {
        val uri = "http://duckduckgo.com/settings".toUri()
        assertTrue(testee.shouldRewriteRequest(uri))

        val uri2 = "http://duckduckgo.com/params".toUri()
        assertTrue(testee.shouldRewriteRequest(uri2))
    }

    @Test
    fun whenShouldRewriteRequestAndUrlIsDuckDuckGoEmailThenReturnFalse() {
        val uri = "http://duckduckgo.com/email".toUri()
        assertFalse(testee.shouldRewriteRequest(uri))
    }
}
