

package com.duckduckgo.app.brokensite

import android.content.Context
import android.content.Intent
import com.duckduckgo.browser.api.brokensite.BrokenSiteData
import com.duckduckgo.browser.api.brokensite.BrokenSiteNav
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class AppBrokenSiteNav @Inject constructor() : BrokenSiteNav {
    override fun navigate(
        context: Context,
        data: BrokenSiteData,
    ): Intent {
        return BrokenSiteActivity.intent(context, data)
    }
}
