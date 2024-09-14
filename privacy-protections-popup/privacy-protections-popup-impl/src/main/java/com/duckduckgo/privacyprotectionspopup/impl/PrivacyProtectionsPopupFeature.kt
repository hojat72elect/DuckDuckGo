

package com.duckduckgo.privacyprotectionspopup.impl

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.feature.toggles.api.Toggle.DefaultValue
import com.duckduckgo.feature.toggles.api.Toggle.InternalAlwaysEnabled

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "privacyProtectionsPopup",
)
interface PrivacyProtectionsPopupFeature {
    @DefaultValue(false)
    @InternalAlwaysEnabled
    fun self(): Toggle
}

fun PrivacyProtectionsPopupFeature.isEnabled() = self().isEnabled()
