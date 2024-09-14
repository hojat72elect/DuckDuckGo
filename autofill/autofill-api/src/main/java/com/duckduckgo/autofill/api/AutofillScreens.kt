

package com.duckduckgo.autofill.api

import com.duckduckgo.autofill.api.domain.app.LoginCredentials
import com.duckduckgo.navigation.api.GlobalActivityStarter.ActivityParams

sealed interface AutofillScreens {

    /**
     * Launch the Autofill management activity, which will show the full list of available credentials
     * @param source is used to indicate from where in the app Autofill management activity was launched
     */
    data class AutofillSettingsScreen(val source: AutofillSettingsLaunchSource) : ActivityParams

    /**
     * Launch the Autofill management activity, which will show suggestions for the current url and the full list of available credentials
     * @param currentUrl The current URL the user is viewing. This is used to show suggestions for the current site if available.
     * @param source is used to indicate from where in the app Autofill management activity was launched
     * @param privacyProtectionEnabled whether privacy protection is enabled for the current web site
     */
    data class AutofillSettingsScreenShowSuggestionsForSiteParams(
        val currentUrl: String?,
        val source: AutofillSettingsLaunchSource,
        val privacyProtectionEnabled: Boolean,
    ) : ActivityParams

    /**
     * Launch the Autofill management activity, directly showing particular credentials
     * @param loginCredentials jump directly into viewing mode for these credentials
     * @param source is used to indicate from where in the app Autofill management activity was launched
     */
    data class AutofillSettingsScreenDirectlyViewCredentialsParams(
        val loginCredentials: LoginCredentials,
        val source: AutofillSettingsLaunchSource,
    ) : ActivityParams
}

enum class AutofillSettingsLaunchSource {
    SettingsActivity,
    BrowserOverflow,
    Sync,
    BrowserSnackbar,
    InternalDevSettings,
    Unknown,
    NewTabShortcut,
    DisableInSettingsPrompt,
}
