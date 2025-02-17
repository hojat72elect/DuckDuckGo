

package com.duckduckgo.downloads.store

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.downloads.store.DownloadStatus.FINISHED
import com.duckduckgo.downloads.store.DownloadStatus.STARTED
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DownloadsDaoTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private lateinit var db: com.duckduckgo.downloads.store.DownloadsDatabase
    private lateinit var dao: com.duckduckgo.downloads.store.DownloadsDao

    @Before
    fun before() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, com.duckduckgo.downloads.store.DownloadsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.downloadsDao()
    }

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenDownloadItemAddedThenItIsInList() = runTest {
        val downloadItem = oneItem()

        dao.insert(downloadItem)

        val list = dao.getDownloads()
        assertEquals(listOf(downloadItem), list)
    }

    @Test
    fun whenDownloadItemsAddedThenTheyAreInTheList() = runTest {
        val downloadItems = listOf(
            oneItem(),
            otherItem(),
        )

        dao.insertAll(downloadItems)

        val list = dao.getDownloads()
        assertEquals(downloadItems, list)
    }

    @Test
    fun whenDownloadItemUpdatedByIdThenItIsInListWithNewValue() = runTest {
        val downloadItem = oneItem().copy(downloadStatus = STARTED, contentLength = 0L)
        val updatedStatus = FINISHED
        val updatedContentLength = 11111L
        dao.insert(downloadItem)

        dao.update(
            downloadId = downloadItem.downloadId,
            downloadStatus = updatedStatus,
            contentLength = updatedContentLength,
        )

        val list = dao.getDownloads()
        val actualItem = list.first()
        assertEquals(updatedStatus, actualItem.downloadStatus)
        assertEquals(updatedContentLength, actualItem.contentLength)
    }

    @Test
    fun whenDownloadItemUpdatedByFileNameThenItIsInListWithNewValue() = runTest {
        val downloadItem = oneItem().copy(downloadStatus = STARTED, contentLength = 0L, downloadId = 0L)
        val updatedStatus = FINISHED
        val updatedContentLength = 11111L
        dao.insert(downloadItem)

        dao.update(
            fileName = downloadItem.fileName,
            downloadStatus = updatedStatus,
            contentLength = updatedContentLength,
        )

        val list = dao.getDownloads()
        val actualItem = list.first()
        assertEquals(updatedStatus, actualItem.downloadStatus)
        assertEquals(updatedContentLength, actualItem.contentLength)
    }

    @Test
    fun whenDownloadItemDeletedThenItIsNoLongerInTheList() = runTest {
        val downloadItem = oneItem()
        dao.insert(downloadItem)

        dao.delete(downloadItem.downloadId)

        val list = dao.getDownloads()
        assertTrue(list.isEmpty())
    }

    @Test
    fun whenDownloadItemsDeletedThenTheyAreNoLongerInTheList() = runTest {
        val downloadItems = listOf(
            oneItem(),
            otherItem(),
        )
        dao.insertAll(downloadItems)

        dao.delete()

        val list = dao.getDownloads()
        assertTrue(list.isEmpty())
    }

    @Test
    fun whenDownloadItemsRetrievedThenTheCorrectItemIsReturned() = runTest {
        val itemToRetrieve = oneItem()
        val downloadItems = listOf(
            otherItem(),
            itemToRetrieve,
        )
        dao.insertAll(downloadItems)

        val actualItem = dao.getDownloadItem(itemToRetrieve.downloadId)

        assertEquals(itemToRetrieve, actualItem)
    }

    private fun oneItem() =
        com.duckduckgo.downloads.store.DownloadEntity(
            id = 1L,
            downloadId = 10L,
            downloadStatus = FINISHED,
            fileName = "file.jpg",
            contentLength = 100L,
            createdAt = "2022-02-21T10:56:22",
            filePath = "/",
        )

    private fun otherItem() =
        com.duckduckgo.downloads.store.DownloadEntity(
            id = 2L,
            downloadId = 20L,
            downloadStatus = FINISHED,
            fileName = "other-file.jpg",
            contentLength = 120L,
            createdAt = "2022-02-21T10:56:22",
            filePath = "/",
        )
}
