

package com.duckduckgo.app.browser.shortcut

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.duckduckgo.app.browser.BrowserActivity
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.commands.Command
import java.util.UUID
import javax.inject.Inject

class ShortcutBuilder @Inject constructor() {

    private fun buildPinnedPageShortcut(
        context: Context,
        homeShortcut: Command.AddHomeShortcut,
    ): ShortcutInfoCompat {
        val intent = Intent(context, BrowserActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(Intent.EXTRA_TEXT, homeShortcut.url)
        intent.putExtra(SHORTCUT_EXTRA_ARG, true)

        val icon = when {
            homeShortcut.icon != null -> IconCompat.createWithBitmap(homeShortcut.icon)
            else -> IconCompat.createWithResource(context, R.drawable.logo_mini)
        }

        return ShortcutInfoCompat.Builder(context, UUID.randomUUID().toString())
            .setShortLabel(homeShortcut.title)
            .setIntent(intent)
            .setIcon(icon)
            .build()
    }

    private fun buildPendingIntent(
        context: Context,
        url: String,
        title: String,
    ): PendingIntent? {
        val pinnedShortcutCallbackIntent = Intent(context, ShortcutReceiver::class.java)
        pinnedShortcutCallbackIntent.putExtra(SHORTCUT_URL_ARG, url)
        pinnedShortcutCallbackIntent.putExtra(SHORTCUT_TITLE_ARG, title)
        return PendingIntent.getBroadcast(context, SHORTCUT_ADDED_CODE, pinnedShortcutCallbackIntent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
    }

    fun requestPinShortcut(
        context: Context,
        homeShortcut: Command.AddHomeShortcut,
    ) {
        val shortcutInfo = buildPinnedPageShortcut(context, homeShortcut)
        val pendingIntent = buildPendingIntent(context, homeShortcut.url, homeShortcut.title)

        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, pendingIntent?.intentSender)
    }

    companion object {
        const val SHORTCUT_ADDED_CODE = 9000

        const val SHORTCUT_EXTRA_ARG = "shortCutAdded"
        const val SHORTCUT_URL_ARG = "shortcutUrl"
        const val SHORTCUT_TITLE_ARG = "shortcutTitle"
    }
}
