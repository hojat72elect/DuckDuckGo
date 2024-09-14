

package com.duckduckgo.sync.settings.impl

import androidx.arch.core.executor.testing.*
import androidx.room.*
import androidx.test.ext.junit.runners.*
import androidx.test.platform.app.*
import com.duckduckgo.app.*
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.test.FileUtilities
import com.duckduckgo.sync.api.engine.*
import com.duckduckgo.sync.api.engine.SyncMergeResult.Success
import com.duckduckgo.sync.api.engine.SyncableDataPersister.SyncConflictResolution.DEDUPLICATION
import com.duckduckgo.sync.api.engine.SyncableDataPersister.SyncConflictResolution.TIMESTAMP
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.*
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class SettingsSyncDataPersisterTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    val db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, SettingsDatabase::class.java)
        .allowMainThreadQueries()
        .build()

    val metadataDao = db.settingsSyncDao()
    val duckAddressSetting = spy(FakeSyncableSetting())
    val settingSyncStore = FakeSettingsSyncStore()
    val syncableSettingsPP = SyncableSettingsPluginPoint(mutableListOf(duckAddressSetting))

    private val testee = SettingsSyncDataPersister(
        syncableSettings = syncableSettingsPP,
        settingsSyncMetadataDao = metadataDao,
        syncSettingsSyncStore = settingSyncStore,
        syncCrypto = FakeCrypto(),
        dispatchers = coroutineRule.testDispatcherProvider,
    )

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenPersistChangesDeduplicationWithdValueThenCallDeduplicateWithValue() {
        val result = testee.onSuccess(
            changes = SyncChangesResponse(
                type = SyncableType.SETTINGS,
                jsonString = responseWithValuesObject,
            ),
            conflictResolution = DEDUPLICATION,
        )

        assertTrue(result is Success)
        verify(duckAddressSetting).deduplicate("fake_value")
    }

    @Test
    fun whenPersistChangesDeduplicationWithDeletedValueThenCallDeduplicateWithNull() {
        val result = testee.onSuccess(
            changes = SyncChangesResponse(
                type = SyncableType.SETTINGS,
                jsonString = responseWithDeletedObject,
            ),
            conflictResolution = DEDUPLICATION,
        )

        assertTrue(result is Success)
        verify(duckAddressSetting).deduplicate(null)
    }

    @Test
    fun whenPersistChangesTimestampAndNoRecentChangeThenCallMergeWithValue() {
        settingSyncStore.startTimeStamp = "2023-08-31T10:06:16.022Z"
        val result = testee.onSuccess(
            changes = SyncChangesResponse(
                type = SyncableType.SETTINGS,
                jsonString = responseWithValuesObject,
            ),
            conflictResolution = TIMESTAMP,
        )

        assertTrue(result is Success)
        verify(duckAddressSetting).save("fake_value")
    }

    @Test
    fun whenPersistChangesTimestampWithDeletedValueThenCallSaveWithNull() {
        val result = testee.onSuccess(
            changes = SyncChangesResponse(
                type = SyncableType.SETTINGS,
                jsonString = responseWithDeletedObject,
            ),
            conflictResolution = TIMESTAMP,
        )

        assertTrue(result is Success)
        verify(duckAddressSetting).save(null)
    }

    @Test
    fun whenPersistChangesTimestampButRecentlyModifiedThenSkip() {
        settingSyncStore.startTimeStamp = "2023-08-31T10:06:16.022Z"
        metadataDao.addOrUpdate(
            SettingsSyncMetadataEntity(
                key = "fake_setting",
                modified_at = "2023-08-31T10:06:17.022Z",
                deleted_at = null,
            ),
        )

        val result = testee.onSuccess(
            changes = SyncChangesResponse(
                type = SyncableType.SETTINGS,
                jsonString = responseWithValuesObject,
            ),
            conflictResolution = TIMESTAMP,
        )

        assertTrue(result is Success)
        verify(duckAddressSetting, times(0)).save("fake_value")
    }

    @Test
    fun whenPersistChangesSucceedsThenUpdateServerAndClientTimestamps() {
        settingSyncStore.startTimeStamp = "2023-08-31T10:06:16.022Z"

        val result = testee.onSuccess(
            changes = SyncChangesResponse(
                type = SyncableType.SETTINGS,
                jsonString = responseWithValuesObject,
            ),
            conflictResolution = DEDUPLICATION,
        )

        assertTrue(result is Success)
        assertEquals("2023-08-31T10:06:16.022Z", settingSyncStore.serverModifiedSince)
        assertEquals("2023-08-31T10:06:16.022Z", settingSyncStore.clientModifiedSince)
    }

    companion object {
        val responseWithDeletedObject = FileUtilities.loadText(javaClass.classLoader!!, "json/settings_with_deleted_object_response.json")
        val responseWithValuesObject = FileUtilities.loadText(javaClass.classLoader!!, "json/settings_with_values_response.json")
    }
}
