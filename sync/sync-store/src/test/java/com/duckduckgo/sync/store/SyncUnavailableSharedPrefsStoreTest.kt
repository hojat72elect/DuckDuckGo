package com.duckduckgo.sync.store

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncUnavailableSharedPrefsStoreTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val store = SyncUnavailableSharedPrefsStore(TestSharedPrefsProvider(context))

    @Test
    fun whenIsSyncUnavailableIsSetThenItIsStored() {
        store.isSyncUnavailable = true
        assertTrue(store.isSyncUnavailable)
    }

    @Test
    fun whenClearErrorIsCalledThenErrorIsClearedExceptNotifiedAt() {
        val timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        store.isSyncUnavailable = true
        store.syncErrorCount = 100
        store.syncUnavailableSince = timestamp
        store.userNotifiedAt = timestamp

        store.clearError()

        assertFalse(store.isSyncUnavailable)
        assertEquals(0, store.syncErrorCount)
        assertEquals("", store.syncUnavailableSince)
        assertEquals(timestamp, store.userNotifiedAt)
    }

    @Test
    fun whenClearAllThenStoreEmpty() {
        val timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        store.isSyncUnavailable = true
        store.syncErrorCount = 100
        store.syncUnavailableSince = timestamp
        store.userNotifiedAt = timestamp

        store.clearAll()

        assertFalse(store.isSyncUnavailable)
        assertEquals(0, store.syncErrorCount)
        assertEquals("", store.syncUnavailableSince)
        assertEquals("", store.userNotifiedAt)
    }
}
