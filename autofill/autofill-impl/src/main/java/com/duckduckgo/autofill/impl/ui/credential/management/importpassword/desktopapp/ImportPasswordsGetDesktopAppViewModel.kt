

package com.duckduckgo.autofill.impl.ui.credential.management.importpassword.desktopapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.autofill.impl.pixel.AutofillPixelNames.AUTOFILL_IMPORT_PASSWORDS_COPIED_DESKTOP_LINK
import com.duckduckgo.autofill.impl.pixel.AutofillPixelNames.AUTOFILL_IMPORT_PASSWORDS_SHARED_DESKTOP_LINK
import com.duckduckgo.autofill.impl.ui.credential.management.AutofillClipboardInteractor
import com.duckduckgo.autofill.impl.ui.credential.management.importpassword.desktopapp.ImportPasswordsGetDesktopAppViewModel.Command.ShareLink
import com.duckduckgo.autofill.impl.ui.credential.management.importpassword.desktopapp.ImportPasswordsGetDesktopAppViewModel.Command.ShowCopiedNotification
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@ContributesViewModel(AppScope::class)
class ImportPasswordsGetDesktopAppViewModel @Inject constructor(
    private val pixel: Pixel,
    private val dispatchers: DispatcherProvider,
    private val autofillClipboardInteractor: AutofillClipboardInteractor,
) : ViewModel() {

    private val commandChannel = Channel<Command>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val commands = commandChannel.receiveAsFlow()

    sealed class Command {
        data class ShareLink(val link: String) : Command()
        data object ShowCopiedNotification : Command()
    }

    data class ViewState(val windowsFeatureEnabled: Boolean)

    fun onShareClicked() {
        viewModelScope.launch {
            commandChannel.send(ShareLink(buildLink()))

            pixel.fire(AUTOFILL_IMPORT_PASSWORDS_SHARED_DESKTOP_LINK)
        }
    }

    fun onLinkClicked() {
        viewModelScope.launch(dispatchers.io()) {
            autofillClipboardInteractor.copyToClipboard(buildLink(), isSensitive = false)

            if (autofillClipboardInteractor.shouldShowCopyNotification()) {
                commandChannel.send(ShowCopiedNotification)
            }

            pixel.fire(AUTOFILL_IMPORT_PASSWORDS_COPIED_DESKTOP_LINK)
        }
    }

    private fun buildLink(): String {
        return "$BASE_LINK?$ATTRIBUTION"
    }

    companion object {
        private const val BASE_LINK = "https://duckduckgo.com/browser"
        private const val ATTRIBUTION = "origin=funnel_browser_android_sync"
    }
}
