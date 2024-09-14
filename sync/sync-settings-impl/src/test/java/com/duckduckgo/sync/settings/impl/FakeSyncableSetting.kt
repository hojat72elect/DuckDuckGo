

package com.duckduckgo.sync.settings.impl

import com.duckduckgo.sync.settings.api.SyncableSetting

open class FakeSyncableSetting : SyncableSetting {
    private lateinit var listener: () -> Unit

    override var key: String = "fake_setting"

    private var value: String? = "fake_value"

    override fun getValue(): String? = value

    override fun save(value: String?): Boolean {
        this.value = value
        return true
    }

    override fun deduplicate(value: String?): Boolean {
        this.value = value
        return true
    }

    override fun registerToRemoteChanges(onDataChanged: () -> Unit) {
        this.listener = onDataChanged
    }

    override fun onSettingChanged() {
        this.listener.invoke()
    }

    override fun onSyncDisabled() {
        // no-op
    }
}
