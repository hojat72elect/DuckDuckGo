package com.duckduckgo.app.pixels.campaign.params

import com.duckduckgo.common.utils.plugins.PluginPoint
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RealAdditionalPixelParamsGeneratorTest {
    private class MockPluginPoint(
        val plugins: List<AdditionalPixelParamPlugin>,
    ) :
        PluginPoint<AdditionalPixelParamPlugin> {
        override fun getPlugins(): Collection<AdditionalPixelParamPlugin> = plugins
    }

    @Test
    fun whenThereAreNoPluginsThenGenerateAdditionalParamsReturnEmpty() = runTest {
        val plugins = MockPluginPoint(emptyList())
        val generator = RealAdditionalPixelParamsGenerator(plugins)

        assertTrue(generator.generateAdditionalParams().isEmpty())
    }

    @Test
    fun whenPluginsAreAvailableThenGeneratedParamsShouldBeCorrectSize() = runTest {
        val plugins = MockPluginPoint(
            listOf(
                object : AdditionalPixelParamPlugin {
                    override suspend fun params(): Pair<String, String> = "testKey1" to "testValue1"
                },
                object : AdditionalPixelParamPlugin {
                    override suspend fun params(): Pair<String, String> = "testKey2" to "testValue2"
                },
                object : AdditionalPixelParamPlugin {
                    override suspend fun params(): Pair<String, String> = "testKey3" to "testValue3"
                },
                object : AdditionalPixelParamPlugin {
                    override suspend fun params(): Pair<String, String> = "testKey4" to "testValue4"
                },
                object : AdditionalPixelParamPlugin {
                    override suspend fun params(): Pair<String, String> = "testKey5" to "testValue5"
                },
                object : AdditionalPixelParamPlugin {
                    override suspend fun params(): Pair<String, String> = "testKey6" to "testValue6"
                },
                object : AdditionalPixelParamPlugin {
                    override suspend fun params(): Pair<String, String> = "testKey7" to "testValue7"
                },
                object : AdditionalPixelParamPlugin {
                    override suspend fun params(): Pair<String, String> = "testKey8" to "testValue8"
                },
                object : AdditionalPixelParamPlugin {
                    override suspend fun params(): Pair<String, String> = "testKey9" to "testValue9"
                },
            ),
        )
        val generator = RealAdditionalPixelParamsGenerator(plugins)
        assertEquals(6, generator.generateAdditionalParams().size)
    }
}
