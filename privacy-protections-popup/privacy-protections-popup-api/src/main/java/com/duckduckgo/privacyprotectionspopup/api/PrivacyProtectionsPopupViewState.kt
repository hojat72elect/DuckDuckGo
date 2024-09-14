

package com.duckduckgo.privacyprotectionspopup.api

/**
 * Represents the view state of the Privacy Protections Popup
 * Should be emitted by [PrivacyProtectionsPopupManager] and consumed by [PrivacyProtectionsPopup]
 */
sealed class PrivacyProtectionsPopupViewState {

    data class Visible(
        /**
         * Indicates whether the popup should show the "Don't show again" button.
         */
        val doNotShowAgainOptionAvailable: Boolean,
    ) : PrivacyProtectionsPopupViewState()

    data object Gone : PrivacyProtectionsPopupViewState()
}
