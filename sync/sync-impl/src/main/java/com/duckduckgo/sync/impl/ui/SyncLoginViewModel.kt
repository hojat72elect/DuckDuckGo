

package com.duckduckgo.sync.impl.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.sync.impl.AccountErrorCodes.ALREADY_SIGNED_IN
import com.duckduckgo.sync.impl.AccountErrorCodes.CONNECT_FAILED
import com.duckduckgo.sync.impl.AccountErrorCodes.CREATE_ACCOUNT_FAILED
import com.duckduckgo.sync.impl.AccountErrorCodes.INVALID_CODE
import com.duckduckgo.sync.impl.AccountErrorCodes.LOGIN_FAILED
import com.duckduckgo.sync.impl.R
import com.duckduckgo.sync.impl.Result.Error
import com.duckduckgo.sync.impl.SyncAccountRepository
import com.duckduckgo.sync.impl.pixels.SyncPixels
import com.duckduckgo.sync.impl.ui.SyncConnectViewModel.Command
import com.duckduckgo.sync.impl.ui.SyncLoginViewModel.Command.LoginSucess
import com.duckduckgo.sync.impl.ui.SyncLoginViewModel.Command.ReadTextCode
import com.duckduckgo.sync.impl.ui.SyncLoginViewModel.Command.ShowError
import javax.inject.*
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@ContributesViewModel(ActivityScope::class)
class SyncLoginViewModel @Inject constructor(
    private val syncAccountRepository: SyncAccountRepository,
    private val syncPixels: SyncPixels,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {
    private val command = Channel<Command>(1, DROP_OLDEST)
    fun commands(): Flow<Command> = command.receiveAsFlow()

    sealed class Command {
        object ReadTextCode : Command()
        object LoginSucess : Command()
        object Error : Command()
        data class ShowError(@StringRes val message: Int, val reason: String = "") : Command()
    }

    fun onReadTextCodeClicked() {
        viewModelScope.launch {
            command.send(ReadTextCode)
        }
    }

    fun onLoginSuccess() {
        viewModelScope.launch {
            syncPixels.fireLoginPixel()
            command.send(LoginSucess)
        }
    }

    fun onErrorDialogDismissed() {
        viewModelScope.launch(dispatchers.io()) {
            command.send(Command.Error)
        }
    }

    fun onQRCodeScanned(qrCode: String) {
        viewModelScope.launch(dispatchers.io()) {
            val result = syncAccountRepository.processCode(qrCode)
            if (result is Error) {
                when (result.code) {
                    ALREADY_SIGNED_IN.code -> R.string.sync_login_authenticated_device_error
                    LOGIN_FAILED.code -> R.string.sync_connect_login_error
                    CONNECT_FAILED.code -> R.string.sync_connect_generic_error
                    CREATE_ACCOUNT_FAILED.code -> R.string.sync_create_account_generic_error
                    INVALID_CODE.code -> R.string.sync_invalid_code_error
                    else -> null
                }?.let { message ->
                    command.send(ShowError(message = message, reason = result.reason))
                }
            } else {
                syncPixels.fireLoginPixel()
                command.send(LoginSucess)
            }
        }
    }
}
