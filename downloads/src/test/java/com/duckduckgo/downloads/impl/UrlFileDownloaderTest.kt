

package com.duckduckgo.downloads.impl

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.downloads.api.DownloadFailReason
import com.duckduckgo.downloads.api.FileDownloader
import java.io.File
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import retrofit2.Call
import retrofit2.Response

class UrlFileDownloaderTest {
    @get:Rule
    @Suppress("unused")
    val coroutineRule = CoroutineTestRule()

    private val downloadFileService: DownloadFileService = mock()
    private val call: Call<ResponseBody> = mock()
    private lateinit var realFileDownloadManager: RealUrlFileDownloadCallManager

    private lateinit var urlFileDownloader: UrlFileDownloader

    @Before
    fun setup() {
        realFileDownloadManager = RealUrlFileDownloadCallManager()
        whenever(downloadFileService.downloadFile(anyString(), anyString())).thenReturn(call)

        urlFileDownloader = UrlFileDownloader(
            downloadFileService,
            realFileDownloadManager,
            FakeCookieManagerWrapper(),
        )
    }

    @Test
    fun whenDownloadFileSuccessfulThenCallOnStartInProgressAndSuccessCallbacks() = runTest {
        val pendingFileDownload = buildPendingDownload("https://example.com/file.txt")
        val filename = "file.txt"
        val downloadCallback = mock<DownloadCallback>()

        whenever(call.execute()).thenReturn(Response.success("success".toResponseBody()))

        urlFileDownloader.downloadFile(pendingFileDownload, filename, downloadCallback)

        verify(downloadCallback).onStart(any())
        verify(downloadCallback).onProgress(any(), eq(filename), eq(100))
        verify(downloadCallback).onSuccess(any(), eq("success".length.toLong()), eq(File(pendingFileDownload.directory, filename)), anyOrNull())
    }

    @Test
    fun whenDownloadFileFailedAsCancelledThenCallOnCancelCallback() = runTest {
        val pendingFileDownload = buildPendingDownload("https://example.com/file.txt")
        val filename = "file.txt"
        val downloadCallback = mock<DownloadCallback>()

        whenever(call.isCanceled).thenReturn(true)
        whenever(call.execute()).thenReturn(Response.success("success".toResponseBody()))
        // this is hacky, but it is to throw while writing file, which will cause response success but on error path
        whenever(downloadCallback.onProgress(any(), any(), any())).thenThrow(IllegalStateException("hacky throw"))

        urlFileDownloader.downloadFile(pendingFileDownload, filename, downloadCallback)

        verify(downloadCallback).onStart(any())
        verify(downloadCallback).onProgress(any(), any(), any())
        verify(downloadCallback, never()).onSuccess(any(), any(), any(), anyOrNull())
        verify(downloadCallback, never()).onError(any(), any(), any())

        verify(downloadCallback).onCancel(any())
    }

    @Test
    fun whenDownloadFileFailedAsWriteErrorThenCallOnCancelCallback() = runTest {
        val pendingFileDownload = buildPendingDownload("https://example.com/file.txt")
        val filename = "file.txt"
        val downloadCallback = mock<DownloadCallback>()

        whenever(call.isCanceled).thenReturn(false)
        whenever(call.execute()).thenReturn(Response.success("success".toResponseBody()))
        // this is hacky, but it is to throw while writing file, which will cause response success but on error path
        whenever(downloadCallback.onProgress(any(), any(), any())).thenThrow(IllegalStateException("hacky throw"))

        urlFileDownloader.downloadFile(pendingFileDownload, filename, downloadCallback)

        verify(downloadCallback).onStart(any())
        verify(downloadCallback).onProgress(any(), any(), any())
        verify(downloadCallback, never()).onSuccess(any(), any(), any(), anyOrNull())
        verify(downloadCallback, never()).onCancel(any())

        verify(downloadCallback).onError(any(), any(), eq(DownloadFailReason.Other))
    }

    @Test
    fun whenDownloadFileFailedAsErrorThenCallOnErrorCallback() = runTest {
        val pendingFileDownload = buildPendingDownload("https://example.com/file.txt")
        val filename = "file.txt"
        val downloadCallback = mock<DownloadCallback>()

        whenever(call.execute()).thenReturn(Response.error(400, "error".toResponseBody()))

        urlFileDownloader.downloadFile(pendingFileDownload, filename, downloadCallback)

        verify(downloadCallback).onStart(any())
        verify(downloadCallback, never()).onProgress(any(), any(), any())
        verify(downloadCallback, never()).onSuccess(any(), any(), any(), anyOrNull())
        verify(downloadCallback, never()).onCancel(any())

        verify(downloadCallback).onError(any(), any(), any())
    }

    @Test
    fun whenDownloadFileSucceedsAsCancelledThenCallOnCancelCallback() = runTest {
        val pendingFileDownload = buildPendingDownload("https://example.com/file.txt")
        val filename = "file.txt"
        val downloadCallback = mock<DownloadCallback>()

        whenever(call.isCanceled).thenReturn(true)
        whenever(call.execute()).thenReturn(Response.success("success".toResponseBody()))
        // this is hacky, but it is to throw while writing file, which will cause response success but on error path
        whenever(downloadCallback.onProgress(any(), any(), any())).thenThrow(IllegalStateException("hacky throw"))

        urlFileDownloader.downloadFile(pendingFileDownload, filename, downloadCallback)

        verify(downloadCallback).onStart(any())
        verify(downloadCallback).onProgress(any(), any(), any())
        verify(downloadCallback, never()).onSuccess(any(), any(), any(), anyOrNull())

        verify(downloadCallback).onCancel(any())
        verify(downloadCallback, never()).onError(any(), any(), any())
    }

    @Test
    fun whenDownloadFileSucceedsAsFailedToWriteThenCallOnErrorCallback() = runTest {
        val pendingFileDownload = buildPendingDownload("https://example.com/file.txt")
        val filename = "file.txt"
        val downloadCallback = mock<DownloadCallback>()

        whenever(call.isCanceled).thenReturn(false)
        whenever(call.execute()).thenReturn(Response.success("success".toResponseBody()))
        // this is hacky, but it is to throw while writing file, which will cause response success but on error path
        whenever(downloadCallback.onProgress(any(), any(), any())).thenThrow(IllegalStateException("hacky throw"))

        urlFileDownloader.downloadFile(pendingFileDownload, filename, downloadCallback)

        verify(downloadCallback).onStart(any())
        verify(downloadCallback).onProgress(any(), any(), any())
        verify(downloadCallback, never()).onSuccess(any(), any(), any(), anyOrNull())

        verify(downloadCallback, never()).onCancel(any())
        verify(downloadCallback, never()).onError(eq(pendingFileDownload.url), any(), eq(
            DownloadFailReason.ConnectionRefused))
    }

    private fun buildPendingDownload(
        url: String,
        contentDisposition: String? = null,
        mimeType: String? = null,
    ): FileDownloader.PendingFileDownload {
        return FileDownloader.PendingFileDownload(
            url = url,
            contentDisposition = contentDisposition,
            mimeType = mimeType,
            subfolder = "folder",
            directory = File("directory"),
        )
    }

    private class FakeCookieManagerWrapper : CookieManagerWrapper {
        override fun getCookie(url: String): String? {
            return null
        }
    }
}
