

package com.duckduckgo.sync.settings.impl

import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.sync.settings.api.SyncableSetting

class SyncableSettingsPluginPoint(
    val syncableSettings: MutableList<SyncableSetting>,
) : PluginPoint<SyncableSetting> {
    override fun getPlugins(): Collection<SyncableSetting> {
        return syncableSettings
    }
}
