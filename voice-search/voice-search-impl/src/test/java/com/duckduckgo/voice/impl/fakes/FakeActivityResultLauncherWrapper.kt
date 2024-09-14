

package com.duckduckgo.voice.impl.fakes

import androidx.activity.result.ActivityResultCaller
import com.duckduckgo.voice.impl.ActivityResultLauncherWrapper
import com.duckduckgo.voice.impl.ActivityResultLauncherWrapper.Action
import com.duckduckgo.voice.impl.ActivityResultLauncherWrapper.Request

class FakeActivityResultLauncherWrapper : ActivityResultLauncherWrapper {
    var lastKnownRequest: Request? = null
    var lastKnownAction: Action? = null

    override fun register(
        caller: ActivityResultCaller,
        request: Request,
    ) {
        lastKnownRequest = request
    }

    override fun launch(action: Action) {
        lastKnownAction = action
    }
}
