

package com.duckduckgo.sync.impl.engine

import com.duckduckgo.sync.store.model.SyncAttempt
import com.duckduckgo.sync.store.model.SyncAttemptState.FAIL
import com.duckduckgo.sync.store.model.SyncAttemptState.IN_PROGRESS
import com.duckduckgo.sync.store.model.SyncAttemptState.SUCCESS
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SyncSchedulerTest {

    private val syncStateRepository: SyncStateRepository = mock()
    lateinit var syncScheduler: SyncScheduler

    @Before
    fun before() {
        syncScheduler = RealSyncScheduler(syncStateRepository)
    }

    @Test
    fun whenFirstSyncThenSyncCanBeExecuted() {
        whenever(syncStateRepository.current()).thenReturn(null)

        val syncOperation = syncScheduler.scheduleOperation()

        assertEquals(syncOperation, SyncOperation.EXECUTE)
    }

    @Test
    fun whenLastSyncFailedThenSyncIsExecuted() {
        val lastSyncTimestamp = timestamp(Instant.now().minus(5, ChronoUnit.MINUTES))
        val lastSync = SyncAttempt(timestamp = lastSyncTimestamp, state = FAIL)

        whenever(syncStateRepository.current()).thenReturn(lastSync)

        val syncOperation = syncScheduler.scheduleOperation()

        assertEquals(syncOperation, SyncOperation.EXECUTE)
    }

    @Test
    fun whenLastSyncInProgressThenSyncIsDiscarded() {
        val lastSyncTimestamp = timestamp(Instant.now().minus(5, ChronoUnit.MINUTES))
        val lastSync = SyncAttempt(timestamp = lastSyncTimestamp, state = IN_PROGRESS)

        whenever(syncStateRepository.current()).thenReturn(lastSync)

        val syncOperation = syncScheduler.scheduleOperation()

        assertEquals(syncOperation, SyncOperation.DISCARD)
    }

    @Test
    fun whenLastSyncWasBeforeDebouncePeriodThenSyncIsDiscarded() {
        val lastSyncTimestamp = timestamp(Instant.now().minus(5, ChronoUnit.MINUTES))
        val lastSync = SyncAttempt(timestamp = lastSyncTimestamp, state = SUCCESS)

        whenever(syncStateRepository.current()).thenReturn(lastSync)

        val syncOperation = syncScheduler.scheduleOperation()

        assertEquals(syncOperation, SyncOperation.DISCARD)
    }

    @Test
    fun whenLastSyncWasAfterDebouncePeriodThenSyncIsDiscarded() {
        val lastSyncTimestamp = timestamp(Instant.now().minus(30, ChronoUnit.MINUTES))
        val lastSync = SyncAttempt(timestamp = lastSyncTimestamp, state = SUCCESS)

        whenever(syncStateRepository.current()).thenReturn(lastSync)

        val syncOperation = syncScheduler.scheduleOperation()

        assertEquals(syncOperation, SyncOperation.EXECUTE)
    }

    private fun timestamp(instant: Instant): String {
        return instant.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
    }
}
