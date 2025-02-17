

package com.duckduckgo.app.browser.shortcut

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.shortcut.ShortcutBuilder.Companion.SHORTCUT_TITLE_ARG
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ReceiverScope
import dagger.android.AndroidInjection
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@InjectWith(ReceiverScope::class)
class ShortcutReceiver : BroadcastReceiver() {

    @Inject
    lateinit var pixel: Pixel

    @Inject lateinit var dispatcher: DispatcherProvider

    @Inject @AppCoroutineScope
    lateinit var appCoroutineScope: CoroutineScope

    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        AndroidInjection.inject(this, context)
        onShortcutAdded(context, intent)
    }

    @VisibleForTesting
    fun onShortcutAdded(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra(SHORTCUT_TITLE_ARG)
        if (!IGNORE_MANUFACTURERS_LIST.contains(Build.MANUFACTURER)) {
            context?.let {
                Toast.makeText(it, it.getString(R.string.shortcutAddedText, title), Toast.LENGTH_SHORT).show()
            }
        }
        appCoroutineScope.launch(dispatcher.io()) {
            pixel.fire(AppPixelName.SHORTCUT_ADDED)
        }
    }

    companion object {
        val IGNORE_MANUFACTURERS_LIST = listOf("samsung", "huawei")
    }
}
