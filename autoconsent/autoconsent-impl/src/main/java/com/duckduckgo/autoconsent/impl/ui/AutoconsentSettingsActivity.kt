

package com.duckduckgo.autoconsent.impl.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.autoconsent.impl.R
import com.duckduckgo.autoconsent.impl.databinding.ActivityAutoconsentSettingsBinding
import com.duckduckgo.autoconsent.impl.ui.AutoconsentSettingsViewModel.Command
import com.duckduckgo.autoconsent.impl.ui.AutoconsentSettingsViewModel.ViewState
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
class AutoconsentSettingsActivity : DuckDuckGoActivity() {

    @Inject
    lateinit var globalActivityStarter: GlobalActivityStarter

    private val binding: ActivityAutoconsentSettingsBinding by viewBinding()

    private val viewModel: AutoconsentSettingsViewModel by bindViewModel()

    private val toolbar
        get() = binding.includeToolbar.toolbar

    private val autoconsentToggleListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        viewModel.onUserToggleAutoconsent(isChecked)
    }

    private val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            viewModel.onLearnMoreSelected()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setupToolbar(toolbar)

        configureUiEventHandlers()
        configureClickableLink()
        observeViewModel()
    }

    private fun configureUiEventHandlers() {
        binding.autoconsentToggle.setOnCheckedChangeListener(autoconsentToggleListener)
    }

    private fun observeViewModel() {
        viewModel.viewState.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { render(it) }
            .launchIn(lifecycleScope)

        viewModel.commands()
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { processCommand(it) }
            .launchIn(lifecycleScope)
    }

    private fun render(viewState: ViewState) {
        binding.autoconsentToggle.quietlySetIsChecked(viewState.autoconsentEnabled, autoconsentToggleListener)
    }

    private fun processCommand(it: Command) {
        when (it) {
            is Command.LaunchLearnMoreWebPage -> launchLearnMoreWebPage(it)
        }
    }

    private fun launchLearnMoreWebPage(it: Command.LaunchLearnMoreWebPage) {
        val options = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        globalActivityStarter.start(this, WebViewActivityWithParams(it.url, getString(it.titleId)), options)
    }

    private fun configureClickableLink() {
        val htmlText = getString(R.string.autoconsentDescription).html(this)
        val spannableString = SpannableStringBuilder(htmlText)
        val urlSpans = htmlText.getSpans(0, htmlText.length, URLSpan::class.java)
        urlSpans?.forEach {
            spannableString.apply {
                setSpan(
                    clickableSpan,
                    spannableString.getSpanStart(it),
                    spannableString.getSpanEnd(it),
                    spannableString.getSpanFlags(it),
                )
                removeSpan(it)
                trim()
            }
        }
        binding.autoconsentDescription.apply {
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, AutoconsentSettingsActivity::class.java)
        }
    }
}
