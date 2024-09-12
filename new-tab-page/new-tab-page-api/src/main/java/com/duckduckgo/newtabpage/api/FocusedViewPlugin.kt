package com.duckduckgo.newtabpage.api

import android.content.Context
import android.view.View
import com.duckduckgo.common.utils.plugins.ActivePlugin

/**
 * This class is used to provide one of the two different version of FocusedView
 * Legacy -> What existed before https://app.asana.com/0/1174433894299346/1207064372575037
 * New -> Implementation of https://app.asana.com/0/1174433894299346/1207064372575037
 */
interface FocusedViewPlugin : ActivePlugin {

    /**
     * This method returns a [View] that will be used as the Focused View content
     * @return [View]
     */
    fun getView(context: Context): View

    companion object {
        const val PRIORITY_NEW_FOCUSED_PAGE = 0
        const val PRIORITY_LEGACY_FOCUSED_PAGE = 100
    }
}
