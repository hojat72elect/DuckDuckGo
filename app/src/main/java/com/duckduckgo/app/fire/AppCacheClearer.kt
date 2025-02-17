

package com.duckduckgo.app.fire

import android.content.Context
import com.duckduckgo.app.browser.favicon.FileBasedFaviconPersister.Companion.FAVICON_PERSISTED_DIR
import com.duckduckgo.app.global.api.NetworkApiCache
import com.duckduckgo.app.global.file.FileDeleter

interface AppCacheClearer {

    suspend fun clearCache()
}

class AndroidAppCacheClearer(
    private val context: Context,
    private val fileDeleter: FileDeleter,
) : AppCacheClearer {

    override suspend fun clearCache() {
        fileDeleter.deleteContents(context.cacheDir, FILENAMES_EXCLUDED_FROM_DELETION)
    }

    companion object {

        /*
         * Exclude the WebView cache directories, based on warning from Firefox Focus:
         *   "If the folder or its contents are deleted, WebView will stop using the disk cache entirely."
         *
         * To date, there are two known WebView cache directories; only one is present at a time but which is used is based on WebView version.
         */
        private const val WEBVIEW_CACHE_DIR = "WebView"
        private const val WEBVIEW_CACHE_DIR_LEGACY = "org.chromium.android_webview"

        /*
         * Exclude the OkHttp networking cache from being deleted. This doesn't contain any sensitive information.
         * Deleting this would just cause large amounts of non-sensitive data to have be downloaded again when app next launches.
         */
        private const val NETWORK_CACHE_DIR = NetworkApiCache.FILE_NAME

        private val FILENAMES_EXCLUDED_FROM_DELETION = listOf(
            WEBVIEW_CACHE_DIR,
            WEBVIEW_CACHE_DIR_LEGACY,
            NETWORK_CACHE_DIR,
            FAVICON_PERSISTED_DIR,
        )
    }
}
