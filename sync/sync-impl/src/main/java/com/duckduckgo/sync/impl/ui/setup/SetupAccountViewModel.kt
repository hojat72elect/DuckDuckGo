

package com.duckduckgo.sync.impl.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.sync.impl.ui.setup.SetupAccountActivity.Companion.Screen
import com.duckduckgo.sync.impl.ui.setup.SetupAccountActivity.Companion.Screen.RECOVERY_CODE
import com.duckduckgo.sync.impl.ui.setup.SetupAccountActivity.Companion.Screen.RECOVERY_INTRO
import com.duckduckgo.sync.impl.ui.setup.SetupAccountActivity.Companion.Screen.SETUP_COMPLETE
import com.duckduckgo.sync.impl.ui.setup.SetupAccountActivity.Companion.Screen.SYNC_INTRO
import com.duckduckgo.sync.impl.ui.setup.SetupAccountActivity.Companion.Screen.SYNC_SETUP
import com.duckduckgo.sync.impl.ui.setup.SetupAccountViewModel.Command.Close
import com.duckduckgo.sync.impl.ui.setup.SetupAccountViewModel.Command.Finish
import com.duckduckgo.sync.impl.ui.setup.SetupAccountViewModel.Command.RecoverAccount
import com.duckduckgo.sync.impl.ui.setup.SetupAccountViewModel.ViewMode.AskSaveRecoveryCode
import com.duckduckgo.sync.impl.ui.setup.SetupAccountViewModel.ViewMode.CreateAccount
import com.duckduckgo.sync.impl.ui.setup.SetupAccountViewModel.ViewMode.IntroCreateAccount
import com.duckduckgo.sync.impl.ui.setup.SetupAccountViewModel.ViewMode.IntroRecoveryCode
import com.duckduckgo.sync.impl.ui.setup.SetupAccountViewModel.ViewMode.SyncSetupCompleted
import javax.inject.*
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@ContributesViewModel(ActivityScope::class)
class SetupAccountViewModel @Inject constructor(private val dispatchers: DispatcherProvider) : ViewModel() {

    private val command = Channel<Command>(1, DROP_OLDEST)
    private val viewState = MutableStateFlow(ViewState())
    private var initialStateProcessed = false

    fun viewState(screen: Screen): Flow<ViewState> = viewState.onStart {
        if (!initialStateProcessed) {
            val viewMode = when (screen) {
                SYNC_SETUP -> CreateAccount
                SETUP_COMPLETE -> SyncSetupCompleted
                RECOVERY_CODE -> AskSaveRecoveryCode
                SYNC_INTRO -> IntroCreateAccount
                RECOVERY_INTRO -> IntroRecoveryCode
            }
            viewState.emit(ViewState(viewMode))
            initialStateProcessed = true
        }
    }

    fun commands(): Flow<Command> = command.receiveAsFlow()

    data class ViewState(
        val viewMode: ViewMode = SyncSetupCompleted,
    )

    sealed class ViewMode {
        object CreateAccount : ViewMode()
        object AskSaveRecoveryCode : ViewMode()
        object SyncSetupCompleted : ViewMode()

        object IntroCreateAccount : ViewMode()
        object IntroRecoveryCode : ViewMode()
    }

    sealed class Command {
        object RecoverAccount : Command()
        object Finish : Command()
        object Close : Command()
    }

    fun onBackPressed() {
        viewModelScope.launch(dispatchers.main()) {
            command.send(Close)
        }
    }

    fun onCreateAccount() {
        viewModelScope.launch {
            viewState.emit(ViewState(viewMode = CreateAccount))
        }
    }

    fun onRecoverAccount() {
        viewModelScope.launch {
            command.send(RecoverAccount)
        }
    }

    fun onDeviceConnected() {
        viewModelScope.launch {
            viewState.emit(ViewState(viewMode = SyncSetupCompleted))
        }
    }

    fun onSetupFinished() {
        viewModelScope.launch {
            command.send(Finish)
        }
    }

    fun onRecoveryCodePrompt() {
        viewModelScope.launch {
            viewState.emit(ViewState(viewMode = AskSaveRecoveryCode))
        }
    }
}
