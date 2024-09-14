

package com.duckduckgo.mobile.android.vpn.ui.report

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.mobile.android.vpn.databinding.ActivityAppTrackersInfoBinding
import com.duckduckgo.mobile.android.vpn.pixels.DeviceShieldPixels
import javax.inject.Inject

@InjectWith(ActivityScope::class)
class DeviceShieldAppTrackersInfo : DuckDuckGoActivity() {

    @Inject
    lateinit var deviceShieldPixels: DeviceShieldPixels

    private val binding: ActivityAppTrackersInfoBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setupToolbar(binding.includeToolbar.toolbar)

        deviceShieldPixels.privacyReportArticleDisplayed()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {

        internal fun intent(context: Context): Intent {
            return Intent(context, DeviceShieldAppTrackersInfo::class.java)
        }
    }
}
