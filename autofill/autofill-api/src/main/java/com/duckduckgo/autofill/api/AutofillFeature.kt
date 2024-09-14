

package com.duckduckgo.autofill.api

import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.feature.toggles.api.Toggle.InternalAlwaysEnabled

/**
 * This is the class that represents the autofill feature flags
 */
interface AutofillFeature {
    /**
     * @return `true` when the remote config has the global "autofill" feature flag enabled
     * If the remote feature is not present defaults to `true`
     */
    @Toggle.DefaultValue(true)
    fun self(): Toggle

    /**
     * Kill switch for if we should inject Autofill javascript into the browser.
     *
     * @return `true` when the remote config has the global "canIntegrateAutofillInWebView" autofill sub-feature flag enabled
     * If the remote feature is not present defaults to `true`
     */
    @Toggle.DefaultValue(true)
    fun canIntegrateAutofillInWebView(): Toggle

    /**
     * @return `true` when the remote config has the global "canInjectCredentials" autofill sub-feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    fun canInjectCredentials(): Toggle

    /**
     * @return `true` when the remote config has the global "canSaveCredentials" autofill sub-feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    fun canSaveCredentials(): Toggle

    /**
     * @return `true` when the remote config has the global "canGeneratePasswords" autofill sub-feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    fun canGeneratePasswords(): Toggle

    /**
     * @return `true` when the remote config has the global "canAccessCredentialManagement" autofill sub-feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    fun canAccessCredentialManagement(): Toggle

    /**
     * @return `true` when the remote config has the global "onByDefault" autofill sub-feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    fun onByDefault(): Toggle

    /**
     * Remote Flag to control logic that decides if existing users that had autofill disabled by default, should have it enabled
     * @return `true` when the remote config has the global "onForExistingUsers" autofill sub-feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    @InternalAlwaysEnabled
    fun onForExistingUsers(): Toggle

    /**
     * Remote Flag that enables the old dialog prompt to disable autofill
     * @return `true` when the remote config has the global "allowToDisableAutofill" autofill sub-feature flag enabled
     * If the remote feature is not present defaults to `false`
     */
    @Toggle.DefaultValue(false)
    fun showDisableDialogAutofillPrompt(): Toggle
}
