

package com.duckduckgo.autofill.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeCredentialsSyncStore : CredentialsSyncStore {
    override var syncPausedReason: String = ""
    override var serverModifiedSince: String = "0"
    override var startTimeStamp: String = "0"
    override var clientModifiedSince: String = "0"
    override var isSyncPaused: Boolean = false
    override fun isSyncPausedFlow(): Flow<Boolean> = emptyFlow()
    override var invalidEntitiesIds: List<String> = emptyList()
}
