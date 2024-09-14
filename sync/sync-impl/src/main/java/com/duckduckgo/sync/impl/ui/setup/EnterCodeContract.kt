

package com.duckduckgo.sync.impl.ui.setup

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.duckduckgo.sync.impl.ui.EnterCodeActivity
import com.duckduckgo.sync.impl.ui.EnterCodeActivity.Companion.Code

class EnterCodeContract : ActivityResultContract<Code, Boolean>() {
    override fun createIntent(
        context: Context,
        codeType: Code,
    ): Intent {
        return EnterCodeActivity.intent(context, codeType)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}
