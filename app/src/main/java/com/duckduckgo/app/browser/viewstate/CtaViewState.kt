package com.duckduckgo.app.browser.viewstate

import com.duckduckgo.app.cta.ui.Cta

data class CtaViewState(
    val cta: Cta? = null,
    val daxOnboardingComplete: Boolean = false,
    val isBrowserShowing: Boolean = true,
)
