

package com.duckduckgo.sync.impl

import app.cash.turbine.test
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.sync.api.SyncState
import com.duckduckgo.sync.api.SyncState.OFF
import com.duckduckgo.sync.impl.engine.SyncStateRepository
import com.duckduckgo.sync.store.SyncStore
import com.duckduckgo.sync.store.model.SyncAttempt
import com.duckduckgo.sync.store.model.SyncAttemptState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RealSyncStateMonitorTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val syncStore: SyncStore = mock()
    private val syncStoreRepository: SyncStateRepository = mock()

    private val signedInFlow = MutableStateFlow(false)
    private val stateFlow = MutableStateFlow(SyncAttempt(state = SyncAttemptState.SUCCESS))

    private lateinit var testee: RealSyncStateMonitor

    @Before
    fun setUp() {
        testee = RealSyncStateMonitor(
            syncStore,
            syncStoreRepository,
            coroutineRule.testDispatcherProvider,
        )

        whenever(syncStore.isSignedInFlow()).thenReturn(signedInFlow.asStateFlow())
        whenever(syncStoreRepository.state()).thenReturn(stateFlow.asStateFlow())
    }

    @Test
    fun whenSyncDisabledThenStateIsOff() = runTest {
        testee.syncState().test {
            val state = awaitItem()
            Assert.assertTrue(state == OFF)
        }
    }

    @Test
    fun whenSyncEnabledThenStateIsREADY() = runTest {
        testee.syncState().test {
            signedInFlow.emit(true)
            val state = expectMostRecentItem()
            Assert.assertTrue(state == SyncState.READY)
        }
    }

    @Test
    fun whenSyncEnabledAndAttemptSuccessThenStateIsReady() = runTest {
        testee.syncState().test {
            signedInFlow.emit(true)
            stateFlow.emit(SyncAttempt(state = SyncAttemptState.SUCCESS))
            val state = expectMostRecentItem()
            Assert.assertTrue(state == SyncState.READY)
        }
    }

    @Test
    fun whenSyncEnabledAndAttemptFailThenStateIsFailed() = runTest {
        testee.syncState().test {
            signedInFlow.emit(true)
            stateFlow.emit(SyncAttempt(state = SyncAttemptState.FAIL))
            val state = expectMostRecentItem()
            Assert.assertTrue(state == SyncState.FAILED)
        }
    }

    @Test
    fun whenSyncEnabledAndAttemptInProgressThenStateIsInProgress() = runTest {
        testee.syncState().test {
            signedInFlow.emit(true)
            stateFlow.emit(SyncAttempt(state = SyncAttemptState.IN_PROGRESS))
            val state = expectMostRecentItem()
            Assert.assertTrue(state == SyncState.IN_PROGRESS)
        }
    }
}
