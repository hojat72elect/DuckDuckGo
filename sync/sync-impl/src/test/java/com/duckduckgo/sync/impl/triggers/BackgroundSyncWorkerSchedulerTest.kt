

package com.duckduckgo.sync.impl.triggers

import androidx.lifecycle.LifecycleOwner
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.WorkManager
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.sync.api.DeviceSyncState
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BackgroundSyncWorkerSchedulerTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val deviceSyncState: DeviceSyncState = mock()
    private val mockWorkManager: WorkManager = mock()
    private val mockOwner: LifecycleOwner = mock()

    lateinit var syncBackgroundWorkerScheduler: SyncBackgroundWorkerScheduler

    @Before
    fun before() {
        syncBackgroundWorkerScheduler =
            SyncBackgroundWorkerScheduler(mockWorkManager, deviceSyncState, TestScope(), coroutineRule.testDispatcherProvider)
    }

    @Test
    fun whenOnCreateAndSyncEnabledThenWorkerEnqueued() {
        whenever(deviceSyncState.isUserSignedInOnDevice()).thenReturn(true)
        syncBackgroundWorkerScheduler.onCreate(mockOwner)

        verify(mockWorkManager).enqueueUniquePeriodicWork(any(), eq(KEEP), any())
    }

    @Test
    fun whenOnCreateAndSyncDisabledThenWorkerIsNotEnqueued() {
        whenever(deviceSyncState.isUserSignedInOnDevice()).thenReturn(false)
        syncBackgroundWorkerScheduler.onCreate(mockOwner)

        verify(mockWorkManager).cancelAllWorkByTag(any())
    }
}
