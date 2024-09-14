

package com.duckduckgo.app.global.api

import com.duckduckgo.common.test.api.FakeChain
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PixelReQueryInterceptorTest {

    private lateinit var pixelReQueryInterceptor: PixelReQueryInterceptor

    @Before
    fun setup() {
        pixelReQueryInterceptor = PixelReQueryInterceptor()
    }

    @Test
    fun whenRq0PixelIsSendThenRemoveDeviceAndFormFactor() {
        assertEquals(
            EXPECTED_RQ_0_URL.toHttpUrl(),
            pixelReQueryInterceptor.intercept(FakeChain(RQ_0_PHONE_URL)).request.url,
        )

        assertEquals(
            EXPECTED_RQ_0_URL.toHttpUrl(),
            pixelReQueryInterceptor.intercept(FakeChain(RQ_0_TABLET_URL)).request.url,
        )
    }

    @Test
    fun whenRq1PixelIsSendThenRemoveDeviceAndFormFactor() {
        assertEquals(
            EXPECTED_RQ_1_URL.toHttpUrl(),
            pixelReQueryInterceptor.intercept(FakeChain(RQ_1_PHONE_URL)).request.url,
        )

        assertEquals(
            EXPECTED_RQ_1_URL.toHttpUrl(),
            pixelReQueryInterceptor.intercept(FakeChain(RQ_1_TABLET_URL)).request.url,
        )
    }

    @Test
    fun whenPixelOtherThanRqIsSendThenDoNotModify() {
        assertEquals(
            EXPECTED_OTHER_PIXEL_PHONE_URL.toHttpUrl(),
            pixelReQueryInterceptor.intercept(FakeChain(OTHER_PIXEL_PHONE_URL)).request.url,
        )

        assertEquals(
            EXPECTED_OTHER_PIXEL_TABLET_URL.toHttpUrl(),
            pixelReQueryInterceptor.intercept(FakeChain(OTHER_PIXEL_TABLET_URL)).request.url,
        )
    }

    private companion object {
        private const val RQ_0_PHONE_URL = "https://improving.duckduckgo.com/t/rq_0_android_phone?atb=v255-7zu&appVersion=5.74.0&test=1"
        private const val RQ_0_TABLET_URL = "https://improving.duckduckgo.com/t/rq_0_android_tablet?atb=v255-7zu&appVersion=5.74.0&test=1"
        private const val RQ_1_PHONE_URL = "https://improving.duckduckgo.com/t/rq_1_android_phone?atb=v255-7zu&appVersion=5.74.0&test=1"
        private const val RQ_1_TABLET_URL = "https://improving.duckduckgo.com/t/rq_1_android_tablet?atb=v255-7zu&appVersion=5.74.0&test=1"
        private const val OTHER_PIXEL_PHONE_URL = "https://improving.duckduckgo.com/t/my_pixel_android_phone?atb=v255-7zu&appVersion=5.74.0&test=1"
        private const val OTHER_PIXEL_TABLET_URL = "https://improving.duckduckgo.com/t/my_pixel_android_tablet?atb=v255-7zu&appVersion=5.74.0&test=1"

        private const val EXPECTED_RQ_0_URL = "https://improving.duckduckgo.com/t/rq_0?atb=v255-7zu&appVersion=5.74.0&test=1"
        private const val EXPECTED_RQ_1_URL = "https://improving.duckduckgo.com/t/rq_1?atb=v255-7zu&appVersion=5.74.0&test=1"
        private const val EXPECTED_OTHER_PIXEL_PHONE_URL =
            "https://improving.duckduckgo.com/t/my_pixel_android_phone?atb=v255-7zu&appVersion=5.74.0&test=1"
        private const val EXPECTED_OTHER_PIXEL_TABLET_URL =
            "https://improving.duckduckgo.com/t/my_pixel_android_tablet?atb=v255-7zu&appVersion=5.74.0&test=1"
    }
}
