

package com.duckduckgo.voice.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultCaller
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.voice.impl.ActivityResultLauncherWrapper.Action.LaunchPermissionRequest
import com.duckduckgo.voice.impl.ActivityResultLauncherWrapper.Request
import com.duckduckgo.voice.store.VoiceSearchRepository
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface PermissionRequest {
    fun registerResultsCallback(
        caller: ActivityResultCaller,
        activity: Activity,
        onPermissionsGranted: () -> Unit,
        onVoiceSearchDisabled: () -> Unit = {},
    )

    fun launch(activity: Activity)
}

@ContributesBinding(ActivityScope::class)
class MicrophonePermissionRequest @Inject constructor(
    private val pixel: Pixel,
    private val voiceSearchRepository: VoiceSearchRepository,
    private val voiceSearchPermissionDialogsLauncher: VoiceSearchPermissionDialogsLauncher,
    private val activityResultLauncherWrapper: ActivityResultLauncherWrapper,
    private val permissionRationale: PermissionRationale,
) : PermissionRequest {
    companion object {
        private const val SCHEME_PACKAGE = "package"
    }

    private lateinit var voiceSearchDisabled: () -> Unit

    override fun registerResultsCallback(
        caller: ActivityResultCaller,
        activity: Activity,
        onPermissionsGranted: () -> Unit,
        onVoiceSearchDisabled: () -> Unit,
    ) {
        activityResultLauncherWrapper.register(
            caller,
            Request.Permission { result ->
                if (result) {
                    onPermissionsGranted()
                } else {
                    if (!permissionRationale.shouldShow(activity)) {
                        voiceSearchRepository.declinePermissionForever()
                    }
                }
            },
        )
        voiceSearchDisabled = onVoiceSearchDisabled
    }

    override fun launch(activity: Activity) {
        if (voiceSearchRepository.getHasPermissionDeclinedForever()) {
            voiceSearchPermissionDialogsLauncher.showNoMicAccessDialog(
                activity,
                { activity.launchDuckDuckGoSettings() },
                { showRemoveVoiceSearchDialog(activity) },
            )
        } else {
            if (voiceSearchRepository.getHasAcceptedRationaleDialog()) {
                activityResultLauncherWrapper.launch(LaunchPermissionRequest)
            } else {
                voiceSearchPermissionDialogsLauncher.showPermissionRationale(
                    activity,
                    { handleRationaleAccepted() },
                    { handleRationaleCancelled(activity) },
                )
            }
        }
    }

    private fun Context.launchDuckDuckGoSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts(SCHEME_PACKAGE, packageName, null)
        }
        startActivity(intent)
    }

    private fun handleRationaleAccepted() {
        pixel.fire(VoiceSearchPixelNames.VOICE_SEARCH_PRIVACY_DIALOG_ACCEPTED)
        voiceSearchRepository.acceptRationaleDialog()
        activityResultLauncherWrapper.launch(LaunchPermissionRequest)
    }

    private fun handleRationaleCancelled(context: Context) {
        pixel.fire(VoiceSearchPixelNames.VOICE_SEARCH_PRIVACY_DIALOG_REJECTED)
        showRemoveVoiceSearchDialog(context)
    }

    private fun showRemoveVoiceSearchDialog(context: Context) {
        voiceSearchPermissionDialogsLauncher.showRemoveVoiceSearchDialog(
            context,
            onRemoveVoiceSearch = {
                voiceSearchRepository.setVoiceSearchUserEnabled(false)
                voiceSearchDisabled()
            },
        )
    }
}
