

package com.duckduckgo.mobile.android.vpn.integration

import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.mobile.android.vpn.AppTpVpnFeature
import com.duckduckgo.mobile.android.vpn.FakeVpnFeaturesRegistry
import com.duckduckgo.mobile.android.vpn.VpnFeature
import com.duckduckgo.mobile.android.vpn.VpnFeaturesRegistry
import com.duckduckgo.mobile.android.vpn.network.FakeVpnNetworkStack
import com.duckduckgo.mobile.android.vpn.network.VpnNetworkStack
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class VpnNetworkStackProviderTest {

    private val vpnNetworkStacks: PluginPoint<VpnNetworkStack> = mock()
    private lateinit var vpnFeaturesRegistry: VpnFeaturesRegistry

    private lateinit var vpnNetworkStackProvider: VpnNetworkStackProvider
    private val fakeVpnNetworkStack = FakeVpnNetworkStack(AppTpVpnFeature.APPTP_VPN.featureName)

    @Before
    fun setup() {
        whenever(vpnNetworkStacks.getPlugins()).thenReturn(listOf(fakeVpnNetworkStack))
        vpnFeaturesRegistry = FakeVpnFeaturesRegistry()

        vpnNetworkStackProvider = VpnNetworkStackProviderImpl(vpnNetworkStacks, vpnFeaturesRegistry)
    }

    @Test
    fun whenProvideNetworkStackAndNoFeaturesRegisteredThenThrowsException() = runTest {
        assertEquals(VpnNetworkStack.EmptyVpnNetworkStack, vpnNetworkStackProvider.provideNetworkStack())
    }

    @Test
    fun whenProviderNetworkStackAndAppTpRegisteredThenReturnNetworkStack() = runTest {
        vpnFeaturesRegistry.registerFeature(AppTpVpnFeature.APPTP_VPN)
        val networkStack = vpnNetworkStackProvider.provideNetworkStack()

        assertEquals(fakeVpnNetworkStack, networkStack)
    }

    @Test
    fun whenProviderNetworkStackAndUnknownFeatureRegisteredThenThrowsException() = runTest {
        vpnFeaturesRegistry.registerFeature(VpnFeature { "unknown" })
        vpnNetworkStackProvider.provideNetworkStack()
        assertEquals(VpnNetworkStack.EmptyVpnNetworkStack, vpnNetworkStackProvider.provideNetworkStack())
    }

    @Test
    fun whenProviderNetworkStackAndFeaturesContainAppTpThenReturnNetworkStack() = runTest {
        vpnFeaturesRegistry.registerFeature(VpnFeature { "unknown" })
        vpnFeaturesRegistry.registerFeature(AppTpVpnFeature.APPTP_VPN)

        val networkStack = vpnNetworkStackProvider.provideNetworkStack()

        assertEquals(fakeVpnNetworkStack, networkStack)
    }
}
