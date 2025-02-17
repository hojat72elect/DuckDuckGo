package com.duckduckgo.app.browser.httperrors

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface HttpErrorPixels {
    fun updateCountPixel(httpErrorPixelName: HttpErrorPixelName)
    fun fireCountPixel(httpErrorPixelName: HttpErrorPixelName)
}

@ContributesBinding(AppScope::class)
class RealHttpErrorPixels @Inject constructor(
    private val pixel: Pixel,
    private val context: Context,
) : HttpErrorPixels {

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(
            FILENAME,
            Context.MODE_PRIVATE
        )
    }

    override fun updateCountPixel(httpErrorPixelName: HttpErrorPixelName) {
        val count = preferences.getInt(httpErrorPixelName.appendCountSuffix(), 0)
        preferences.edit { putInt(httpErrorPixelName.appendCountSuffix(), count + 1) }
    }

    override fun fireCountPixel(httpErrorPixelName: HttpErrorPixelName) {
        val now = Instant.now().toEpochMilli()

        val count = preferences.getInt(httpErrorPixelName.appendCountSuffix(), 0)
        if (count == 0) {
            return
        }

        val timestamp = preferences.getLong(httpErrorPixelName.appendTimestampSuffix(), 0L)
        if (timestamp == 0L || now >= timestamp) {
            pixel.fire(
                httpErrorPixelName,
                mapOf(HttpErrorPixelParameters.HTTP_ERROR_CODE_COUNT to count.toString())
            )
                .also {
                    preferences.edit {
                        putLong(
                            httpErrorPixelName.appendTimestampSuffix(),
                            now.plus(TimeUnit.HOURS.toMillis(WINDOW_INTERVAL_HOURS))
                        )
                        putInt(httpErrorPixelName.appendCountSuffix(), 0)
                    }
                }
        }
    }

    private fun HttpErrorPixelName.appendTimestampSuffix(): String {
        return "${this.pixelName}_timestamp"
    }

    private fun HttpErrorPixelName.appendCountSuffix(): String {
        return "${this.pixelName}_count"
    }

    companion object {
        private const val FILENAME = "com.duckduckgo.app.browser.httperrors"
        private const val WINDOW_INTERVAL_HOURS = 24L
    }
}
