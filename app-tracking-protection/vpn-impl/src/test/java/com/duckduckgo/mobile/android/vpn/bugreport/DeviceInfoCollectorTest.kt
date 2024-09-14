

package com.duckduckgo.mobile.android.vpn.bugreport

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.appbuildconfig.api.BuildFlavor
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class DeviceInfoCollectorTest {

    @Mock private lateinit var appBuildConfig: AppBuildConfig

    private lateinit var deviceInfoCollector: DeviceInfoCollector

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        whenever(appBuildConfig.flavor).thenReturn(BuildFlavor.PLAY)
        whenever(appBuildConfig.sdkInt).thenReturn(30)

        deviceInfoCollector = DeviceInfoCollector(appBuildConfig, { false })
    }

    @Test
    fun whenCollectVpnRelatedStateThenReturnDeviceInfo() = runTest {
        val state = deviceInfoCollector.collectVpnRelatedState()

        assertEquals("deviceInfo", deviceInfoCollector.collectorName)

        assertEquals(3, state.length())
        assertEquals("PLAY", state.get("buildFlavor"))
        assertEquals(30, state.get("os"))
        assertEquals("true", state.get("batteryOptimizations"))
    }

    @Test
    fun whenIgnoringBatteryOptimizationsThenReportBatteryOptimizationsTrue() = runTest {
        val state = DeviceInfoCollector(appBuildConfig, { true }).collectVpnRelatedState()

        assertEquals("deviceInfo", deviceInfoCollector.collectorName)

        assertEquals(3, state.length())
        assertEquals("PLAY", state.get("buildFlavor"))
        assertEquals(30, state.get("os"))
        assertEquals("false", state.get("batteryOptimizations"))
    }
}
