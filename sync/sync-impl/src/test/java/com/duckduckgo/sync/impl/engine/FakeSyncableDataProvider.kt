

package com.duckduckgo.sync.impl.engine

import com.duckduckgo.sync.api.engine.SyncChangesRequest
import com.duckduckgo.sync.api.engine.SyncableDataProvider
import com.duckduckgo.sync.api.engine.SyncableType
import com.duckduckgo.sync.api.engine.SyncableType.BOOKMARKS

class FakeSyncableDataProvider(
    private val syncableType: SyncableType = BOOKMARKS,
    private val fakeChanges: SyncChangesRequest,
) : SyncableDataProvider {
    override fun getType(): SyncableType = syncableType

    override fun getChanges(): SyncChangesRequest {
        return fakeChanges
    }
}
