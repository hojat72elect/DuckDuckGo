

package com.duckduckgo.app.launch

import androidx.lifecycle.ViewModel
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.app.onboarding.store.UserStageStore
import com.duckduckgo.app.onboarding.store.isNewUser
import com.duckduckgo.app.referral.AppInstallationReferrerStateListener
import com.duckduckgo.app.referral.AppInstallationReferrerStateListener.Companion.MAX_REFERRER_WAIT_TIME_MS
import com.duckduckgo.common.utils.SingleLiveEvent
import com.duckduckgo.di.scopes.ActivityScope
import javax.inject.Inject
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

@ContributesViewModel(ActivityScope::class)
class LaunchViewModel @Inject constructor(
    private val userStageStore: UserStageStore,
    private val appReferrerStateListener: AppInstallationReferrerStateListener,
) :
    ViewModel() {

    val command: SingleLiveEvent<Command> = SingleLiveEvent()

    sealed class Command {
        data object Onboarding : Command()
        data class Home(val replaceExistingSearch: Boolean = false) : Command()
    }

    suspend fun determineViewToShow() {
        waitForReferrerData()

        if (userStageStore.isNewUser()) {
            command.value = Command.Onboarding
        } else {
            command.value = Command.Home()
        }
    }

    private suspend fun waitForReferrerData() {
        val startTime = System.currentTimeMillis()

        withTimeoutOrNull(MAX_REFERRER_WAIT_TIME_MS) {
            Timber.d("Waiting for referrer")
            return@withTimeoutOrNull appReferrerStateListener.waitForReferrerCode()
        }

        Timber.d("Waited ${System.currentTimeMillis() - startTime}ms for referrer")
    }
}
