package com.duckduckgo.app.browser.httperrors

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.pixels.Pixel.PixelType.COUNT
import com.duckduckgo.common.test.api.InMemorySharedPreferences
import java.time.Instant
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RealHttpErrorPixelsTest {

    private lateinit var testee: HttpErrorPixels
    private lateinit var prefs: SharedPreferences

    private val mockPixel: Pixel = mock()
    private val mockContext: Context = mock()

    @Before
    fun setup() {
        prefs = InMemorySharedPreferences()
        whenever(
            mockContext.getSharedPreferences(
                "com.duckduckgo.app.browser.httperrors",
                0
            )
        ).thenReturn(prefs)
        testee = RealHttpErrorPixels(mockPixel, mockContext)
    }

    @Test
    fun whenUpdateCountPixelCalledThenSharedPrefUpdated() {
        val key = "${HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY.pixelName}_count"
        assertEquals(0, prefs.getInt(key, 0))

        testee.updateCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY)
        testee.updateCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY)

        assertEquals(2, prefs.getInt(key, 0))
    }

    @Test
    fun whenFireCountPixelCalledForZeroCountThenPixelNotSent() {
        val key = "${HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY.pixelName}_count"
        assertEquals(0, prefs.getInt(key, 0))

        testee.fireCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY)

        verify(mockPixel, never()).fire(
            pixel = eq(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY),
            parameters = any(),
            encodedParameters = any(),
            type = eq(COUNT),
        )
    }

    @Test
    fun whenFireCountPixelCalledForNonZeroCountAndCurrentTimeNotSetThenPixelSent() {
        val key = "${HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY.pixelName}_count"
        testee.updateCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY)
        assertEquals(1, prefs.getInt(key, 0))

        testee.fireCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY)

        verify(mockPixel).fire(
            pixel = eq(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY),
            parameters = eq(mapOf(HttpErrorPixelParameters.HTTP_ERROR_CODE_COUNT to "1")),
            encodedParameters = any(),
            type = eq(COUNT),
        )
    }

    @Test
    fun whenFireCountPixelCalledForNonZeroCountAndCurrentTimeBeforeTimestampThenPixelNotSent() {
        val key = "${HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY.pixelName}_count"
        val timestampKey =
            "${HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY.pixelName}_timestamp"
        val now = Instant.now().toEpochMilli()
        testee.updateCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY)
        assertEquals(1, prefs.getInt(key, 0))
        prefs.edit { putLong(timestampKey, now.plus(TimeUnit.HOURS.toMillis(1))) }

        testee.fireCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY)

        verify(mockPixel, never()).fire(
            pixel = eq(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY),
            parameters = any(),
            encodedParameters = any(),
            type = eq(COUNT),
        )
    }

    @Test
    fun whenFireCountPixelCalledForNonZeroCountAndCurrentTimeAfterTimestampThenPixelSent() {
        val key = "${HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY.pixelName}_count"
        val timestampKey =
            "${HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY.pixelName}_timestamp"
        val now = Instant.now().toEpochMilli()
        testee.updateCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY)
        assertEquals(1, prefs.getInt(key, 0))
        prefs.edit { putLong(timestampKey, now.minus(TimeUnit.HOURS.toMillis(1))) }

        testee.fireCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY)

        verify(mockPixel).fire(
            pixel = eq(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY),
            parameters = eq(mapOf(HttpErrorPixelParameters.HTTP_ERROR_CODE_COUNT to "1")),
            encodedParameters = any(),
            type = eq(COUNT),
        )
    }
}
