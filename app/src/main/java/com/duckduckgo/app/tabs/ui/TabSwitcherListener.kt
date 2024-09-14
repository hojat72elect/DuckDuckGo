

package com.duckduckgo.app.tabs.ui

import com.duckduckgo.app.tabs.model.TabEntity

interface TabSwitcherListener {
    fun onNewTabRequested(fromOverflowMenu: Boolean)
    fun onTabSelected(tab: TabEntity)
    fun onTabDeleted(position: Int, deletedBySwipe: Boolean)
    fun onTabMoved(from: Int, to: Int)
}
