

package com.duckduckgo.mobile.android.vpn

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.test.api.InMemorySharedPreferences
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.data.store.api.SharedPreferencesProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class VpnFeaturesRegistryImplTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    private val sharedPreferencesProvider: com.duckduckgo.data.store.api.SharedPreferencesProvider = mock()
    private lateinit var vpnServiceWrapper: TestVpnServiceWrapper

    private lateinit var vpnFeaturesRegistry: VpnFeaturesRegistry

    @Before
    fun setup() {
        val prefs = InMemorySharedPreferences()
        vpnServiceWrapper = TestVpnServiceWrapper(coroutineTestRule.testDispatcherProvider)

        whenever(
            sharedPreferencesProvider.getSharedPreferences(eq("com.duckduckgo.mobile.android.vpn.feature.registry.v1"), eq(true), eq(false)),
        ).thenReturn(prefs)

        vpnFeaturesRegistry = VpnFeaturesRegistryImpl(
            vpnServiceWrapper,
            sharedPreferencesProvider,
            coroutineTestRule.testDispatcherProvider,
        )
    }

    @Test
    fun whenRegisterFeatureThenRestartVpnService() = runTest {
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.FOO)

        assertEquals(0, vpnServiceWrapper.restartCount)
        assertTrue(vpnServiceWrapper.isServiceRunning())
    }

    @Test
    fun whenFeaturesAreRegisteredThenVpnIsStarted() = runTest {
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.FOO)
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.BAR)

        assertTrue(vpnFeaturesRegistry.isFeatureRunning(TestVpnFeatures.FOO))
        assertTrue(vpnFeaturesRegistry.isFeatureRunning(TestVpnFeatures.BAR))
        assertTrue(vpnServiceWrapper.isServiceRunning())
    }

    @Test
    fun whenIsFeatureRegisteredAndFeaturesAreRegisteredAndVpnIsRunningThenReturnTrue() = runTest {
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.FOO)
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.BAR)
        vpnServiceWrapper.startService()

        assertTrue(vpnFeaturesRegistry.isFeatureRunning(TestVpnFeatures.FOO))
        assertTrue(vpnFeaturesRegistry.isFeatureRunning(TestVpnFeatures.BAR))
    }

    @Test
    fun whenIsAnyFeatureRegisteredAndFeaturesAreNotRegisteredThenReturnFalse() = runTest {
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.FOO)
    }

    @Test
    fun whenIsAnyFeatureRegisteredAndFeaturesAreRegisteredThenReturnTrue() = runTest {
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.FOO)

        assertTrue(vpnFeaturesRegistry.isAnyFeatureRunning())
        assertTrue(vpnServiceWrapper.isServiceRunning())
    }

    @Test
    fun whenIsAnyFeatureRegisteredAndFeaturesAreRegisteredAndVpnDisabledThenReturnFalse() = runTest {
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.FOO)
        vpnServiceWrapper.stopService()

        assertFalse(vpnFeaturesRegistry.isAnyFeatureRunning())
        assertFalse(vpnServiceWrapper.isServiceRunning())
    }

    @Test
    fun whenUnregisterFeatureThenFeatureIsUnregistered() = runTest {
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.FOO)
        vpnFeaturesRegistry.unregisterFeature(TestVpnFeatures.FOO)

        assertFalse(vpnFeaturesRegistry.isFeatureRunning(TestVpnFeatures.FOO))
    }

    @Test
    fun whenUnregisterLastFeatureThenVpnIsNotRunning() = runTest {
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.FOO)
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.BAR)
        vpnFeaturesRegistry.unregisterFeature(TestVpnFeatures.FOO)
        vpnFeaturesRegistry.unregisterFeature(TestVpnFeatures.BAR)

        assertFalse(vpnServiceWrapper.isServiceRunning())
    }

    @Test
    fun whenUnregisterFeatureIfFeaturesStillRegisteredThenRestartVpnService() = runTest {
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.FOO) // no restart
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.BAR) // restart
        vpnFeaturesRegistry.unregisterFeature(TestVpnFeatures.FOO) // restart

        assertEquals(2, vpnServiceWrapper.restartCount)
    }

    @Test
    fun whenRefreshUnregisteredFeatureThenDoNotRestartVpn() = runTest {
        vpnFeaturesRegistry.refreshFeature(TestVpnFeatures.FOO)

        assertEquals(0, vpnServiceWrapper.restartCount)
    }

    @Test
    fun whenRefreshRegisteredFeatureThenRestartVpn() = runTest {
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.FOO)
        vpnFeaturesRegistry.refreshFeature(TestVpnFeatures.FOO)

        assertTrue(vpnServiceWrapper.isServiceRunning())
        assertEquals(1, vpnServiceWrapper.restartCount)
    }

    @Test
    fun whenRegisterFeatureThenStartVpn() = runTest {
        vpnFeaturesRegistry.registerFeature(TestVpnFeatures.FOO)

        assertTrue(vpnServiceWrapper.isServiceRunning())
        assertEquals(0, vpnServiceWrapper.restartCount)
    }

    private enum class TestVpnFeatures(override val featureName: String) : VpnFeature {
        FOO("FOO"),
        BAR("BAR"),
    }

    private class TestVpnServiceWrapper constructor(
        dispatcher: DispatcherProvider,
    ) : VpnServiceWrapper(InstrumentationRegistry.getInstrumentation().context, dispatcher) {
        private var isRunning = false
        var restartCount = 0

        override fun restartVpnService(forceRestart: Boolean) {
            if (isServiceRunning()) {
                restartCount++
            } else if (forceRestart) {
                startService()
            }
        }

        override fun stopService() {
            isRunning = false
        }

        override fun startService() {
            isRunning = true
        }

        override fun isServiceRunning(): Boolean {
            return isRunning
        }
    }
}
