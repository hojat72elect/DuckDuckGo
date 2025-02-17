

package com.duckduckgo.app.browser.filechooser

import android.content.Intent
import android.net.Uri
import java.util.*
import javax.inject.Inject
import timber.log.Timber

class FileChooserIntentBuilder @Inject constructor() {

    fun intent(
        acceptTypes: Array<String>,
        canChooseMultiple: Boolean = false,
    ): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).also {
            it.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            configureSelectableFileTypes(it, acceptTypes)
            configureAllowMultipleFile(it, canChooseMultiple)
        }
    }

    /**
     * Some apps return data data as `intent.data` value, some in the `intent.clipData`; some use both.
     *
     * If a user selects multiple files, then both the `data` and `clipData` might be populated, but we'd want to use `clipData`.
     * If we inspect `data` first, we might conclude there is only a single file selected. So we look for `clipData` first.
     *
     * Empirically, the first value of `clipData` might mirror what is in the `data` value. So if we have any in `clipData`, use
     * them and return early.
     *
     * Order is important;
     *     we want to use the clip data if it exists.
     *     failing that, we check `data` value`.
     *     failing that, we bail.
     */
    fun extractSelectedFileUris(intent: Intent): Array<Uri>? {
        // first try to determine if multiple files were selected
        val clipData = intent.clipData
        if (clipData != null && clipData.itemCount > 0) {
            val uris = arrayListOf<Uri>()
            for (i in 0 until clipData.itemCount) {
                uris.add(clipData.getItemAt(i).uri)
            }
            return uris.toTypedArray()
        }

        // next try to determine if a single file was selected
        val singleFileResult = intent.data
        if (singleFileResult != null) {
            return arrayOf(singleFileResult)
        }

        // failing that, give up
        Timber.w("Failed to extract selected file information")
        return null
    }

    private fun configureSelectableFileTypes(
        intent: Intent,
        acceptTypes: Array<String>,
    ) {
        intent.type = "*/*"

        val acceptedMimeTypes = mutableSetOf<String>()

        acceptTypes
            .filter { it.isNotBlank() }
            .forEach { acceptedMimeTypes.add(it.lowercase(Locale.getDefault())) }

        if (acceptedMimeTypes.isNotEmpty()) {
            Timber.d("Selectable file types limited to $acceptedMimeTypes")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, acceptedMimeTypes.toTypedArray())
        } else {
            Timber.d("No selectable file type filters applied")
        }
    }

    private fun configureAllowMultipleFile(
        intent: Intent,
        canChooseMultiple: Boolean,
    ) {
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, canChooseMultiple)
    }
}
