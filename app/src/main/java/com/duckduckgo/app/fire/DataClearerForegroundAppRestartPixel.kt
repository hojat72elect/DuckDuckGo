

package com.duckduckgo.app.fire

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.lifecycle.LifecycleOwner
import com.duckduckgo.app.global.intentText
import com.duckduckgo.app.lifecycle.MainProcessLifecycleObserver
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.systemsearch.SystemSearchActivity
import com.duckduckgo.di.scopes.AppScope
import dagger.SingleInstanceIn
import javax.inject.Inject
import timber.log.Timber

/**
 * Stores information about unsent automatic data clearer restart Pixels, detecting if user started the app from an external Intent.
 * Contains logic to send unsent pixels.
 *
 * When writing values here to SharedPreferences, it is crucial to use `commit = true`. As otherwise the change can be lost in the process restart.
 */
@SingleInstanceIn(AppScope::class)
class DataClearerForegroundAppRestartPixel @Inject constructor(
    private val context: Context,
    private val pixel: Pixel,
) : MainProcessLifecycleObserver {
    private var detectedUserIntent: Boolean = false

    private val pendingAppForegroundRestart: Int
        get() = preferences.getInt(KEY_UNSENT_CLEAR_APP_RESTARTED_PIXELS, 0)

    private val pendingAppForegroundRestartWithIntent: Int
        get() = preferences.getInt(KEY_UNSENT_CLEAR_APP_RESTARTED_WITH_INTENT_PIXELS, 0)

    @UiThread
    override fun onCreate(owner: LifecycleOwner) {
        Timber.i("onAppCreated firePendingPixels")
        firePendingPixels()
    }

    @UiThread
    override fun onStop(owner: LifecycleOwner) {
        Timber.i("Registered App on_stop")
        detectedUserIntent = false
    }

    fun registerIntent(intent: Intent?) {
        detectedUserIntent = widgetActivity(intent) || !intent?.intentText.isNullOrEmpty()
    }

    fun incrementCount() {
        if (detectedUserIntent) {
            Timber.i("Registered restart with intent")
            incrementCount(pendingAppForegroundRestart, KEY_UNSENT_CLEAR_APP_RESTARTED_WITH_INTENT_PIXELS)
        } else {
            Timber.i("Registered restart without intent")
            incrementCount(pendingAppForegroundRestartWithIntent, KEY_UNSENT_CLEAR_APP_RESTARTED_PIXELS)
        }
    }

    fun firePendingPixels() {
        firePendingPixels(pendingAppForegroundRestart, AppPixelName.FORGET_ALL_AUTO_RESTART)
        firePendingPixels(pendingAppForegroundRestartWithIntent, AppPixelName.FORGET_ALL_AUTO_RESTART_WITH_INTENT)
        resetCount()
    }

    private fun incrementCount(
        counter: Int,
        sharedPrefKey: String,
    ) {
        val updated = counter + 1
        preferences.edit(commit = true) {
            putInt(sharedPrefKey, updated)
        }
    }

    private fun firePendingPixels(
        counter: Int,
        pixelName: Pixel.PixelName,
    ) {
        if (counter > 0) {
            for (i in 1..counter) {
                Timber.i("Fired pixel: ${pixelName.pixelName}/$counter")
                pixel.fire(pixelName)
            }
        }
    }

    private fun resetCount() {
        preferences.edit(commit = true) {
            putInt(KEY_UNSENT_CLEAR_APP_RESTARTED_PIXELS, 0)
            putInt(KEY_UNSENT_CLEAR_APP_RESTARTED_WITH_INTENT_PIXELS, 0)
        }
        Timber.i("counter reset")
    }

    private fun widgetActivity(intent: Intent?): Boolean =
        intent?.component?.className?.contains(SystemSearchActivity::class.java.canonicalName.orEmpty()) == true

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
    }

    companion object {
        @VisibleForTesting
        const val FILENAME = "com.duckduckgo.app.fire.unsentpixels.settings"
        const val KEY_UNSENT_CLEAR_APP_RESTARTED_PIXELS = "KEY_UNSENT_CLEAR_APP_RESTARTED_PIXELS"
        const val KEY_UNSENT_CLEAR_APP_RESTARTED_WITH_INTENT_PIXELS = "KEY_UNSENT_CLEAR_APP_RESTARTED_WITH_INTENT_PIXELS"
    }
}
