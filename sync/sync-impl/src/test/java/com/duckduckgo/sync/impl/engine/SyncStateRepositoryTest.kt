

package com.duckduckgo.sync.impl.engine

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.sync.store.SyncDatabase
import com.duckduckgo.sync.store.dao.SyncAttemptDao
import com.duckduckgo.sync.store.model.SyncAttempt
import com.duckduckgo.sync.store.model.SyncAttemptState
import com.duckduckgo.sync.store.model.SyncAttemptState.SUCCESS
import junit.framework.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncStateRepositoryTest {

    lateinit var repository: SyncStateRepository
    lateinit var syncAttemptDao: SyncAttemptDao

    private lateinit var db: SyncDatabase

    @Before
    fun before() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, SyncDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        syncAttemptDao = db.syncAttemptsDao()

        repository = AppSyncStateRepository(syncAttemptDao)
    }

    @Test
    fun whenSyncDaoIsEmptyCurrentReturnsNull() {
        val currentSync = repository.current()
        assertTrue(currentSync == null)
    }

    @Test
    fun whenSyncInProgressThenCurrentReturnsAttempt() {
        val sync = SyncAttempt(state = SyncAttemptState.IN_PROGRESS)
        repository.store(sync)

        val current = repository.current()
        assertEquals(sync.timestamp, current!!.timestamp)
    }

    @Test
    fun whenSyncStateIsUpdatedThenDaoIsUpdated() {
        val syncInProgress = SyncAttempt(state = SyncAttemptState.IN_PROGRESS)
        repository.store(syncInProgress)

        repository.updateSyncState(SUCCESS)
        val lastSync = repository.current()

        assertEquals(syncInProgress.timestamp, lastSync!!.timestamp)
        assertEquals(SUCCESS, lastSync.state)
    }
}
