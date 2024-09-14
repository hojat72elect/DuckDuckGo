

package com.duckduckgo.app.brokensite.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.duckduckgo.app.brokensite.model.SiteProtectionsState
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.databinding.ViewSiteProtectionsToggleBinding
import com.duckduckgo.common.ui.viewbinding.viewBinding

class SiteProtectionsToggle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSiteProtectionsToggleBinding by viewBinding()
    private var state = SiteProtectionsState.DISABLED

    fun setState(state: SiteProtectionsState) {
        if (this.state != state) {
            this.state = state
            updateViewState()
        }
    }

    fun setOnProtectionsToggledListener(listener: (Boolean) -> Unit) {
        binding.protectionsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != areProtectionsEnabled()) {
                listener.invoke(isChecked)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateViewState()
    }

    private fun updateViewState() = with(binding) {
        when (state) {
            SiteProtectionsState.ENABLED -> {
                protectionsSwitch.isChecked = true
                protectionsSwitch.isEnabled = true
                protectionsSwitchLabel.setText(R.string.brokenSiteProtectionsOn)
                protectionsBannerMessage.setText(R.string.brokenSiteProtectionsOnBannerMessage)
                protectionsBannerMessageContainer.isVisible = true
                protectionsBannerMessageContainer.setBackgroundResource(R.drawable.background_site_protections_toggle_banner_tooltip)
            }
            SiteProtectionsState.DISABLED -> {
                protectionsSwitch.isChecked = false
                protectionsSwitch.isEnabled = true
                protectionsSwitchLabel.setText(R.string.brokenSiteProtectionsOff)
                protectionsBannerMessage.text = null
                protectionsBannerMessageContainer.isVisible = false
                protectionsBannerMessageContainer.background = null
            }
            SiteProtectionsState.DISABLED_BY_REMOTE_CONFIG -> {
                protectionsSwitch.isChecked = false
                protectionsSwitch.isEnabled = false
                protectionsSwitchLabel.setText(R.string.brokenSiteProtectionsOff)
                protectionsBannerMessage.setText(R.string.brokenSiteProtectionsOffByRemoteConfigBannerMessage)
                protectionsBannerMessageContainer.isVisible = true
                protectionsBannerMessageContainer.setBackgroundResource(R.drawable.background_site_protections_toggle_banner_alert)
            }
        }
    }

    private fun areProtectionsEnabled(): Boolean = when (state) {
        SiteProtectionsState.ENABLED -> true
        SiteProtectionsState.DISABLED,
        SiteProtectionsState.DISABLED_BY_REMOTE_CONFIG,
        -> false
    }
}
