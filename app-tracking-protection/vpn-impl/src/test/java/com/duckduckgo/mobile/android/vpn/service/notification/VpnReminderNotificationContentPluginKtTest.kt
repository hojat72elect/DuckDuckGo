

package com.duckduckgo.mobile.android.vpn.service.notification

import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.mobile.android.vpn.service.FakeVpnReminderNotificationContentPlugin
import com.duckduckgo.mobile.android.vpn.service.VpnReminderNotificationContentPlugin
import com.duckduckgo.mobile.android.vpn.service.VpnReminderNotificationContentPlugin.NotificationPriority.HIGH
import com.duckduckgo.mobile.android.vpn.service.VpnReminderNotificationContentPlugin.NotificationPriority.LOW
import com.duckduckgo.mobile.android.vpn.service.VpnReminderNotificationContentPlugin.NotificationPriority.NORMAL
import com.duckduckgo.mobile.android.vpn.service.VpnReminderNotificationContentPlugin.Type.DISABLED
import com.duckduckgo.mobile.android.vpn.service.VpnReminderNotificationContentPlugin.Type.REVOKED
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VpnReminderNotificationContentPluginKtTest {
    @Test
    fun whenEmptyPluginPointThenReturnNull() {
        assertNull(createPluginPoint(emptyList()).getHighestPriorityPluginForType(REVOKED))
    }

    @Test
    fun whenPluginForTypeAvailableThenReturnPLugin() {
        val plugin = createPluginPoint(
            listOf(
                FakeVpnReminderNotificationContentPlugin(type = DISABLED, NORMAL),
            ),
        ).getHighestPriorityPluginForType(DISABLED)

        assertEquals(DISABLED, plugin?.getType())
    }

    @Test
    fun whenNoPluginsForTypeThenReturnNull() {
        val plugin = createPluginPoint(
            listOf(
                FakeVpnReminderNotificationContentPlugin(type = REVOKED, NORMAL),
            ),
        ).getHighestPriorityPluginForType(DISABLED)

        assertNull(plugin)
    }

    @Test
    fun whenMultiplePluginsForTypeThenReturnHighestPriority() {
        val plugin = createPluginPoint(
            listOf(
                FakeVpnReminderNotificationContentPlugin(type = DISABLED, NORMAL),
                FakeVpnReminderNotificationContentPlugin(type = DISABLED, LOW),
                FakeVpnReminderNotificationContentPlugin(type = DISABLED, HIGH),
            ),
        ).getHighestPriorityPluginForType(DISABLED)

        assertEquals(HIGH, plugin?.getPriority())
    }

    private fun createPluginPoint(plugins: List<VpnReminderNotificationContentPlugin>): PluginPoint<VpnReminderNotificationContentPlugin> {
        return object : PluginPoint<VpnReminderNotificationContentPlugin> {
            override fun getPlugins(): Collection<VpnReminderNotificationContentPlugin> {
                return plugins
            }
        }
    }
}
