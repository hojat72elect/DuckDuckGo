package com.duckduckgo.sync.impl.error

import com.duckduckgo.sync.impl.pixels.SyncPixels
import com.duckduckgo.sync.store.model.SyncOperationErrorType
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class SyncOperationErrorRecorderTest {

    private val syncPixels: SyncPixels = mock()
    private val repository: SyncOperationErrorRepository = mock()

    private val recorder = RealSyncOperationErrorRecorder(syncPixels, repository)

    @Test
    fun wheneverEncryptErrorReportedThenRepositoryAddsError() {
        val error = SyncOperationErrorType.DATA_ENCRYPT

        recorder.record(error)

        verify(repository).addError(error)
    }

    @Test
    fun wheneverDecryptErrorReportedThenRepositoryAddsError() {
        val error = SyncOperationErrorType.DATA_DECRYPT

        recorder.record(error)

        verify(repository).addError(error)
    }
}
