

package com.duckduckgo.app.webtrackingprotection

import android.app.ActivityOptions
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.duckduckgo.anvil.annotations.ContributeToActivityStarter
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.databinding.ActivityWebTrackingProtectionBinding
import com.duckduckgo.app.globalprivacycontrol.ui.GlobalPrivacyControlActivity
import com.duckduckgo.app.privacy.ui.AllowListActivity
import com.duckduckgo.app.webtrackingprotection.WebTrackingProtectionViewModel.Command
import com.duckduckgo.browser.api.ui.BrowserScreens.WebViewActivityWithParams
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.common.utils.extensions.html
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.navigation.api.GlobalActivityStarter
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@InjectWith(ActivityScope::class)
@ContributeToActivityStarter(WebTrackingProtectionScreenNoParams::class)
class WebTrackingProtectionActivity : DuckDuckGoActivity() {

    @Inject
    lateinit var globalActivityStarter: GlobalActivityStarter

    private val viewModel: WebTrackingProtectionViewModel by bindViewModel()
    private val binding: ActivityWebTrackingProtectionBinding by viewBinding()

    private val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            viewModel.onLearnMoreSelected()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setupToolbar(binding.includeToolbar.toolbar)

        configureUiEventHandlers()
        configureClickableLink()
        observeViewModel()
    }

    private fun configureUiEventHandlers() {
        binding.globalPrivacyControlSetting.setClickListener { viewModel.onGlobalPrivacyControlClicked() }
        binding.allowlist.setClickListener { viewModel.onManageAllowListSelected() }
    }

    private fun configureClickableLink() {
        val htmlGPCText = getString(R.string.webTrackingProtectionDescription).html(this)
        val gpcSpannableString = SpannableStringBuilder(htmlGPCText)
        val urlSpans = htmlGPCText.getSpans(0, htmlGPCText.length, URLSpan::class.java)
        urlSpans?.forEach {
            gpcSpannableString.apply {
                setSpan(
                    clickableSpan,
                    gpcSpannableString.getSpanStart(it),
                    gpcSpannableString.getSpanEnd(it),
                    gpcSpannableString.getSpanFlags(it),
                )
                removeSpan(it)
                trim()
            }
        }
        binding.webTrackingProtectionDescription.apply {
            text = gpcSpannableString
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun observeViewModel() {
        viewModel.viewState()
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .onEach { viewState ->
                setGlobalPrivacyControlSetting(viewState.globalPrivacyControlEnabled)
            }.launchIn(lifecycleScope)

        viewModel.commands()
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { processCommand(it) }
            .launchIn(lifecycleScope)
    }

    private fun setGlobalPrivacyControlSetting(enabled: Boolean) {
        val stateText = if (enabled) {
            getString(R.string.enabled)
        } else {
            getString(R.string.disabled)
        }
        binding.globalPrivacyControlSetting.setSecondaryText(stateText)
    }

    private fun processCommand(it: Command) {
        when (it) {
            is Command.LaunchLearnMoreWebPage -> launchLearnMoreWebPage(it.url)
            is Command.LaunchGlobalPrivacyControl -> launchGlobalPrivacyControl()
            is Command.LaunchAllowList -> launchAllowList()
        }
    }

    private fun launchLearnMoreWebPage(url: String) {
        globalActivityStarter.start(
            this,
            WebViewActivityWithParams(
                url = url,
                getString(R.string.webTrackingProtectionLearnMoreTitle),
            ),
        )
    }

    private fun launchAllowList() {
        val options = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        startActivity(AllowListActivity.intent(this), options)
    }

    private fun launchGlobalPrivacyControl() {
        val options = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        startActivity(GlobalPrivacyControlActivity.intent(this), options)
    }
}
