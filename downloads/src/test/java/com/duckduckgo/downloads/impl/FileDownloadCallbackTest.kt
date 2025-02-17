package com.duckduckgo.downloads.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.downloads.R
import com.duckduckgo.downloads.api.DownloadCommand.ShowDownloadFailedMessage
import com.duckduckgo.downloads.api.DownloadCommand.ShowDownloadStartedMessage
import com.duckduckgo.downloads.api.DownloadCommand.ShowDownloadSuccessMessage
import com.duckduckgo.downloads.api.DownloadFailReason
import com.duckduckgo.downloads.api.DownloadsRepository
import com.duckduckgo.downloads.api.FileDownloadNotificationManager
import com.duckduckgo.downloads.api.model.DownloadItem
import com.duckduckgo.downloads.impl.pixels.DownloadsPixelName
import com.duckduckgo.downloads.store.DownloadStatus.FINISHED
import java.io.File
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class FileDownloadCallbackTest {

    @get:Rule
    @Suppress("unused")
    val coroutineRule = CoroutineTestRule()

    private val mockDownloadsRepository: DownloadsRepository = mock()

    private val mockPixel: Pixel = mock()

    private val mockFileDownloadNotificationManager: FileDownloadNotificationManager = mock()

    private val mockMediaScanner: MediaScanner = mock()

    private lateinit var callback: FileDownloadCallback

    @Before
    fun before() {
        callback = FileDownloadCallback(
            fileDownloadNotificationManager = mockFileDownloadNotificationManager,
            downloadsRepository = mockDownloadsRepository,
            pixel = mockPixel,
            dispatchers = coroutineRule.testDispatcherProvider,
            appCoroutineScope = TestScope(),
            mediaScanner = mockMediaScanner,
        )
    }

    @Test
    fun whenOnStartCalledThenPixelFiredAndItemInsertedAndDownloadStartedCommandSent() = runTest {
        val item = oneItem()

        callback.onStart(downloadItem = item)

        verify(mockPixel).fire(DownloadsPixelName.DOWNLOAD_REQUEST_STARTED)
        verify(mockDownloadsRepository).insert(downloadItem = item)
        verify(mockFileDownloadNotificationManager).showDownloadInProgressNotification(
            any(),
            eq(item.fileName),
            eq(0)
        )
        callback.commands().test {
            val actualItem = awaitItem()
            assertTrue(actualItem is ShowDownloadStartedMessage)
            assertEquals(R.string.downloadsDownloadStartedMessage, actualItem.messageId)
            assertEquals(item.fileName, (actualItem as ShowDownloadStartedMessage).fileName)
        }
    }

    @Test
    fun whenOnSuccessCalledForDownloadIdThenPixelFiredAndItemUpdatedAndDownloadSuccessCommandSent() =
        runTest {
            val item = oneItem()
            val updatedContentLength = 20L
            whenever(mockDownloadsRepository.getDownloadItem(item.downloadId)).thenReturn(
                item.copy(
                    contentLength = updatedContentLength
                )
            )

            val file: File = mock()
            callback.onSuccess(item.downloadId, updatedContentLength, file, "type")

            verify(mockPixel).fire(DownloadsPixelName.DOWNLOAD_REQUEST_SUCCEEDED)
            verify(mockMediaScanner).scan(file)
            verify(mockDownloadsRepository).update(
                downloadId = item.downloadId,
                downloadStatus = FINISHED,
                contentLength = updatedContentLength,
            )
            verify(mockFileDownloadNotificationManager).showDownloadFinishedNotification(
                item.downloadId,
                file,
                "type"
            )
            callback.commands().test {
                val actualItem = awaitItem()
                assertTrue(actualItem is ShowDownloadSuccessMessage)
                assertEquals(R.string.downloadsDownloadFinishedMessage, actualItem.messageId)
                assertEquals(item.fileName, (actualItem as ShowDownloadSuccessMessage).fileName)
                assertEquals(item.filePath, actualItem.filePath)
            }
        }

    @Test
    fun whenOnSuccessCalledForFileThenPixelFiredAndItemUpdatedAndDownloadSuccessCommandSent() =
        runTest {
            val item = oneItem()
            val mimeType = "image/jpeg"
            val file = File(item.fileName)

            callback.onSuccess(file = file, mimeType = mimeType)

            verify(mockPixel).fire(DownloadsPixelName.DOWNLOAD_REQUEST_SUCCEEDED)
            verify(mockMediaScanner).scan(file)
            verify(mockDownloadsRepository).update(
                fileName = item.fileName,
                downloadStatus = FINISHED,
                contentLength = file.length(),
            )
            verify(mockFileDownloadNotificationManager).showDownloadFinishedNotification(
                0,
                file,
                mimeType
            )
            callback.commands().test {
                val actualItem = awaitItem()
                assertTrue(actualItem is ShowDownloadSuccessMessage)
                assertEquals(R.string.downloadsDownloadFinishedMessage, actualItem.messageId)
                assertEquals(item.fileName, (actualItem as ShowDownloadSuccessMessage).fileName)
                assertEquals(item.filePath, actualItem.filePath)
                assertEquals(mimeType, actualItem.mimeType)
            }
        }

    @Test
    fun whenOnErrorCalledForDownloadIdAndConnectionRefusedThenPixelFiredAndItemDeleted() = runTest {
        val downloadId = 1L
        val failReason = DownloadFailReason.ConnectionRefused

        callback.onError(downloadId = downloadId, reason = failReason)

        verify(mockPixel).fire(DownloadsPixelName.DOWNLOAD_REQUEST_FAILED)
        verify(mockDownloadsRepository).delete(
            downloadIdList = listOf(downloadId),
        )
    }

    @Test
    fun whenOnCancelCalledForDownloadIdAndNoItemInDbThenPixelFired() = runTest {
        val downloadId = 1L

        whenever(mockDownloadsRepository.getDownloadItem(downloadId)).thenReturn(null)

        callback.onCancel(downloadId = downloadId)

        verify(mockPixel).fire(DownloadsPixelName.DOWNLOAD_REQUEST_CANCELLED)
        verify(mockDownloadsRepository, never()).delete(listOf(downloadId))
    }

    @Test
    fun whenOnCancelCalledForDownloadIdAndExistingItemInDbThenPixelFired() = runTest {
        val downloadId = 1L
        whenever(mockDownloadsRepository.getDownloadItem(downloadId)).thenReturn(oneItem())

        callback.onCancel(downloadId = downloadId)

        verify(mockPixel).fire(DownloadsPixelName.DOWNLOAD_REQUEST_CANCELLED)
        verify(mockDownloadsRepository).delete(listOf(downloadId))
    }

    @Test
    fun whenOnErrorCalledForUrlAndConnectionRefusedThenPixelFiredAndDownloadFailedCommandSent() =
        runTest {
            val failReason = DownloadFailReason.ConnectionRefused

            callback.onError(url = "url", reason = failReason)

            verify(mockPixel).fire(DownloadsPixelName.DOWNLOAD_REQUEST_FAILED)
            verify(mockFileDownloadNotificationManager, never()).showDownloadFinishedNotification(
                any(),
                any(),
                anyOrNull()
            )
            callback.commands().test {
                val actualItem = awaitItem()
                assertTrue(actualItem is ShowDownloadFailedMessage)
                assertEquals(R.string.downloadsErrorMessage, actualItem.messageId)
            }
        }

    @Test
    fun whenOnErrorCalledForNoDownloadIdAndUnsupportedUrlThenPixelFiredAndNotificationSentAndDownloadFailedCommandSent() =
        runTest {
            callback.onError(url = "url", reason = DownloadFailReason.UnsupportedUrlType)

            verify(mockPixel).fire(DownloadsPixelName.DOWNLOAD_REQUEST_FAILED)
            verify(mockFileDownloadNotificationManager).showDownloadFailedNotification(
                any(),
                eq("url")
            )
            callback.commands().test {
                val actualItem = awaitItem()
                assertTrue(actualItem is ShowDownloadFailedMessage)
                assertEquals(R.string.downloadsDownloadGenericErrorMessage, actualItem.messageId)
            }
        }

    private fun oneItem() =
        DownloadItem(
            downloadId = 10L,
            downloadStatus = FINISHED,
            fileName = "file.jpg",
            contentLength = 100L,
            createdAt = "2022-02-21T10:56:22",
            filePath = File("file.jpg").absolutePath,
        )
}
