

package com.duckduckgo.app.statistics.pixels

import com.duckduckgo.app.statistics.api.PixelSender
import com.duckduckgo.app.statistics.api.PixelSender.SendPixelResult.PIXEL_SENT
import com.duckduckgo.app.statistics.pixels.Pixel.PixelType.COUNT
import com.duckduckgo.app.statistics.pixels.RxBasedPixelTest.TestPixels.TEST
import com.duckduckgo.common.test.InstantSchedulersRule
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeoutException
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RxBasedPixelTest {

    @get:Rule
    @Suppress("unused")
    val schedulers = InstantSchedulersRule()

    @Mock
    val mockPixelSender = mock<PixelSender>()

    @Test
    fun whenPixelWithoutQueryParamsFiredThenPixelSentWithDefaultParams() {
        givenSendPixelSucceeds()

        val pixel = RxBasedPixel(mockPixelSender)
        pixel.fire(TEST)

        verify(mockPixelSender).sendPixel("test", emptyMap(), emptyMap(), COUNT)
    }

    @Test
    fun whenPixelWithoutQueryParamsFiredFailsThenErrorHandled() {
        givenSendPixelFails()

        val pixel = RxBasedPixel(mockPixelSender)
        pixel.fire(TEST)

        verify(mockPixelSender).sendPixel("test", emptyMap(), emptyMap(), COUNT)
    }

    @Test
    fun whenPixelWithQueryParamsFiredThenPixelSentWithParams() {
        givenSendPixelSucceeds()

        val pixel = RxBasedPixel(mockPixelSender)
        val params = mapOf("param1" to "value1", "param2" to "value2")

        pixel.fire(TEST, params)
        verify(mockPixelSender).sendPixel("test", params, emptyMap(), COUNT)
    }

    @Test
    fun whenPixelWithoutQueryParamsEnqueuedThenPixelEnqueuedWithDefaultParams() {
        givenEnqueuePixelSucceeds()

        val pixel = RxBasedPixel(mockPixelSender)
        pixel.enqueueFire(TEST)

        verify(mockPixelSender).enqueuePixel("test", emptyMap(), emptyMap())
    }

    @Test
    fun whenPixelWithoutQueryParamsEnqueuedThenErrorHandled() {
        givenEnqueuePixelFails()

        val pixel = RxBasedPixel(mockPixelSender)
        pixel.enqueueFire(TEST)

        verify(mockPixelSender).enqueuePixel("test", emptyMap(), emptyMap())
    }

    @Test
    fun whenPixelWithQueryParamsEnqueuedThenPixelEnqueuedWithParams() {
        givenEnqueuePixelSucceeds()

        val pixel = RxBasedPixel(mockPixelSender)
        val params = mapOf("param1" to "value1", "param2" to "value2")
        pixel.enqueueFire(TEST, params)

        verify(mockPixelSender).enqueuePixel("test", params, emptyMap())
    }

    private fun givenEnqueuePixelSucceeds() {
        whenever(mockPixelSender.enqueuePixel(any(), any(), any())).thenReturn(Completable.complete())
    }

    private fun givenEnqueuePixelFails() {
        whenever(mockPixelSender.enqueuePixel(any(), any(), any())).thenReturn(Completable.error(TimeoutException()))
    }

    private fun givenSendPixelSucceeds() {
        whenever(mockPixelSender.sendPixel(any(), any(), any(), any())).thenReturn(Single.just(PIXEL_SENT))
    }

    private fun givenSendPixelFails() {
        whenever(mockPixelSender.sendPixel(any(), any(), any(), any())).thenReturn(Single.error(TimeoutException()))
    }

    enum class TestPixels(override val pixelName: String, val enqueue: Boolean = false) : Pixel.PixelName {
        TEST("test"),
    }
}
