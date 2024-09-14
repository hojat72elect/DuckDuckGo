

package com.duckduckgo.sync.settings.impl

class FakeSettingsSyncStore : SettingsSyncStore {
    override var serverModifiedSince: String = "0"

    override var clientModifiedSince: String = "0"

    override var startTimeStamp: String = "0"
}
