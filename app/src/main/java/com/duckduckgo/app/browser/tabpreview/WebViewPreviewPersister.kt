

package com.duckduckgo.app.browser.tabpreview

import android.content.Context
import android.graphics.Bitmap
import com.duckduckgo.app.global.file.FileDeleter
import com.duckduckgo.common.utils.DispatcherProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.withContext
import timber.log.Timber

interface WebViewPreviewPersister {

    fun fullPathForFile(
        tabId: String,
        previewName: String,
    ): String

    suspend fun save(
        bitmap: Bitmap,
        tabId: String,
    ): String

    suspend fun deleteAll()
    suspend fun deletePreviewsForTab(
        tabId: String,
        currentPreviewImage: String?,
    )
}

class FileBasedWebViewPreviewPersister(
    val context: Context,
    private val fileDeleter: FileDeleter,
    private val dispatchers: DispatcherProvider,
) : WebViewPreviewPersister {

    override suspend fun deleteAll() {
        fileDeleter.deleteDirectory(previewDestinationDirectory())
    }

    override suspend fun save(
        bitmap: Bitmap,
        tabId: String,
    ): String {
        return withContext(dispatchers.io()) {
            val previewFile = prepareDestinationFile(tabId)
            writeBytesToFile(previewFile, bitmap)

            Timber.d("Wrote bitmap preview to ${previewFile.absolutePath}")
            return@withContext previewFile.name
        }
    }

    override suspend fun deletePreviewsForTab(
        tabId: String,
        currentPreviewImage: String?,
    ) {
        val directoryToDelete = directoryForTabPreviews(tabId)

        if (currentPreviewImage == null) {
            Timber.i("Deleting all tab previews for $tabId")
            fileDeleter.deleteDirectory(directoryToDelete)
        } else {
            Timber.i("Keeping tab preview $currentPreviewImage but deleting the rest for $tabId")
            val exclusionList = listOf(currentPreviewImage)
            fileDeleter.deleteContents(directoryToDelete, exclusionList)
        }

        Timber.i("Does tab preview directory still exist? ${directoryToDelete.exists()}")
    }

    override fun fullPathForFile(
        tabId: String,
        previewName: String,
    ): String {
        return fileForPreview(tabId, previewName).absolutePath
    }

    private fun fileForPreview(
        tabId: String,
        previewName: String,
    ): File {
        val tabPreviewDirectory = directoryForTabPreviews(tabId)
        return File(tabPreviewDirectory, previewName)
    }

    private fun directoryForTabPreviews(tabId: String): File {
        return File(previewDestinationDirectory(), tabId)
    }

    private fun prepareDestinationFile(tabId: String): File {
        val previewFileDestination = directoryForTabPreviews(tabId)
        previewFileDestination.mkdirs()

        val timestamp = System.currentTimeMillis()
        return File(previewFileDestination, "$timestamp.jpg")
    }

    private fun writeBytesToFile(
        previewFile: File,
        bitmap: Bitmap,
    ) {
        FileOutputStream(previewFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
        }
    }

    private fun previewDestinationDirectory(): File {
        return File(context.cacheDir, TAB_PREVIEW_DIRECTORY)
    }

    companion object {
        const val TAB_PREVIEW_DIRECTORY = "tabPreviews"
    }
}
