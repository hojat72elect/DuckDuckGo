

package com.duckduckgo.feature.toggles.impl

import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.feature.toggles.api.FeatureTogglesPlugin
import org.junit.Assert.*
import org.junit.Test

class RealFeatureToggleImplTest {

    private val testee: RealFeatureToggleImpl =
        RealFeatureToggleImpl(FakeFeatureTogglePluginPoint())

    @Test
    fun whenFeatureNameCanBeHandledByPluginThenReturnTheCorrectValue() {
        val result = testee.isFeatureEnabled(TrueFeatureName().value, false)
        assertNotNull(result)
        assertTrue(result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun whenFeatureNameCannotBeHandledByAnyPluginThenThrowException() {
        testee.isFeatureEnabled(NullFeatureName().value, false)
    }

    class FakeTruePlugin : FeatureTogglesPlugin {
        override fun isEnabled(
            featureName: String,
            defaultValue: Boolean,
        ): Boolean? {
            return if (featureName == TrueFeatureName().value) {
                true
            } else {
                null
            }
        }
    }

    class FakeFeatureTogglePluginPoint : PluginPoint<FeatureTogglesPlugin> {
        override fun getPlugins(): Collection<FeatureTogglesPlugin> {
            return listOf(FakeTruePlugin())
        }
    }

    data class TrueFeatureName(val value: String = "true")
    data class NullFeatureName(val value: String = "null")
}
