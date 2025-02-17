

package com.duckduckgo.app.global.api

import com.duckduckgo.common.test.api.FakeChain
import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.common.utils.plugins.pixel.PixelParamRemovalPlugin
import com.duckduckgo.common.utils.plugins.pixel.PixelParamRemovalPlugin.PixelParameter
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PixelParamRemovalInterceptorTest {
    private lateinit var pixelRemovalInterceptor: PixelParamRemovalInterceptor

    @Before
    fun setup() {
        pixelRemovalInterceptor = PixelParamRemovalInterceptor(
            pixelsPlugin = object : PluginPoint<PixelParamRemovalPlugin> {
                override fun getPlugins(): Collection<PixelParamRemovalPlugin> {
                    return listOf(
                        object : PixelParamRemovalPlugin {
                            override fun names(): List<Pair<String, Set<PixelParameter>>> {
                                return testPixels
                            }
                        },
                    )
                }
            },
        )
    }

    @Test
    fun whenSendPixelRedactAppVersion() {
        testPixels.filter { it.second == PixelParameter.removeVersion() }.map { it.first }.forEach { pixelName ->
            val pixelUrl = String.format(PIXEL_TEMPLATE, pixelName)
            val interceptedUrl = pixelRemovalInterceptor.intercept(FakeChain(pixelUrl)).request.url
            assertNotNull(interceptedUrl.queryParameter("atb"))
            assertNull(interceptedUrl.queryParameter("appVersion"))
        }
    }

    @Test
    fun whenSendPixelRedactAtb() {
        testPixels.filter { it.second == PixelParameter.removeAtb() }.map { it.first }.forEach { pixelName ->
            val pixelUrl = String.format(PIXEL_TEMPLATE, pixelName)
            val interceptedUrl = pixelRemovalInterceptor.intercept(FakeChain(pixelUrl)).request.url
            assertNull(interceptedUrl.queryParameter("atb"))
            assertNotNull(interceptedUrl.queryParameter("appVersion"))
        }
    }

    @Test
    fun whenSendPixelRedactOSVersion() {
        testPixels.filter { it.second == PixelParameter.removeOSVersion() }.map { it.first }.forEach { pixelName ->
            val pixelUrl = String.format(PIXEL_TEMPLATE, pixelName)
            val interceptedUrl = pixelRemovalInterceptor.intercept(FakeChain(pixelUrl)).request.url
            assertNotNull(interceptedUrl.queryParameter("atb"))
            assertNotNull(interceptedUrl.queryParameter("appVersion"))
            assertNull(interceptedUrl.queryParameter("os_version"))
        }
    }

    @Test
    fun whenSendPixelRedactAtbAndAppAndOSVersion() {
        testPixels.filter { it.second.containsAll(PixelParameter.removeAll()) }
            .map { it.first }
            .forEach { pixelName ->
                val pixelUrl = String.format(PIXEL_TEMPLATE, pixelName)
                val interceptedUrl = pixelRemovalInterceptor.intercept(FakeChain(pixelUrl)).request.url
                assertNull(interceptedUrl.queryParameter("atb"))
                assertNull(interceptedUrl.queryParameter("appVersion"))
                assertNull(interceptedUrl.queryParameter("os_version"))
            }
    }

    companion object {
        private const val PIXEL_TEMPLATE = "https://improving.duckduckgo.com/t/%s_android_phone?atb=v255-7zu&appVersion=5.74.0&os_version=1.0&test=1"
        private val testPixels = listOf(
            "atb_and_version_and_os_redacted" to PixelParameter.removeAll(),
            "atb_redacted" to PixelParameter.removeAtb(),
            "version_redacted" to PixelParameter.removeVersion(),
            "os_version_redacted" to PixelParameter.removeOSVersion(),
        )
    }
}
