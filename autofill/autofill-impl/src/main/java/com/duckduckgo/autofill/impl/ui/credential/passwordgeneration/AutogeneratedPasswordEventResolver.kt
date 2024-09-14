

package com.duckduckgo.autofill.impl.ui.credential.passwordgeneration

import com.duckduckgo.autofill.api.domain.app.LoginCredentials
import com.duckduckgo.autofill.impl.ui.credential.passwordgeneration.Actions.DeleteAutoLogin
import com.duckduckgo.autofill.impl.ui.credential.passwordgeneration.Actions.DiscardAutoLoginId
import com.duckduckgo.autofill.impl.ui.credential.passwordgeneration.Actions.PromptToSave
import com.duckduckgo.autofill.impl.ui.credential.passwordgeneration.Actions.UpdateSavedAutoLogin
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface AutogeneratedPasswordEventResolver {
    fun decideActions(
        autoSavedLogin: LoginCredentials?,
        autogenerated: Boolean,
    ): List<Actions>
}

sealed interface Actions {
    object PromptToSave : Actions
    data class UpdateSavedAutoLogin(val autologinId: Long) : Actions
    data class DeleteAutoLogin(val autologinId: Long) : Actions
    object DiscardAutoLoginId : Actions
}

@ContributesBinding(AppScope::class)
class PasswordEventResolver @Inject constructor() : AutogeneratedPasswordEventResolver {

    override fun decideActions(
        autoSavedLogin: LoginCredentials?,
        autogenerated: Boolean,
    ): List<Actions> {
        val outcomes = mutableListOf<Actions>()
        val autoSavedLoginId = autoSavedLogin?.id

        if (autoSavedLoginId == null) {
            outcomes.add(PromptToSave)
        } else if (autogenerated) {
            outcomes.add(UpdateSavedAutoLogin(autoSavedLoginId))
        } else {
            outcomes.add(DeleteAutoLogin(autoSavedLoginId))
            outcomes.add(DiscardAutoLoginId)
            outcomes.add(PromptToSave)
        }

        return outcomes
    }
}
