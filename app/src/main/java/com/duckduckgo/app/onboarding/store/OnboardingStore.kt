

package com.duckduckgo.app.onboarding.store

import com.duckduckgo.app.cta.ui.DaxBubbleCta.DaxDialogIntroOption

interface OnboardingStore {
    var onboardingDialogJourney: String?
    fun getSearchOptions(): List<DaxDialogIntroOption>
    fun getSitesOptions(): List<DaxDialogIntroOption>
}
