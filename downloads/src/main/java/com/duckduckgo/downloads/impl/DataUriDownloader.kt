

package com.duckduckgo.downloads.impl

import android.util.Base64
import androidx.annotation.WorkerThread
import com.duckduckgo.common.utils.formatters.time.DatabaseDateFormatter
import com.duckduckgo.downloads.api.DownloadFailReason
import com.duckduckgo.downloads.api.DownloadFailReason.DataUriParseException
import com.duckduckgo.downloads.api.FileDownloader.PendingFileDownload
import com.duckduckgo.downloads.api.model.DownloadItem
import com.duckduckgo.downloads.impl.DataUriParser.GeneratedFilename
import com.duckduckgo.downloads.impl.DataUriParser.ParseResult
import com.duckduckgo.downloads.store.DownloadStatus.STARTED
import java.io.File
import java.io.IOException
import javax.inject.Inject
import logcat.asLog
import logcat.logcat

class DataUriDownloader @Inject constructor(
    private val dataUriParser: DataUriParser,
) {

    @WorkerThread
    fun download(
        pending: PendingFileDownload,
        callback: DownloadCallback,
    ) {
        try {
            when (val parsedDataUri = dataUriParser.generate(pending.url)) {
                is ParseResult.Invalid -> {
                    logcat { "Failed to extract data from data URI" }
                    callback.onError(url = pending.url, reason = DataUriParseException)
                    return
                }
                is ParseResult.ParsedDataUri -> {
                    val file = initialiseFilesOnDisk(pending, parsedDataUri.filename)

                    callback.onStart(
                        DownloadItem(
                            downloadId = 0L,
                            downloadStatus = STARTED,
                            fileName = file.name,
                            contentLength = 0L,
                            filePath = file.absolutePath,
                            createdAt = DatabaseDateFormatter.timestamp(),
                        ),
                    )

                    runCatching {
                        writeBytesToFiles(parsedDataUri.data, file)
                    }
                        .onSuccess {
                            logcat { "Succeeded to decode Base64" }
                            callback.onSuccess(file = file, mimeType = parsedDataUri.mimeType)
                        }
                        .onFailure {
                            logcat { "Failed to decode Base64: ${it.asLog()}" }
                            callback.onError(url = pending.url, reason = DownloadFailReason.DataUriParseException)
                        }
                }
            }
        } catch (e: IOException) {
            logcat { "Failed to save data uri: ${e.asLog()}" }
            callback.onError(url = pending.url, reason = DownloadFailReason.DataUriParseException)
        }
    }

    private fun writeBytesToFiles(
        data: String?,
        file: File,
    ) {
        val imageByteArray = Base64.decode(data, Base64.DEFAULT)
        file.writeBytes(imageByteArray)
    }

    private fun initialiseFilesOnDisk(
        pending: PendingFileDownload,
        generatedFilename: GeneratedFilename,
    ): File {
        val downloadDirectory = pending.directory
        val file = File(downloadDirectory, generatedFilename.toString())

        if (!downloadDirectory.exists()) downloadDirectory.mkdirs()
        if (!file.exists()) file.createNewFile()
        return file
    }
}
