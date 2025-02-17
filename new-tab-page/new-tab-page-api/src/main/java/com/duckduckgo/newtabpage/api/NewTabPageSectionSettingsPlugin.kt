package com.duckduckgo.newtabpage.api

import android.content.Context
import android.view.View

/**
 * This class is used to provide each of the Sections that build the New Tab Page
 * Implementation of https://app.asana.com/0/1174433894299346/12070643725750
 */

interface NewTabPageSectionSettingsPlugin {

    /** Name of the focused view version */
    val name: String

    /**
     * This method returns a [View] that will be used as the NewTabPage content
     * @return [View]
     */
    fun getView(context: Context): View?

    /**
     * This method returns a [Boolean] that represents if the plugin should be visible in New Tab Settings
     * Specially built for AppTP Setting. We don't want to show the Setting unless AppTP has ever been enabled
     * @return [View]
     */
    suspend fun isActive(): Boolean

    // Every time you want to add a setting add the priority (order) to the list below and use it in the plugin
    companion object {
        const val APP_TRACKING_PROTECTION = 100
        const val FAVOURITES = 200
        const val SHORTCUTS = 300
    }
}
