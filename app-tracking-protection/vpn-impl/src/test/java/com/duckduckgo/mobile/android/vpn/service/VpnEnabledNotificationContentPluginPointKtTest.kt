

package com.duckduckgo.mobile.android.vpn.service

import com.duckduckgo.common.utils.plugins.PluginPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VpnEnabledNotificationContentPluginPointKtTest {
    @Test
    fun whenEmptyPluginPointThenReturnNull() {
        assertNull(createPluginPoint(emptyList()).getHighestPriorityPlugin())
    }

    @Test
    fun whenInactivePluginsThenReturnNull() {
        assertNull(
            createPluginPoint(
                listOf(
                    FakeVpnEnabledNotificationContentPlugin(isActive = false),
                    FakeVpnEnabledNotificationContentPlugin(isActive = false),
                ),
            ).getHighestPriorityPlugin(),
        )
    }

    @Test
    fun whenActivePluginsThenReturnThemInPriorityOrder() {
        val plugin = createPluginPoint(
            listOf(
                FakeVpnEnabledNotificationContentPlugin(isActive = true, VpnEnabledNotificationContentPlugin.VpnEnabledNotificationPriority.NORMAL),
                FakeVpnEnabledNotificationContentPlugin(isActive = true, VpnEnabledNotificationContentPlugin.VpnEnabledNotificationPriority.LOW),
                FakeVpnEnabledNotificationContentPlugin(isActive = true, VpnEnabledNotificationContentPlugin.VpnEnabledNotificationPriority.HIGH),
            ),
        ).getHighestPriorityPlugin()

        assertEquals(VpnEnabledNotificationContentPlugin.VpnEnabledNotificationPriority.HIGH, plugin?.getPriority())
    }

    private fun createPluginPoint(plugins: List<VpnEnabledNotificationContentPlugin>): PluginPoint<VpnEnabledNotificationContentPlugin> {
        return object : PluginPoint<VpnEnabledNotificationContentPlugin> {
            override fun getPlugins(): Collection<VpnEnabledNotificationContentPlugin> {
                return plugins
            }
        }
    }
}
