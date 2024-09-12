package com.duckduckgo.app.browser.viewstate

data class OmnibarViewState(
    val omnibarText: String = "",
    val isEditing: Boolean = false,
    val shouldMoveCaretToEnd: Boolean = false,
    val forceExpand: Boolean = true,
)
