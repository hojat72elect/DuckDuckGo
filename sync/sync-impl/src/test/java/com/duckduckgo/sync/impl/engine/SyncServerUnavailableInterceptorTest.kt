package com.duckduckgo.sync.impl.engine

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.common.test.api.FakeChain
import com.duckduckgo.sync.impl.API_CODE.TOO_MANY_REQUESTS_1
import com.duckduckgo.sync.impl.SyncService
import com.duckduckgo.sync.impl.error.SyncUnavailableRepository
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class SyncServerUnavailableInterceptorTest {

    private val syncUnavailableRepository = mock<SyncUnavailableRepository>()

    private val serverUnavailableInterceptor =
        SyncServerUnavailableInterceptor(syncUnavailableRepository)

    @Test
    fun whenInterceptingSyncGetResponseTooManyRequestsThenServerUnavailable() {
        val chain = givenGetRequest(SyncService.SYNC_PROD_ENVIRONMENT_URL, TOO_MANY_REQUESTS_1.code)

        serverUnavailableInterceptor.intercept(chain)

        verify(syncUnavailableRepository).onServerUnavailable()
    }

    @Test
    fun whenInterceptingSyncResponseSuccessfulThenServerAvailable() {
        val chain = givenGetRequest(SyncService.SYNC_PROD_ENVIRONMENT_URL)

        serverUnavailableInterceptor.intercept(chain)

        verify(syncUnavailableRepository).onServerAvailable()
    }

    @Test
    fun whenInterceptingSyncPatchResponseTooManyRequestsThenServerUnavailable() {
        val chain =
            givenPatchRequest(SyncService.SYNC_PROD_ENVIRONMENT_URL, TOO_MANY_REQUESTS_1.code)

        serverUnavailableInterceptor.intercept(chain)

        verify(syncUnavailableRepository).onServerUnavailable()
    }

    @Test
    fun whenInterceptingSyncPatchResponseSuccessfulThenServerAvailable() {
        val chain = givenPatchRequest(SyncService.SYNC_PROD_ENVIRONMENT_URL)

        serverUnavailableInterceptor.intercept(chain)

        verify(syncUnavailableRepository).onServerAvailable()
    }

    private fun givenGetRequest(
        url: String,
        expectedResponseCode: Int? = null,
    ): Chain {
        return object : FakeChain(url, expectedResponseCode) {
            override fun request() = Request.Builder().url(url).method("GET", null).build()
        }
    }

    private fun givenPatchRequest(
        url: String,
        expectedResponseCode: Int? = null,
    ): Chain {
        return object : FakeChain(url, expectedResponseCode) {
            override fun request() =
                Request.Builder().url(url).method("PATCH", "".toRequestBody()).build()
        }
    }
}
