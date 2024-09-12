package com.duckduckgo.app.tabs.model

data class TabSwitcherData(
    val userState: UserState,
    val wasAnnouncementDismissed: Boolean,
    val announcementDisplayCount: Int,
    val layoutType: LayoutType,
) {
    enum class UserState {
        NEW,
        EXISTING,
        UNKNOWN,
    }

    enum class LayoutType {
        GRID, LIST
    }
}
