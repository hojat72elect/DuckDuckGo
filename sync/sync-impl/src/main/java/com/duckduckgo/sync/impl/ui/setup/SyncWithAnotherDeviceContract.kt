

package com.duckduckgo.sync.impl.ui.setup

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.duckduckgo.sync.impl.ui.SyncWithAnotherDeviceActivity

internal class SyncWithAnotherDeviceContract : ActivityResultContract<Void?, Boolean>() {
    override fun createIntent(
        context: Context,
        input: Void?,
    ): Intent {
        return SyncWithAnotherDeviceActivity.intent(context)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}
