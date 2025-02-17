

package com.duckduckgo.voice.impl

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.voice.impl.ActivityResultLauncherWrapper.Action
import com.duckduckgo.voice.impl.ActivityResultLauncherWrapper.Action.LaunchPermissionRequest
import com.duckduckgo.voice.impl.ActivityResultLauncherWrapper.Action.LaunchVoiceSearch
import com.duckduckgo.voice.impl.ActivityResultLauncherWrapper.Request
import com.duckduckgo.voice.impl.ActivityResultLauncherWrapper.Request.Permission
import com.duckduckgo.voice.impl.ActivityResultLauncherWrapper.Request.ResultFromVoiceSearch
import com.duckduckgo.voice.impl.listeningmode.VoiceSearchActivity
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface ActivityResultLauncherWrapper {
    fun register(
        caller: ActivityResultCaller,
        request: Request,
    )

    fun launch(action: Action)

    sealed class Request {
        data class Permission(val onResult: (Boolean) -> Unit) : Request()
        data class ResultFromVoiceSearch(
            val onResult: (Int, String) -> Unit,
        ) : Request()
    }

    enum class Action {
        LaunchPermissionRequest,
        LaunchVoiceSearch,
    }
}

@ContributesBinding(ActivityScope::class)
class RealActivityResultLauncherWrapper @Inject constructor(
    private val context: Context,
) : ActivityResultLauncherWrapper {

    private var permissionLauncher: ActivityResultLauncher<String>? = null
    private lateinit var voiceSearchActivityLaucher: ActivityResultLauncher<Intent>

    override fun register(
        caller: ActivityResultCaller,
        request: Request,
    ) {
        when (request) {
            is Permission -> registerPermissionRequest(caller, request.onResult)
            is ResultFromVoiceSearch -> registerResultFromVoiceSearch(caller, request.onResult)
        }
    }

    private fun registerResultFromVoiceSearch(
        caller: ActivityResultCaller,
        onResult: (Int, String) -> Unit,
    ) {
        voiceSearchActivityLaucher = caller.registerForActivityResult(StartActivityForResult()) {
            onResult(it.resultCode, it.data?.getStringExtra(VoiceSearchActivity.EXTRA_VOICE_RESULT) ?: "")
        }
    }

    private fun registerPermissionRequest(
        caller: ActivityResultCaller,
        onResult: (Boolean) -> Unit,
    ) {
        permissionLauncher = caller.registerForActivityResult(RequestPermission()) {
            onResult(it)
        }
    }

    override fun launch(action: Action) {
        when (action) {
            LaunchPermissionRequest -> launchPermissionRequest()
            LaunchVoiceSearch -> launchVoiceSearch()
        }
    }

    private fun launchVoiceSearch() {
        voiceSearchActivityLaucher.launch(Intent(context, VoiceSearchActivity::class.java))
    }

    private fun launchPermissionRequest() {
        permissionLauncher?.launch(Manifest.permission.RECORD_AUDIO)
    }
}
