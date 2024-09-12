package com.duckduckgo.autofill.sync

import com.duckduckgo.autofill.sync.provider.CredentialsSyncLocalValidationFeature
import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.feature.toggles.api.toggle.TestToggle

class FakeCredentialsSyncLocalValidationFeature : CredentialsSyncLocalValidationFeature {
    var enabled = true

    override fun self(): Toggle = TestToggle(enabled)
}
