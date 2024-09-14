

package com.duckduckgo.app.browser

import com.duckduckgo.app.browser.viewstate.BrowserViewState
import io.reactivex.annotations.CheckReturnValue

class BrowserStateModifier {

    @CheckReturnValue
    fun copyForBrowserShowing(original: BrowserViewState): BrowserViewState {
        return original.copy(
            browserShowing = true,
            canChangePrivacyProtection = true,
            canFireproofSite = true,
            canReportSite = true,
            canSharePage = true,
            canSaveSite = true,
            canChangeBrowsingMode = true,
            canFindInPage = true,
            addToHomeEnabled = true,
            canPrintPage = true,
        )
    }

    @CheckReturnValue
    fun copyForHomeShowing(original: BrowserViewState): BrowserViewState {
        return original.copy(
            browserShowing = false,
            canChangePrivacyProtection = false,
            canFireproofSite = false,
            canReportSite = false,
            canSharePage = false,
            canSaveSite = false,
            canFindInPage = false,
            canChangeBrowsingMode = false,
            addToHomeEnabled = false,
            canGoBack = false,
            canPrintPage = false,
        )
    }
}
