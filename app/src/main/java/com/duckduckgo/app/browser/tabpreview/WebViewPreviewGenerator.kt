

package com.duckduckgo.app.browser.tabpreview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebView
import androidx.core.view.drawToBitmap
import com.duckduckgo.common.utils.DispatcherProvider
import kotlinx.coroutines.withContext

interface WebViewPreviewGenerator {
    suspend fun generatePreview(webView: WebView): Bitmap
}

class FileBasedWebViewPreviewGenerator(private val dispatchers: DispatcherProvider) : WebViewPreviewGenerator {

    override suspend fun generatePreview(webView: WebView): Bitmap {
        disableScrollbars(webView)
        val fullSizeBitmap = createBitmap(webView)
        val scaledBitmap = scaleBitmap(fullSizeBitmap)
        enableScrollbars(webView)
        return scaledBitmap
    }

    @SuppressLint("AvoidComputationUsage")
    private suspend fun createBitmap(webView: WebView): Bitmap {
        return withContext(dispatchers.computation()) {
            webView.drawToBitmap()
        }
    }

    private suspend fun enableScrollbars(webView: WebView) {
        withContext(dispatchers.main()) {
            webView.isVerticalScrollBarEnabled = true
            webView.isHorizontalScrollBarEnabled = true
        }
    }

    private suspend fun disableScrollbars(webView: WebView) {
        withContext(dispatchers.main()) {
            webView.isVerticalScrollBarEnabled = false
            webView.isHorizontalScrollBarEnabled = false
        }
    }

    @SuppressLint("AvoidComputationUsage")
    private suspend fun scaleBitmap(bitmap: Bitmap): Bitmap {
        return withContext(dispatchers.computation()) {
            return@withContext Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * COMPRESSION_RATIO).toInt(),
                (bitmap.height * COMPRESSION_RATIO).toInt(),
                false,
            )
        }
    }

    companion object {
        private const val COMPRESSION_RATIO = 0.5
    }
}
