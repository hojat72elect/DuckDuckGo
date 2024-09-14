

package com.duckduckgo.windows.impl.ui

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.duckduckgo.anvil.annotations.ContributeToActivityStarter
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.macos.api.MacOsScreenWithEmptyParams
import com.duckduckgo.navigation.api.GlobalActivityStarter
import com.duckduckgo.windows.api.ui.WindowsScreenWithEmptyParams
import com.duckduckgo.windows.impl.R
import com.duckduckgo.windows.impl.WindowsLinkShareBroadcastReceiver
import com.duckduckgo.windows.impl.databinding.ActivityWindowsBinding
import com.duckduckgo.windows.impl.ui.WindowsViewModel.Command
import com.duckduckgo.windows.impl.ui.WindowsViewModel.Command.GoToMacClientSettings
import com.duckduckgo.windows.impl.ui.WindowsViewModel.Command.ShareLink
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@InjectWith(ActivityScope::class)
@ContributeToActivityStarter(WindowsScreenWithEmptyParams::class)
class WindowsActivity : DuckDuckGoActivity() {

    private val viewModel: WindowsViewModel by bindViewModel()
    private val binding: ActivityWindowsBinding by viewBinding()

    @Inject
    lateinit var globalActivityStarter: GlobalActivityStarter

    private val toolbar
        get() = binding.includeToolbar.toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.commands.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { executeCommand(it) }
            .launchIn(lifecycleScope)

        setContentView(binding.root)
        setupToolbar(toolbar)
        configureUiEventHandlers()
    }

    private fun configureUiEventHandlers() {
        binding.windowsShareButton.setOnClickListener {
            viewModel.onShareClicked()
        }

        binding.lookingForMacVersionButton.setOnClickListener {
            viewModel.onGoToMacClicked()
        }
    }

    private fun executeCommand(command: Command) {
        when (command) {
            is ShareLink -> launchSharePageChooser()
            is GoToMacClientSettings -> launchMacClientSettings()
        }
    }

    private fun launchSharePageChooser() {
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.windows_share_text))
            putExtra(Intent.EXTRA_TITLE, getString(R.string.windows_share_title))
        }

        val pi = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, WindowsLinkShareBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        try {
            startActivity(Intent.createChooser(share, getString(R.string.windows_share_title), pi.intentSender))
        } catch (e: ActivityNotFoundException) {
            Timber.w(e, "Activity not found")
        }
    }

    private fun launchMacClientSettings() {
        globalActivityStarter.start(this, MacOsScreenWithEmptyParams)
        finish()
    }
}
