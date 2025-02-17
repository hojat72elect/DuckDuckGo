

package com.duckduckgo.sync.impl.pixels

import android.content.SharedPreferences
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.test.api.InMemorySharedPreferences
import com.duckduckgo.common.utils.formatters.time.DatabaseDateFormatter
import com.duckduckgo.sync.api.engine.SyncableType
import com.duckduckgo.sync.impl.API_CODE
import com.duckduckgo.sync.impl.Result.Error
import com.duckduckgo.sync.impl.stats.DailyStats
import com.duckduckgo.sync.impl.stats.SyncStatsRepository
import com.duckduckgo.sync.store.SharedPrefsProvider
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealSyncPixelsTest {

    private var pixel: Pixel = mock()
    private var syncStatsRepository: SyncStatsRepository = mock()
    private var sharedPrefsProv: SharedPrefsProvider = mock()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var testee: RealSyncPixels

    @Before
    fun setUp() {
        sharedPreferences = InMemorySharedPreferences()
        whenever(
            sharedPrefsProv.getSharedPrefs(eq("com.duckduckgo.sync.pixels.v1")),
        ).thenReturn(sharedPreferences)

        testee = RealSyncPixels(
            pixel,
            syncStatsRepository,
            sharedPrefsProv,
        )
    }

    @Test
    fun whenDailyPixelCalledThenPixelFired() {
        val dailyStats = givenSomeDailyStats()

        testee.fireDailySuccessRatePixel()

        val payload = mapOf(
            SyncPixelParameters.COUNT to dailyStats.attempts,
            SyncPixelParameters.DATE to dailyStats.date,
        ).plus(dailyStats.apiErrorStats)

        verify(pixel).fire(SyncPixelName.SYNC_DAILY_SUCCESS_RATE_PIXEL, payload)
    }

    @Test
    fun whenDailyPixelCalledTwiceThenPixelFiredOnce() {
        val dailyStats = givenSomeDailyStats()

        testee.fireDailySuccessRatePixel()
        testee.fireDailySuccessRatePixel()

        val payload = mapOf(
            SyncPixelParameters.COUNT to dailyStats.attempts,
            SyncPixelParameters.DATE to dailyStats.date,
        ).plus(dailyStats.apiErrorStats).plus(dailyStats.operationErrorStats)

        verify(pixel, times(1)).fire(SyncPixelName.SYNC_DAILY_SUCCESS_RATE_PIXEL, payload)
    }

    @Test
    fun whenLoginPixelCalledThenPixelFired() {
        testee.fireLoginPixel()

        verify(pixel).fire(SyncPixelName.SYNC_LOGIN)
    }

    @Test
    fun whenSignupDirectPixelCalledThenPixelFired() {
        testee.fireSignupDirectPixel()

        verify(pixel).fire(SyncPixelName.SYNC_SIGNUP_DIRECT)
    }

    @Test
    fun whenSignupConnectPixelCalledThenPixelFired() {
        testee.fireSignupConnectPixel()

        verify(pixel).fire(SyncPixelName.SYNC_SIGNUP_CONNECT)
    }

    @Test
    fun whenfireDailyApiErrorForObjectLimitExceededThenPixelSent() {
        testee.fireDailySyncApiErrorPixel(SyncableType.BOOKMARKS, Error(code = API_CODE.COUNT_LIMIT.code))

        verify(pixel).fire("m_sync_bookmarks_object_limit_exceeded_daily", emptyMap(), emptyMap(), type = Pixel.PixelType.DAILY)
    }

    @Test
    fun whenfireDailyApiErrorForRequestSizeLimitExceededThenPixelSent() {
        testee.fireDailySyncApiErrorPixel(SyncableType.BOOKMARKS, Error(code = API_CODE.CONTENT_TOO_LARGE.code))

        verify(pixel).fire("m_sync_bookmarks_request_size_limit_exceeded_daily", emptyMap(), emptyMap(), type = Pixel.PixelType.DAILY)
    }

    @Test
    fun whenfireDailyApiErrorForValidationErrorThenPixelSent() {
        testee.fireDailySyncApiErrorPixel(SyncableType.BOOKMARKS, Error(code = API_CODE.VALIDATION_ERROR.code))

        verify(pixel).fire("m_sync_bookmarks_validation_error_daily", emptyMap(), emptyMap(), type = Pixel.PixelType.DAILY)
    }

    @Test
    fun whenfireDailyApiErrorForTooManyRequestsThenPixelSent() {
        testee.fireDailySyncApiErrorPixel(SyncableType.BOOKMARKS, Error(code = API_CODE.TOO_MANY_REQUESTS_1.code))
        testee.fireDailySyncApiErrorPixel(SyncableType.BOOKMARKS, Error(code = API_CODE.TOO_MANY_REQUESTS_2.code))

        verify(pixel, times(2)).fire("m_sync_bookmarks_too_many_requests_daily", emptyMap(), emptyMap(), type = Pixel.PixelType.DAILY)
    }

    private fun givenSomeDailyStats(): DailyStats {
        val date = DatabaseDateFormatter.getUtcIsoLocalDate()
        val dailyStats = DailyStats("1", date, emptyMap())
        whenever(syncStatsRepository.getYesterdayDailyStats()).thenReturn(dailyStats)

        return dailyStats
    }
}
