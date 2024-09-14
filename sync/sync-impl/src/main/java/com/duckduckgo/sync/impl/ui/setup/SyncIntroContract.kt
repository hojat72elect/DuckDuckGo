

package com.duckduckgo.sync.impl.ui.setup

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.duckduckgo.sync.impl.ui.setup.SetupAccountActivity.Companion.Screen

class SyncIntroContract : ActivityResultContract<Screen, Boolean>() {
    override fun createIntent(
        context: Context,
        screen: Screen,
    ): Intent {
        return SetupAccountActivity.intent(context, screen)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}
