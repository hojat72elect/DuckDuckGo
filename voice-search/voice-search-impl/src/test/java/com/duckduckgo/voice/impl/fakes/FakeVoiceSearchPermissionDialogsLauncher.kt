

package com.duckduckgo.voice.impl.fakes

import android.content.Context
import com.duckduckgo.voice.impl.VoiceSearchPermissionDialogsLauncher

class FakeVoiceSearchPermissionDialogsLauncher : VoiceSearchPermissionDialogsLauncher {
    var noMicAccessDialogShown = false
    var rationaleDialogShown = false
    var removeVoiceSearchDialogShown = false
    var boundOnRationaleAccepted: () -> Unit = {}
    var boundOnRationaleDeclined: () -> Unit = {}
    var boundNoMicAccessDialogDeclined: () -> Unit = {}
    var boundRemoveVoiceSearchAccepted: () -> Unit = {}

    override fun showNoMicAccessDialog(
        context: Context,
        onSettingsLaunchSelected: () -> Unit,
        onSettingsLaunchDeclined: () -> Unit,
    ) {
        noMicAccessDialogShown = true
        boundNoMicAccessDialogDeclined = onSettingsLaunchDeclined
    }

    override fun showPermissionRationale(
        context: Context,
        onRationaleAccepted: () -> Unit,
        onRationaleDeclined: () -> Unit,
    ) {
        rationaleDialogShown = true
        boundOnRationaleAccepted = onRationaleAccepted
        boundOnRationaleDeclined = onRationaleDeclined
    }

    override fun showRemoveVoiceSearchDialog(
        context: Context,
        onRemoveVoiceSearch: () -> Unit,
        onRemoveVoiceSearchCancelled: () -> Unit,
    ) {
        removeVoiceSearchDialogShown = true
        boundRemoveVoiceSearchAccepted = onRemoveVoiceSearch
    }
}
