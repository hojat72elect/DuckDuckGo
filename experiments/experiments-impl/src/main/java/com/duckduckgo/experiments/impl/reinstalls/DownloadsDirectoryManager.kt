package com.duckduckgo.experiments.impl.reinstalls

import android.os.Environment
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import java.io.File
import javax.inject.Inject
import timber.log.Timber

interface DownloadsDirectoryManager {

    fun getDownloadsDirectory(): File
    fun createNewDirectory(directoryName: String)
}

@ContributesBinding(AppScope::class)
class DownloadsDirectoryManagerImpl @Inject constructor() : DownloadsDirectoryManager {

    override fun getDownloadsDirectory(): File {
        val downloadDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadDirectory.exists()) {
            Timber.i(
                "Download directory doesn't exist; trying to create it. %s",
                downloadDirectory.absolutePath
            )
            downloadDirectory.mkdirs()
        }
        return downloadDirectory
    }

    override fun createNewDirectory(directoryName: String) {
        val directory = File(getDownloadsDirectory(), directoryName)
        val success = directory.mkdirs()
        Timber.i("Directory creation success: %s", success)
        if (!success) {
            Timber.e("Directory creation failed")
            kotlin.runCatching {
                val directoryCreationSuccess = directory.createNewFile()
                Timber.i("File creation success: %s", directoryCreationSuccess)
            }.onFailure {
                Timber.w("Failed to create file: %s", it.message)
            }
        }
    }
}
