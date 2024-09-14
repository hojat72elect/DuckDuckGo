

package com.duckduckgo.sync.impl.engine

import com.duckduckgo.sync.api.engine.SyncChangesResponse
import com.duckduckgo.sync.api.engine.SyncErrorResponse
import com.duckduckgo.sync.api.engine.SyncMergeResult
import com.duckduckgo.sync.api.engine.SyncableDataPersister
import com.duckduckgo.sync.api.engine.SyncableDataPersister.SyncConflictResolution

class FakeSyncableDataPersister(private val orphans: Boolean = false, private val timestampConflict: Boolean = false) : SyncableDataPersister {
    override fun onSyncEnabled() {
        // no-op
    }

    override fun onSuccess(
        changes: SyncChangesResponse,
        conflictResolution: SyncConflictResolution,
    ): SyncMergeResult {
        return SyncMergeResult.Success(orphans, timestampConflict)
    }

    override fun onError(error: SyncErrorResponse) {
        // no-op
    }

    override fun onSyncDisabled() {
        // no-op
    }
}
