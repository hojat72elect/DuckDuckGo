

package com.duckduckgo.feature.toggles.api.toggle

import com.duckduckgo.autofill.api.AutofillFeature
import com.duckduckgo.autofill.impl.reporting.remoteconfig.AutofillSiteBreakageReportingFeature
import com.duckduckgo.feature.toggles.api.Toggle

class AutofillTestFeature : AutofillFeature {
    var topLevelFeatureEnabled: Boolean = false
    var canInjectCredentials: Boolean = false
    var canSaveCredentials: Boolean = false
    var canGeneratePassword: Boolean = false
    var canAccessCredentialManagement: Boolean = false
    var onByDefault: Boolean = false
    var canIntegrateWithWebView: Boolean = false
    var onForExistingUsers: Boolean = false
    var showDisableDialogAutofillPrompt: Boolean = false

    override fun self(): Toggle = TestToggle(topLevelFeatureEnabled)
    override fun canInjectCredentials(): Toggle = TestToggle(canInjectCredentials)
    override fun canIntegrateAutofillInWebView() = TestToggle(canIntegrateWithWebView)
    override fun canSaveCredentials(): Toggle = TestToggle(canSaveCredentials)
    override fun canGeneratePasswords(): Toggle = TestToggle(canGeneratePassword)
    override fun canAccessCredentialManagement(): Toggle = TestToggle(canAccessCredentialManagement)
    override fun onByDefault(): Toggle = TestToggle(onByDefault)
    override fun onForExistingUsers(): Toggle = TestToggle(onForExistingUsers)
    override fun showDisableDialogAutofillPrompt(): Toggle = TestToggle(showDisableDialogAutofillPrompt)
}

class AutofillReportBreakageTestFeature : AutofillSiteBreakageReportingFeature {
    var topLevelFeatureEnabled: Boolean = false

    override fun self() = TestToggle(topLevelFeatureEnabled)
}

open class TestToggle(val enabled: Boolean) : Toggle {
    override fun getRawStoredState(): Toggle.State? = null
    override fun setEnabled(state: Toggle.State) {}
    override fun isEnabled(): Boolean = enabled
}
