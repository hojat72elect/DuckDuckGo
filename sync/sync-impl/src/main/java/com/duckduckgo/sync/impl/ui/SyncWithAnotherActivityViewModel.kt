

package com.duckduckgo.sync.impl.ui

import android.graphics.Bitmap
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
import com.duckduckgo.sync.impl.Clipboard
import com.duckduckgo.sync.impl.QREncoder
import com.duckduckgo.sync.impl.R
import com.duckduckgo.sync.impl.R.dimen
import com.duckduckgo.sync.impl.R.string
import com.duckduckgo.sync.impl.Result.Error
import com.duckduckgo.sync.impl.Result.Success
import com.duckduckgo.sync.impl.SyncAccountRepository
import com.duckduckgo.sync.impl.getOrNull
import com.duckduckgo.sync.impl.onFailure
import com.duckduckgo.sync.impl.onSuccess
import com.duckduckgo.sync.impl.pixels.SyncPixels
import com.duckduckgo.sync.impl.ui.SyncWithAnotherActivityViewModel.Command.FinishWithError
import com.duckduckgo.sync.impl.ui.SyncWithAnotherActivityViewModel.Command.LoginSuccess
import com.duckduckgo.sync.impl.ui.SyncWithAnotherActivityViewModel.Command.ReadTextCode
import com.duckduckgo.sync.impl.ui.SyncWithAnotherActivityViewModel.Command.ShowError
import com.duckduckgo.sync.impl.ui.SyncWithAnotherActivityViewModel.Command.ShowMessage
import javax.inject.*
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ContributesViewModel(ActivityScope::class)
class SyncWithAnotherActivityViewModel @Inject constructor(
    private val syncAccountRepository: SyncAccountRepository,
    private val qrEncoder: QREncoder,
    private val clipboard: Clipboard,
    private val syncPixels: SyncPixels,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {
    private val command = Channel<Command>(1, DROP_OLDEST)
    fun commands(): Flow<Command> = command.receiveAsFlow()

    private val viewState = MutableStateFlow(ViewState())
    fun viewState(): Flow<ViewState> = viewState.onStart {
        generateQRCode()
    }

    private fun generateQRCode() {
        viewModelScope.launch(dispatchers.io()) {
            showQRCode()
        }
    }

    private suspend fun showQRCode() {
        syncAccountRepository.getRecoveryCode()
            .onSuccess { connectQR ->
                val qrBitmap = withContext(dispatchers.io()) {
                    qrEncoder.encodeAsBitmap(connectQR, dimen.qrSizeSmall, dimen.qrSizeSmall)
                }
                viewState.emit(viewState.value.copy(qrCodeBitmap = qrBitmap))
            }.onFailure {
                command.send(Command.FinishWithError)
            }
    }

    fun onErrorDialogDismissed() {
        viewModelScope.launch(dispatchers.io()) {
            command.send(FinishWithError)
        }
    }

    fun onCopyCodeClicked() {
        viewModelScope.launch(dispatchers.io()) {
            syncAccountRepository.getRecoveryCode().getOrNull()?.let { code ->
                clipboard.copyToClipboard(code)
                command.send(ShowMessage(string.sync_code_copied_message))
            } ?: command.send(FinishWithError)
        }
    }

    data class ViewState(
        val qrCodeBitmap: Bitmap? = null,
    )

    sealed class Command {
        object ReadTextCode : Command()
        object LoginSuccess : Command()
        data class ShowMessage(val messageId: Int) : Command()
        object FinishWithError : Command()
        data class ShowError(@StringRes val message: Int, val reason: String = "") : Command()
    }

    fun onReadTextCodeClicked() {
        viewModelScope.launch {
            command.send(ReadTextCode)
        }
    }

    fun onQRCodeScanned(qrCode: String) {
        viewModelScope.launch(dispatchers.io()) {
            when (val result = syncAccountRepository.processCode(qrCode)) {
                is Error -> {
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
                }

                is Success -> {
                    syncPixels.fireLoginPixel()
                    command.send(LoginSuccess)
                }
            }
        }
    }

    fun onLoginSuccess() {
        viewModelScope.launch {
            syncPixels.fireLoginPixel()
            command.send(LoginSuccess)
        }
    }
}
