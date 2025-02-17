

package com.duckduckgo.app.browser

import android.content.Context
import android.view.ContextMenu
import android.view.MenuItem
import android.webkit.URLUtil
import android.webkit.WebView
import com.duckduckgo.app.browser.LongPressHandler.RequiredAction
import com.duckduckgo.app.browser.LongPressHandler.RequiredAction.*
import com.duckduckgo.app.browser.model.LongPressTarget
import com.duckduckgo.app.pixels.AppPixelName.*
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.customtabs.api.CustomTabDetector
import javax.inject.Inject
import timber.log.Timber

interface LongPressHandler {
    fun handleLongPress(
        longPressTargetType: Int,
        longPressTargetUrl: String?,
        menu: ContextMenu,
    )

    fun userSelectedMenuItem(
        longPressTarget: LongPressTarget,
        item: MenuItem,
    ): RequiredAction

    sealed class RequiredAction {
        object None : RequiredAction()
        class OpenInNewTab(val url: String) : RequiredAction()
        class OpenInNewBackgroundTab(val url: String) : RequiredAction()
        class DownloadFile(val url: String) : RequiredAction()
        class ShareLink(val url: String) : RequiredAction()
        class CopyLink(val url: String) : RequiredAction()
    }
}

class WebViewLongPressHandler @Inject constructor(
    private val context: Context,
    private val pixel: Pixel,
    private val customTabDetector: CustomTabDetector,
) : LongPressHandler {

    override fun handleLongPress(
        longPressTargetType: Int,
        longPressTargetUrl: String?,
        menu: ContextMenu,
    ) {
        menu.setHeaderTitle(longPressTargetUrl?.take(MAX_TITLE_LENGTH) ?: context.getString(R.string.options))

        var menuShown = true
        when (longPressTargetType) {
            WebView.HitTestResult.IMAGE_TYPE -> {
                if (isLinkSupported(longPressTargetUrl)) {
                    addDownloadImageMenuOptions(menu)
                    if (!URLUtil.isDataUrl(longPressTargetUrl) && !customTabDetector.isCustomTab()) {
                        addImageMenuOpenInTabOptions(menu)
                    }
                }
            }
            WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                if (isLinkSupported(longPressTargetUrl)) {
                    addDownloadImageMenuOptions(menu)
                    if (!URLUtil.isDataUrl(longPressTargetUrl)) {
                        if (!customTabDetector.isCustomTab()) {
                            addImageMenuOpenInTabOptions(menu)
                            addLinkMenuOpenInTabOptions(menu)
                        }
                        addLinkMenuOtherOptions(menu)
                    }
                }
            }
            WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                if (isLinkSupported(longPressTargetUrl)) {
                    if (!customTabDetector.isCustomTab()) {
                        addLinkMenuOpenInTabOptions(menu)
                    }
                    addLinkMenuOtherOptions(menu)
                }
            }
            else -> {
                Timber.v("App does not yet handle target type: $longPressTargetType")
                menuShown = false
            }
        }

        if (menuShown) {
            pixel.fire(LONG_PRESS)
        }
    }

    private fun addDownloadImageMenuOptions(menu: ContextMenu) {
        menu.add(0, CONTEXT_MENU_ID_DOWNLOAD_IMAGE, CONTEXT_MENU_ID_DOWNLOAD_IMAGE, R.string.downloadImage)
    }

    private fun addImageMenuOpenInTabOptions(menu: ContextMenu) {
        menu.add(0, CONTEXT_MENU_ID_OPEN_IMAGE_IN_NEW_BACKGROUND_TAB, CONTEXT_MENU_ID_OPEN_IMAGE_IN_NEW_BACKGROUND_TAB, R.string.openImageInNewTab)
    }

    private fun addLinkMenuOpenInTabOptions(menu: ContextMenu) {
        menu.add(0, CONTEXT_MENU_ID_OPEN_IN_NEW_TAB, CONTEXT_MENU_ID_OPEN_IN_NEW_TAB, R.string.openInNewTab)
        menu.add(0, CONTEXT_MENU_ID_OPEN_IN_NEW_BACKGROUND_TAB, CONTEXT_MENU_ID_OPEN_IN_NEW_BACKGROUND_TAB, R.string.openInNewBackgroundTab)
    }

    private fun addLinkMenuOtherOptions(menu: ContextMenu) {
        menu.add(0, CONTEXT_MENU_ID_COPY, CONTEXT_MENU_ID_COPY, R.string.copyUrl)
        menu.add(0, CONTEXT_MENU_ID_SHARE_LINK, CONTEXT_MENU_ID_SHARE_LINK, R.string.shareLink)
    }

    private fun isLinkSupported(longPressTargetUrl: String?) = URLUtil.isNetworkUrl(longPressTargetUrl) || URLUtil.isDataUrl(longPressTargetUrl)

    override fun userSelectedMenuItem(
        longPressTarget: LongPressTarget,
        item: MenuItem,
    ): RequiredAction {
        return when (item.itemId) {
            CONTEXT_MENU_ID_OPEN_IN_NEW_TAB -> {
                pixel.fire(LONG_PRESS_NEW_TAB)
                val url = longPressTarget.url ?: return None
                return OpenInNewTab(url)
            }
            CONTEXT_MENU_ID_OPEN_IN_NEW_BACKGROUND_TAB -> {
                pixel.fire(LONG_PRESS_NEW_BACKGROUND_TAB)
                val url = longPressTarget.url ?: return None
                return OpenInNewBackgroundTab(url)
            }
            CONTEXT_MENU_ID_DOWNLOAD_IMAGE -> {
                pixel.fire(LONG_PRESS_DOWNLOAD_IMAGE)
                val url = longPressTarget.imageUrl ?: return None
                return DownloadFile(url)
            }
            CONTEXT_MENU_ID_OPEN_IMAGE_IN_NEW_BACKGROUND_TAB -> {
                pixel.fire(LONG_PRESS_OPEN_IMAGE_IN_BACKGROUND_TAB)
                val url = longPressTarget.imageUrl ?: return None
                return OpenInNewBackgroundTab(url)
            }
            CONTEXT_MENU_ID_SHARE_LINK -> {
                pixel.fire(LONG_PRESS_SHARE)
                val url = longPressTarget.url ?: return None
                return ShareLink(url)
            }
            CONTEXT_MENU_ID_COPY -> {
                pixel.fire(LONG_PRESS_COPY_URL)
                val url = longPressTarget.url ?: return None
                return CopyLink(url)
            }
            else -> None
        }
    }

    companion object {
        const val CONTEXT_MENU_ID_OPEN_IN_NEW_TAB = 1
        const val CONTEXT_MENU_ID_OPEN_IN_NEW_BACKGROUND_TAB = 2
        const val CONTEXT_MENU_ID_COPY = 3
        const val CONTEXT_MENU_ID_SHARE_LINK = 4
        const val CONTEXT_MENU_ID_DOWNLOAD_IMAGE = 5
        const val CONTEXT_MENU_ID_OPEN_IMAGE_IN_NEW_BACKGROUND_TAB = 6

        private const val MAX_TITLE_LENGTH = 100
    }
}
