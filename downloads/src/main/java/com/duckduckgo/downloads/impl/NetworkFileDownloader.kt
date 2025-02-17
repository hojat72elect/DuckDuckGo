

package com.duckduckgo.downloads.impl

import com.duckduckgo.downloads.api.DownloadFailReason.ConnectionRefused
import com.duckduckgo.downloads.api.FileDownloader.PendingFileDownload
import javax.inject.Inject
import logcat.logcat

class NetworkFileDownloader @Inject constructor(
    private val filenameExtractor: FilenameExtractor,
    private val fileService: DownloadFileService,
    private val urlFileDownloader: UrlFileDownloader,
) {

    fun download(pendingDownload: PendingFileDownload, callback: DownloadCallback) {
        logcat { "Start download for ${pendingDownload.url}." }

        logcat {
            "Content-Disposition is ${pendingDownload.contentDisposition} and " +
                "Content-Type is ${pendingDownload.mimeType} for ${pendingDownload.url}."
        }

        if (pendingDownload.contentDisposition != null && pendingDownload.mimeType != null) {
            downloadFile(pendingDownload, callback)
        } else {
            requestHeaders(pendingDownload, callback)
        }
    }

    private fun requestHeaders(pendingDownload: PendingFileDownload, callback: DownloadCallback) {
        logcat { "Make a HEAD request for ${pendingDownload.url} as there are no values for Content-Disposition or Content-Type." }

        runCatching {
            fileService.getFileDetails(pendingDownload.url)?.execute()?.let { response ->
                var updatedPendingDownload = pendingDownload.copy()

                if (response.isSuccessful) {
                    logcat { "HEAD request successful for ${pendingDownload.url}" }
                    val contentType = response.headers().get("content-type")
                    val contentDisposition = response.headers().get("content-disposition")

                    logcat {
                        "Retrieved new values from the HEAD request. " +
                            "Content-Disposition is $contentDisposition and Content-Type is $contentType."
                    }

                    if (contentType != null) {
                        updatedPendingDownload = updatedPendingDownload.copy(mimeType = contentType)
                    }

                    if (contentDisposition != null) {
                        updatedPendingDownload = updatedPendingDownload.copy(contentDisposition = contentDisposition)
                    }
                } else {
                    // This is a non-[200..300) response code. Proceed with download using the Download Manager.
                    logcat {
                        "HEAD request unsuccessful. " +
                            "Got a non-[200..300) response code for ${pendingDownload.url}. Error body: ${response.errorBody()}"
                    }
                }

                downloadFile(updatedPendingDownload, callback)
            }
        }.onFailure {
            // Network exception occurred talking to the server or an unexpected exception occurred creating the request/processing the response.
            callback.onError(url = pendingDownload.url, reason = ConnectionRefused)
        }
    }

    private fun downloadFile(pendingDownload: PendingFileDownload, callback: DownloadCallback) {
        when (val extractionResult = filenameExtractor.extract(pendingDownload)) {
            is FilenameExtractor.FilenameExtractionResult.Extracted -> downloadFile(pendingDownload, extractionResult.filename, callback)
            is FilenameExtractor.FilenameExtractionResult.Guess -> downloadFile(pendingDownload, extractionResult.bestGuess, callback)
        }
    }

    private fun downloadFile(
        pendingDownload: PendingFileDownload,
        guessedFileName: String,
        callback: DownloadCallback,
    ) {
        urlFileDownloader.downloadFile(pendingDownload, guessedFileName, callback)
    }
}
