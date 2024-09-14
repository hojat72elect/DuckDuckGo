

package com.duckduckgo.app.browser.favicon

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.duckduckgo.common.utils.DispatcherProvider
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.withContext

interface FaviconDownloader {
    suspend fun getFaviconFromDisk(file: File): Bitmap?
    suspend fun getFaviconFromDisk(
        file: File,
        cornerRadius: Int,
        width: Int,
        height: Int,
    ): Bitmap?

    suspend fun getFaviconFromUrl(uri: Uri): Bitmap?
}

class GlideFaviconDownloader @Inject constructor(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider,
) : FaviconDownloader {

    override suspend fun getFaviconFromDisk(file: File): Bitmap? {
        return withContext(dispatcherProvider.io()) {
            return@withContext runCatching {
                Glide.with(context)
                    .asBitmap()
                    .load(file)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .submit()
                    .get()
            }.getOrNull()
        }
    }

    override suspend fun getFaviconFromDisk(
        file: File,
        cornerRadius: Int,
        width: Int,
        height: Int,
    ): Bitmap? {
        return withContext(dispatcherProvider.io()) {
            return@withContext runCatching {
                Glide.with(context)
                    .asBitmap()
                    .load(file)
                    .transform(RoundedCorners(cornerRadius))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .submit(width, height)
                    .get()
            }.getOrNull()
        }
    }

    override suspend fun getFaviconFromUrl(uri: Uri): Bitmap? {
        return withContext(dispatcherProvider.io()) {
            return@withContext runCatching {
                Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .submit()
                    .get()
            }.getOrNull()
        }
    }
}
